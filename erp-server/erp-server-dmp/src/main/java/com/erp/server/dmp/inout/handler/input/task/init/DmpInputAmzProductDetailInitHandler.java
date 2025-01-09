package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.api.CatalogApi;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.*;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.Item;
import com.erp.sdk.oms.amz.spapi.model.catalogitems.ItemSearchResults;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzProductDetailInitHandler extends DmpInputAmzCommonInitHandler {

    public static final String PRODUCT_ID_TYPE = "productIdType";
    public static final String AMAZON_LISTING_DATA = "amazon_listing_data";
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 顶级mongo数据
        List<Map<String, Object>> reportMongoData = getParentStorageMongoData();
        if (CollectionUtils.isEmpty(reportMongoData)) {
            // 主数据不存在明细无需处理
            log.warn("亚马逊Listing下载主任务taskId={},报告为空明细无需处理", dmpInputTaskEntity.getParentTaskId());
            return Collections.emptyList();
        }
        String shopId = reportMongoData.get(0).getOrDefault("shopId", "").toString();
        if (StringUtils.isBlank(shopId)) {
            ServiceException.runError("未找到mongo中requestShopId信息:taskId=" + dmpInputTaskEntity.getId());
        }
        // 获取店铺授权
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 当前站点
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());

        // 查询报告内容信息
        List<ParamData> paramDataList = new ArrayList<>();
        List<String> reportIdList = reportMongoData.stream().map(f -> f.get("reportId").toString()).collect(Collectors.toList());
        paramDataList.add(new ParamData("reportId", "reportId", PannoEnum.IN, reportIdList));
        paramDataList.add(new ParamData("requestShopId", "requestShopId", PannoEnum.EQ, shopId));
        List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList, AMAZON_LISTING_DATA);

        // 批量查询数量仅支持20
        int pageSize = 20;
        // 结果
        List<JSONObject> resultList = new LinkedList<>();

        // 查询商品详情客户端
        CatalogApi catalogApi = CatalogApi.init(marketPlaceEnum.getEndpointsEnum(), shopInfoDTO, false, null);
        // 限流类型
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.PRODUCT_ITEMS;

        // 过滤不支持查询明细的sku
        List<Map<String, Object>> canQueryList = findMongoData.stream().filter(e -> canUpdateDetail(e, marketPlaceEnum)).collect(Collectors.toList());

        // 待查询产品 1
        List<Map<String, Object>> waitQueryList = new LinkedList<>();

        // 缓存优先
        checkAndGetRedisList(canQueryList, resultList, waitQueryList);
        if (CollectionUtils.isEmpty(waitQueryList)){
            return convertDmpInputTaskInitDTOS(resultList);
        }

        // 待查询按商品类型分组
        Map<AmazonIdentifiersTypeEnum, List<Map<String, Object>>> gourpByTypeMap = waitQueryList.stream()
                .collect(Collectors.groupingBy(e -> convertIdentifiersType(e.getOrDefault(PRODUCT_ID_TYPE, "").toString())));

        // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
        String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
        // 默认请求速率配置
        String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();

        for (Map.Entry<AmazonIdentifiersTypeEnum, List<Map<String, Object>>> entry : gourpByTypeMap.entrySet()) {
            // 批量转换标识符
            List<String> identifiers = entry.getValue()
                    .stream()
                    .map(this::checkAndGetIdentifier)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询数量仅支持20
            List<List<String>> partition = Lists.partition(identifiers, 20);

            for (List<String> curIdentifiersList : partition) {
                try {
                    // 获取动态速率
                    Object limitObj = redisUtil.get(limitKey);
                    if (null != limitObj) {
                        log.warn("【亚马逊listing详情】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                        // 触发限流不执行当前
                        DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                        initDmpResponse.setDoNextStatus(false);
                        return Collections.emptyList();
                    }

                    String identifiersType = entry.getKey().getCode();
                    List<String> includedData = AmazonIncludedDataEnum.getAllWithoutVendor();
                    // 请求
                    ApiResponse<ItemSearchResults> apiResponse = catalogApi.searchCatalogItemsWithHttpInfo(Collections.singletonList(marketPlaceEnum.getMarketplaceId()), curIdentifiersList, identifiersType, includedData, null, null, null, null, null, pageSize, null, null);
                    List<String> limitArray = apiResponse.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
                    rateLimitStr = limitArray.get(0);
                    List<Item> items = apiResponse.getData().getItems();
                    if (CollectionUtils.isEmpty(items)) {
                        continue;
                    }
                    for (Item item : items) {
                        String productId = item.getKeyByIdentifierType(entry.getKey(), marketPlaceEnum);
                        JSONObject jsonObject = convertJsonObject(item, productId);
                        String shipmentIdResultKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.PRODUCT_ITEMS.getBusinessTypeName(), productId);
                        // 缓存倒redis
                        redisUtil.set(shipmentIdResultKey, jsonObject.toJSONString(), 900);
                        // 添加到当前结果
                        resultList.add(jsonObject);
                    }
                } catch (ApiException e) {
                    if (429 == e.getCode()) {
                        // 设置动态速率，失效时间=1/limit
                        BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                        redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                        log.warn("【亚马逊listing详情】 platformShopCode={},首次429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                        // 触发限流不执行当前
                        DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                        initDmpResponse.setDoNextStatus(false);
                        return Collections.emptyList();
                    }
                    throw new ServiceException("[Amazon SP-APi] 查询FBA货件item失败" + e);
                }
            }
            log.warn("查询亚马逊listing详情成功, platformShopCode={}", shopInfoDTO.getPlatformShopCode());
        }

        return convertDmpInputTaskInitDTOS(resultList);
    }

    /**
     * 转换JSONObject
     */
    private static JSONObject convertJsonObject(Item item, String productId) {
        JSONObject jsonObject = (JSONObject) JSON.toJSON(item);
        jsonObject.put("productId", productId);
        return jsonObject;
    }

    /**
     * 转换中台响应DTO
     */
    private static List<DmpInputTaskInitDTO> convertDmpInputTaskInitDTOS(List<JSONObject> resultList) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new LinkedList<>();
        for (JSONObject jsonObject : resultList) {
            dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(jsonObject)));
        }
        return dmpInputTaskInitDTOList;
    }

    /**
     * 检查和设置到redis
     */
    private void checkAndGetRedisList(List<Map<String, Object>> canQueryList, List<JSONObject> resultList, List<Map<String, Object>> waitQueryList) {
        for (Map<String, Object> findMongoDataItem : canQueryList) {
            String productId = findMongoDataItem.getOrDefault("productId", "").toString();
            if (StringUtils.isBlank(productId)) {
                ServiceException.runError("亚马逊listing数据异常:无productId：{}", findMongoDataItem);
            }
            // 缓存获取结果
            String shipmentIdResultKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.PRODUCT_ITEMS.getBusinessTypeName(), productId);
            Object resultObj = redisUtil.get(shipmentIdResultKey);
            if (null != resultObj) {
                JSONObject jsonObject = JSON.parseObject(resultObj.toString(), JSONObject.class);
                resultList.add(jsonObject);
            } else {
                waitQueryList.add(findMongoDataItem);
            }
        }
    }


    /**
     * 转换IdentifiersType
     */
    public AmazonIdentifiersTypeEnum convertIdentifiersType(String productIdType) {
        if ("3".equals(productIdType)) {
            return AmazonIdentifiersTypeEnum.UPC;
        } else {
            return AmazonIdentifiersTypeEnum.ASIN;
        }
    }

    /**
     * 当前sku是否支持查询明细
     */
    public boolean canUpdateDetail(Map<String, Object> mongoItem, AmazonMarketplaceEnum marketPlaceEnum) {
        String productIdType = mongoItem.getOrDefault("productIdType", "").toString();
        String status = mongoItem.getOrDefault("status", "").toString();
        if ("1".equalsIgnoreCase(productIdType) && AmazonListingStatusEnum.INACTIVE.getCode().equalsIgnoreCase(status)) {
            //ProductIdType=ASIN,停售无法更新明细");
            return false;
        }
        // 日本异常数据
        if (AmazonMarketplaceEnum.JP.equals(marketPlaceEnum)) {
            // 日本站点ProductIdType=4无法更新明细";
            return !"4".equals(productIdType);
        }
        return true;
    }


    /**
     * 检查和转换识别吗Identifiers
     */
    public String checkAndGetIdentifier(Map<String, Object> mongoMap) {
        String productIdType = mongoMap.getOrDefault("productIdType", "").toString();
        String productId = mongoMap.getOrDefault("productId", "").toString();
        String asin1 = mongoMap.getOrDefault("asin1", "").toString();
        if (("4".equals(productIdType) || "2".equals(productIdType)) && StringUtils.isNotBlank(asin1)) {
            return asin1;
        }
        return productId;
    }
}
