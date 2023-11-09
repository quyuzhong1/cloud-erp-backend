package com.sdk.tms.yuntu.server;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.BeanMapUtil;
import com.sdk.tms.yuntu.constants.YunTuConstants;
import com.sdk.tms.yuntu.dto.request.*;
import com.sdk.tms.yuntu.dto.response.*;
import com.sdk.tms.yuntu.utils.YunTuUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;


@Component
@Validated
public class YunTuService {

    /**
     *  查询全部已开通的渠道
     */
    public YunTuResponse<List<YunTuChannel>> getAllChannel(){
        String response = YunTuUtils.sendGet(YunTuConstants.METHOD_CHANNEL_GET,null);
        return JSONObject.parseObject(response,new TypeReference<YunTuResponse<List<YunTuChannel>>>() {}.getType());
    }

    /**
     *  运单申请
     */
    public YunTuResponse<List<YunTuCreateOrder>> createOrder(@Valid List<YunTuCreateOrderRequest> request){
        List<Map<String,Object>> paramsMapList =  BeanMapUtil.beanToMapList(request);
        String response = YunTuUtils.sendPost(YunTuConstants.METHOD_CREATE_ORDER,paramsMapList);
        return JSONObject.parseObject(response,new TypeReference<YunTuResponse<List<YunTuCreateOrder>>>() {}.getType());
    }

    /**
     *  查询跟踪号
     */
    public YunTuResponse<List<YunTuTrackingNumber>> getTrackingNumber(@Valid YunTuGetTrackingNumRequest request){
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = YunTuUtils.sendGet(YunTuConstants.METHOD_GET_TRACKINGNUMBER,paramsMap);
        return JSONObject.parseObject(response,new TypeReference<YunTuResponse<List<YunTuTrackingNumber>>>() {}.getType());
    }

    /**
     *  标签打印
     */
    public YunTuResponse<List<YunTuPrintLabel>> getPrintLabel(@Valid YunTuPrintLabelRequest request){
        String response = YunTuUtils.sendPostList(YunTuConstants.METHOD_PRINT_LABEL,request.getOrderNumbers());
        return JSONObject.parseObject(response,new TypeReference<YunTuResponse<List<YunTuPrintLabel>>>() {}.getType());
    }

    /**
     *  订单拦截
     */
    public YunTuResponse<YunTuInterceptOrder> interceptOrder(@Valid YunTuInterceptOrderRequest request){
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = YunTuUtils.sendPost(YunTuConstants.METHOD_INTERCEPT_ORDER,paramsMap);
        return JSONObject.parseObject(response,new TypeReference<YunTuResponse<YunTuInterceptOrder>>() {}.getType());
    }

    /**
     *  订单删除
     */
    public YunTuResponse<YunTuCancelOrder> cancelOrder(@Valid YunTuCancelOrderRequest request){
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = YunTuUtils.sendPost(YunTuConstants.METHOD_CANCEL_ORDER,paramsMap);
        return JSONObject.parseObject(response,new TypeReference<YunTuResponse<YunTuCancelOrder>>() {}.getType());
    }
}
