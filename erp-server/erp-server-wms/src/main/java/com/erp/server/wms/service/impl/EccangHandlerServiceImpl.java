package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.convert.ThirdWarehouseConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.antu.dto.request.*;
import com.sdk.wms.antu.dto.response.AntuCalculateFeeResp;
import com.sdk.wms.antu.dto.response.AntuResponse;
import com.sdk.wms.antu.dto.response.AntuUploadFileResp;
import com.sdk.wms.antu.dto.response.AntuWarehouseResp;
import com.sdk.wms.antu.enums.AntuEnums;
import com.sdk.wms.antu.service.AntuService;
import com.sdk.wms.damai.dto.request.DaMaiGetOrderRequest;
import com.sdk.wms.damai.dto.response.DaMaiBaseResp;
import com.sdk.wms.damai.dto.response.DaMaiGetOrderResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
@Validated
public class EccangHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private AntuService antuService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.OMS_ECCANG;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return success();
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        AntuCreateInboundReq antuCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToAntu(createInboundReq);
        //中转代发并且自发头程，默认物流产品
        if(AntuEnums.TransitTypeEnum.TRANSFER.getCode().equals(antuCreateInboundReq.getReceivingType()) &&
                AntuEnums.IncomeTypeEnum.SELF_DELIVERY.getCode().equals(antuCreateInboundReq.getIncomeType()) &&
            CharSequenceUtil.isBlank(antuCreateInboundReq.getSmCode())){
            antuCreateInboundReq.setSmCode("TCHY");
        }
        log.warn(getPlatForm().getName()+"创建入库单json :{}", JSONUtil.toJsonStr(antuCreateInboundReq));
        AntuResponse<String> antuResponse = antuService.createInboundBill(antuCreateInboundReq,getPlatForm());
        return isSuccess(antuResponse.getAsk()) ? success(antuResponse.getData()) : failure(antuResponse.getMessage());
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        AntuCreateInboundReq antuCreateInboundReq = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToAntu(createInboundReq);
        log.warn(getPlatForm().getName()+"编辑入库单json :{}", JSONUtil.toJsonStr(antuCreateInboundReq));
        //中转代发并且自发头程，默认物流产品
        if(AntuEnums.TransitTypeEnum.TRANSFER.getCode().equals(antuCreateInboundReq.getReceivingType()) &&
                AntuEnums.IncomeTypeEnum.SELF_DELIVERY.getCode().equals(antuCreateInboundReq.getIncomeType()) &&
                CharSequenceUtil.isBlank(antuCreateInboundReq.getSmCode())){
            antuCreateInboundReq.setSmCode("TCHY");
        }
        // 修改入库单
        AntuResponse<String> antuResponse = antuService.editInboundBill(antuCreateInboundReq,getPlatForm());
        return isSuccess(antuResponse.getAsk()) ? success(antuResponse.getData()) : failure(antuResponse.getMessage());
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        AntuResponse<String> response = antuService.cancelInboundBill(cancelInboundReq.getReceivingCode(),getPlatForm());
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }
    @Override
    public ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(@Valid ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        AntuCalculateFeeReq antuCalculateFeeReq = ThirdWarehouseConverter.INSTANCE.reqToAntuCalculateFeeReq(calculateFeeReq);
        AntuResponse<List<AntuCalculateFeeResp>> response = antuService.getCalculateFeeBatch(antuCalculateFeeReq,getPlatForm());
        List<AntuCalculateFeeResp> antuCalculateFeeRespList = response.getData();
        List<ThirdWarehouseCalculateFeeResponse> dataList = ThirdWarehouseConverter.INSTANCE.antuResToThirdWarehouseResponse(antuCalculateFeeRespList);
        return isSuccess(response.getAsk()) ? success(dataList) : failure(response.getMessage());
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        AntuCreateOutboundReq antuCreateOutboundReq = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToAntu(createOutboundReq);
        this.handleData(antuCreateOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单json :{}", JSONUtil.toJsonStr(antuCreateOutboundReq));
        AntuResponse<String> response =  antuService.createOutboundBill(antuCreateOutboundReq,getPlatForm());
        if(response.getMessage().contains("参考编号已存在")){
            return success(response.getOrderCode());
        }
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }
    @Override
    public ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@Valid ThirdWarehouseUploadFileReq uploadFileReq) {
        AntuUploadFileReq antuUploadFileReq = ThirdWarehouseConverter.INSTANCE.reqToAntuUpdateFileReq(uploadFileReq);
        if (CharSequenceUtil.isNotBlank(uploadFileReq.getFileType())){
            antuUploadFileReq.setFileType(uploadFileReq.getFileType());
        }
        if (CharSequenceUtil.isNotBlank(uploadFileReq.getModule())){
            antuUploadFileReq.setModule(uploadFileReq.getModule());
        }
        AntuResponse<AntuUploadFileResp> response = antuService.uploadFile(antuUploadFileReq,getPlatForm());
        AntuUploadFileResp antuCalculateFeeRespList = response.getData();
        ThirdWarehouseUploadFileResponse resToThirdWarehouseResponse = ThirdWarehouseConverter.INSTANCE.antuResToThirdWarehouseUploadFileResponse(antuCalculateFeeRespList);
        return isSuccess(response.getAsk()) ? success(resToThirdWarehouseResponse) : failure(response.getMessage());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return ApiResult.error("功能未开发");
    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        AntuResponse<String> response = antuService.cancelOutboundBill(cancelOutboundReq.getOrderCode(),cancelOutboundReq.getReason(),getPlatForm());
        if(!isSuccess(response.getAsk())){
            return failure(response.getMessage());
        }
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
        throw new ServiceException("查询eccang出库单失败");
    }
    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        AntuResponse<List<AntuWarehouseResp>> response = antuService.getWarehouse(AntuBaseRequest.builder()
                        .pageSize(1)
                        .page(1)
                .build(),getPlatForm());
        if(!isSuccess(response.getAsk())){
            throw new ServiceException("授权失败,"+response.getMessage());
        }
        return isSuccess(response.getAsk());
    }


    private void handleData(AntuCreateOutboundReq antuCreateOutboundReq) {
        //处理地址1
        if(CharSequenceUtil.isBlank(antuCreateOutboundReq.getAddress1())){
            antuCreateOutboundReq.setAddress1(CharSequenceUtil.isNotBlank(antuCreateOutboundReq.getAddress2())?antuCreateOutboundReq.getAddress2():antuCreateOutboundReq.getAddress3());
        }
        //处理邮编
        if(CharSequenceUtil.isNotBlank(antuCreateOutboundReq.getZipcode())){
            antuCreateOutboundReq.setZipcode(antuCreateOutboundReq.getZipcode().replace("-",""));
        }
        //处理省份
        if(CharSequenceUtil.isNotBlank(antuCreateOutboundReq.getProvince())){
            if(antuCreateOutboundReq.getProvince().length() != 2){
                List<DictCityEntity> dictCityEntityList = FeignQuery.create(DictCityEntity.class)
                        .eq(DictCityEntity::getCountryCode,antuCreateOutboundReq.getCountryCode())
                        .eq(DictCityEntity::getType,"province")
                        .last(CharSequenceUtil.format("and (code_en = '{}'  or code_pt = '{}')",antuCreateOutboundReq.getProvince(),antuCreateOutboundReq.getProvince()))
                        .list();
                if(CollUtil.isEmpty(dictCityEntityList)){
                    throw new ServiceException(getPlatForm().getName()+"不支持该省份下单");
                }
                antuCreateOutboundReq.setProvince(dictCityEntityList.get(0).getCodeTwo());
            }
        }
    }
    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
