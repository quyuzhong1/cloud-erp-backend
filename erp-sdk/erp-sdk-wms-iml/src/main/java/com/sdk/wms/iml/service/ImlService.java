package com.sdk.wms.iml.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.core.utils.Md5Util;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.iml.constants.ImlConstants;
import com.sdk.wms.iml.dto.ImlBaseResp;
import com.sdk.wms.iml.dto.request.*;
import com.sdk.wms.iml.dto.response.*;
import com.sdk.wms.iml.utils.ImlUtils;
import io.seata.common.util.StringUtils;
import jodd.util.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuruipeng
 */
@Component
@Validated
public class ImlService {

    public static final String RECEIVING_CODE = "receiving_code";

    private String getPreUrl(){
        if (BusinessCommonConstants.hasProfile("prod")) {
            return "https://open.imlb2c.com/";
        } else {
            return "https://pre-open.imlb2c.cn/";
        }
    }

    private static final String APP_ID = "1929841041771364354";
    private static final String APP_SECRET = "dx-zosnwtgwo3=u=276qgzu+3weguyst";
    private static final String API_URL = "https://pre-open.imlb2c.cn/open-sdk/oms/query_warehouse";
    private static final String REQUEST_TOKEN = "ZOFsMc85N29ly-sA4qKbDXQgJS6QF2A8IzlCWWXH_UgoaGoY6Az8aZuU_uWuQ6s0";

    public static void main(String[] args) {
        Map<String,Object> body = new HashMap<>();
        String timestamp = String.valueOf(new Date().getTime());
        String appSign = Md5Util.md5(APP_SECRET + timestamp + JSONObject.toJSONString(body));
        Map<String,String> headerMap = new HashMap<>();
        headerMap.put("x-app-id",APP_ID);
        headerMap.put("x-app-sign",appSign);
        headerMap.put("x-request-time",timestamp);
        headerMap.put("x-request-token",REQUEST_TOKEN);
        String bodyStr = OkHttpUtils.doPostJson(API_URL,body, headerMap);
        System.out.println(bodyStr);
    }

    /**
     * 获取仓库列表
     */
    public ImlBaseResp<String> getWarehouse(){
        Map<String, String> headerMap = ImlUtils.buildHearderMap(new HashMap<>());
        String path = "open-sdk/oms/query_warehouse";
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,new HashMap<>(), headerMap);
        return ImlUtils.parseToImlResp(bodyStr, String.class);
    }

    /**
     * 获取物流产品
     */
    public ImlBaseResp<List<ImlLogisticChannelResp>> getShippingMethod(){
        Map<String, String> headerMap = ImlUtils.buildHearderMap(new HashMap<>());
        String path = "open-sdk/fms/product_query";
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,new HashMap<>(), headerMap);
        return ImlUtils.parseToImlResp(bodyStr, new TypeReference<ImlBaseResp<List<ImlLogisticChannelResp>>>() {});
    }

    /**
     * 获取入库单
     */
    public ImlResponse<List<ImlReceiptResp>> getReceiptBatch(@Valid ImlGetReceiptReq imlGetReceiptReq){
        String response = ImlUtils.callService(ImlConstants.METHOD_GET_RECEIPT,imlGetReceiptReq);
        return JSON.parseObject(response,new TypeReference<ImlResponse<List<ImlReceiptResp>>>() {}.getType());
    }

    /**
     * 创建入库单
     */
    public ImlResponse<String> createInboundBill(@Valid ImlCreateInboundReq imlGetReceiptReq){
        String response = ImlUtils.callService(ImlConstants.METHOD_CREATE_INBOUND,imlGetReceiptReq);
        ImlResponse<String> respDto = JSON.parseObject(response,new TypeReference<ImlResponse<String>>() {}.getType());
        //处理返回值
        if (StringUtil.isNotBlank(respDto.getData())) {
            respDto.setData(JSON.parseObject(respDto.getData()).getString(RECEIVING_CODE));
        }
        if(StringUtil.isNotBlank(respDto.getReceivingCode()) && StringUtil.isBlank(respDto.getData())){
            respDto.setData(respDto.getReceivingCode());
        }
        return respDto;
    }

    /**
     * 编辑入库单
     */
    public ImlResponse<String> editInboundBill(@Valid ImlCreateInboundReq imlGetReceiptReq){
        String response = ImlUtils.callService(ImlConstants.METHOD_EDIT_INBOUND,imlGetReceiptReq);
        ImlResponse<String> respDto = JSON.parseObject(response,new TypeReference<ImlResponse<String>>() {}.getType());
        //处理返回值
        if (StringUtil.isNotBlank(respDto.getData())) {
            respDto.setData(JSON.parseObject(respDto.getData()).getString(RECEIVING_CODE));
        }
        if(StringUtil.isNotBlank(respDto.getReceivingCode()) && StringUtil.isBlank(respDto.getData())){
            respDto.setData(respDto.getReceivingCode());
        }
        if(StringUtils.isNotBlank(respDto.getMessage()) && respDto.getMessage().contains("不可编辑")){
            respDto.setAsk("Success");
            respDto.setData(imlGetReceiptReq.getReceivingCode());
        }
        return respDto;
    }
    /**
     * 取消入库单
     */
    public ImlResponse<String> cancelInboundBill(@Valid @NotEmpty(message = "入库单号不能为空") String receivingCode){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put(RECEIVING_CODE,receivingCode);
        String response = ImlUtils.callService(ImlConstants.METHOD_CANCEL_INBOUND,paramsMap);
        return JSON.parseObject(response,new TypeReference<ImlResponse<String>>() {}.getType());
    }


    /**
     * 创建出库单
     */
    public ImlResponse<String> createOutboundBill(@Valid ImlCreateOutboundReq imlCreateOutboundReq){
        String response = ImlUtils.callService(ImlConstants.METHOD_CREATE_ORDER,imlCreateOutboundReq);
        ImlResponse<String> respDto = JSON.parseObject(response,new TypeReference<ImlResponse<String>>() {}.getType());
        //处理返回值
        if(StringUtil.isNotBlank(respDto.getOrderCode())){
            respDto.setData(respDto.getOrderCode());
        }
        return respDto;
    }

    /**
     * 取消出库单
     */
    public ImlResponse<String> cancelOutboundBill(@Valid @NotEmpty(message = "入库单号不能为空") String orderCode,String reason){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("order_code",orderCode);
        paramsMap.put("reason",reason);
        String response = ImlUtils.callService(ImlConstants.METHOD_CANCEL_ORDER,paramsMap);
        return JSON.parseObject(response,new TypeReference<ImlResponse<String>>() {}.getType());
    }
}
