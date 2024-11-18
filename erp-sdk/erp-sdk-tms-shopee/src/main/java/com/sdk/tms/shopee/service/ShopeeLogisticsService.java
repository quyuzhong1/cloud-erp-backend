package com.sdk.tms.shopee.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.sdk.tms.shopee.constant.PathConstants;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.model.base.BaseResponse;
import com.sdk.tms.shopee.model.logistics.request.ShipOrderRequest;
import com.sdk.tms.shopee.model.logistics.request.ShippingDocumentRequest;
import com.sdk.tms.shopee.model.logistics.request.ShippingOrderRequest;
import com.sdk.tms.shopee.model.logistics.request.TrackRequest;
import com.sdk.tms.shopee.model.logistics.response.ShipDetailResponse;
import com.sdk.tms.shopee.model.logistics.response.ShippingDocumentParameterResponse;
import com.sdk.tms.shopee.model.logistics.response.TrackNumber;
import com.sdk.tms.shopee.model.logistics.response.TrackResponse;
import com.sdk.tms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 获取标发参数
     * @param baseRequest
     * @return
     */
    public ShipDetailResponse getShippingParameter(BaseRequest baseRequest, String orderSn, String packageNumber) {
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
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(baseRequest.getHost() + path, paramMap);
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            log.error(ERR_BF, baseResponse);
            throw new ServiceException(CharSequenceUtil.format(ERR_MSG_GET,orderSn,baseResponse));
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString(ERR_NAME);
        if (StringUtils.isNotEmpty(error)) {
            log.error(ERR_BF, error);
            throw new ServiceException(CharSequenceUtil.format(ERR_MSG_GET,orderSn,error));
        }
        ShipDetailResponse shipDetailResponse = JSON.parseObject(response.toJSONString(), ShipDetailResponse.class);
        if (Objects.isNull(shipDetailResponse)){
            throw new ServiceException(CharSequenceUtil.format("虾皮【{}】获取标发参数接口异常:{}",orderSn,response));
        }
        return shipDetailResponse;
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
}
