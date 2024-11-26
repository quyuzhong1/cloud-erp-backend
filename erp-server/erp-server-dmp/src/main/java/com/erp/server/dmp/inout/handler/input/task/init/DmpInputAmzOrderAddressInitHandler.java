package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.sdk.oms.amz.spapi.api.OrdersV0Api;
import com.erp.sdk.oms.amz.spapi.client.ApiClient;
import com.erp.sdk.oms.amz.spapi.client.ApiException;
import com.erp.sdk.oms.amz.spapi.client.ApiResponse;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.handler.AmazonOrderHandler;
import com.erp.sdk.oms.amz.spapi.model.orders.Address;
import com.erp.sdk.oms.amz.spapi.model.orders.GetOrderAddressResponse;
import com.erp.sdk.oms.amz.spapi.model.orders.GetOrderBuyerInfoResponse;
import com.erp.sdk.oms.amz.spapi.model.orders.OrderBuyerInfo;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitChildResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.DmpInputTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
public class DmpInputAmzOrderAddressInitHandler extends DmpInputAmzCommonInitHandler {
    public static final String ORDER_STATUS = "orderStatus";
    public static final String ORDER_ID_LIST = "orderIdList";
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private AmazonOrderHandler amazonOrderHandler;
    @Resource
    private RedisUtil redisUtil;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        // 获取上一级mongo数据
        List<Map<String, Object>> findMongoData = getParentStorageMongoData();
        if (CollUtil.isEmpty(findMongoData)) {
            return Collections.emptyList();
        }
        // 解析到当前店铺ID
        String shopId = parseShopId(findMongoData);
        // 需要查询的店铺
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 获取父任务信息
        DmpInputTaskEntity parentDmpInputTaskEntity = null;
        if (null != dmpInputTaskEntity && StringUtils.isNotBlank(dmpInputTaskEntity.getParentTaskId())){
            parentDmpInputTaskEntity = dmpInputTaskService.getById(dmpInputTaskEntity.getParentTaskId());
        }

        // 主单信息
        List<Map<String, Object>> mainMongoDataList = getMainOrderMongoDate(findMongoData, shopInfoDTO.getPlatformShopCode());

        //响应结果
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        OrdersV0Api ordersVoApi = AmazonSpApiInitUtils.create(OrdersV0Api.class, shopInfoDTO, false);

