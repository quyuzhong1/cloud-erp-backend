package com.sdk.wms.goodcang.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.core.exception.ServiceException;
import com.sdk.wms.goodcang.constants.GoodCangConstants;
import com.sdk.wms.goodcang.dto.request.*;
import com.sdk.wms.goodcang.dto.response.*;
import com.sdk.wms.goodcang.utils.GoodCangUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Slf4j
@Component
@Validated
public class GoodCangService {

    public static final String GOOG_CANG_RESPONSE = "谷仓接口返回为空";
    public static final String GOOG_CANG_RESPONSE_ERROR = "谷仓接口返回格式错误";
    public static final String RECEIVING_CODE = "receiving_code";

    /**
     * 授权（调用拉取仓库接口，接口调用成功则说明授权成功）
     */
    public GoodCangResponse<Object> authorization(){
        return GoodCangResponse.builder().build();
    }

    /**
     * 获取商品列表
     */
    public GoodCangResponse<List<GoodCangSkuResp>> getSkuList(@Valid GoodCangGetSkuReq goodCangGetSkuReq){
        String json = JSON.toJSONString(goodCangGetSkuReq);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_SKU_LIST,json);
        GoodCangResponse<List<GoodCangSkuResp>> result = JSON.parseObject(response,new TypeReference<GoodCangResponse<List<GoodCangSkuResp>>>() {}.getType());
        if(Objects.isNull(result)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return result;
    }

    /**
     * 获取仓库列表
     */
    public GoodCangResponse<List<GoodCangWarehouseResp>> getWarehouse(){
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_WAREHOUSE,new HashMap<>());
        GoodCangResponse<List<GoodCangWarehouseResp>> result = JSON.parseObject(response,new TypeReference<GoodCangResponse<List<GoodCangWarehouseResp>>>() {}.getType());
        if(Objects.isNull(result)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return result;
    }

    /**
     * 获取物流产品与目的仓中转仓
     */
    public GoodCangResponse<GoodCangLogisticsAndWarehouseResp> getSmCodeTwcToWarehouse(){
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_SMCODE_TWC_TO_WAREHOUSE,new HashMap<>());
        GoodCangResponse<GoodCangLogisticsAndWarehouseResp> result = JSON.parseObject(response,new TypeReference<GoodCangResponse<GoodCangLogisticsAndWarehouseResp>>() {}.getType());
        if(Objects.isNull(result)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return result;
    }

