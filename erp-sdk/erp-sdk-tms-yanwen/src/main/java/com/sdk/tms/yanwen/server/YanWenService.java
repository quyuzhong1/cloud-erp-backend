package com.sdk.tms.yanwen.server;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.core.enums.ApiError;
import com.sdk.tms.yanwen.constants.YanWenConstants;
import com.sdk.tms.yanwen.dto.request.YanWenCancelOrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.request.YanWenGetLabelRequest;
import com.sdk.tms.yanwen.dto.response.YanWenChannel;
import com.sdk.tms.yanwen.dto.response.YanWenCreateWayBill;
import com.sdk.tms.yanwen.dto.response.YanWenGetLabel;
import com.sdk.tms.yanwen.dto.response.YanWenResponse;
import com.sdk.tms.yanwen.utils.YanWenUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;


@Component
@Validated
public class YanWenService {

    /**
     *  查询全部已开通的渠道
     * @return List<YanWenChannel>
     */
    public YanWenResponse<List<YanWenChannel>> getAllChannel(){
        String response = YanWenUtils.sendPost(YanWenConstants.METHOD_CHANNEL_GETLIST,null);
        YanWenResponse<List<YanWenChannel>> yanWenResponseDTO;
        try {
            yanWenResponseDTO = JSONObject.parseObject(response,new TypeReference<YanWenResponse<List<YanWenChannel>>>() {}.getType());
        }catch (JSONException e){
            yanWenResponseDTO = YanWenResponse.error(ApiError.ERROR_400.code.toString(),response);
        }

        return yanWenResponseDTO;
    }

    /**
     *  创建运单
     */
    public YanWenResponse<YanWenCreateWayBill> createWayBill(@Valid YanWenCreateWayBillRequest request){
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = YanWenUtils.sendPost(YanWenConstants.METHOD_ORDER_CREATE,paramsMap);
        YanWenResponse<YanWenCreateWayBill> yanWenResponseDTO;
        try {
            yanWenResponseDTO = JSONObject.parseObject(response,new TypeReference<YanWenResponse<YanWenCreateWayBill>>() {}.getType());
        }catch (JSONException e){
            yanWenResponseDTO = YanWenResponse.error(ApiError.ERROR_400.code.toString(),response);
        }

        return yanWenResponseDTO;
    }

    /**
     * 打印标签
     */
    public YanWenResponse<YanWenGetLabel> getLabel(@Valid YanWenGetLabelRequest request){
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = YanWenUtils.sendPost(YanWenConstants.METHOD_ORDER_LABEL_GET,paramsMap);
        YanWenResponse<YanWenGetLabel> yanWenResponseDTO;
        try {
            yanWenResponseDTO =  JSONObject.parseObject(response,new TypeReference<YanWenResponse<YanWenGetLabel>>() {}.getType());
        }catch (JSONException e){
            yanWenResponseDTO = YanWenResponse.error(ApiError.ERROR_400.code.toString(),response);
        }

        return yanWenResponseDTO;
    }

    /**
     *  取消订单
     */
    public YanWenResponse cancelOrder(@Valid YanWenCancelOrderRequest request){
        Map<String, Object> paramsMap = BeanUtil.beanToMap(request);
        String response = YanWenUtils.sendPost(YanWenConstants.METHOD_ORDER_CANCEL,paramsMap);
        YanWenResponse yanWenResponseDTO;
        try {
            yanWenResponseDTO =  JSONObject.parseObject(response,YanWenResponse.class);
        }catch (JSONException e){
            yanWenResponseDTO = YanWenResponse.error(ApiError.ERROR_400.code.toString(),response);
        }

        return yanWenResponseDTO;
    }
}
