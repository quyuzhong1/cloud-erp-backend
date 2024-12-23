package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
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
        ImlCreateInboundReq imlCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToIml(createInboundReq);;
        ImlResponse<String> imlResponse = imlService.createInboundBill(imlCreateInboundReq);
        return isSuccess(imlResponse.getAsk()) ? success(imlResponse.getData()) : failure(imlResponse.getMessage());
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        ImlCreateInboundReq imlCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToIml(createInboundReq);
        // 修改入库单
        if("FHD24031900016".equals(createInboundReq.getReferenceNo())){
            imlCreateInboundReq.setSmCode("XBLY");
        }
        ImlResponse<String> imlResponse = imlService.editInboundBill(imlCreateInboundReq);
        return isSuccess(imlResponse.getAsk()) ? success(imlResponse.getData()) : failure(imlResponse.getMessage());
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        ImlResponse<String> response = imlService.cancelInboundBill(cancelInboundReq.getReceivingCode());
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return ApiResult.error("功能未开发");
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        ImlCreateOutboundReq imlCreateOutboundReq = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToIml(createOutboundReq);
        // 艾姆勒同个客户同个参考号5分钟内不允许重复提交
        String key = "wms-iml:"+createOutboundReq.getReferenceNo();
        if(redisUtil.get(key) != null){
            return failure("艾姆勒同个客户同个参考号5分钟内不允许重复提交");
        }
        redisUtil.set("wms-iml:"+createOutboundReq.getReferenceNo(),createOutboundReq.getReferenceNo(),300);
        ImlResponse<String> response =  imlService.createOutboundBill(imlCreateOutboundReq);
        if(response.getMessage().contains("参考编号已存在")){
            return ApiResult.success();
        }
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
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
