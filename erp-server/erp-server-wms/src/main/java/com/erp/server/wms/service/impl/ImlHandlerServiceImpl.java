package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.iml.dto.ImlBaseResp;
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
import java.util.Objects;

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

    @Resource
    private RedisUtil redisUtil;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_IML;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return success();
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {

        return null;
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {

        return null;
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {

        return null;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return ApiResult.error("功能未开发");
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return ApiResult.error("功能未开发");
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return ApiResult.error("功能未开发");
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        return null;
    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        ImlResponse<String> response = imlService.cancelOutboundBill(cancelOutboundReq.getOrderCode(),cancelOutboundReq.getReason());
        if(Objects.isNull(response.getCancelStatus())){
            return failure(response.getMessage());
        }
        if(response.getCancelStatus().equals(1)){
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
        }
        if(response.getCancelStatus().equals(3)){
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        ImlBaseResp<String> response = imlService.getWarehouse();
        if(!isSuccess(response.getCode())){
            throw new ServiceException("授权失败,"+response.getMessage());
        }
        return true;
    }

    public boolean isSuccess(Integer code){
        return code.equals(0);
    }
}
