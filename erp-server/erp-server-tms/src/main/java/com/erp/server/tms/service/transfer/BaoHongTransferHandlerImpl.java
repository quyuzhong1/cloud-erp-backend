package com.erp.server.tms.service.transfer;

import com.common.business.annotation.TransferLogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateInboundReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsOrderDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsProductDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.server.tms.convert.BaoHongConverter;
import com.erp.server.tms.handler.AbstractTransferLogisticsHandler;
import com.sdk.tms.baohong.api.order.CreateOrderInfo;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.api.product.DataRow;
import com.sdk.tms.baohong.dto.response.BaoHongResponse;
import com.sdk.tms.baohong.service.BaoHongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    protected ApiResult<List<TransferLogisticsChannelEntity>> getShippingMethodList() {
        BaoHongResponse<List<SmRow>> baoHongResponse = baoHongService.getShippingMethodList();
        if(isFailure(baoHongResponse)){
            return failure(baoHongResponse.getMessage());
        }
        List<TransferLogisticsChannelEntity> transferLogisticsChannelEntityList = BaoHongConverter.INSTANCE.transferLogisticsChannelConvert(baoHongResponse.getData());
        return success(transferLogisticsChannelEntityList);
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
    protected ApiResult<String> createOrder(@Valid TransferLogisticsCreateOrderReq createOrderReq) {
        CreateOrderInfo createOrderInfo  = BaoHongConverter.INSTANCE.createOrderConvert(createOrderReq);
        BaoHongResponse<String> result = baoHongService.createOrder(createOrderInfo);
        if(isFailure(result)){
            return failure(result.getMessage());
        }
        return success(result.getData());
    }

    @Override
    protected ApiResult<TransferLogisticsOrderDTO> getOrderByCode(String orderCode) {
        return null;
    }

    @Override
    protected ApiResult<String> createInbound(TransferLogisticsCreateInboundReq createInboundReq) {
        return null;
    }

    @Override
    protected ApiResult<String> printLabel(String orderCode) {
        return null;
    }

    private boolean isSuccess(BaoHongResponse<?> response){
        return response.getAsk().equals("1");
    }

    private boolean isFailure(BaoHongResponse<?> response){
        return response.getAsk().equals("0");
    }
}
