package com.sdk.tms.weishi.server;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.sdk.tms.weishi.constants.WeiShiConstants;
import com.sdk.tms.weishi.dto.request.WeiShiCancelOrderRequest;
import com.sdk.tms.weishi.dto.request.WeiShiCreateOrderRequest;
import com.sdk.tms.weishi.dto.request.WeiShiGetLabelUrlRequest;
import com.sdk.tms.weishi.dto.request.WeiShiInterceptOrderRequest;
import com.sdk.tms.weishi.dto.response.WeiShiChannel;
import com.sdk.tms.weishi.dto.response.WeiShiCreateOrder;
import com.sdk.tms.weishi.dto.response.WeiShiGetLabelUrl;
import com.sdk.tms.weishi.dto.response.WeiShiResponse;
import com.sdk.tms.weishi.utils.WeiShiUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;

@Component
@Validated
public class WeiShiService {

    /**
     *  查询全部已开通的渠道
     * @return List<YanWenChannel>
     */
    public WeiShiResponse<List<WeiShiChannel>> getAllChannel(){
        String response = WeiShiUtils.sendPost(WeiShiConstants.METHOD_GET_SHIPPING,null);
        return JSONObject.parseObject(response,new TypeReference<WeiShiResponse<List<WeiShiChannel>>>() {}.getType());
    }

    /**
     *  创建订单
     */
    public WeiShiCreateOrder createOrder(@Valid WeiShiCreateOrderRequest request){
        String response = WeiShiUtils.sendPost(WeiShiConstants.METHOD_CREATE_ORDER,JSONObject.toJSONString(request));
        return JSONObject.parseObject(response,WeiShiCreateOrder.class);
    }

    /**
     *  打印标签
     */
    public WeiShiGetLabelUrl getLabelUrl(@Valid WeiShiGetLabelUrlRequest request){
        String response = WeiShiUtils.sendPost(WeiShiConstants.METHOD_GET_LABEL,JSONObject.toJSONString(request));
        return JSONObject.parseObject(response,WeiShiGetLabelUrl.class);
    }

    /**
     * 拦截订单
     */
    public WeiShiResponse interceptOrder(@Valid WeiShiInterceptOrderRequest request){
        String response = WeiShiUtils.sendPost(WeiShiConstants.METHOD_INTERCEPT_ORDER,JSONObject.toJSONString(request));
        return JSONObject.parseObject(response,WeiShiResponse.class);
    }

    /**
     * 拦截订单
     */
    public WeiShiResponse cancelOrder(@Valid WeiShiCancelOrderRequest request){
        String response = WeiShiUtils.sendPost(WeiShiConstants.METHOD_ORDER_CANCEL,JSONObject.toJSONString(request));
        return JSONObject.parseObject(response,WeiShiResponse.class);
    }
}
