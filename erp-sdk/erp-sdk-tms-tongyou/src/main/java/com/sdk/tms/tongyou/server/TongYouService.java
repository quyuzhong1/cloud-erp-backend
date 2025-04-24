package com.sdk.tms.tongyou.server;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.sdk.tms.tongyou.constants.TongYouConstants;
import com.sdk.tms.tongyou.dto.TongYouSignDTO;
import com.sdk.tms.tongyou.dto.request.*;
import com.sdk.tms.tongyou.dto.response.*;
import com.sdk.tms.tongyou.utils.TongYouUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
@Slf4j
@Component
@Validated
public class TongYouService {

    public static final String CLIENT_SECRET = "clientSecret";

    /**
     *  查询全部已开通的渠道
     */
    public TongYouResponse<List<TongYouChannel>> getAllChannel(Map<String, String> authMap){
        String response = TongYouUtils.sendPost(TongYouConstants.METHOD_GET_CHANNEL,null,authMap.get(CLIENT_SECRET));
        return JSON.parseObject(response,new TypeReference<TongYouResponse<List<TongYouChannel>>>() {}.getType());
    }


    /**
     *  创建订单
     */
    public TongYouCreateOrder createOrder(TongYouCreateOrderRequest request,Map<String, String> authMap){
        log.info("==========TongYouService.createOrder==========start");
        log.warn("authMap:{}, orderRequest:{}",authMap, request);
        TongYouSignDTO tongYouSignDTO = TongYouSignDTO.builder()
                .logisticsId(request.getLogisticsId())
                .orderNo(request.getOrderNo())
                .countryCode(request.getRecipient().getCountryCode())
                .telNo(request.getRecipient().getTelNo())
                .address(request.getRecipient().getAddress())
                .city(request.getRecipient().getCity())
                .zip(request.getRecipient().getZip())
                .province(request.getRecipient().getProvince())
                .mobileNo(request.getRecipient().getMobileNo())
                .address3(request.getRecipient().getAddress3())
                .address2(request.getRecipient().getAddress2())
                .contactPerson(request.getRecipient().getContactPerson())
                .build();
        String sign = TongYouUtils.getCreateOrderSign(tongYouSignDTO,authMap.get(CLIENT_SECRET));
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = TongYouUtils.sendPost(TongYouConstants.METHOD_CREATE_ORDER,paramsMap,sign,authMap.get(CLIENT_SECRET));
        log.warn("下单完成：{}", JSON.toJSONString(response));
        return JSON.parseObject(response,TongYouCreateOrder.class);
    }

    /**
     *  回调订单
     */
    public TongYouCallBackOrder callBackOrderInfo(TongYouCallBackOrderRequest request,Map<String, String> authMap){
        String sign = TongYouUtils.getCallBackOrderSign(request.getLogisticsId(),request.getOrderNo(),authMap.get(CLIENT_SECRET));
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = TongYouUtils.sendPost(TongYouConstants.METHOD_ORDER_CALL_BACK,paramsMap,sign,authMap.get(CLIENT_SECRET));
        return JSON.parseObject(response, TongYouCallBackOrder.class);
    }


    /**
     *  打印标签
     */
    public TongYouPrintLabel printLabel(TongYouPrintLabelRequest request,Map<String, String> authMap){
        String sign = TongYouUtils.getPrintLabelSign(request.getLogisticsId(),request.getOrderNo(),request.getTrackNo(),authMap.get(CLIENT_SECRET));
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = TongYouUtils.sendPost(TongYouConstants.METHOD_PRINT_LABEL,paramsMap,sign,authMap.get(CLIENT_SECRET));
        return JSON.parseObject(response,TongYouPrintLabel.class);
    }


    /**
     *  查询订单信息
     */
    public TongYouOrderInfo getOrderInfo(TongYouGetOrderRequest request,Map<String, String> authMap){
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = TongYouUtils.sendPost(TongYouConstants.METHOD_ORDER_GET,paramsMap,authMap.get(CLIENT_SECRET));
        return JSON.parseObject(response,TongYouOrderInfo.class);
    }

    /**
     *  更新重量
     */
    public TongYouResponse<String> updateWeight(TongYouUpdateWeightRequest request, Map<String, String> authMap){
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String sign = TongYouUtils.getUpdateWeightSign(request,authMap.get(CLIENT_SECRET));
        String response = TongYouUtils.sendPost(TongYouConstants.METHOD_UPDATE_WEIGHT,paramsMap,sign,authMap.get(CLIENT_SECRET));
        return JSON.parseObject(response,new TypeReference<TongYouResponse<String>>() {}.getType());
    }
}
