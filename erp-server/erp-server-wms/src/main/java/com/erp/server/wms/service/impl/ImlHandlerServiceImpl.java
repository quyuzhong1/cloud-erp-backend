package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.goodcang.dto.request.GoodCangCreateInboundReq;
import com.sdk.wms.goodcang.dto.response.GoodCangResponse;
import com.sdk.wms.iml.dto.request.ImlBaseRequest;
import com.sdk.wms.iml.dto.request.ImlCreateInboundReq;
import com.sdk.wms.iml.dto.request.ImlCreateOutboundReq;
import com.sdk.wms.iml.dto.response.ImlResponse;
import com.sdk.wms.iml.dto.response.ImlWarehouseResp;
import com.sdk.wms.iml.service.ImlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
@Validated
public class ImlHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private ImlService imlService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_IML;
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        ImlCreateInboundReq imlCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToIml(createInboundReq);
        // 修改入库单
        ImlResponse<String> imlResponse = imlService.createInboundBill(imlCreateInboundReq);
        return isSuccess(imlResponse.getAsk()) ? success(imlResponse.getData()) : failure(imlResponse.getMessage());
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        ImlCreateInboundReq imlCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToIml(createInboundReq);
        // 修改入库单
        ImlResponse<String> imlResponse = imlService.editInboundBill(imlCreateInboundReq);
        return isSuccess(imlResponse.getAsk()) ? success(imlResponse.getData()) : failure(imlResponse.getMessage());
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        ImlResponse<String> response = imlService.cancelInboundBill(cancelInboundReq.getReceivingCode());
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        ImlCreateOutboundReq imlCreateOutboundReq = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToIml(createOutboundReq);
        //艾姆勒没有测试环境，测试时默认不审核，上生产去掉
        imlCreateOutboundReq.setVerify(0);
        ImlResponse<String> response =  imlService.createOutboundBill(imlCreateOutboundReq);
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        ImlResponse<String> response = imlService.cancelOutboundBill(cancelOutboundReq.getOrderCode(),cancelOutboundReq.getReason());
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }

    @Override
    protected Boolean hasWarehouse() {
        ImlResponse<List<ImlWarehouseResp>> response = imlService.getWarehouse(ImlBaseRequest.builder()
                        .pageSize(1)
                        .page(1)
                .build());
        if(!isSuccess(response.getAsk())){
            throw new ServiceException("授权失败,"+response.getMessage());
        }
        return isSuccess(response.getAsk());
    }

    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
