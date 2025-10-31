package com.sdk.wms.antu.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.enums.OmsPlatformEnum;
import com.sdk.wms.antu.constants.AntuConstants;
import com.sdk.wms.antu.dto.request.*;
import com.sdk.wms.antu.dto.response.*;
import com.sdk.wms.antu.utils.AntuUtils;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
@Validated
public class AntuService {

    private final static String RECEIVE_CODE = "receiving_code";
    /**
     * 获取商品列表
     */
    public AntuResponse<List<AntuProductResp>> getSkuList(@Valid AntuGetProductReq antuProductReq,OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.METHOD_GET_PRODUCT_LIST, antuProductReq);
        return JSON.parseObject(response,new TypeReference<AntuResponse<List<AntuProductResp>>>() {}.getType());
    }

    /**
     * 获取仓库列表
     */
    public AntuResponse<List<AntuWarehouseResp>> getWarehouse(AntuBaseRequest antuBaseRequest,OmsPlatformEnum platformEnum){
        String apiResponse = AntuUtils.callService(platformEnum,AntuConstants.METHOD_GET_WAREHOUSE,antuBaseRequest);
        AntuResponse<List<AntuWarehouseResp>> response = JSON.parseObject(apiResponse,new TypeReference<AntuResponse<List<AntuWarehouseResp>>>() {}.getType());
//        if(CollectionUtils.isNotEmpty(response.getData())){
//            //只要标准的仓库
//            response.setData(response.getData().stream().filter(v->"0".equals(v.getWarehouseType())).collect(Collectors.toList()));
//        }
        return response;
    }
    /**
     * 获取中转仓库列表
     */
    public AntuResponse<List<AntuWarehouseResp>> getTransferWarehouse(AntuBaseRequest antuBaseRequest,OmsPlatformEnum platformEnum){
        String apiResponse = AntuUtils.callService(platformEnum,AntuConstants.METHOD_GET_WAREHOUSE,antuBaseRequest);
        AntuResponse<List<AntuWarehouseResp>> response = JSON.parseObject(apiResponse,new TypeReference<AntuResponse<List<AntuWarehouseResp>>>() {}.getType());
//        if(CollectionUtils.isNotEmpty(response.getData())){
//            response.setData(response.getData().stream().filter(v->"1".equals(v.getWarehouseType())).collect(Collectors.toList()));
//        }
        return response;
    }

    /**
     * 获取揽收区域
     */
    public AntuResponse<List<AntuRegionResp>> getReceivingRegion(OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.METHOD_GET_RECEIVING_REGION,null);
        return JSON.parseObject(response,new TypeReference<AntuResponse<List<AntuRegionResp>>>() {}.getType());
    }

    /**
     * 获取库存
     */
    public AntuResponse<List<AntuInventoryResp>> getProductInventory(@Valid AntuGetInventoryReq antuGetInventoryReq,OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.METHOD_GET_PRODUCT_INVENTORY,antuGetInventoryReq);
        return JSON.parseObject(response,new TypeReference<AntuResponse<List<AntuInventoryResp>>>() {}.getType());
    }
    
    /**
     * 获取入库单
     */
    public AntuResponse<List<AntuReceiptResp>> getReceiptBatch(@Valid AntuGetReceiptReq antuGetReceiptReq,OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.METHOD_GET_RECEIPT,antuGetReceiptReq);
        return JSON.parseObject(response,new TypeReference<AntuResponse<List<AntuReceiptResp>>>() {}.getType());
    }

    /**
     * 获取出库单
     */
    public AntuResponse<List<AntuOutboundResp>> getOutboundBatch(AntuGetOutboundReq antuGetOutboundReq,OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.GET_ORDER_LIST,antuGetOutboundReq);
        return JSON.parseObject(response,new TypeReference<AntuResponse<List<AntuOutboundResp>>>() {}.getType());
    }


    /**
     * 根据参考号获取出库订单信息
     * @param antuGetOutboundRefReq
     * @param platformEnum
     * @return
     */
    public AntuResponse<AntuOutboundResp> getOrderByRefCode(AntuGetOutboundRefReq antuGetOutboundRefReq,OmsPlatformEnum platformEnum){
        log.warn("getOrderByRefCode request :{}", JSONUtil.toJsonStr(antuGetOutboundRefReq));
        String response = AntuUtils.callService(platformEnum,AntuConstants.GET_ORDER_BY_REF_CODE,antuGetOutboundRefReq);
        log.warn("getOrderByRefCode response :{}", response);
        return JSON.parseObject(response,new TypeReference<AntuResponse<AntuOutboundResp>>() {}.getType());
    }
    /**
     * 获取物流产品
     */
    public AntuResponse<List<AntuLogisticsProductsResp>> getShippingMethod(String warehouseCode,OmsPlatformEnum platformEnum){
        Map<String,Object> paramsMap = new HashMap<>();
        if(StringUtils.isNotBlank(warehouseCode)){
            paramsMap.put("warehouseCode",warehouseCode);
        }
        String response = AntuUtils.callService(platformEnum,AntuConstants.GET_SHIPPING_METHOD,paramsMap);
        return JSON.parseObject(response,new TypeReference<AntuResponse<List<AntuLogisticsProductsResp>>>() {}.getType());
    }

    /**
     * 创建出库单
     */
    public AntuResponse<String> createOutboundBill(@Valid AntuCreateOutboundReq antuCreateOutboundReq,OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.METHOD_CREATE_ORDER,antuCreateOutboundReq);
        AntuResponse<String> respDto = JSON.parseObject(response,new TypeReference<AntuResponse<String>>() {}.getType());
        //处理返回值
        if(StringUtil.isNotBlank(respDto.getOrderCode())){
            respDto.setData(respDto.getOrderCode());
        }
        return respDto;
    }

    /**
     * 取消出库单 入参是平台的单号，不能是参考号
     */
    public AntuResponse<String> cancelOutboundBill(@Valid @NotEmpty(message = "入库单号不能为空") String orderCode, String reason,OmsPlatformEnum platformEnum){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("order_code",orderCode);
        paramsMap.put("reason",reason);
        String response = AntuUtils.callService(platformEnum,AntuConstants.METHOD_CANCEL_ORDER,paramsMap);
        return JSON.parseObject(response,new TypeReference<AntuResponse<String>>() {}.getType());
    }

    /**
     * 创建入库单
     */
    public AntuResponse<String> createInboundBill(@Valid AntuCreateInboundReq antuGetReceiptReq,OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.METHOD_CREATE_INBOUND,antuGetReceiptReq);
        AntuResponse<String> respDto = JSON.parseObject(response,new TypeReference<AntuResponse<String>>() {}.getType());
        //处理返回值
        if (StringUtil.isNotBlank(respDto.getData())) {
            respDto.setData(JSON.parseObject(respDto.getData()).getString(RECEIVE_CODE));
        }
        if(StringUtil.isNotBlank(respDto.getReceivingCode()) && StringUtil.isBlank(respDto.getData())){
            respDto.setData(respDto.getReceivingCode());
        }
        return respDto;
    }

    /**
     * 编辑入库单
     */
    public AntuResponse<String> editInboundBill(@Valid AntuCreateInboundReq antuGetReceiptReq,OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.METHOD_EDIT_INBOUND,antuGetReceiptReq);
        AntuResponse<String> respDto = JSON.parseObject(response,new TypeReference<AntuResponse<String>>() {}.getType());
        //处理返回值
        if (StringUtil.isNotBlank(respDto.getData())) {
            respDto.setData(JSON.parseObject(respDto.getData()).getString(RECEIVE_CODE));
        }
        if(StringUtil.isNotBlank(respDto.getReceivingCode()) && StringUtil.isBlank(respDto.getData())){
            respDto.setData(respDto.getReceivingCode());
        }
        if(StringUtils.isNotBlank(respDto.getMessage()) && respDto.getMessage().contains("不可编辑")){
            respDto.setAsk("Success");
            respDto.setData(antuGetReceiptReq.getReceivingCode());
        }
        return respDto;
    }
    /**
     * 取消入库单
     */
    public AntuResponse<String> cancelInboundBill(@Valid @NotEmpty(message = "入库单号不能为空") String receivingCode,OmsPlatformEnum platformEnum){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put(RECEIVE_CODE,receivingCode);
        String response = AntuUtils.callService(platformEnum,AntuConstants.METHOD_CANCEL_INBOUND,paramsMap);
        return JSON.parseObject(response,new TypeReference<AntuResponse<String>>() {}.getType());
    }

    /**
     * 获取退货单
     */
    public AntuResponse<List<AntuReturnResp>> getReturnInstock(@Valid AntuGetReturnReq antuGetReturnReq, OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.GET_SPECIAL_ORDERS_LIST,antuGetReturnReq);
        log.debug(platformEnum.getName() + "退货入库单:{}", response);
        return JSON.parseObject(response,new TypeReference<AntuResponse<List<AntuReturnResp>>>() {}.getType());
    }
    /**
     * 批量运费试算
     */
    public AntuResponse<List<AntuCalculateFeeResp>> getCalculateFeeBatch(@Valid AntuCalculateFeeReq antuCalculateFeeReq,OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.GET_CALCULATE_FEE_BATCH,antuCalculateFeeReq);
        log.debug(platformEnum.getName() + "批量运费试算:{}", response);
        return JSON.parseObject(response,new TypeReference<AntuResponse<List<AntuCalculateFeeResp>>>() {}.getType());
    }

    /**
     * 上传文件
     */
    public AntuResponse<AntuUploadFileResp> uploadFile(@Valid AntuUploadFileReq antuUploadFileReq,OmsPlatformEnum platformEnum){
        String response = AntuUtils.callService(platformEnum,AntuConstants.GET_UPLOAD_FILE,antuUploadFileReq);
        log.debug("上传文件:{}", response);
        return JSON.parseObject(response,new TypeReference<AntuResponse<AntuUploadFileResp>>() {}.getType());
    }
}
