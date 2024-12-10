package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
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
import java.util.Collections;
import java.util.List;
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
public class DmpInputAmzFbaInventoryApiInitHandler extends DmpInputInitHandler {

    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        String shopId = dmpInputTaskEntity.getNextLevelId();
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        // 根据明细类型扩展
        String extendJson = dmpInputTaskEntity.getExtendJson();
        // 查询所有
        boolean hasAll = false;
        if(StringUtils.isNotBlank(extendJson)) {
            JSONObject parseObject = JSON.parseObject(extendJson);
            if(parseObject != null) {
                hasAll = parseObject.getBooleanValue("hasAll");
            }
        }
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
        // Sku列表
        List<String> sellerSkus = null;
        FbaInventoryApi api = AmazonSpApiInitUtils.create(FbaInventoryApi.class, shopInfoDTO, false);

        try {
            List<InventorySummary> allList = api.getAllInventorySummaries(granularityType, granularityId, marketplaceIds, details, startDateTime, sellerSkus);
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


}
