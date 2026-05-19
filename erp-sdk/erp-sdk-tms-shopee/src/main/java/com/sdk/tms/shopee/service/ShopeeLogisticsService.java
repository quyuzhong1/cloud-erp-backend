package com.sdk.tms.shopee.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.sdk.tms.shopee.constant.PathConstants;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.model.base.BaseResponse;
import com.sdk.tms.shopee.model.firstmile.request.BindFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.request.CourierDeliveryWaybillRequest;
import com.sdk.tms.shopee.model.firstmile.request.FirstMileTrackingNumberListRequest;
import com.sdk.tms.shopee.model.firstmile.request.FirstMileWaybillRequest;
import com.sdk.tms.shopee.model.firstmile.request.GenerateAndBindFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.request.GenerateFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.request.UnbindFirstMileTrackingNumberAllRequest;
import com.sdk.tms.shopee.model.firstmile.request.UnbindFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.response.BindFirstMileTrackingNumberResponse;
import com.sdk.tms.shopee.model.firstmile.response.CourierDeliveryChannelResponse;
import com.sdk.tms.shopee.model.firstmile.response.CourierDeliveryWaybillResponse;
import com.sdk.tms.shopee.model.firstmile.response.FirstMileChannelListResponse;
import com.sdk.tms.shopee.model.firstmile.response.FirstMileTrackingNumberListResponse;
import com.sdk.tms.shopee.model.firstmile.response.GenerateAndBindFirstMileTrackingNumberResponse;
import com.sdk.tms.shopee.model.firstmile.response.GenerateFirstMileTrackingNumberResponse;
import com.sdk.tms.shopee.model.firstmile.response.TransitWarehouseListResponse;
import com.sdk.tms.shopee.model.firstmile.response.UnbindFirstMileTrackingNumberAllResponse;
import com.sdk.tms.shopee.model.firstmile.response.UnbindFirstMileTrackingNumberResponse;
import com.sdk.tms.shopee.model.logistics.request.ShipOrderRequest;
import com.sdk.tms.shopee.model.logistics.request.ShippingOrderRequest;
import com.sdk.tms.shopee.model.logistics.request.TrackRequest;
import com.sdk.tms.shopee.model.logistics.response.ShopeeAddressListResponse;
import com.sdk.tms.shopee.model.logistics.response.ShipDetailResponse;
import com.sdk.tms.shopee.model.logistics.response.ShippingDocumentParameterResponse;
import com.sdk.tms.shopee.model.logistics.response.TrackNumber;
import com.sdk.tms.shopee.model.logistics.response.TrackResponse;
import com.sdk.tms.shopee.model.merchant.request.MerchantPrepaidAccountRequest;
import com.sdk.tms.shopee.model.merchant.response.MerchantPrepaidAccountListResponse;
import com.sdk.tms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName ShopeeLogisticsService
 * @description: 物流供应商接口对接
 * @date 2023年10月30日
 * @version: 1.0
 */
@Slf4j
@Component
public class ShopeeLogisticsService {

    private static String ERR_MSG = "虾皮获取跟踪号列表请求异常:{}";

    private static String ERR_MSG_GET = "虾皮【{}】获取标发参数异常请求异常:{}";

    private static String ERR_BF = "获取标发参数异常：{}";
    
    private static String ERR_NAME = "error";
    
    private static String ORDER_LIST = "order_list";
    
    private static String RESULT_LIST= "result_list";

    private static String REGION = "region";

    private static String SHIPMENT_METHOD = "shipment_method";

    private static String MERCHANT_ID = "merchant_id";

    private static String PAGE_NO = "page_no";

    private static String PAGE_SIZE = "page_size";

