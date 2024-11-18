package com.erp.server.tms.service;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.dto.transfer.*;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

public interface TransferLogisticsService {
    /**
     * 获取平台标识
     */
    LogisticsPlatformEnum getPlatForm();

    /**
     * 服务商授权
     */
    ApiResult<Object>authorization(Map<String, String> authConfig);

    /**
     * 获取物流产品
     */
    ApiResult<List<TransferLogisticsChannelEntity>> getShippingMethodList(String authId);

    /**
     * 创建订单
     * @return 服务商订单号
     */
    ApiResult<String> createOrder(TransferLogisticsCreateOrderReq createOrderReq, String authId);

    /**
     * 查询单个订单信息
     * @param orderCode 服务商订单号
     */
    ApiResult<TransferLogisticsOrderDTO> getOrderByCode(String orderCode, String authId);

    /**
     * 查询全部产品信息
     */
    ApiResult<List<ProductRegistrationEntity>> getAllProductInfo(String authId);

    /**
     * 查询单个sku
     */
    ApiResult<ProductRegistrationEntity> getProductBySku(String skuNo,String authId);

    /**
     * 创建入库单
     * @return 服务商入库单号
     */
    ApiResult<String> createInbound(TransferLogisticsCreateInboundReq createInboundReq, String authId);

    /**
     * 打印标签
     * @param  orderCode 服务商订单号
     * @return BASE64编码
     */
    ApiResult<String> printLabel(String orderCode, String authId);

    /**
     * 备案产品
     * @param authId
     * @return
     */
    ApiResult<String> createProduct(@Valid TransferLogisticsCreateProductReq createProductReq, String authId);

    /**
     * 取消订单
     * @return
     */
    ApiResult<String> cancelOrder(TransferCancelOrderReq cancelOrderReq, String authId);
}
