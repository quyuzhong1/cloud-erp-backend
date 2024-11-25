package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
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
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzOrderAddressInitHandler extends DmpInputAmzCommonInitHandler {
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

        // 主单信息
        List<Map<String, Object>> mainMongoDataList = getMainOrderMongoDate(findMongoData, shopInfoDTO.getPlatformShopCode());

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        for (Map<String, Object> mongoData : findMongoData) {
            // 主单ID
            String amazonOrderId = checkAndGetMongoValue(mongoData, "amazonOrderId");
            // 检查是否查询
            if (orderOtherCheckCanDoNextRequest(mainMongoDataList, amazonOrderId)) {
                log.warn("FBA或多渠道订单不获取地址信息:{}", amazonOrderId);
                continue;
            }
            OrdersV0Api ordersVoApi = AmazonSpApiInitUtils.create(OrdersV0Api.class, shopInfoDTO, false);

            // 生成RDT权限获取地址信息
            // amazon-rdt-token:店铺ID:订单ID
            String rdtToken = amazonOrderHandler.queryAndGetRDT(amazonOrderId, shopInfoDTO);

            // 修改x-amz-access-token的token
            ordersVoApi.getApiClient().addDefaultHeader(ApiClient.SIGNED_ACCESS_TOKEN_HEADER_NAME, rdtToken);
            String platformShopCode = shopInfoDTO.getPlatformShopCode();

            // 查询买家信息
            OrderBuyerInfo buyerInfo;
            String uniqueId = StrUtil.format("{}_{}", platformShopCode, amazonOrderId);
            String key = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.BUYER_INFO.getBusinessTypeName(), uniqueId);
            Object resultObj = redisUtil.get(key);
            if (null != resultObj) {
                buyerInfo =  JSONUtil.toBean(resultObj.toString(), OrderBuyerInfo.class);
            } else {
                AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.BUYER_INFO;
                String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), platformShopCode, requestTypeRateLimiterEnum.getBusinessTypeName());
                Object limitObj = redisUtil.get(limitKey);
                if (null != limitObj){
                    log.warn("【订单买家信息拉取】 platformShopCode={},存在429等待恢复:放弃当前请求任务", platformShopCode);
                    // 触发限流不执行当前
                    DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                    initDmpResponse.setDoNextChain(false);
                    return Collections.emptyList();
                }
                String rateLimitStr = AmazonRequestTypeRateLimiterEnum.BUYER_INFO.getRateLimit();
                try {
                    buyerInfo = getOrderBuyerInfo(ordersVoApi, amazonOrderId, rateLimitStr, limitKey, key);
                } catch (ApiException e) {
                    if (429 == e.getCode()){
                        // 设置动态速率，失效时间=1/limit
                        BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                        redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                        log.warn("【订单买家信息拉取】 platformShopCode={},接口首次429:放弃当前请求任务", platformShopCode);
                        // 触发限流不执行当前
                        DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                        initDmpResponse.setDoNextChain(false);
                        return Collections.emptyList();
                    }
                    throw new ServiceException("查询亚马逊订单买家信息失败：API异常："+JSONUtil.toJsonStr(e));
                } catch (Exception e) {
                    throw new ServiceException("查询亚马逊订单买家信息失败："+JSONUtil.toJsonStr(e));
                }
            }


            // 设置地址
            Address shippingAddress;
            String addressKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS.getBusinessTypeName(), uniqueId);
            Object addressResultObj = redisUtil.get(addressKey);
            if (null != addressResultObj) {
                shippingAddress = JSONUtil.toBean(addressResultObj.toString(), Address.class);
            } else {
                // 获取动态速率
                // 平台请求中:平台类型:sellerId:业务类型
                AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS;
                String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), platformShopCode, requestTypeRateLimiterEnum.getBusinessTypeName());
                Object limitObj = redisUtil.get(limitKey);
                if (null != limitObj){
                    log.warn("【订单地址拉取】 UniqueId={},存在429等待恢复:放弃当前请求任务", uniqueId);
                    // 触发限流不执行当前
                    DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                    initDmpResponse.setDoNextChain(false);
                    return Collections.emptyList();
                }
                String rateLimitStr = AmazonRequestTypeRateLimiterEnum.ORDER_ADDRESS.getRateLimit();
                try {
                    shippingAddress = getAddress(ordersVoApi, amazonOrderId, rateLimitStr, key);
                } catch (ApiException e) {
                    if (429 == e.getCode()){
                        // 设置动态速率，失效时间=1/limit
                        BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
                        redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
                        log.warn("【订单地址拉取】 platformShopCode={},接口首次429:放弃当前请求任务", platformShopCode);
                        // 触发限流不执行当前
                        DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                        initDmpResponse.setDoNextChain(false);
                        return Collections.emptyList();
                    }
                    throw new ServiceException("查询亚马逊订单地址失败：API异常："+JSONUtil.toJsonStr(e));
                } catch (Exception e) {
                    throw new ServiceException("查询亚马逊订单地址失败："+JSONUtil.toJsonStr(e));
                }
            }

            if (null == shippingAddress){
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
    private Address getAddress(OrdersV0Api ordersVoApi, String amazonOrderId, String rateLimitStr, String key) throws ApiException {
        ApiResponse<GetOrderAddressResponse> orderAddressResp = ordersVoApi.getOrderAddressWithHttpInfo(amazonOrderId);
        List<String> limitArray = orderAddressResp.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
        rateLimitStr = limitArray.get(0);
        GetOrderAddressResponse response = orderAddressResp.getData();
        // 结果缓存倒redis(消费完移除)
        redisUtil.set(key, JSONUtil.toJsonStr(response.getPayload().getShippingAddress()), 300);
        return response.getPayload().getShippingAddress();
    }

    /**
     * 请求买家信息
     */
    private OrderBuyerInfo getOrderBuyerInfo(OrdersV0Api ordersVoApi, String amazonOrderId, String rateLimitStr, String limitKey, String key) throws ApiException {
        ApiResponse<GetOrderBuyerInfoResponse> orderBuyerInfoResp = ordersVoApi.getOrderBuyerInfoWithHttpInfo(amazonOrderId);
        List<String> limitArray = orderBuyerInfoResp.getHeaders().get(ApiClient.X_AMAZON_RATE_LIMIT);
        rateLimitStr = limitArray.get(0);
        GetOrderBuyerInfoResponse response = orderBuyerInfoResp.getData();
        if (StringUtils.isNotBlank(rateLimitStr)){
            // 设置动态速率，失效时间=1/limit
            BigDecimal timeOut = BigDecimal.ONE.max(BigDecimal.ONE.divide(new BigDecimal(rateLimitStr), 8, RoundingMode.DOWN));
            redisUtil.set(limitKey, rateLimitStr, timeOut.longValue());
        }
        // 结果缓存倒redis(消费完移除)
        redisUtil.set(key, JSONUtil.toJsonStr(response.getPayload()), 300);
        return response.getPayload();
    }
}
