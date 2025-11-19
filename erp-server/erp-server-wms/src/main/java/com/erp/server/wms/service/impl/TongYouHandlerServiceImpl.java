package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.erp.server.wms.convert.TongYouCreateInboundConverter;
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
        if(!isSuccess(TongYouInboundRespTongYouBaseResp.getError())){
            return failure(TongYouInboundRespTongYouBaseResp.getContent());
        }
        return success(TongYouInboundRespTongYouBaseResp.getData().getOrderNo());
    }


    /**
     * 处理request信息
     * @author will
     * @date 2025/11/17 17:35
     * @param createInboundReq
     * @return TongYouCreateInboundReq
     */
    private TongYouCreateInboundReq buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        TongYouCreateInboundReq request = new TongYouCreateInboundReq();
        //主表信息
        TongYouCreateInboundReq.AddDTO addDTO = TongYouCreateInboundConverter.INSTANCE.InboundToThird(createInboundReq);
        if (CharSequenceUtil.equals(createInboundReq.getReceivingType(), OverseasInstockTypeEnum.SELF_HEADWAY.getCode())) {
            addDTO.setJhfs("693");
            addDTO.setOrder_types("718");
        } else if (CharSequenceUtil.equals(createInboundReq.getReceivingType(), OverseasInstockTypeEnum.TRANSFER_AGENT.getCode())) {
            addDTO.setJhfs("731");
            addDTO.setOrder_types("717");
            addDTO.setTcck(createInboundReq.getTransitWarehouseCode());
            addDTO.setChqd(createInboundReq.getLogisticsChannel());
        }
        addDTO.setWaybill(createInboundReq.getc);

        //明细信息
        List<TongYouCreateInboundReq.AddDetailDTO> addDetailDTOList = TongYouCreateInboundConverter.INSTANCE.InboundDetailToThird(createInboundReq.getItems());
        addDTO.setOrder_products(addDetailDTOList);
        request.setOrder_list(Collections.singletonList(addDTO));
        return request;
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
        if(!isSuccess(tongYouBaseResp.getError())){
            return failure(tongYouBaseResp.getContent());
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
        dto.getAuthJson().put("token",dto.getAuthJson().get("appToken"));
        TongYouBaseResp<String> response = tongYouService.getWarehouse(dto.getAuthJson());
        if(!isSuccess(response.getError())){
            throw new ServiceException("授权失败,"+response.getContent());
        }
        return true;
    }

    public boolean isSuccess(String code){
        return CharSequenceUtil.equals(code,"T");
    }

}
