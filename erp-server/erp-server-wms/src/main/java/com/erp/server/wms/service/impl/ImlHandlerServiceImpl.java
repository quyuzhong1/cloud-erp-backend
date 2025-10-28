package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FileUtil;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.OverseasDeliveryModeEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.damai.dto.request.DaMaiCalculateFeeRequest;
import com.sdk.wms.damai.dto.response.DaMaiCalculateFeeResp;
import com.sdk.wms.damai.dto.response.DaMaiPageBaseResp;
import com.sdk.wms.iml.dto.ImlBaseResp;
import com.sdk.wms.iml.dto.request.*;
import com.sdk.wms.iml.dto.response.ImlCalculateFeeResp;
import com.sdk.wms.iml.dto.response.ImlInboundResp;
import com.sdk.wms.iml.dto.response.ImlOutboundResp;
import com.sdk.wms.iml.service.ImlService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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
        ImlCreateInboundReq imlCreateInboundReq =  this.buildInboundDto(createInboundReq);
        ImlBaseResp<ImlInboundResp> imlInboundRespImlBaseResp = imlService.createInboundBill(imlCreateInboundReq);
        if(!isSuccess(imlInboundRespImlBaseResp.getCode())){
            return failure(imlInboundRespImlBaseResp.getMessage());
        }
        return success(imlInboundRespImlBaseResp.getData().getOrderNo());
    }

    private ImlCreateInboundReq buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {

        if(StringUtils.isBlank(createInboundReq.getFileBase64())){
            throw new ServiceException("入库单附件不能为空");
        }

        ImlCreateInboundReq imlCreateInboundReq = ImlCreateInboundReq.builder()
                .needCustomerAudit("N")
                .platformOrderNo(createInboundReq.getReferenceNo())
                .bizType("TOC")
                .customsType(createInboundReq.getDeclareType().equals("Y")?"SEPARATE_TAX":createInboundReq.getDeclareType().equals("N")?"NO_TAX":"")
                .destWarehouseCode(createInboundReq.getWarehouseCode())
                .inboundType(createInboundReq.getReceivingType().equals(OverseasInstockTypeEnum.SELF_HEADWAY.getCode())?"DIRECT":createInboundReq.getReceivingType().equals(OverseasInstockTypeEnum.TRANSFER_AGENT.getCode())?"TRANSIT":"")
                .expectedDate(createInboundReq.getEtaDate().atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli())
                .attachments(Arrays.asList(
                        ImlCreateInboundReq.AttachmentsDTO.builder()
                                .fileName(createInboundReq.getFileName())
                                .fileType(FileUtil.getFileSuffix(createInboundReq.getFileName()))
                                .fileData(createInboundReq.getFileBase64())
                                .build()
                ))
                .build();
        if(StringUtils.isNotBlank(createInboundReq.getReceivingCode())){
            imlCreateInboundReq.setCode(createInboundReq.getReceivingCode());
        }
        if(createInboundReq.getReceivingType().equals(OverseasInstockTypeEnum.SELF_HEADWAY.getCode())){
            imlCreateInboundReq.setDirect(
                    ImlCreateInboundReq.DirectDTO.builder()
                            .trackingNumber(createInboundReq.getTrackingNumber())
                            .build()
            );
        }else if (createInboundReq.getReceivingType().equals(OverseasInstockTypeEnum.TRANSFER_AGENT.getCode())){
            imlCreateInboundReq.setLogisticsCode(createInboundReq.getSmCode());
            imlCreateInboundReq.setTransit(
                    ImlCreateInboundReq.TransitDTO.builder()
                            .transitWarehouseCode(createInboundReq.getTransitWarehouseCode())
                            .deliveryType(createInboundReq.getIncomeType().equals(OverseasDeliveryModeEnum.SELF_DELIVERY.getCode())?"DELIVERY":createInboundReq.getIncomeType().equals(OverseasDeliveryModeEnum.COLLECT_AT_HOME.getCode())?"COLLECT":"")
                            .collectAddress(
                                    ImlCreateInboundReq.TransitDTO.CollectAddressDTO.builder()
                                            .countryCode(createInboundReq.getCollect().getCollectCountryCode())
                                            .postcode(createInboundReq.getCollect().getCollectZipcode())
                                            .province(createInboundReq.getCollect().getCollectStateId())
                                            .city(createInboundReq.getCollect().getCollectCityId())
                                            .county(createInboundReq.getCollect().getCollectAreaId())
                                            .street(createInboundReq.getCollect().getCollectStreet())
                                            .contacter(createInboundReq.getCollect().getContacterName())
                                            .contactPhone(createInboundReq.getCollect().getContactPhone())
                                            .build()
                            )
                            .build()
            );
        }
        //明细
        List<ImlCreateInboundReq.BoxsDTO> boxs = new ArrayList<>();
        List<ThirdWarehouseCreateInboundReq.Item> items = createInboundReq.getItems();
        //根据箱号排序
        items.sort(Comparator.comparing(ThirdWarehouseCreateInboundReq.Item::getBoxNo));
        // 使用 LinkedHashMap 保证分组后的 Key 顺序
        Map<Integer, List<ThirdWarehouseCreateInboundReq.Item>> itemMap = items.stream()
                .collect(Collectors.groupingBy(
                        ThirdWarehouseCreateInboundReq.Item::getBoxNo,
                        LinkedHashMap::new,  // 指定有序 Map 实现
                        Collectors.toList()
                ));
        itemMap.forEach((boxNo,itemList) -> {
            ImlCreateInboundReq.BoxsDTO boxListDTO = new ImlCreateInboundReq.BoxsDTO();
            ThirdWarehouseCreateInboundReq.Item firstItem = itemList.get(0);
            if(firstItem.getWeightUnit().equals(UnitEnum.WeightUnitEnum.KG.code)){
                boxListDTO.setBoxWeight(firstItem.getPackageWeight().multiply(new BigDecimal(1000)));
            }else{
                boxListDTO.setBoxWeight(firstItem.getPackageWeight());
            }
            boxListDTO.setBoxLength(firstItem.getBoxLength());
            boxListDTO.setBoxWidth(firstItem.getBoxWidth());
            boxListDTO.setBoxHeight(firstItem.getBoxHeight());
            boxListDTO.setBoxNo(createInboundReq.getReferenceNo() + "-" + boxNo);
            List<ImlCreateInboundReq.BoxsDTO.BoxDetailsDTO> skuVosDTOS = new ArrayList<>();
            for (ThirdWarehouseCreateInboundReq.Item item : itemList) {
                ImlCreateInboundReq.BoxsDTO.BoxDetailsDTO skuVosDTO = new ImlCreateInboundReq.BoxsDTO.BoxDetailsDTO();
                skuVosDTO.setSkuCode(item.getProductSku());
                skuVosDTO.setQuantity(item.getQuantity());
                skuVosDTO.setIsInsurance("N");
                skuVosDTOS.add(skuVosDTO);
            }
            boxListDTO.setBoxDetails(skuVosDTOS);
            boxs.add(boxListDTO);
        });
        imlCreateInboundReq.setBoxs(boxs);
        return imlCreateInboundReq;
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        ImlCreateInboundReq imlCreateInboundReq =  this.buildInboundDto(createInboundReq);
        ImlBaseResp<ImlInboundResp> imlInboundRespImlBaseResp = imlService.editInboundBill(imlCreateInboundReq);
        if(!isSuccess(imlInboundRespImlBaseResp.getCode())){
            return failure(imlInboundRespImlBaseResp.getMessage());
        }
        return success(imlInboundRespImlBaseResp.getData().getOrderNo());
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        ImlBaseResp<String> resp = imlService.cancelInboundBill(cancelInboundReq.getReceivingCode());
        if(!isSuccess(resp.getCode())){
            return failure(resp.getMessage());
        }
        return success();
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        List<ImlCalculateFeeReq.SkusDTO> skusDTOS = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(calculateFeeReq.getSkus())){
            for (ThirdWarehouseCalculateFeeReq.SkusDTO sku : calculateFeeReq.getSkus()) {
                ImlCalculateFeeReq.SkusDTO skusDTO = new ImlCalculateFeeReq.SkusDTO();
                skusDTO.setSkuBarcode(sku.getPlatformSkuNo());
                skusDTO.setQty(sku.getQty());
                skusDTOS.add(skusDTO);
            }
        }
        ImlCalculateFeeReq imlCalculateFeeReq = ImlCalculateFeeReq.builder()
                .orderType("OUTBOUND")
                .bizType("TOC")
                .transportProductCode(calculateFeeReq.getChannelCode())
                .orderBoxes( Arrays.asList(
                        ImlCalculateFeeReq.OrderBoxesDTO.builder()
                                .length(calculateFeeReq.getLength())
                                .width(calculateFeeReq.getWidth())
                                .height(calculateFeeReq.getHeight())
                                .weight(calculateFeeReq.getWeight())
                                .build()
                ))
                .skus( skusDTOS)
                .address( ImlCalculateFeeReq.AddressDTO.builder()
                        .country(calculateFeeReq.getCountryCode())
                        .city(calculateFeeReq.getCity())
                        .postcode(calculateFeeReq.getPostCode())
                        .build()
                )
                .build();
        ImlBaseResp<ImlCalculateFeeResp> resp = imlService.calculateFee(imlCalculateFeeReq);
        if(!isSuccess(resp.getCode())){
            return failure(resp.getMessage());
        }

        ImlCalculateFeeResp imlCalculateFeeResp = resp.getData();
        if(Objects.isNull(imlCalculateFeeResp) || CollectionUtils.isEmpty(imlCalculateFeeResp.getFeeDetails())){
            return success(Collections.emptyList());
        }
        List<ImlCalculateFeeResp.FeeDetailsDTO> feeDetails = imlCalculateFeeResp.getFeeDetails();
        BigDecimal transportFee = feeDetails.stream().filter(v->v.getFeeCode().equals("TRANSPORT")).findFirst().map(ImlCalculateFeeResp.FeeDetailsDTO::getFeeAmount).orElse(BigDecimal.ZERO);
        BigDecimal totalAmount = imlCalculateFeeResp.getAmount();
        List<ThirdWarehouseCalculateFeeResponse> responseList = new ArrayList<>();
        ThirdWarehouseCalculateFeeResponse response = new ThirdWarehouseCalculateFeeResponse();
        response.setChannelCode(calculateFeeReq.getChannelCode());
        response.setCurrency(imlCalculateFeeResp.getCurrencyCode());
        response.setOtherCost(totalAmount.subtract(transportFee));
        response.setShippingCost(transportFee);
        response.setTotalShippingCost(totalAmount);
        responseList.add(response);
        return success(responseList);
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return success(new ThirdWarehouseUploadFileResponse());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        if(CollectionUtils.isEmpty(uploadFileReq.getFileUrlList())){
            return failure("面单文件不能为空");
        }
        ImlUploadLabelReq imlUploadLabelReq = new ImlUploadLabelReq();
        imlUploadLabelReq.setOrderNo(uploadFileReq.getOrderCode());
        imlUploadLabelReq.setLabelFilePath(uploadFileReq.getFileUrlList().get(0));
        ImlBaseResp<String> resp = imlService.uploadOrderLabel(imlUploadLabelReq);
        if(!isSuccess(resp.getCode())){
            return failure(resp.getMessage());
        }
        return success(ThirdWarehouseUploadOrderLabelResponse.builder()
                .orderCode(uploadFileReq.getOrderCode())
                .build());
    }

    @Override
    public ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        ImlCreateOutboundReq imlCreateOutboundReq =  this.buildOutboundDto(createOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单请求:{}", JSONUtil.toJsonStr(imlCreateOutboundReq));
        ImlBaseResp<ImlOutboundResp> imlInboundRespImlBaseResp = imlService.createOutboundBill(imlCreateOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单结果:{}", JSONUtil.toJsonStr(imlInboundRespImlBaseResp));
        if(!isSuccess(imlInboundRespImlBaseResp.getCode())){
            return failure(imlInboundRespImlBaseResp.getMessage());
        }
        return success(imlInboundRespImlBaseResp.getData().getOrderNo());
    }

    private ImlCreateOutboundReq buildOutboundDto(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        List<ImlCreateOutboundReq.DetailListDTO> detailListDTOS = new ArrayList<>();
        createOutboundReq.getItems().forEach(item -> {
            ImlCreateOutboundReq.DetailListDTO detailListDTO = new ImlCreateOutboundReq.DetailListDTO();
            detailListDTO.setSkuBarcode(item.getProductSku());
            detailListDTO.setSkuCount(item.getQuantity());
            detailListDTOS.add(detailListDTO);
        });
        ImlCreateOutboundReq imlCreateOutboundReq = ImlCreateOutboundReq.builder()
                .platformOrderNo(createOutboundReq.getReferenceNo())
                .ecPlatformOrderNo(createOutboundReq.getPlatformCode())
                .logisticsCode(createOutboundReq.getShippingMethod())
                .bizType("TOC")
                .warehouseCode(createOutboundReq.getWarehouseCode())
                .trackNumber(createOutboundReq.getTrackingNo())
                .buyerCountry(createOutboundReq.getReceiverInfo().getCountryCode())
                .buyerProvince(createOutboundReq.getReceiverInfo().getProvince())
                .buyerCity(createOutboundReq.getReceiverInfo().getCity())
                .buyerAddress(createOutboundReq.getReceiverInfo().getAddress1()+createOutboundReq.getReceiverInfo().getAddress2()+createOutboundReq.getReceiverInfo().getAddress3())
                .buyerName(createOutboundReq.getReceiverInfo().getName())
                .buyerPhone(createOutboundReq.getReceiverInfo().getPhone())
                .buyerEmail(createOutboundReq.getReceiverInfo().getEmail())
                .buyerPostcode(createOutboundReq.getReceiverInfo().getZipCode())
                .detailList(detailListDTOS)
                .build();
        return imlCreateOutboundReq;

    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        ImlCancelOutboundReq imlCancelOutboundReq = ImlCancelOutboundReq.builder().build();
        imlCancelOutboundReq.setOrderNo(cancelOutboundReq.getOrderCode());
        ImlBaseResp<String> response = imlService.cancelOutboundBill(imlCancelOutboundReq);
        if(!isSuccess(response.getCode())){
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }
    @Override
    protected ApiResult<String> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq){
        return ApiResult.error("查询Iml出库单失败");
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

    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        ImlUploadFileReq imlUploadLabelReq = ImlUploadFileReq.builder()
                .platformCustomerCode(uploadHandoverFileReq.getOwnerCode())
                .fileNumber(uploadHandoverFileReq.getFileName())
                .fileName(uploadHandoverFileReq.getFileName())
                .filePath(uploadHandoverFileReq.getFileUrl())
                .type(uploadHandoverFileReq.getDictPlatform())
                .orderList( Arrays.asList(
                        ImlUploadFileReq.OrderListDTO.builder()
                                .orderNo(uploadHandoverFileReq.getOrderCode())
                                .build()
                ))
                .build();
        ImlBaseResp<String> resp = imlService.uploadFile(imlUploadLabelReq);

        return null;
    }

}
