package com.sdk.tms.baohong.service;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.common.business.threadlocal.TransferLogisticsContext;
import com.sdk.tms.baohong.api.asn.ASNData;
import com.sdk.tms.baohong.api.asn.ReceivingInfo;
import com.sdk.tms.baohong.api.asn.ServiceForAsn;
import com.sdk.tms.baohong.api.order.*;
import com.sdk.tms.baohong.api.order.ErrorType;
import com.sdk.tms.baohong.api.order.HeaderRequest;
import com.sdk.tms.baohong.api.product.*;
import com.sdk.tms.baohong.dto.response.BaoHongResponse;
import com.sdk.tms.baohong.utils.BaoHongUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.xml.ws.Holder;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
@Slf4j
@Component
@Validated
public class BaoHongService {

    /**
     * 获取物流产品
     * @return
     */
    public BaoHongResponse<List<SmRow>> getShippingMethodList(){
        HeaderRequest headerRequest = BaoHongUtils.getOrderHeader();
        ServiceForOrder service = BaoHongUtils.getOrderService();
        Holder<Integer> pageHolder = new Holder<>();
        Holder<Integer> pageSizeHolder = new Holder<>();
        Holder<String> askHolder = new Holder<>();
        Holder<String> messageHolder = new Holder<>();
        Holder<Integer> totalHolder = new Holder<>();
        Holder<List<SmRow>> smRowListHolder = new Holder<>();
        BaoHongResponse<List<SmRow>> response = new BaoHongResponse<>();
        //最大页码100，从第一页开始查询
        List<SmRow> smRowList = new ArrayList<>();
        int page = 1;
        while (true) {
            pageHolder.value = page;
            pageSizeHolder.value = 100;
            service.getShippingMethodList(headerRequest, pageHolder, pageSizeHolder, askHolder, messageHolder, totalHolder, smRowListHolder);
            if(askHolder.value.equals("0")){
                if(messageHolder.value.equals("无数据")){
                    break;
                }
                response.setAsk(askHolder.value);
                response.setMessage(messageHolder.value);
                return response;
            }
            smRowList.addAll(smRowListHolder.value);
            if (smRowListHolder.value.size() < 100) {
                break;
            }
            page++;
        }
        response.setAsk("1");
        response.setData(smRowList);
        return response;
    }

    /**
     * 创建订单
     * @return
     */
    public BaoHongResponse<String> createOrder(CreateOrderInfo createOrderInfo){
//        createOrderInfo.getOrderProduct().forEach(v->v.setCurrencyCode("USD"));
        createOrderInfo.getOrderProduct().forEach(v->v.setPurposeDeclaredValue(null));
        log.warn("==========BaoHongService.createOrder==========createOrderInfo:{}",createOrderInfo);
        TransferLogisticsContext.setRequestJson(JSONObject.toJSONString(createOrderInfo));
        HeaderRequest headerRequest = BaoHongUtils.getOrderHeader();
        ServiceForOrder service = BaoHongUtils.getOrderService();
        Holder<String> askHolder = new Holder<>();
        Holder<String> messageHolder = new Holder<>();
        Holder<String> orderCodeHolder = new Holder<>();
        Holder<List<ErrorType>> error = new Holder<>();
        Holder<List<String>> skuHolder  = new Holder<>();
        service.createOrder(headerRequest,createOrderInfo,askHolder,messageHolder,orderCodeHolder,error,skuHolder);
        return BaoHongUtils.buildBaseResponse(askHolder,messageHolder,error,orderCodeHolder.value);
    }

    /**
     * 取消订单
     * @return
     */
    public BaoHongResponse<String> cancelOrder(String orderCode,String reason){
        TransferLogisticsContext.setRequestJson(JSONObject.toJSONString(orderCode));
        HeaderRequest headerRequest = BaoHongUtils.getOrderHeader();
        ServiceForOrder service = BaoHongUtils.getOrderService();
        Holder<String> askHolder = new Holder<>();
        Holder<String> messageHolder = new Holder<>();
        service.intercept(headerRequest,orderCode,reason,askHolder,messageHolder);
        return BaoHongUtils.buildBaseResponse(askHolder,messageHolder,"");
    }

    /**
     * 获取单个订单信息
     * @param orderCode:创建订单时传的参考号 或者返回的 保宏orderCode都行
     * @return
     */
    public BaoHongResponse<OrderDataArr> getOrderByCode(String orderCode){
        TransferLogisticsContext.setRequestJson(JSONObject.toJSONString(orderCode));
        HeaderRequest headerRequest = BaoHongUtils.getOrderHeader();
        ServiceForOrder service = BaoHongUtils.getOrderService();
        Holder<String> askHolder = new Holder<>();
        Holder<String> messageHolder = new Holder<>();
        Holder<OrderDataArr> data = new Holder<>();
        Holder<List<ErrorType>> error = new Holder<>();
        service.getOrderByCode(headerRequest,orderCode,askHolder,data,error);
        return BaoHongUtils.buildBaseResponse(askHolder,messageHolder,error,data.value);
    }