    public static void main(String[] args) {

        ShopeeLogisticsService shopeeOrderService = new ShopeeLogisticsService();
        BaseRequest baseRequest = BaseRequest.builder()
                .host("https://openplatform.shopee.cn")
                .accessToken("eyJhbGciOiJIUzI1NiJ9.CLa8ehABGLHL7qgGIAEooM-qzAYwseSXwgU4AUAB.cI2aba0IK4sOn5xwgZt7tS3DCfM22QUQP56CbKc1jBE")
                .shopId(Long.parseLong("1696310705"))
                .partnerId(Long.parseLong("2006582"))
                .partnerKey("446568575a4b52694578456c4c78645969735a6f716b4b6550496754705a7a63")
                .build();
        JSONObject jsonObject = shopeeOrderService.requestShippingParameter(baseRequest,"2602060WJX5CUW","");
        System.out.println(jsonObject);
    }
    /**
     * 获取渠道列表
     *
     * @param baseRequest
     * @return
     */
    public BaseResponse getChannelList(BaseRequest baseRequest) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.GET_CHANNEL_LIST_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        return ShopeeApiUtils.sendGet(baseRequest.getHost() + baseRequest.getPath(), paramMap);
    }

    /**
     * 获取头程物流渠道列表。
     *
     * @param baseRequest 授权信息
     * @param region      发货区域，可选 CN、KR
     * @return 头程物流渠道列表
     */
    public FirstMileChannelListResponse getFirstMileChannelList(BaseRequest baseRequest, String region) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.GET_FIRST_MILE_CHANNEL_LIST_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        if (StringUtils.isNotBlank(region)) {
            paramMap.put(REGION, region);
        }
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(baseRequest.getHost() + baseRequest.getPath(), paramMap);
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取头程物流渠道列表请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("获取头程物流渠道列表异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮获取头程物流渠道列表接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取头程物流渠道列表响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), FirstMileChannelListResponse.class);
    }

    /**
     * 获取快递服务公司。
     *
     * @param baseRequest 授权信息
     * @param region      发货区域，国内快递寄送传 CN
     * @return 快递服务公司列表
     */
    public CourierDeliveryChannelResponse getCourierDeliveryChannelList(BaseRequest baseRequest, String region) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.GET_COURIER_DELIVERY_CHANNEL_LIST_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        if (StringUtils.isNotBlank(region)) {
            paramMap.put(REGION, region);
        }
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(baseRequest.getHost() + baseRequest.getPath(), paramMap);
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取快递服务公司请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("获取快递服务公司异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮获取快递服务公司接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取快递服务公司响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), CourierDeliveryChannelResponse.class);
    }

    /**
     * 获取Shopee转运仓信息。
     *
     * @param baseRequest    授权信息
     * @param region         发货区域，国内传 CN
     * @param shipmentMethod 发货方式：pickup、dropoff、self_deliver、courier_delivery
     * @return 转运仓列表
     */
    public TransitWarehouseListResponse getTransitWarehouseList(BaseRequest baseRequest, String region, String shipmentMethod) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.GET_TRANSIT_WAREHOUSE_LIST_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        if (StringUtils.isNotBlank(region)) {
            paramMap.put(REGION, region);
        }
        if (StringUtils.isNotBlank(shipmentMethod)) {
            paramMap.put(SHIPMENT_METHOD, shipmentMethod);
        }
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(baseRequest.getHost() + baseRequest.getPath(), paramMap);
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取转运仓信息请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("获取转运仓信息异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮获取转运仓信息接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取转运仓信息响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), TransitWarehouseListResponse.class);
    }

    /**
     * 获取卖家设置的地址。
     *
     * @param baseRequest 授权信息
     * @return 卖家地址列表
     */
    public ShopeeAddressListResponse getAddressList(BaseRequest baseRequest) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.GET_ADDRESS_LIST_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(baseRequest.getHost() + baseRequest.getPath(), paramMap);
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取卖家地址请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("获取卖家地址异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮获取卖家地址接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取卖家地址响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), ShopeeAddressListResponse.class);
    }

    /**
     * 获取卖家设置的月结账号。
     *
     * @param request 商户授权和分页信息
     * @return 月结账号列表
     */
    public MerchantPrepaidAccountListResponse getMerchantPrepaidAccountList(MerchantPrepaidAccountRequest request) {
        long timestamp = System.currentTimeMillis() / 1000L;
        request.setPath(PathConstants.GET_MERCHANT_PREPAID_ACCOUNT_LIST_URL);
        request.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getMerchantCommonParam(request);
        paramMap.put(PAGE_NO, request.getPageNo());
        paramMap.put(PAGE_SIZE, request.getPageSize());
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(request.getHost() + request.getPath(), paramMap);
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取卖家月结账号请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("获取卖家月结账号异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮获取卖家月结账号接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取卖家月结账号响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), MerchantPrepaidAccountListResponse.class);
    }

    /**
     * 快递寄送模式生成并绑定头程追踪号。
     *
     * @param baseRequest 授权信息
     * @param request     生成并绑定请求
     * @return 绑定结果
     */
    public GenerateAndBindFirstMileTrackingNumberResponse generateAndBindFirstMileTrackingNumber(BaseRequest baseRequest,
                                                                                                GenerateAndBindFirstMileTrackingNumberRequest request) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.POST_GENERATE_AND_BIND_FIRST_MILE_TRACKING_NUMBER_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), paramMap, JSON.toJSONString(request));
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮生成并绑定头程追踪号请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("生成并绑定头程追踪号异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮生成并绑定头程追踪号接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮生成并绑定头程追踪号响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), GenerateAndBindFirstMileTrackingNumberResponse.class);
    }

    /**
     * 快递寄送模式获取头程面单。
     *
     * @param baseRequest 授权信息
     * @param request     绑定ID列表请求
     * @return 面单下载信息
     */
    public CourierDeliveryWaybillResponse getCourierDeliveryWaybill(BaseRequest baseRequest, CourierDeliveryWaybillRequest request) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.POST_GET_COURIER_DELIVERY_WAYBILL_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), paramMap, JSON.toJSONString(request));
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取快递寄送模式面单请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("获取快递寄送模式面单异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮获取快递寄送模式面单接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取快递寄送模式面单响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), CourierDeliveryWaybillResponse.class);
    }

    /**
     * 解绑订单头程追踪号或绑定ID。
     *
     * @param baseRequest 授权信息
     * @param request     订单列表请求
     * @return 解绑结果
     */
    public UnbindFirstMileTrackingNumberAllResponse unbindFirstMileTrackingNumberAll(BaseRequest baseRequest,
                                                                                    UnbindFirstMileTrackingNumberAllRequest request) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.POST_UNBIND_FIRST_MILE_TRACKING_NUMBER_ALL_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), paramMap, JSON.toJSONString(request));
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮解绑头程追踪号请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("解绑头程追踪号异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮解绑头程追踪号接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮解绑头程追踪号响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), UnbindFirstMileTrackingNumberAllResponse.class);
    }

    /**
     * 解绑指定头程追踪号。
     *
     * @param baseRequest 授权信息
     * @param request     头程追踪号和订单列表
     * @return 解绑结果
     */
    public UnbindFirstMileTrackingNumberResponse unbindFirstMileTrackingNumber(BaseRequest baseRequest,
                                                                              UnbindFirstMileTrackingNumberRequest request) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.POST_UNBIND_FIRST_MILE_TRACKING_NUMBER_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), paramMap, JSON.toJSONString(request));
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮解绑指定头程追踪号请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("解绑指定头程追踪号异常：{}", baseResponse.getError());
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮解绑指定头程追踪号响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), UnbindFirstMileTrackingNumberResponse.class);
    }

    /**
     * 生成头程追踪号。
     *
     * @param baseRequest 授权信息
     * @param request     申报日期和生成数量
     * @return 头程追踪号列表
     */
    public GenerateFirstMileTrackingNumberResponse generateFirstMileTrackingNumber(BaseRequest baseRequest,
                                                                                  GenerateFirstMileTrackingNumberRequest request) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.POST_GENERATE_FIRST_MILE_TRACKING_NUMBER_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), paramMap, JSON.toJSONString(request));
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮生成头程追踪号请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("生成头程追踪号异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮生成头程追踪号接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮生成头程追踪号响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), GenerateFirstMileTrackingNumberResponse.class);
    }

    /**
     * 绑定头程追踪号。
     *
     * @param baseRequest 授权信息
     * @param request     头程追踪号和订单列表
     * @return 绑定结果
     */
    public BindFirstMileTrackingNumberResponse bindFirstMileTrackingNumber(BaseRequest baseRequest,
                                                                          BindFirstMileTrackingNumberRequest request) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.POST_BIND_FIRST_MILE_TRACKING_NUMBER_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), paramMap, JSON.toJSONString(request));
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮绑定头程追踪号请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("绑定头程追踪号异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮绑定头程追踪号接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮绑定头程追踪号响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), BindFirstMileTrackingNumberResponse.class);
    }

    /**
     * 获取头程面单文件。
     *
     * @param baseRequest 授权信息
     * @param request     头程追踪号列表
     * @return 面单文件base64
     */
    public String getWaybill(BaseRequest baseRequest, FirstMileWaybillRequest request) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.POST_GET_FIRST_MILE_WAYBILL_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        return ShopeeApiUtils.sendPostBase64(baseRequest.getHost() + baseRequest.getPath(), paramMap, JSON.toJSONString(request));
    }

    /**
     * 获取跟踪号（单个接口）
     *
     * @param baseRequest
     * @return
     */
    public TrackResponse getTrackNumber(BaseRequest baseRequest, String orderSn) {
        baseRequest.setPath(PathConstants.GET_TRACK_NUMBER_URL);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        paramMap.put("order_sn", orderSn);
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(baseRequest.getHost() + baseRequest.getPath(), paramMap);
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取【{}】物流单请求异常:{}",orderSn,baseResponse));
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString(ERR_NAME);
        if (StringUtils.isNotEmpty(error)) {
            log.error("获取追踪号异常：{}", error);
            throw new ServiceException(CharSequenceUtil.format("虾皮获取【{}】物流单接口异常:{}",orderSn,error));
        }
        TrackResponse trackResponse = JSON.parseObject(response.toJSONString(), TrackResponse.class);
        if (Objects.isNull(trackResponse) || StringUtils.isBlank(trackResponse.getTrackingNumber())){
            throw new ServiceException(CharSequenceUtil.format("虾皮获取【{}】物流单接口参数异常:{}",orderSn,trackResponse));
        }
        return trackResponse;
    }

    /**
     * 获取跟踪号列表
     *
     * @param trackRequest
     * @return
     */
    public TrackNumber getTrackNumberList(TrackRequest trackRequest) {
        trackRequest.setPath(PathConstants.GET_TRACK_NUMBER_LIST_URL);
        long timestamp = System.currentTimeMillis() / 1000L;
        trackRequest.setTimestamp(timestamp);
        BaseRequest baseRequest = BaseRequest.builder()
                .timestamp(trackRequest.getTimestamp())
                .path(trackRequest.getPath())
                .accessToken(trackRequest.getAccessToken())
                .partnerId(trackRequest.getPartnerId())
                .partnerKey(trackRequest.getPartnerKey())
                .shopId(trackRequest.getShopId())
                .build();
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        paramMap.put("from_date", trackRequest.getFromDate());
        paramMap.put("to_date", trackRequest.getToDate());
        paramMap.put("page_size", trackRequest.getPageSize());
        paramMap.put("cursor", trackRequest.getCursor());
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(baseRequest.getHost() + baseRequest.getPath(), paramMap);
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format(ERR_MSG,trackRequest));
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString(ERR_NAME);
        if (StringUtils.isNotEmpty(error)) {
            log.error("获取追踪号异常：{}", error);
            throw new ServiceException(CharSequenceUtil.format("虾皮获取跟踪号列表接口异常:{}",error));
        }
        //追踪号列表
        return JSON.parseObject(response.toJSONString(), TrackNumber.class);
    }

    /**
     * 获取头程追踪号列表。
     *
     * @param baseRequest 授权信息
     * @param request     查询日期和分页参数
     * @return 头程追踪号列表
     */
    public FirstMileTrackingNumberListResponse getTrackNumberList(BaseRequest baseRequest,
                                                                  FirstMileTrackingNumberListRequest request) {
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setPath(PathConstants.GET_TRACK_NUMBER_LIST_URL);
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        paramMap.put("from_date", request.getFromDate());
        paramMap.put("to_date", request.getToDate());
        if (Objects.nonNull(request.getPageSize())) {
            paramMap.put("page_size", request.getPageSize());
        }
        if (StringUtils.isNotBlank(request.getCursor())) {
            paramMap.put("cursor", request.getCursor());
        }
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(baseRequest.getHost() + baseRequest.getPath(), paramMap);
        if (Objects.isNull(baseResponse)) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取头程追踪号列表请求异常:{}", baseResponse));
        }
        if (StringUtils.isNotEmpty(baseResponse.getError())) {
            log.error("获取头程追踪号列表异常：{}", baseResponse.getError());
            throw new ServiceException(CharSequenceUtil.format("虾皮获取头程追踪号列表接口异常:{}", baseResponse.getError()));
        }
        if (Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取头程追踪号列表响应为空:{}", baseResponse));
        }
        return JSON.parseObject(baseResponse.getResponse().toJSONString(), FirstMileTrackingNumberListResponse.class);
    }

    /**
     * 获取面单打印参数
     * @param baseRequest
     * @param orderRequestList
     * @return
     */
    public List<ShippingDocumentParameterResponse> getShippingDocumentParameter(BaseRequest baseRequest, List<ShippingOrderRequest> orderRequestList) {
        baseRequest.setPath(PathConstants.POST_GET_SHIPPING_DOCUMENT_PARAMETER);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> urlParams = getOrderCommonParam(baseRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ORDER_LIST,orderRequestList);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), urlParams, jsonObject.toJSONString());
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format(ERR_MSG,orderRequestList));
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString(ERR_NAME);
        if (StringUtils.isNotEmpty(error)) {
            log.error("获取面单打印参数异常：{}", error);
            throw new ServiceException(CharSequenceUtil.format(ERR_MSG,error));
        }
        //打印列表
        return JSON.parseArray(response.getString(RESULT_LIST), ShippingDocumentParameterResponse.class);
    }
    /**
     * 创建面单打印
     * @param baseRequest
     * @param orderRequestList
     * @return
     */
    public List<ShippingDocumentParameterResponse> createShippingDocument(BaseRequest baseRequest, List<ShippingOrderRequest> orderRequestList) {
        baseRequest.setPath(PathConstants.POST_CREATE_SHIPPING_DOCUMENT);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> urlParams = getOrderCommonParam(baseRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ORDER_LIST,orderRequestList);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), urlParams, jsonObject.toJSONString());
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮创建面单打印请求异常:{}",baseResponse));
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString(ERR_NAME);
        if (StringUtils.isNotEmpty(error)) {
            log.error("创建面单打印异常：{}", error);
            throw new ServiceException(CharSequenceUtil.format("虾皮创建面单打印请求异常:{}",error));
        }
        //打印列表
        return JSON.parseArray(response.getString(RESULT_LIST), ShippingDocumentParameterResponse.class);
    }

    /**
     * 获取创建发货面单结果
     * @param baseRequest
     * @param orderRequestList
     * @return
     */
    public List<ShippingDocumentParameterResponse> getShippingDocumentResult(BaseRequest baseRequest, List<ShippingOrderRequest> orderRequestList) {
        baseRequest.setPath(PathConstants.POST_GET_SHIPPING_DOCUMENT_RESULT);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> urlParams = getOrderCommonParam(baseRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ORDER_LIST,orderRequestList);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), urlParams, jsonObject.toJSONString());
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            throw new ServiceException(CharSequenceUtil.format("虾皮获取创建发货面单结果请求异常:{}",orderRequestList));
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString(ERR_NAME);
        if (StringUtils.isNotEmpty(error)) {
            log.error("获取创建发货面单异常：{}", error);
            throw new ServiceException(CharSequenceUtil.format("虾皮获取创建发货面单结果请求异常:{}",error));
        }
        //打印列表
        return JSON.parseArray(response.getString(RESULT_LIST), ShippingDocumentParameterResponse.class);
    }

    /**
     * 下载发货面单文件
     * @param baseRequest
     * @param orderRequestList
     * @param shippingDocumentType  The type of shipping document. Available values: NORMAL_AIR_WAYBILL,THERMAL_AIR_WAYBILL,NORMAL_JOB_AIR_WAYBILL,THERMAL_JOB_AIR_WAYBILL
     * @return
     */
    public String downloadShippingDocument(BaseRequest baseRequest, List<ShippingOrderRequest> orderRequestList, String shippingDocumentType) {
        baseRequest.setPath(PathConstants.POST_DOWNLOAD_SHIPPING_DOCUMENT);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> urlParams = getOrderCommonParam(baseRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shipping_document_type",shippingDocumentType);
        jsonObject.put(ORDER_LIST,orderRequestList);
        return ShopeeApiUtils.sendPostBase64(baseRequest.getHost() + baseRequest.getPath(), urlParams, jsonObject.toJSONString());
    }

    /**
     * 请求配送信息和解析内容
     * 获取标发参数
     * @param baseRequest
     * @return
     */
    public ShipDetailResponse getShippingParameter(BaseRequest baseRequest, String orderSn, String packageNumber) {
        JSONObject response = requestShippingParameter(baseRequest, orderSn, packageNumber);
        ShipDetailResponse shipDetailResponse = JSON.parseObject(response.toJSONString(), ShipDetailResponse.class);
        if (Objects.isNull(shipDetailResponse)){
            throw new ServiceException(CharSequenceUtil.format("虾皮【{}】获取标发参数接口异常:{}",orderSn,response));
        }
        return shipDetailResponse;
    }

    /**
     * 请求配送信息
     */
    public JSONObject requestShippingParameter(BaseRequest baseRequest, String orderSn, String packageNumber) {
        String path = PathConstants.GET_SHIPPING_PARAMETER;
        baseRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        //请求参数
        paramMap.put("order_sn", orderSn);
        if (StringUtils.isNotBlank(packageNumber)){
            paramMap.put("package_number", packageNumber);
        }
        System.out.println(JSONUtil.toJsonStr(paramMap));
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(baseRequest.getHost() + path, paramMap);
        System.out.println(JSONUtil.toJsonStr(baseResponse));
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            log.error(ERR_BF, baseResponse);
            throw new ServiceException(CharSequenceUtil.format(ERR_MSG_GET, orderSn,baseResponse));
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString(ERR_NAME);
        if (StringUtils.isNotEmpty(error)) {
            log.error(ERR_BF, error);
            throw new ServiceException(CharSequenceUtil.format(ERR_MSG_GET, orderSn,error));
        }
        return response;
    }

    public BaseResponse shippingOrder(BaseRequest baseRequest, ShipOrderRequest shipOrderRequest) {

        String path = PathConstants.POST_SHIPPING_ORDER;
        baseRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + path, paramMap, JSONUtil.toJsonStr(shipOrderRequest));
        if (Objects.isNull(baseResponse)) {
            log.error(ERR_BF, baseResponse);
            throw new ServiceException(CharSequenceUtil.format("虾皮【{}】获取标发参数异常请求异常",shipOrderRequest.getOrderSn()));
        }
        String error = baseResponse.getError();
        if (StringUtils.isNotEmpty(error)) {
            log.error(ERR_BF, error);
            throw new ServiceException(CharSequenceUtil.format(ERR_MSG_GET,shipOrderRequest.getOrderSn(),error));
        }
        return baseResponse;
    }

    private HashMap<String, Object> getOrderCommonParam(BaseRequest baseRequest) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("timestamp", baseRequest.getTimestamp());
        paramMap.put("sign", ShopeeApiUtils.getOrderSign(baseRequest.getPath(), baseRequest.getAccessToken(), baseRequest.getPartnerId(),
                baseRequest.getPartnerKey(), baseRequest.getShopId()));
        paramMap.put("shop_id", baseRequest.getShopId());
        paramMap.put("partner_id", baseRequest.getPartnerId());
        paramMap.put("access_token", baseRequest.getAccessToken());
        return paramMap;
    }

    private HashMap<String, Object> getMerchantCommonParam(MerchantPrepaidAccountRequest request) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("timestamp", request.getTimestamp());
        paramMap.put("sign", ShopeeApiUtils.getMerchantSign(request.getPath(), request.getAccessToken(), request.getPartnerId(),
                request.getPartnerKey(), request.getMerchantId(), request.getTimestamp()));
        paramMap.put(MERCHANT_ID, request.getMerchantId());
        paramMap.put("partner_id", request.getPartnerId());
        paramMap.put("access_token", request.getAccessToken());
        return paramMap;
    }
}
