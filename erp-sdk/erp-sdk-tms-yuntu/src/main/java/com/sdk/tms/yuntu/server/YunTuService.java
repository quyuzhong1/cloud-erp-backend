package com.sdk.tms.yuntu.server;

import com.alibaba.fastjson.JSON;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.BeanMapUtil;
import com.sdk.tms.yuntu.constants.YunTuConstants;
import com.sdk.tms.yuntu.dto.request.*;
import com.sdk.tms.yuntu.dto.response.*;
import com.sdk.tms.yuntu.utils.YunTuUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Validated
public class YunTuService {

    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";

    private void validate(String appKey, String appSecret, String url){
        if (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(appSecret)|| StringUtils.isEmpty(url)) throw new ServiceException("授权信息不能为空");
    }
    /**
     *  查询全部已开通的渠道
     */
    public YunTuResponse<List<YunTuChannel>> getAllChannel(Map<String, String> authMap){
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(appKey,appSecret,url);
        String response = YunTuUtils.sendGet(url,YunTuConstants.METHOD_CHANNEL_GET,null,appKey,appSecret);
        return JSON.parseObject(response,new TypeReference<YunTuResponse<List<YunTuChannel>>>() {}.getType());
    }

    /**
     *  运单申请
     */
    public YunTuResponse<List<YunTuCreateOrder>> createOrder(@Valid List<YunTuCreateOrderRequest> request,Map<String, String> authMap){
        log.info("==========YunTuService.createOrder==========start");
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(appKey,appSecret,url);
        List<Map<String,Object>> paramsMapList =  BeanMapUtil.beanToMapList(request);
        log.warn("云途下单请求参数:{}", JSONObject.toJSONString(paramsMapList));
        String response = YunTuUtils.sendPost(url,YunTuConstants.METHOD_CREATE_ORDER,paramsMapList,appKey,appSecret);

        log.warn("下单完成：{}", JSON.toJSONString(response));
        return JSON.parseObject(response,new TypeReference<YunTuResponse<List<YunTuCreateOrder>>>() {}.getType());
    }

    /**
     *  查询跟踪号
     */
    public YunTuResponse<List<YunTuTrackingNumber>> getTrackingNumber(@Valid YunTuGetTrackingNumRequest request,Map<String, String> authMap){
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(appKey,appSecret,url);
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = YunTuUtils.sendGet(url,YunTuConstants.METHOD_GET_TRACKINGNUMBER,paramsMap,appKey,appSecret);
        return JSON.parseObject(response,new TypeReference<YunTuResponse<List<YunTuTrackingNumber>>>() {}.getType());
    }

    /**
     *  标签打印
     */
    public YunTuResponse<List<YunTuPrintLabel>> getPrintLabel(@Valid YunTuPrintLabelRequest request,Map<String, String> authMap){
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(appKey,appSecret,url);
        String response = YunTuUtils.sendPostList(url,YunTuConstants.METHOD_PRINT_LABEL,request.getOrderNumbers(),appKey,appSecret);
        return JSON.parseObject(response,new TypeReference<YunTuResponse<List<YunTuPrintLabel>>>() {}.getType());
    }

    /**
     *  订单拦截
     */
    public YunTuResponse<YunTuInterceptOrder> interceptOrder(@Valid YunTuInterceptOrderRequest request,Map<String, String> authMap){
        log.warn("云途拦截物流单：{}",JSON.toJSONString(request));
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(appKey,appSecret,url);
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = YunTuUtils.sendPost(url,YunTuConstants.METHOD_INTERCEPT_ORDER,paramsMap,appKey,appSecret);
        return JSON.parseObject(response,new TypeReference<YunTuResponse<YunTuInterceptOrder>>() {}.getType());
    }

    /**
     *  订单删除
     */
    public YunTuResponse<YunTuCancelOrder> cancelOrder(@Valid YunTuCancelOrderRequest request,Map<String, String> authMap){
        log.warn("云途取消物流单：{}",JSON.toJSONString(request));
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(appKey,appSecret,url);
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = YunTuUtils.sendPost(url,YunTuConstants.METHOD_CANCEL_ORDER,paramsMap,appKey,appSecret);
        log.warn("云途取消物流单传参:{},回参:,{}",JSON.toJSONString(request),response);
        return JSON.parseObject(response,new TypeReference<YunTuResponse<YunTuCancelOrder>>() {}.getType());
    }


    /**
     *  更新重量
     */
    public YunTuResponse<String> updateWeight(@Valid YunTuUpdateWeightRequest request,Map<String, String> authMap){
        String appKey = authMap.get(CLIENT_ID);
        String appSecret = authMap.get(CLIENT_SECRET);
        String url = authMap.get("url");
        validate(appKey,appSecret,url);
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = YunTuUtils.sendPost(url,YunTuConstants.METHOD_UPDATE_WEIGHT,paramsMap,appKey,appSecret);
        return JSON.parseObject(response,new TypeReference<YunTuResponse<String>>() {}.getType());
    }
}
