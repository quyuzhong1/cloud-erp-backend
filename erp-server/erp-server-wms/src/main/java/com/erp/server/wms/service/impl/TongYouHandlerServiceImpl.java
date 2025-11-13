package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.tongyou.dto.request.TongYouCreateInboundReq;
import com.sdk.wms.tongyou.dto.request.TongYouCreateOutboundReq;
import com.sdk.wms.tongyou.dto.response.TongYouBaseResp;
import com.sdk.wms.tongyou.dto.response.TongYouInboundResp;
import com.sdk.wms.tongyou.dto.response.TongYouOutboundResp;
import com.sdk.wms.tongyou.service.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

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
        TongYouCreateInboundReq tongYouCreateInboundReq =  this.buildInboundDto(createInboundReq);
        TongYouBaseResp<TongYouInboundResp> TongYouInboundRespTongYouBaseResp = tongYouService.createInboundBill(tongYouCreateInboundReq);
        if(!isSuccess(TongYouInboundRespTongYouBaseResp.getCode())){
            return failure(TongYouInboundRespTongYouBaseResp.getMessage());
        }
        return success(TongYouInboundRespTongYouBaseResp.getData().getOrderNo());
    }



    private TongYouCreateInboundReq buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {


        return new TongYouCreateInboundReq();
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        throw new ServiceException("该仓库入库单不允许修改，请取消入库单后重新创建");
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        return success();
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return success(Collections.singletonList(new ThirdWarehouseCalculateFeeResponse()));
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return success(new ThirdWarehouseUploadFileResponse());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return success(new ThirdWarehouseUploadOrderLabelResponse());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return null;
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        TongYouCreateOutboundReq TongYouCreateOutboundReq =  this.buildOutboundDto(createOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单请求:{}", JSONUtil.toJsonStr(TongYouCreateOutboundReq));
        TongYouBaseResp<TongYouOutboundResp> tongYouBaseResp = tongYouService.createOutboundBill(TongYouCreateOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单结果:{}", JSONUtil.toJsonStr(tongYouBaseResp));
        if(!isSuccess(tongYouBaseResp.getCode())){
            return failure(tongYouBaseResp.getMessage());
        }
        return success(tongYouBaseResp.getData().getOrderCode());
    }

    private TongYouCreateOutboundReq buildOutboundDto(ThirdWarehouseCreateOutboundReq createOutboundReq) {

        return new TongYouCreateOutboundReq();

    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        return ApiResult.error("取消出库单失败");

    }
    @Override
    protected ApiResult<String> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq){
        return ApiResult.error("查询Iml出库单失败");
    }
    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        TongYouBaseResp<String> response = tongYouService.getWarehouse();
        if(!isSuccess(response.getCode())){
            throw new ServiceException("授权失败,"+response.getMessage());
        }
        return true;
    }

    public boolean isSuccess(Integer code){
        return code.equals(0);
    }

}