    /**
     * 查询全部产品信息
     * @return
     */
    public BaoHongResponse<List<DataRow>> getAllProductInfo(){
        com.sdk.tms.baohong.api.product.HeaderRequest headerRequest = BaoHongUtils.getProductHeader();
        ServiceForProduct service = BaoHongUtils.getProductService();
        Holder<String> askHolder = new Holder<>();
        Holder<String> messageHolder = new Holder<>();
        Holder<Integer> pageHolder = new Holder<>();
        Holder<Integer> pageSizeHolder = new Holder<>();
        Holder<Integer> totalHolder = new Holder<>();
        Holder<List<DataRow>> dataList = new Holder<>();
        BaoHongResponse<List<DataRow>> response = new BaoHongResponse<>();
        List<DataRow> dataRowList = new ArrayList<>();
        int page = 1;
        while (true) {
            pageHolder.value = page;
            pageSizeHolder.value = 500;
            service.getProductList(headerRequest,pageHolder,pageSizeHolder,askHolder,messageHolder,totalHolder,dataList);
            if(askHolder.value.equals("0")){
                if(messageHolder.value.equals("无数据")){
                    break;
                }
                response.setAsk(askHolder.value);
                response.setMessage(messageHolder.value);
                return response;
            }
            dataRowList.addAll(dataList.value);
            if (dataList.value.size() < 500) {
                break;
            }
            page++;
        }
        response.setAsk("1");
        response.setData(dataRowList);
        return response;
    }

    /**
     * 查询指定SKU产品信息
     * @return
     */
    public BaoHongResponse<ProductRow> getProductInfo(String skuNo){
        com.sdk.tms.baohong.api.product.HeaderRequest headerRequest = BaoHongUtils.getProductHeader();
        ServiceForProduct service = BaoHongUtils.getProductService();
        Holder<String> askHolder = new Holder<>();
        Holder<String> messageHolder = new Holder<>();
        Holder<ProductRow> data = new Holder<>();
        Holder<List<ErrorCodeMsgType>> errorCodeMsg = new Holder<>();
        service.getProduct(headerRequest,skuNo,askHolder,messageHolder,data,errorCodeMsg);
        return BaoHongUtils.buildBaseResponse(askHolder,messageHolder,data.value);
    }


    /**
     * 产品备案
     * @return
     */
    public BaoHongResponse<RecordItemResponse> filingProduct(RecordItemRequest recordItem){
        com.sdk.tms.baohong.api.product.HeaderRequest headerRequest = BaoHongUtils.getProductHeader();
        ServiceForProduct service = BaoHongUtils.getProductService();
        CreateRecordRequest parameters = new CreateRecordRequest();
        parameters.setHeaderRequest(headerRequest);
        parameters.setRecordItem(Collections.singletonList(recordItem));
        CreateRecordResponse createRecordResponse = service.createRecord(parameters);
        if(createRecordResponse.getAsk() != 1){
            return BaoHongUtils.buildBaseResponse(String.valueOf(createRecordResponse.getAsk()),createRecordResponse.getMessage(),null);
        }
        RecordItemResponse response = createRecordResponse.getRecordItem().get(0);
        return BaoHongUtils.buildBaseResponse(String.valueOf(response.getStatus()),response.getMessage(),response);
    }
    /**
     * 创建入库单
     * @return
     */
    public BaoHongResponse<String> createReceiving(ReceivingInfo receivingInfo){
        TransferLogisticsContext.setRequestJson(JSONObject.toJSONString(receivingInfo));
        com.sdk.tms.baohong.api.asn.HeaderRequest headerRequest = BaoHongUtils.getAsnHeader();
        ServiceForAsn service = BaoHongUtils.getAsnService();
        Holder<String> askHolder = new Holder<>();
        Holder<String> messageHolder = new Holder<>();
        Holder<String> error = new Holder<>();
        Holder<String> asnCode = new Holder<>();
        service.createReceiving(headerRequest,receivingInfo,askHolder,messageHolder,error,asnCode);
        return BaoHongUtils.buildBaseResponse(askHolder,messageHolder,asnCode.value);
    }
    /**
     * 获取入库单信息
     * @return
//     */
    public BaoHongResponse<ASNData> getReceiving(String code){
        TransferLogisticsContext.setRequestJson(JSONObject.toJSONString(code));
        com.sdk.tms.baohong.api.asn.HeaderRequest headerRequest = BaoHongUtils.getAsnHeader();
        ServiceForAsn service = BaoHongUtils.getAsnService();
        Holder<String> askHolder = new Holder<>();
        Holder<String> messageHolder = new Holder<>();
        Holder<String> error = new Holder<>();
        Holder<ASNData> data = new Holder<>();
        service.getAsnByCode(headerRequest,code,askHolder,messageHolder,error,data);
        return BaoHongUtils.buildBaseResponse(askHolder,messageHolder,data.value);
    }

    /**
     * 打印标签
     * @return
     */
    public BaoHongResponse<String> printLabel(String orderCode){
        TransferLogisticsContext.setRequestJson(JSONObject.toJSONString(orderCode));
        BaoHongResponse<String> result = BaoHongUtils.getPrintLabelBase64(orderCode);
        String base64 = result.getData();
        // 解码Base64
        byte[] decodedBytes = Base64.decodeBase64(base64);
        String jsonString = new String(decodedBytes, StandardCharsets.UTF_8);
        // 尝试解析为JSON
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            //解析成功，说明接口失败，封装失败信息
            result.setAsk("0");
            result.setMessage(jsonObject.get("message").toString());
            result.setData(jsonObject.get("data").toString());
        } catch (JSONException e) {
            // 解析失败，不是JSON格式,正常返回
            return result;
        }
        return result;
    }

}
