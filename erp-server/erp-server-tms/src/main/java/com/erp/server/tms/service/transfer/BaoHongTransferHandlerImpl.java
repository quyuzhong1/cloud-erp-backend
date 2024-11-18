package com.erp.server.tms.service.transfer;

import com.common.business.annotation.TransferLogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.CurrencyEnum;
import com.erp.model.tms.dto.transfer.*;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.server.tms.convert.BaoHongConverter;
import com.erp.server.tms.handler.AbstractTransferLogisticsHandler;
import com.sdk.tms.baohong.api.asn.ReceivingInfo;
import com.sdk.tms.baohong.api.order.CreateOrderInfo;
import com.sdk.tms.baohong.api.order.OrderDataArr;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.api.product.DataRow;
import com.sdk.tms.baohong.api.product.ProductRow;
import com.sdk.tms.baohong.api.product.RecordItemRequest;
import com.sdk.tms.baohong.api.product.RecordItemResponse;
import com.sdk.tms.baohong.dto.response.BaoHongResponse;
import com.sdk.tms.baohong.service.BaoHongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 保宏中转报关服务商
 * @Author Luo_WG
 * @Date 2024/1/20 11:01
 **/
@Slf4j
@Component
@TransferLogisticsPlatformType(LogisticsPlatformEnum.BAO_HONG)
@Validated
public class BaoHongTransferHandlerImpl extends AbstractTransferLogisticsHandler {

    @Resource
    private BaoHongService baoHongService;

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.BAO_HONG;
    }

    @Override
    protected ApiResult<String> cancelOrder(TransferCancelOrderReq cancelOrderReq) {
        BaoHongResponse<String> baoHongResponse = baoHongService.cancelOrder(cancelOrderReq.getThirdPlatformCode(),cancelOrderReq.getReason());
        if(isFailure(baoHongResponse)){
            if(baoHongResponse.getMessage().contains("该订单已拦截,不能再次拦截")){
                return success();
            }
            return failure(baoHongResponse.getMessage());
        }
        return success();
    }

    @Override
    protected ApiResult<List<TransferLogisticsChannelEntity>> getShippingMethodList() {
        BaoHongResponse<List<SmRow>> baoHongResponse = baoHongService.getShippingMethodList();
        if(isFailure(baoHongResponse)){
            return failure(baoHongResponse.getMessage());
        }
        List<TransferLogisticsChannelEntity> transferLogisticsChannelEntityList = BaoHongConverter.INSTANCE.transferLogisticsChannelConvert(baoHongResponse.getData());
        return success(transferLogisticsChannelEntityList);
    }

    @Override
    protected ApiResult<String> createProduct(TransferLogisticsCreateProductReq createProductReq) {
        if(CurrencyEnum.CNY.getCurrencyCode().equals(createProductReq.getCurrencyCode())){
            createProductReq.setCurrencyCode(CurrencyEnum.RMB.getCurrencyCode());
        }
        RecordItemRequest recordItemRequest  = BaoHongConverter.INSTANCE.createProductConvert(createProductReq);
        BaoHongResponse<RecordItemResponse> result = baoHongService.filingProduct(recordItemRequest);
        if(isFailure(result)){
            return failure(result.getMessage());
        }
        return success(result.getData().getMessage());
    }


    @Override
    protected ApiResult<List<ProductRegistrationEntity>> getAllProductInfo() {
        BaoHongResponse<List<DataRow>> baoHongResponse = baoHongService.getAllProductInfo();
        if(isFailure(baoHongResponse)){
            return failure(baoHongResponse.getMessage());
        }
        List<ProductRegistrationEntity> transferLogisticsChannelEntityList = BaoHongConverter.INSTANCE.productRegistrationConvert(baoHongResponse.getData());
        return success(transferLogisticsChannelEntityList);
    }

    @Override
    protected ApiResult<ProductRegistrationEntity> getProductBySku(String skuNo) {
        BaoHongResponse<ProductRow> baoHongResponse = baoHongService.getProductInfo(skuNo);
        if(isFailure(baoHongResponse)){
            return failure(baoHongResponse.getMessage());
        }
        ProductRegistrationEntity entity = BaoHongConverter.INSTANCE.productInfoConvert(baoHongResponse.getData());
        return success(entity);
    }

    @Override
    protected ApiResult<String> createOrder(@Valid TransferLogisticsCreateOrderReq createOrderReq) {
        CreateOrderInfo createOrderInfo  = BaoHongConverter.INSTANCE.createOrderConvert(createOrderReq);
        BaoHongResponse<String> result = baoHongService.createOrder(createOrderInfo);
        if(isFailure(result)){
            if(result.getMessage().contains("系统已经存在该交易订单号")){
                BaoHongResponse<OrderDataArr> response = baoHongService.getOrderByCode(createOrderInfo.getReferenceNo());
                if(isFailure(response)){
                    return failure(response.getMessage());
                }else{
                    return success(response.getData().getOrderCode());
                }
            }
            return failure(result.getMessage());
        }
        return success(result.getData());
    }

    @Override
    protected ApiResult<TransferLogisticsOrderDTO> getOrderByCode(String orderCode) {
        BaoHongResponse<OrderDataArr> response = baoHongService.getOrderByCode(orderCode);
        if(isFailure(response)){
            return failure(response.getMessage());
        }
        TransferLogisticsOrderDTO transferLogisticsOrderDTO =  BaoHongConverter.INSTANCE.createOrderInfoConvert(response.getData());
        return success(transferLogisticsOrderDTO);
    }

    @Override
    protected ApiResult<String> createInbound(TransferLogisticsCreateInboundReq createInboundReq) {
        ReceivingInfo receivingInfo  = BaoHongConverter.INSTANCE.createReceiveOrderConvert(createInboundReq);
        BaoHongResponse<String> result = baoHongService.createReceiving(receivingInfo);
        if(isFailure(result)){
            return failure(result.getMessage());
        }
        return success(result.getData());
    }

    @Override
    protected ApiResult<String> printLabel(String orderCode) {
        BaoHongResponse<String> response = baoHongService.printLabel(orderCode);
        if(isFailure(response)){
            return failure(response.getMessage());
        }
        return success(response.getData());
    }


    private boolean isFailure(BaoHongResponse<?> response){
        return response.getAsk().equals("0");
    }
}
