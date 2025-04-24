package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.DmpFbaInventoryEntity;
import com.erp.sdk.oms.amz.spapi.api.FbaInventoryApi;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fbainventory.InventorySummary;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.DmpFbaInventoryService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 * 亚马逊FBA库存
 *
 * @author Jim
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFbaInventoryApiInitHandler extends DmpInputAmzCommonInitHandler {

    public static final String SHOP_ID = "shopId";

    public static final String AMAZON_LISTING_DATA = "amazon_listing_data";

    public static final String PLATFORM_SHOP_CODE = "platformShopCode";

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private DmpFbaInventoryService dmpFbaInventoryService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String shopId = dmpInputTaskEntity.getNextLevelId();
        // 根据明细类型扩展
        String extendJson = dmpInputTaskEntity.getExtendJson();
        // Sku列表
        List<String> sellerSkus = null;
        // 查询所有
        boolean hasAll = false;
        if(StringUtils.isNotBlank(extendJson)) {
            JSONObject parseObject = JSON.parseObject(extendJson);
            if(parseObject != null) {
                JSONArray jsonArray = parseObject.getJSONArray("sellerSkus");
                if (CollectionUtils.isNotEmpty(jsonArray)){
                    sellerSkus = jsonArray.stream().map(Object::toString).distinct().collect(Collectors.toList());
                }
            }
        }

        // 兼容作为子任务指定sellerSkus查询
        if (StringUtils.isNotBlank(dmpInputTaskEntity.getParentTaskId())) {
            // 顶级mongo数据
            List<Map<String, Object>> reportMongoData = getParentStorageMongoData();
            if (CollectionUtils.isEmpty(reportMongoData)) {
                // 主数据不存在明细无需处理
                log.warn("亚马逊FBA库存补充下载主任务taskId={},报告为空明细无需处理", dmpInputTaskEntity.getParentTaskId());
                return Collections.emptyList();
            }
            shopId = reportMongoData.get(0).getOrDefault(SHOP_ID, "").toString();
            String platformShopCode = reportMongoData.get(0).getOrDefault(DmpInputAmzFbaInventoryApiInitHandler.PLATFORM_SHOP_CODE, "").toString();
            if (StringUtils.isBlank(shopId) || StringUtils.isBlank(platformShopCode) ) {
                ServiceException.runError("未找到mongo中shopId或platformShopCode信息:taskId=" + dmpInputTaskEntity.getParentTaskId());
            }
            // 查询上游是否有需要刷新的fnSku
            sellerSkus = checkAndGetSellerSkus(reportMongoData, shopId, platformShopCode);
            if (CollectionUtils.isEmpty(sellerSkus)){
                return Collections.emptyList();
            }
        }

        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.FBA_INVENTORY;
        // 默认请求速率配置
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT_PREFIX_LAST, shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
        // 校验速率
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("【亚马逊FBA库存查询】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
            // 触发限流不执行当前
            DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
            initDmpResponse.setDoNextStatus(false);
            return Collections.emptyList();
        }
        String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();

        // 目前仅支持市场类型
        String granularityType = "Marketplace";
        // 目前仅支持单个市场ID
        String granularityId = marketplaceEnum.getMarketplaceId();
        List<String> marketplaceIds = Collections.singletonList(marketplaceEnum.getMarketplaceId());
        // 是否包含明细
        Boolean details = true;
        // 数据开始时间(空=全量)
        OffsetDateTime startDateTime = null == dmpInputTaskEntity.getStartTime() || hasAll ? null : DateUtil.plus8SameUtcOffset(dmpInputTaskEntity.getStartTime());
        FbaInventoryApi api = AmazonSpApiInitUtils.create(FbaInventoryApi.class, shopInfoDTO, false);

        try {
            List<InventorySummary> allList = new LinkedList<>();
            if (CollectionUtils.isEmpty(sellerSkus)){
                // 按更新时间请求
                allList = api.getAllInventorySummaries(granularityType, granularityId, marketplaceIds, details, startDateTime, null);
            } else {
                // 按sku请求
                List<List<String>> partition = Lists.partition(sellerSkus, 50);
                for (List<String> curSkuList : partition) {
                    List<InventorySummary> curList = api.getAllInventorySummaries(granularityType, granularityId, marketplaceIds, details, null, curSkuList);
                    allList.addAll(curList);
                }
            }
            if (CollectionUtils.isEmpty(allList)){
                return Collections.emptyList();
            }
            // 拼接来源信息
            List<JSONObject> resultList = allList.stream().map(e -> setAmazonOrderIdAndToJsonObject(e, shopInfoDTO, marketplaceEnum)).collect(Collectors.toList());
            // 组合响应
            DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setMsg(JSON.toJSONString(resultList));
            return Collections.singletonList(dmpInputTaskInitDTO);
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                // 设置动态速率，失效时间=1/limit
                BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                log.warn("【亚马逊FBA库存查询】 platformShopCode={},当前触发429限流:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                // 触发限流不执行当前
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextStatus(false);
                return Collections.emptyList();
            }
            if (!StringUtils.isBlank(e.getMessage())){
                // 其他异常信息
                throw new ServiceException("亚马逊FBA库存查询失败:" + ExceptionUtil.stacktraceToString(e));
            }
            JSONObject jsonObject = JSON.parseObject(e.getResponseBody());
            JSONArray errorJsonArray = jsonObject.getJSONArray("errors");
            if (CollectionUtils.isEmpty(errorJsonArray)){
                // 其他异常信息
                throw new ServiceException("亚马逊FBA库存查询失败:"  + ExceptionUtil.stacktraceToString(e));
            }
            JSONObject errorObj = errorJsonArray.getJSONObject(0);
            String errorMsg = errorObj.getString("message");
            throw new ServiceException("[Amazon SP-APi] 亚马逊FBA库存查询失败:" + errorMsg);
        }

    }

    /**
     * 设置亚马逊店铺信息和转换JSON
     */
    private JSONObject setAmazonOrderIdAndToJsonObject(InventorySummary sourceEntity, AmazonShopInfoDTO shopInfoDTO, AmazonMarketplaceEnum marketplaceEnum) {
        JSONObject json = (JSONObject) JSON.toJSON(sourceEntity);
        json.put("marketplaceId", marketplaceEnum.getMarketplaceId());
        json.put("platformShopCode", shopInfoDTO.getPlatformShopCode());
        if (null != sourceEntity.getLastUpdatedTime()) {
            LocalDateTime updateTime = DateUtil.utcSamePlus8(sourceEntity.getLastUpdatedTime().toLocalDateTime());
            json.put("lastPlatformUpdateTime", updateTime);
        }
        return json;
    }

    /**
     * 检查上游报告需要更新fnSku的SellerSkus
     */
    private List<String> checkAndGetSellerSkus(List<Map<String, Object>> reportMongoData, String shopId, String platformShopCode) {
        List<String> reportIdList = reportMongoData.stream().map(f -> f.get("reportId").toString()).collect(Collectors.toList());
        // 查询报告内容信息
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData("reportId", "reportId", PannoEnum.IN, reportIdList));
        paramDataList.add(new ParamData("requestShopId", "requestShopId", PannoEnum.EQ, shopId));
        List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, AMAZON_LISTING_DATA);

        List<String> sellerSkuList = findMongoData.stream()
                .map(e -> e.getOrDefault("sellerSku", "").toString())
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(sellerSkuList)){
            return Collections.emptyList();
        }
        // 查询对应中台已有库存记录
        List<DmpFbaInventoryEntity> existFbaInventoryList = dmpFbaInventoryService.lambdaQuery()
                .eq(DmpFbaInventoryEntity::getPlatformShopCode, platformShopCode)
                .in(DmpFbaInventoryEntity::getMsku, sellerSkuList)
                .list();
        if (CollectionUtils.isEmpty(existFbaInventoryList)){
            return sellerSkuList;
        }
        // 移除已有的库存的sku
        List<String> existSkuList = existFbaInventoryList.stream()
                .map(DmpFbaInventoryEntity::getMsku)
                .distinct()
                .collect(Collectors.toList());
        return sellerSkuList.stream()
                .filter( e-> !existSkuList.contains(e))
                .collect(Collectors.toList());
    }
}
