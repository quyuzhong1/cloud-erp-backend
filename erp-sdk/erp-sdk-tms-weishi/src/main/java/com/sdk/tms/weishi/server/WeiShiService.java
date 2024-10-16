package com.sdk.tms.weishi.server;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.sdk.tms.weishi.constants.WeiShiConstants;
import com.sdk.tms.weishi.dto.request.*;
import com.sdk.tms.weishi.dto.response.*;
import com.sdk.tms.weishi.utils.WeiShiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
@Validated
public class WeiShiService {

    /**
     *  查询全部已开通的渠道
     * @return List<YanWenChannel>
     */
    public WeiShiResponse<List<WeiShiChannel>> getAllChannel(Map<String, String> authMap){
        String response = WeiShiUtils.sendPost(authMap.get("url"),WeiShiConstants.METHOD_GET_SHIPPING,"",authMap.get("clientId"),authMap.get("clientSecret"));
        return JSONObject.parseObject(response,new TypeReference<WeiShiResponse<List<WeiShiChannel>>>() {}.getType());
    }

    /**
     *  创建订单
     */
    public WeiShiCreateOrder createOrder(@Valid WeiShiCreateOrderRequest request,Map<String, String> authMap){
        log.info("==========WeiShiService.createOrder==========start");
        log.warn("authMap:{}, orderRequest:{}",authMap, JSONObject.toJSONString(request));
        String response = WeiShiUtils.sendPost(authMap.get("url"),WeiShiConstants.METHOD_CREATE_ORDER,JSONObject.toJSONString(request),authMap.get("clientId"),authMap.get("clientSecret"));
        log.warn("下单完成：{}",JSONObject.toJSONString(response));
        return JSONObject.parseObject(response,WeiShiCreateOrder.class);
    }
    /**
     *  创建订单
     */
    public String getOrder(@Valid String referenceNo ,Map<String, String> authMap){
        Map<String,String> map = new HashMap<>();
        map.put("reference_no",referenceNo);
        String response = WeiShiUtils.sendPost(authMap.get("url"),WeiShiConstants.GET_ORDER,JSONObject.toJSONString(map),authMap.get("clientId"),authMap.get("clientSecret"));
        return response;
    }
    /**
     *  打印标签
     */
    public WeiShiGetLabelUrl getLabelUrl(@Valid WeiShiGetLabelUrlRequest request,Map<String, String> authMap){
        String response = WeiShiUtils.sendPost(authMap.get("url"),WeiShiConstants.METHOD_GET_LABEL,JSONObject.toJSONString(request),authMap.get("clientId"),authMap.get("clientSecret"));
        return JSONObject.parseObject(response,WeiShiGetLabelUrl.class);
    }

    /**
     * 拦截订单
     */
    public WeiShiResponse<String> interceptOrder(@Valid WeiShiInterceptOrderRequest request,Map<String, String> authMap){
        String response = WeiShiUtils.sendPost(authMap.get("url"),WeiShiConstants.METHOD_INTERCEPT_ORDER,JSONObject.toJSONString(request),authMap.get("clientId"),authMap.get("clientSecret"));
        return JSONObject.parseObject(response,new TypeReference<WeiShiResponse<String>>() {}.getType());
    }

    /**
     * 取消订单
     */
    public WeiShiResponse<String> cancelOrder(@Valid WeiShiCancelOrderRequest request,Map<String, String> authMap){
        String response = WeiShiUtils.sendPost(authMap.get("url"),WeiShiConstants.METHOD_ORDER_CANCEL,JSONObject.toJSONString(request),authMap.get("clientId"),authMap.get("clientSecret"));
        return JSONObject.parseObject(response,new TypeReference<WeiShiResponse<String>>() {}.getType());
    }

    /**
     * 查询跟踪号
     */
    public WeiShiResponse<List<WeiShiGetTrackNumber>> getTrackNumber(@Valid WeiShiGetTrackNumberRequest request,Map<String, String> authMap){
        String response = WeiShiUtils.sendPost(authMap.get("url"),WeiShiConstants.METHOD_GET_TRACK_NUMBER,JSONObject.toJSONString(request),authMap.get("clientId"),authMap.get("clientSecret"));
        return JSONObject.parseObject(response,new TypeReference<WeiShiResponse<List<WeiShiGetTrackNumber>>>() {}.getType());
    }


    /**
     * 更新重量
     */
    public WeiShiResponse<String> updateWeight(@Valid List<WeiShiUpdateWeightRequest> request,Map<String, String> authMap){
        String response = WeiShiUtils.sendPost(authMap.get("url"),WeiShiConstants.MODIFY_ORDER_WEIGHT,request,authMap.get("clientId"),authMap.get("clientSecret"));
        return JSONObject.parseObject(response,new TypeReference<WeiShiResponse<String>>() {}.getType());
    }
}
