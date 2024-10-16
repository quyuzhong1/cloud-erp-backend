package com.sdk.tms.shopee.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
    public static void main(String[] args) {
        String shop_access_token = "6e41797262594e465057794861595653";
        long partner_id = 1070627;
        String tmp_partner_key = "5975757847654870727869546f436e696f4b454d466a74586f46696555466348";
        long shop_id = 94349;
        ShopeeLogisticsService service = new ShopeeLogisticsService();
        BaseRequest baseRequest = BaseRequest.builder()
                .accessToken(shop_access_token)
                .partnerId(partner_id)
                .partnerKey(tmp_partner_key)
                .shopId(shop_id)
                .build();
        BaseResponse channelList = service.getChannelList(baseRequest);
        System.out.println(channelList);
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
            throw new ServiceException(StrUtil.format("虾皮获取【{}】物流单请求异常:{}",orderSn,baseResponse));
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString("error");
        if (StrUtil.isNotEmpty(error)) {
            log.error("获取追踪号异常：{}", error);
            throw new ServiceException(StrUtil.format("虾皮获取【{}】物流单接口异常:{}",orderSn,error));
        }
        return JSONObject.parseObject(response.toJSONString(), TrackResponse.class);
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
            return null;
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString("error");
        if (StrUtil.isNotEmpty(error)) {
            log.error("获取追踪号异常：{}", error);
            return null;
        }
        //追踪号列表
        return JSONObject.parseObject(response.toJSONString(), TrackNumber.class);
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
        jsonObject.put("order_list",orderRequestList);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), urlParams, jsonObject.toJSONString());
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            return null;
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString("error");
        if (StrUtil.isNotEmpty(error)) {
            log.error("获取面单打印参数异常：{}", error);
            return null;
        }
        //打印列表
        return JSONObject.parseArray(response.getString("result_list"), ShippingDocumentParameterResponse.class);
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
        jsonObject.put("order_list",orderRequestList);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), urlParams, jsonObject.toJSONString());
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            return null;
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString("error");
        if (StrUtil.isNotEmpty(error)) {
            log.error("创建面单打印异常：{}", error);
            return null;
        }
        //打印列表
        return JSONObject.parseArray(response.getString("result_list"), ShippingDocumentParameterResponse.class);
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
        jsonObject.put("order_list",orderRequestList);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), urlParams, jsonObject.toJSONString());
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            return null;
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString("error");
        if (StrUtil.isNotEmpty(error)) {
            log.error("获取创建发货面单异常：{}", error);
            return null;
        }
        //打印列表
        return JSONObject.parseArray(response.getString("result_list"), ShippingDocumentParameterResponse.class);
    }

    /**
     * 下载发货面单文件
     * @param baseRequest
     * @param orderRequestList
     * @param shippingDocumentType  The type of shipping document. Available values: NORMAL_AIR_WAYBILL,THERMAL_AIR_WAYBILL,NORMAL_JOB_AIR_WAYBILL,THERMAL_JOB_AIR_WAYBILL
     * @return
     */
    public byte[] downloadShippingDocument(BaseRequest baseRequest, List<ShippingOrderRequest> orderRequestList, String shippingDocumentType) {
        baseRequest.setPath(PathConstants.POST_DOWNLOAD_SHIPPING_DOCUMENT);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> urlParams = getOrderCommonParam(baseRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shipping_document_type",shippingDocumentType);
        jsonObject.put("order_list",orderRequestList);
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + baseRequest.getPath(), urlParams, jsonObject.toJSONString());
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            log.error("获取面单打印参数异常：{}", baseResponse);
            return null;
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString("error");
        if (StrUtil.isNotEmpty(error)) {
            log.error("获取面单打印参数异常：{}", error);
            return null;
        }
        //打印列表
        byte[] waybills = response.getBytes("waybill");
        return waybills;
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
        if (StrUtil.isNotBlank(packageNumber)){
            paramMap.put("package_number", packageNumber);
        }
        BaseResponse baseResponse = ShopeeApiUtils.sendGet(baseRequest.getHost() + path, paramMap);
        if (Objects.isNull(baseResponse) || Objects.isNull(baseResponse.getResponse())) {
            log.error("获取标发参数异常：{}", baseResponse);
            return null;
        }
        JSONObject response = baseResponse.getResponse();
        String error = response.getString("error");
        if (StrUtil.isNotEmpty(error)) {
            log.error("获取标发参数异常：{}", error);
            return null;
        }
        return JSONObject.parseObject(response.toJSONString(), ShipDetailResponse.class);
    }
    public BaseResponse shippingOrder(BaseRequest baseRequest, ShipOrderRequest shipOrderRequest) {
//        String path = "/api/v2/logistics/ship_order";
        String path = PathConstants.POST_SHIPPING_ORDER;
        baseRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> paramMap = getOrderCommonParam(baseRequest);
        return ShopeeApiUtils.sendPost(baseRequest.getHost() + path, paramMap, JSONUtil.toJsonStr(shipOrderRequest));
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
