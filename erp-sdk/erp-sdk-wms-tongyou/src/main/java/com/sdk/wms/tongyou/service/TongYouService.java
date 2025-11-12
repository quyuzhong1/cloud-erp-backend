package com.sdk.wms.tongyou.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sdk.wms.tongyou.constants.TongYouConstants;
import com.sdk.wms.tongyou.dto.request.*;
import com.sdk.wms.tongyou.dto.response.*;
import com.sdk.wms.tongyou.utils.TongYouUtils;
import io.seata.common.util.StringUtils;
import jodd.util.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuruipeng
 */
@Component
@Validated
public class TongYouService {

    public static final String RECEIVING_CODE = "receiving_code";

    /**
     * 授权（调用拉取仓库接口，接口调用成功则说明授权成功）
     */
    public TongYouResponse<Object> authorization(){
        return TongYouResponse.builder().build();
    }

    /**
     * 获取商品列表
     */
    public TongYouResponse<List<TongYouProductResp>> getSkuList(@Valid TongYouGetProductReq imlProductReq){
        String response = TongYouUtils.callService(TongYouConstants.METHOD_GET_PRODUCT_LIST, imlProductReq);
        return JSON.parseObject(response,new TypeReference<TongYouResponse<List<TongYouProductResp>>>() {}.getType());
    }

    /**
     * 获取仓库列表
     */
    public TongYouResponse<List<TongYouWarehouseResp>> getWarehouse(TongYouBaseRequest tongYouBaseRequest){
        String response = TongYouUtils.callService(TongYouConstants.METHOD_GET_WAREHOUSE, tongYouBaseRequest);
        return JSON.parseObject(response,new TypeReference<TongYouResponse<List<TongYouWarehouseResp>>>() {}.getType());
    }

    /**
     * 获取揽收区域
     */
    public TongYouResponse<List<TongYouRegionResp>> getReceivingRegion(){
        String response = TongYouUtils.callService(TongYouConstants.METHOD_GET_RECEIVING_REGION,null);
        return JSON.parseObject(response,new TypeReference<TongYouResponse<List<TongYouRegionResp>>>() {}.getType());
    }

    /**
     * 获取入库单
     */
    public TongYouResponse<List<TongYouReceiptResp>> getReceiptBatch(@Valid TongYouGetReceiptReq imlGetReceiptReq){
        String response = TongYouUtils.callService(TongYouConstants.METHOD_GET_RECEIPT,imlGetReceiptReq);
        return JSON.parseObject(response,new TypeReference<TongYouResponse<List<TongYouReceiptResp>>>() {}.getType());
    }

    /**
     * 获取库存
     */
    public TongYouResponse<List<TongYouInventoryResp>> getProductInventory(@Valid TongYouGetInventoryReq imlGetInventoryReq){
        String response = TongYouUtils.callService(TongYouConstants.METHOD_GET_PRODUCT_INVENTORY,imlGetInventoryReq);
        return JSON.parseObject(response,new TypeReference<TongYouResponse<List<TongYouInventoryResp>>>() {}.getType());
    }

    /**
     * 获取物流产品
     */
    public TongYouResponse<List<TongYouInventoryLogisticsProductsResp>> getShippingMethod(String warehouseCode){
        Map<String,Object> paramsMap = new HashMap<>();
        if(StringUtils.isNotBlank(warehouseCode)){
            paramsMap.put("warehouseCode",warehouseCode);
        }
        String response = TongYouUtils.callService(TongYouConstants.GET_SHIPPING_METHOD,paramsMap);
        return JSON.parseObject(response,new TypeReference<TongYouResponse<List<TongYouInventoryLogisticsProductsResp>>>() {}.getType());
    }

    /**
     * 获取出库单
     */
    public TongYouResponse<List<TongYouOutboundResp>> getOutboundBatch(TongYouGetOutboundReq imlGetOutboundReq){
        String response = TongYouUtils.callService(TongYouConstants.GET_ORDER_LIST,imlGetOutboundReq);
        return JSON.parseObject(response,new TypeReference<TongYouResponse<List<TongYouOutboundResp>>>() {}.getType());
    }

    /**
     * 创建入库单
     */
    public TongYouResponse<String> createInboundBill(@Valid TongYouCreateInboundReq imlGetReceiptReq){
        String response = TongYouUtils.callService(TongYouConstants.METHOD_CREATE_INBOUND,imlGetReceiptReq);
        TongYouResponse<String> respDto = JSON.parseObject(response,new TypeReference<TongYouResponse<String>>() {}.getType());
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
    public TongYouResponse<String> editInboundBill(@Valid TongYouCreateInboundReq imlGetReceiptReq){
        String response = TongYouUtils.callService(TongYouConstants.METHOD_EDIT_INBOUND,imlGetReceiptReq);
        TongYouResponse<String> respDto = JSON.parseObject(response,new TypeReference<TongYouResponse<String>>() {}.getType());
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
    public TongYouResponse<String> cancelInboundBill(@Valid @NotEmpty(message = "入库单号不能为空") String receivingCode){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put(RECEIVING_CODE,receivingCode);
        String response = TongYouUtils.callService(TongYouConstants.METHOD_CANCEL_INBOUND,paramsMap);
        return JSON.parseObject(response,new TypeReference<TongYouResponse<String>>() {}.getType());
    }


    /**
     * 创建出库单
     */
    public TongYouResponse<String> createOutboundBill(@Valid TongYouCreateOutboundReq tongYouCreateOutboundReq){
        String response = TongYouUtils.callService(TongYouConstants.METHOD_CREATE_ORDER, tongYouCreateOutboundReq);
        TongYouResponse<String> respDto = JSON.parseObject(response,new TypeReference<TongYouResponse<String>>() {}.getType());
        //处理返回值
        if(StringUtil.isNotBlank(respDto.getOrderCode())){
            respDto.setData(respDto.getOrderCode());
        }
        return respDto;
    }

    /**
     * 取消出库单
     */
    public TongYouResponse<String> cancelOutboundBill(@Valid @NotEmpty(message = "入库单号不能为空") String orderCode, String reason){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("order_code",orderCode);
        paramsMap.put("reason",reason);
        String response = TongYouUtils.callService(TongYouConstants.METHOD_CANCEL_ORDER,paramsMap);
        return JSON.parseObject(response,new TypeReference<TongYouResponse<String>>() {}.getType());
    }
}
