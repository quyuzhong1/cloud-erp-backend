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
import com.erp.model.wms.enums.B2bThirdWarehouseCancelResultEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.convert.ThirdWarehouseConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.antu.dto.request.*;
import com.sdk.wms.antu.dto.response.*;
import com.sdk.wms.antu.enums.AntuEnums;
import com.sdk.wms.antu.service.AntuService;
import com.sdk.wms.damai.dto.request.DaMaiGetOrderRequest;
import com.sdk.wms.damai.dto.response.DaMaiBaseResp;
import com.sdk.wms.damai.dto.response.DaMaiGetOrderResp;
import com.sdk.wms.goodcang.enums.GoodCangEnums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
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
    public ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        AntuCreateOutboundReq antuCreateOutboundReq = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToAntu(createOutboundReq);
        this.handleData(antuCreateOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单json :{}", JSONUtil.toJsonStr(antuCreateOutboundReq));
        AntuResponse<String> response =  antuService.createOutboundBill(antuCreateOutboundReq,getPlatForm());
        log.warn(getPlatForm().getName()+"创建出库单结果:{}", JSONUtil.toJsonStr(response));
        if(response.getMessage().contains("参考编号已存在")){
            return success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getOrderCode()).build());
        }
        return isSuccess(response.getAsk()) ? success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getData()).build()) : failure(response.getMessage());
    }
    @Override
    public ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@Valid ThirdWarehouseUploadFileReq uploadFileReq) {
        AntuUploadFileReq antuUploadFileReq = "order_attach".equalsIgnoreCase(uploadFileReq.getModule())
                ? ThirdWarehouseConverter.INSTANCE.reqToAntuB2bAttachmentUploadFileReq(uploadFileReq)
                : ThirdWarehouseConverter.INSTANCE.reqToAntuUpdateFileReq(uploadFileReq);
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
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return success();
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
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        AntuResponse<String> response = antuService.cancelOutboundBill(cancelOutboundReq.getOrderCode(),cancelOutboundReq.getReason(),getPlatForm());
        if(!isSuccess(response.getAsk())){
            return failure(response.getMessage());
        }
        if(Objects.isNull(response.getCancelStatus())){
            return failure(response.getMessage());
        }
        if(response.getCancelStatus().equals(3)){
            return success(B2bThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        return success(B2bThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq){
        AntuGetOutboundRefReq antuGetOutboundReq = AntuGetOutboundRefReq.builder()
                .referenceNo(queryOutboundReq.getErpOrderCode())
                .build();
        AntuResponse<AntuOutboundResp> response = antuService.getOrderByRefCode(antuGetOutboundReq, getPlatForm());
        return Objects.nonNull(response.getData()) ? success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getData().getOrderCode()).build()) : failure(response.getMessage());
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        List<ThirdWarehouseQueryFbaOutboundResponse> resultList = new ArrayList<>();
        if (CollUtil.isNotEmpty(req.getPlatformOrderCodeList())) {
            AntuGetOutboundReq antuGetOutboundReq = AntuGetOutboundReq.builder()
                    .orderCodeArr(req.getPlatformOrderCodeList())
                    .build();
            AntuResponse<List<AntuOutboundResp>> response = antuService.getOutboundBatch(antuGetOutboundReq, getPlatForm());
            if (!isSuccess(response.getAsk())) {
                throw new ServiceException("查询B2B订单失败," + response.getMessage());
            }
            if (CollUtil.isNotEmpty(response.getData())) {
                response.getData().forEach(antuOutboundResp -> {
                    ThirdWarehouseQueryFbaOutboundResponse res = new ThirdWarehouseQueryFbaOutboundResponse();
                    res.setCode(antuOutboundResp.getReferenceNo());
                    res.setPlatformOrderCode(antuOutboundResp.getOrderCode());
                    res.setTrackNo(antuOutboundResp.getTrackNo());
                    if (Objects.nonNull(antuOutboundResp.getOutBoundTime())) {
                        res.setDeliveryTimeStr(antuOutboundResp.getOutBoundTime().toString());
                    }
                    res.setStatus(AntuEnums.B2BOrderStatusEnum.getErpOrderStatus(antuOutboundResp.getOrderStatus()));
                    resultList.add(res);
                });
            }
            return success(resultList);
        }
        req.getErpOrderCodeList().forEach(code -> {
            AntuGetOutboundRefReq antuGetOutboundReq = AntuGetOutboundRefReq.builder()
                    .referenceNo(code)
                    .build();
            AntuResponse<AntuOutboundResp> response = antuService.getOrderByRefCode(antuGetOutboundReq, getPlatForm());
            if (!isSuccess(response.getAsk())) {
                throw new ServiceException("查询B2B订单失败," + response.getMessage());
            }
            if (Objects.nonNull(response.getData())) {
                AntuOutboundResp antuOutboundResp = response.getData();
                ThirdWarehouseQueryFbaOutboundResponse res = new ThirdWarehouseQueryFbaOutboundResponse();
                res.setCode(code);
                res.setPlatformOrderCode(antuOutboundResp.getOrderCode());
                res.setTrackNo(antuOutboundResp.getTrackNo());
                if(Objects.nonNull(antuOutboundResp.getOutBoundTime())){
                    res.setDeliveryTimeStr(antuOutboundResp.getOutBoundTime().toString());
                }
                res.setStatus(AntuEnums.B2BOrderStatusEnum.getErpOrderStatus(antuOutboundResp.getOrderStatus()));
                resultList.add(res);
            }
        });
        return success(resultList);
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
    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        AntuCreateOutboundReq antuCreateOutboundReq = this.buildB2BOrder(createOutboundReq);
        this.handleData(antuCreateOutboundReq);
        AntuResponse<String> response =  antuService.createOutboundBill(antuCreateOutboundReq,getPlatForm());
        if(response.getMessage().contains("参考编号已存在")){
            return success(response.getOrderCode());
        }
        return isSuccess(response.getAsk()) ? success(response.getData()) : failure(response.getMessage());
    }

    private AntuCreateOutboundReq buildB2BOrder(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        AntuCreateOutboundReq antuCreateOutboundReq = new AntuCreateOutboundReq();
        antuCreateOutboundReq.setReferenceNo(createOutboundReq.getReferenceNo());
        antuCreateOutboundReq.setShippingMethod(createOutboundReq.getChannelCode());
        antuCreateOutboundReq.setWarehouseCode(createOutboundReq.getThirdWarehouseCode());
        antuCreateOutboundReq.setCountryCode(createOutboundReq.getReceiverCountryCode());
        antuCreateOutboundReq.setProvince(createOutboundReq.getProvince());
        antuCreateOutboundReq.setCity(createOutboundReq.getCity());
        antuCreateOutboundReq.setAddress1(createOutboundReq.getAddress1());
        antuCreateOutboundReq.setZipcode(createOutboundReq.getPostCode());
        antuCreateOutboundReq.setName(createOutboundReq.getReceiverName());
        antuCreateOutboundReq.setPhone(createOutboundReq.getTelNumber());
        antuCreateOutboundReq.setIsSignature(createOutboundReq.getIsSignature()?1:0);
        antuCreateOutboundReq.setIsInsurance(createOutboundReq.getIsInsurance()?1:0);
        antuCreateOutboundReq.setOrderDesc(createOutboundReq.getRemark());
        antuCreateOutboundReq.setVerify(1);
        List<AntuCreateOutboundReq.Item> items = new ArrayList<>();
        createOutboundReq.getItems().forEach(item->{
            AntuCreateOutboundReq.Item outboundItem = new AntuCreateOutboundReq.Item();
            outboundItem.setProductSku(item.getWarehousePlatformSku());
            outboundItem.setQuantity(item.getDeliveryQty());
            items.add(outboundItem);
        });
        if(StringUtils.isNotBlank(createOutboundReq.getFileId())){
            List<AntuCreateOutboundReq.Attach> attaches = new ArrayList<>();
            attaches.add(new AntuCreateOutboundReq.Attach(createOutboundReq.getFileType(),Integer.valueOf(createOutboundReq.getFileId())));
            antuCreateOutboundReq.setAttach(attaches);
        }
        antuCreateOutboundReq.setItems(items);
        antuCreateOutboundReq.setOrderKind("B2B");
        return antuCreateOutboundReq;
    }

    public boolean isSuccess(String ask){
        return "Success".equals(ask);
    }
}
