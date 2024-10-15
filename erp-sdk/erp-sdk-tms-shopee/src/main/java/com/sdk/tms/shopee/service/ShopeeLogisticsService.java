package com.sdk.tms.shopee.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.sdk.tms.shopee.constant.PathConstants;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.model.base.BaseResponse;
import com.sdk.tms.shopee.model.logistics.request.ShippingDocumentRequest;
import com.sdk.tms.shopee.model.logistics.request.ShippingOrderRequest;
import com.sdk.tms.shopee.model.logistics.request.TrackRequest;
import com.sdk.tms.shopee.model.logistics.response.ShippingDocumentParameterResponse;
import com.sdk.tms.shopee.model.logistics.response.TrackNumber;
import com.sdk.tms.shopee.utils.ShopeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
     * @param trackRequest
     * @return
     */
    public BaseResponse getTrackNumber(TrackRequest trackRequest) {
        trackRequest.setPath(PathConstants.GET_TRACK_NUMBER_URL);
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
        paramMap.put("order_sn", trackRequest.getOrderSn());
        return ShopeeApiUtils.sendGet(baseRequest.getHost() + baseRequest.getPath(), paramMap);
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
        String path = "/api/v2/logistics/get_shipping_document_parameter";
        baseRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> urlParams = getOrderCommonParam(baseRequest);
        HashMap<String, Object> params = new HashMap<>();
        params.put("order_list", JSONUtil.toJsonStr(orderRequestList));
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + path, urlParams, params);
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
        String path = "/api/v2/logistics/create_shipping_document";
        baseRequest.setPath(path);
        long timestamp = System.currentTimeMillis() / 1000L;
        baseRequest.setTimestamp(timestamp);
        HashMap<String, Object> urlParams = getOrderCommonParam(baseRequest);
        HashMap<String, Object> params = new HashMap<>();
        params.put("order_list", JSONUtil.toJsonStr(orderRequestList));
        BaseResponse baseResponse = ShopeeApiUtils.sendPost(baseRequest.getHost() + path, urlParams, params);
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