    /**
     * 获取入库单明细
     * @param receivingCode 入库单号
     */
    public GoodCangResponse<GoodCangReceiptBatchResp> getInboundDetail(@Valid @NotEmpty String receivingCode){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put(RECEIVING_CODE,receivingCode);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_GRN_DETAIL,paramsMap);
        GoodCangResponse<GoodCangReceiptBatchResp> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<GoodCangReceiptBatchResp>>() {}.getType());
        if(Objects.isNull(respDto)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return respDto;
    }

    /**
     * 获取出库数据
     */
    public GoodCangResponse<List<GoodCangOutboundResp>> getOutboundBatch(GoodCangGetOutBoundReq goodCangGetOutBoundReq){
        String json = JSON.toJSONString(goodCangGetOutBoundReq);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_ORDER_LIST,json);
        log.debug("请求谷仓出库单结果:{}", response);
        GoodCangResponse<List<GoodCangOutboundResp>> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<List<GoodCangOutboundResp>>>() {}.getType());
        if(Objects.isNull(respDto)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return respDto;
    }

    /**
     * 获取库存
     */
    public GoodCangResponse<List<GoodCangInventoryResp>> getProductInventory(@Valid GoodCangGetInventoryReq goodCangGetInventoryReq){
        String json = JSON.toJSONString(goodCangGetInventoryReq);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_PRODUCT_INVENTORY,json);
        GoodCangResponse<List<GoodCangInventoryResp>> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<List<GoodCangInventoryResp>>>() {}.getType());
        if(Objects.isNull(respDto)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return respDto;
    }

    /**
     * 获取物流产品
     */
    public GoodCangResponse<List<GoodCangLogisticsProductsResp>> getShippingMethod(String warehouseCode){
        Map<String,Object> paramsMap = new HashMap<>();
        if(StringUtils.isNotBlank(warehouseCode)){
            paramsMap.put("warehouseCode",warehouseCode);
        }
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_SHIPPING_METHOD,paramsMap);
        GoodCangResponse<List<GoodCangLogisticsProductsResp>> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<List<GoodCangLogisticsProductsResp>>>() {}.getType());
        if(Objects.isNull(respDto)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return respDto;
    }

    /**
     * 创建入库单
     */
    public GoodCangResponse<String> createInboundBill(@Valid GoodCangCreateInboundReq goodCangCreateInboundReq){
        String json = JSON.toJSONString(goodCangCreateInboundReq);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_CREATE_INBOUND_BILL,json);
        //处理返回值
        GoodCangResponse<String> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<String>>() {}.getType());
        if(Objects.nonNull(respDto.getData())){
            respDto.setData(JSON.parseObject(respDto.getData()).get(RECEIVING_CODE).toString());
        }
        return respDto;
    }

    /**
     * 编辑入库单
     */
    public GoodCangResponse<String> editInboundBill(@Valid GoodCangCreateInboundReq goodCangCreateInboundReq){
        String json = JSON.toJSONString(goodCangCreateInboundReq);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_EDIT_INBOUND_BILL,json);
        //处理返回值
        GoodCangResponse<String> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<String>>() {}.getType());
        if(Objects.nonNull(respDto.getData())){
            respDto.setData(JSON.parseObject(respDto.getData()).get(RECEIVING_CODE).toString());
        }
        if(StringUtils.isNotBlank(respDto.getMessage()) && respDto.getMessage().contains("不允许修改")){
            respDto.setAsk("Success");
            respDto.setData(goodCangCreateInboundReq.getReceivingCode());
        }
        return respDto;
    }

    /**
     * 取消入库单
     * 入库单审核通过后不能取消
     */
    public GoodCangResponse<String> cancelInboundBill(@Valid @NotEmpty(message = "入库单号不能为空") String receivingCode){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put(RECEIVING_CODE,receivingCode);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_CANCEL_INBOUND_BILL,paramsMap);
        return JSON.parseObject(response,new TypeReference<GoodCangResponse<String>>() {}.getType());
    }

    /**
     * 创建出库单
     */
    public GoodCangResponse<String> createOutboundBill(@Valid GoodCangCreateOutboundReq goodCangCreateOutboundReq){
        String json = JSON.toJSONString(goodCangCreateOutboundReq);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_CREATE_OUTBOUND_BILL,json);
        //处理返回值
        GoodCangResponse<String> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<String>>() {}.getType());
        if(Objects.isNull(respDto)){
            log.error("谷仓创建出库单返回数据为空,返回值:{}", response);
            throw new ServiceException(GOOG_CANG_RESPONSE + ":" +response);
        }
        if(Objects.nonNull(respDto.getOrderCode())){
            respDto.setData(respDto.getOrderCode());
        }
        return respDto;
    }
    /**
     * 获取出库单号
     */
    public GoodCangResponse<String> getOutboundCode(@Valid @NotEmpty(message = "参考单号不能为空")String referenceNo){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("reference_no",referenceNo);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_OUT_BOUND_CODE,paramsMap);
        //处理返回值
        GoodCangResponse<String> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<String>>() {}.getType());
        if(Objects.isNull(respDto)){
            log.error("谷仓获取出库单号返回数据为空,返回值:{}", response);
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        if(Objects.nonNull(respDto.getData())){
            respDto.setData(JSON.parseObject(respDto.getData()).get("order_code").toString());
        }
        return respDto;
    }

    /**
     * 取消出库单
     */
    public GoodCangResponse<String> cancelOutboundBill(@Valid @NotEmpty(message = "订单号不能为空") String orderCode,String reason){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("order_code",orderCode);
        paramsMap.put("reason",reason);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_CANCEL_OUTBOUND_BILL,paramsMap);
        return JSON.parseObject(response,new TypeReference<GoodCangResponse<String>>() {}.getType());
    }

    /**
     * 获取退货入库
     */
    public GoodCangResponse<List<GoodCangReturnInstockResp>> getReturnInstock(GoodCangGetReturnInstockReq goodCangGetReturnInstockReq){
        String json = JSON.toJSONString(goodCangGetReturnInstockReq);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_GET_RETURN_INSTOCK,json);
        GoodCangResponse<List<GoodCangReturnInstockResp>> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<List<GoodCangReturnInstockResp>>>() {}.getType());
        if(Objects.isNull(respDto)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return respDto;
    }
    /**
     * 运费试算
     */
    public GoodCangResponse<List<GoodCangCalculateDeliveryFeeResp>> getCalculateDeliveryFee(@Valid GoodCangCalculateDeliveryFeeReq goodCangCalculateDeliveryFeeReq){
        String json = JSON.toJSONString(goodCangCalculateDeliveryFeeReq);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_POST_CALCULATE_DELIVERY_FEE,json);
        GoodCangResponse<List<GoodCangCalculateDeliveryFeeResp>> respDto = null;
        try {
            respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<List<GoodCangCalculateDeliveryFeeResp>>>() {}.getType());
        }catch (Exception e){
            // 可能是返回的不是json格式
            log.error("谷仓运费试算返回数据异常,返回值:{}", response, e);
            return GoodCangResponse.error(GOOG_CANG_RESPONSE_ERROR + ":" +response);
        }
        if(Objects.isNull(respDto)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return respDto;
    }
    /**
     * 上传附件
     */
    public GoodCangResponse<GoodCangUploadFileResp> uploadFile(@Valid GoodCangUploadFileReq goodCangUploadFileReq){
        String json = JSON.toJSONString(goodCangUploadFileReq);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_POST_UPLOAD_ATTACHMENT,json);
        //{"ask":"Success","message":"Success","data":{"attachment_id":45234,"path":"/oms/order_label/2025/02/17/02/26214c11eb494c74a3ac5a51b40d27c2.pdf"}}
        GoodCangResponse<GoodCangUploadFileResp> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<GoodCangUploadFileResp>>() {}.getType());
        if(Objects.isNull(respDto)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return respDto;
    }

    /**
     * 上传面单
     */
    public GoodCangResponse<GoodCangUploadOrderLabelResp> uploadOrderLabel(@Valid GoodCangUploadOrderLabelReq goodCangUploadOrderLabelReq){
        String json = JSON.toJSONString(goodCangUploadOrderLabelReq);
        String response = GoodCangUtils.sendPost(GoodCangConstants.METHOD_POST_UPLOAD_ORDER_LABEL,json);
        GoodCangResponse<GoodCangUploadOrderLabelResp> respDto = JSON.parseObject(response,new TypeReference<GoodCangResponse<GoodCangUploadOrderLabelResp>>() {}.getType());
        if(Objects.isNull(respDto)){
            return GoodCangResponse.error(GOOG_CANG_RESPONSE + ":" +response);
        }
        return respDto;
    }

}