        for (Map<String, Object> mongoData : findMongoData) {
            // 主单ID
            String amazonOrderId = checkAndGetMongoValue(mongoData, "amazonOrderId");
            // 检查是否查询
            if (orderOtherCheckCanDoNextRequest(mainMongoDataList, amazonOrderId)) {
                log.warn("FBA或多渠道订单不获取地址信息:{}", amazonOrderId);
                continue;
            }

            String platformShopCode = shopInfoDTO.getPlatformShopCode();

            String uniqueId = CharSequenceUtil.format("{}_{}", platformShopCode, amazonOrderId);

            // 查询买家信息
            DmpInputInitChildResponse<OrderBuyerInfo> buyerInfoResponse = checkAndQueryBuyerInfo(ordersVoApi, amazonOrderId, platformShopCode, uniqueId, mainMongoDataList, shopInfoDTO, parentDmpInputTaskEntity);
            // 触发限流不执行当前
            if (!buyerInfoResponse.isDoNextChain()) {
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextChain(false);
                return Collections.emptyList();
            }
            OrderBuyerInfo buyerInfo = buyerInfoResponse.getData();

            // 设置地址
            DmpInputInitChildResponse<Address> addressResponse = checkAndQueryAddress(ordersVoApi, amazonOrderId, platformShopCode, uniqueId, mainMongoDataList, shopInfoDTO, parentDmpInputTaskEntity);
            // 触发限流不执行当前
            if (!addressResponse.isDoNextChain()) {
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextChain(false);
                return Collections.emptyList();
            }
            Address shippingAddress = addressResponse.getData();
            if (null == shippingAddress) {
                log.warn("查询到亚马逊地址为空:amazonOrderId={}", amazonOrderId);
                continue;
            }

            // 合并转json
            JSONObject jsonObject = setAmazonOrderIdAndToJsonObject(shippingAddress, buyerInfo, amazonOrderId, shopInfoDTO.getPlatformShopCode());
            dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(jsonObject)));
        }

        return dmpInputTaskInitDTOList;
    }


    /**
     * 设置亚马逊订单ID和转换JSON
     */
    private JSONObject setAmazonOrderIdAndToJsonObject(Address address, OrderBuyerInfo buyerInfo, String amazonOrderId, String platformShopCode) {
        JSONObject json = (JSONObject) JSON.toJSON(address);
        json.put("amazonOrderId", amazonOrderId);
        json.put("platformShopCode", platformShopCode);
        JSONObject jsonBuyer = (JSONObject) JSON.toJSON(buyerInfo);
        json.put("buyerInfo", jsonBuyer);
        return json;
    }

    /**
     * 请求地址
     */
    private Address getAddress(OrdersV0Api ordersVoApi, String amazonOrderId, String limitKey, String key) throws ApiException {
        ApiResponse<GetOrderAddressResponse> orderAddressResp = ordersVoApi.getOrderAddressWithHttpInfo(amazonOrderId);
        List<String> limitArray = orderAddressResp.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
        String rateLimitStr = limitArray.get(0);
        GetOrderAddressResponse response = orderAddressResp.getData();
        if (StringUtils.isNotBlank(rateLimitStr)) {
            // 设置动态速率，失效时间=1/limit
            BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
            redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
        }
        // 结果缓存倒redis(消费完移除)
        redisUtil.set(key, JSONUtil.toJsonStr(response.getPayload().getShippingAddress()), 300);
        return response.getPayload().getShippingAddress();
    }

    /**
     * 请求买家信息
     */
    private OrderBuyerInfo getOrderBuyerInfo(OrdersV0Api ordersVoApi, String amazonOrderId, String limitKey, String key) throws ApiException {
        ApiResponse<GetOrderBuyerInfoResponse> orderBuyerInfoResp = ordersVoApi.getOrderBuyerInfoWithHttpInfo(amazonOrderId);
        List<String> limitArray = orderBuyerInfoResp.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
        String rateLimitStr = limitArray.get(0);
        GetOrderBuyerInfoResponse response = orderBuyerInfoResp.getData();
        if (StringUtils.isNotBlank(rateLimitStr)) {
            // 设置动态速率，失效时间=1/limit
            BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
            redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
        }
        // 结果缓存倒redis(消费完移除)
        redisUtil.set(key, JSONUtil.toJsonStr(response.getPayload()), 300);
        return response.getPayload();
    }

    /**
     * 检查和查询买家信息
     */
    private DmpInputInitChildResponse<OrderBuyerInfo> checkAndQueryBuyerInfo(OrdersV0Api ordersVoApi, String amazonOrderId, String platformShopCode, String uniqueId, List<Map<String, Object>> mainMongoDataList, AmazonShopInfoDTO shopInfoDTO, DmpInputTaskEntity parentDmpInputTaskEntity) {
        // 查询买家信息
        String key = CharSequenceUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.BUYER_INFO.getBusinessTypeName(), uniqueId);

        // 缓存优先
        Object resultObj = redisUtil.get(key);
        if (null != resultObj) {
            OrderBuyerInfo buyerInfo = JSONUtil.toBean(resultObj.toString(), OrderBuyerInfo.class);
            return new DmpInputInitChildResponse<>(true, buyerInfo);
        }

        // 已发货订单不请求
        if (hasSkipGetAddressBuyerData(mainMongoDataList, amazonOrderId, parentDmpInputTaskEntity)) {
            return new DmpInputInitChildResponse<>(true, null);
        }

        // 生成RDT权限获取地址信息
        // amazon-rdt-token:店铺ID:订单ID
        String rdtToken = amazonOrderHandler.queryAndGetRDT(amazonOrderId, shopInfoDTO);

        // 修改x-amz-access-token的token
        ordersVoApi.getApiClient().addDefaultHeader(ApiClient.SIGNED_ACCESS_TOKEN_HEADER_NAME, rdtToken);

        // 请求亚马逊接口
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.BUYER_INFO;
        String limitKey = CharSequenceUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), platformShopCode, requestTypeRateLimiterEnum.getBusinessTypeName());
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("【订单买家信息拉取】 platformShopCode={},存在429等待恢复:放弃当前请求任务", platformShopCode);
            // 触发限流不执行当前
            return new DmpInputInitChildResponse<>(false, null);
        }
        String rateLimitStr = AmazonRequestTypeRateLimiterEnum.BUYER_INFO.getRateLimit();
        try {
            OrderBuyerInfo buyerInfo = getOrderBuyerInfo(ordersVoApi, amazonOrderId, limitKey, key);
            return new DmpInputInitChildResponse<>(true, buyerInfo);
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                if (!redisUtil.hasKey(limitKey)){
                    // 设置动态速率，失效时间=1/limit
                    BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                    redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                }
                log.warn("【订单买家信息拉取】 platformShopCode={},接口首次429:放弃当前请求任务", platformShopCode);
                // 触发限流不执行当前
                return new DmpInputInitChildResponse<>(false, null);
            }
            throw new ServiceException("查询亚马逊订单买家信息失败：API异常：" + JSONUtil.toJsonStr(e));
        } catch (Exception e) {
            throw new ServiceException("查询亚马逊订单买家信息失败：" + JSONUtil.toJsonStr(e));
        }
    }

    /**
     * 是否跳过请求亚马逊接口
     */
    public boolean hasSkipGetAddressBuyerData(List<Map<String, Object>> mainMongoDataList, String amazonOrderId, DmpInputTaskEntity parentDmpInputTaskEntity) {
        if (null != parentDmpInputTaskEntity && StringUtils.isNotBlank(parentDmpInputTaskEntity.getExtendJson())){
            JSONObject jsonObject = JSON.parseObject(parentDmpInputTaskEntity.getExtendJson());
            JSONArray jsonArray = jsonObject.getJSONArray(ORDER_ID_LIST);
            if (CollectionUtils.isNotEmpty(jsonArray)){
                List<String> orderIdList = jsonArray.stream().map(Object::toString).distinct().collect(Collectors.toList());
                // 指定单号拉取不跳过
                return !orderIdList.contains(amazonOrderId);
            }
        }


        Map<String, Object> mainMongo = checkAndGetMainMongoMap(mainMongoDataList, amazonOrderId);
        // 订单状态
        String orderStatus = checkAndGetMongoValue(mainMongo, ORDER_STATUS);
        // 已发货订单不请求亚马逊
        return "Shipped".equalsIgnoreCase(orderStatus);
    }


    /**
     * 检查和查询地址
     */
    private DmpInputInitChildResponse<Address> checkAndQueryAddress(OrdersV0Api ordersVoApi, String amazonOrderId, String platformShopCode, String uniqueId, List<Map<String, Object>> mainMongoDataList, AmazonShopInfoDTO shopInfoDTO, DmpInputTaskEntity parentDmpInputTaskEntity) {
        String addressKey = CharSequenceUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS.getBusinessTypeName(), uniqueId);
        Object addressResultObj = redisUtil.get(addressKey);
        if (null != addressResultObj) {
            Address shippingAddress = JSONUtil.toBean(addressResultObj.toString(), Address.class);
            return new DmpInputInitChildResponse<>(true, shippingAddress);
        }

        // 已发货订单不请求
        if (hasSkipGetAddressBuyerData(mainMongoDataList, amazonOrderId, parentDmpInputTaskEntity)) {
            return new DmpInputInitChildResponse<>(true, null);
        }

        // 生成RDT权限获取地址信息
        // amazon-rdt-token:店铺ID:订单ID
        String rdtToken = amazonOrderHandler.queryAndGetRDT(amazonOrderId, shopInfoDTO);

        // 修改x-amz-access-token的token
        ordersVoApi.getApiClient().addDefaultHeader(ApiClient.SIGNED_ACCESS_TOKEN_HEADER_NAME, rdtToken);

        // 获取动态速率
        // 平台请求中:平台类型:sellerId:业务类型
        AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS;
        String limitKey = CharSequenceUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), platformShopCode, requestTypeRateLimiterEnum.getBusinessTypeName());
        Object limitObj = redisUtil.get(limitKey);
        if (null != limitObj) {
            log.warn("【订单地址拉取】 UniqueId={},存在429等待恢复:放弃当前请求任务", uniqueId);
            // 触发限流不执行当前
            return new DmpInputInitChildResponse<>(false, null);
        }
        String rateLimitStr = AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS.getRateLimit();
        try {
            Address address = getAddress(ordersVoApi, amazonOrderId, limitKey, addressKey);
            return new DmpInputInitChildResponse<>(true, address);
        } catch (ApiException e) {
            if (429 == e.getCode()) {
                if (!redisUtil.hasKey(limitKey)){
                    // 设置动态速率，失效时间=1/limit
                    BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                    redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                }
                log.warn("【订单地址拉取】 platformShopCode={},接口首次429:放弃当前请求任务", platformShopCode);
                // 触发限流不执行当前
                return new DmpInputInitChildResponse<>(false, null);
            }
            throw new ServiceException("查询亚马逊订单地址失败：API异常：" + JSONUtil.toJsonStr(e));
        } catch (Exception e) {
            throw new ServiceException("查询亚马逊订单地址失败：" + JSONUtil.toJsonStr(e));
        }
    }
}
