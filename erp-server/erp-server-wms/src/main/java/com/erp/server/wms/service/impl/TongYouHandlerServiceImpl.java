package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.tongyou.dto.request.TongYouBaseRequest;
import com.sdk.wms.tongyou.dto.request.TongYouCreateInboundReq;
import com.sdk.wms.tongyou.dto.request.TongYouCreateOutboundReq;
import com.sdk.wms.tongyou.dto.response.TongYouResponse;
import com.sdk.wms.tongyou.dto.response.TongYouWarehouseResp;
import com.sdk.wms.tongyou.service.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * 通邮处理服务实现类
 * @author will
 * @date 2025/11/11 15:21
 */
@Slf4j
@Service
@Validated
public class TongYouHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private TongYouService tongYouService;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.TONG_YOU;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return success();
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        TongYouCreateInboundReq tongYouCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToTongYou(createInboundReq);;
        TongYouResponse<String> tongYouResponse = tongYouService.createInboundBill(tongYouCreateInboundReq);
        return isSuccess(tongYouResponse.getAsk()) ? success(tongYouResponse.getData()) : failure(tongYouResponse.getMessage());
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        TongYouCreateInboundReq tongYouCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToTongYou(createInboundReq);
        TongYouResponse<String> tongYouResponse = tongYouService.editInboundBill(tongYouCreateInboundReq);
        return isSuccess(tongYouResponse.getAsk()) ? success(tongYouResponse.getData()) : failure(tongYouResponse.getMessage());
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        TongYouResponse<String> response = tongYouService.cancelInboundBill(cancelInboundReq.getReceivingCode());
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
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
        TongYouCreateOutboundReq tongYouCreateOutboundReq = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToTongYou(createOutboundReq);
        // 艾姆勒同个客户同个参考号5分钟内不允许重复提交
        String key = "wms-tongyou:"+createOutboundReq.getReferenceNo();
        if(redisUtil.get(key) != null){
            return failure("通邮同个客户同个参考号5分钟内不允许重复提交");
        }
        redisUtil.set("wms-tongyou:"+createOutboundReq.getReferenceNo(),createOutboundReq.getReferenceNo(),300);
        log.warn(getPlatForm().getName()+"创建出库单请求:{}", JSONUtil.toJsonStr(tongYouCreateOutboundReq));
        TongYouResponse<String> response =  tongYouService.createOutboundBill(tongYouCreateOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单结果:{}", JSONUtil.toJsonStr(response));
        if(response.getMessage().contains("参考编号已存在")){
            return ApiResult.success();
        }
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        TongYouResponse<String> response = tongYouService.cancelOutboundBill(cancelOutboundReq.getOrderCode(),cancelOutboundReq.getReason());
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
    protected ApiResult<String> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq){
        return ApiResult.error("查询TongYou出库单失败");
    }
    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        TongYouResponse<List<TongYouWarehouseResp>> response = tongYouService.getWarehouse(TongYouBaseRequest.builder()
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
