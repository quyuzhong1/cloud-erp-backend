package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FileUtil;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.B2bThirdWarehouseCancelResultEnum;
import com.erp.model.wms.enums.OverseasDeliveryModeEnum;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.damai.dto.request.DaMaiCalculateFeeRequest;
import com.sdk.wms.damai.dto.response.DaMaiCalculateFeeResp;
import com.sdk.wms.damai.dto.response.DaMaiPageBaseResp;
import com.sdk.wms.goodcang.enums.GoodCangEnums;
import com.sdk.wms.iml.dto.ImlBaseResp;
import com.sdk.wms.iml.dto.request.*;
import com.sdk.wms.iml.dto.response.ImlCalculateFeeResp;
import com.sdk.wms.iml.dto.response.ImlInboundResp;
import com.sdk.wms.iml.dto.response.ImlOutboundResp;
import com.sdk.wms.iml.dto.response.ImlQueryOutboundResp;
import com.sdk.wms.iml.enums.ImlEnums;
import com.sdk.wms.iml.service.ImlService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    public static void main(String[] args) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        String timeStr = now.format(formatter);
        System.out.println(timeStr);
    }

    private ImlCreateInboundReq buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {

        if(StringUtils.isBlank(createInboundReq.getFileBase64())){
            throw new ServiceException("入库单附件不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        String timeStr = now.format(formatter);
        String referenceNo = createInboundReq.getReferenceNo() +"-" + timeStr;
        ImlCreateInboundReq imlCreateInboundReq = ImlCreateInboundReq.builder()
                .needCustomerAudit("N")
                .platformOrderNo(referenceNo)
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
                                            .province(createInboundReq.getCollect().getCollectStateName())
                                            .city(createInboundReq.getCollect().getCollectCityName())
                                            .county(createInboundReq.getCollect().getCollectCountryCode())
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
                boxListDTO.setBoxWeight(firstItem.getPackageWeight());
            }else{
                boxListDTO.setBoxWeight(firstItem.getPackageWeight().divide(new BigDecimal(1000), 4, RoundingMode.HALF_UP));
            }
            boxListDTO.setBoxLength(firstItem.getBoxLength());
            boxListDTO.setBoxWidth(firstItem.getBoxWidth());
            boxListDTO.setBoxHeight(firstItem.getBoxHeight());
            boxListDTO.setBoxNo(referenceNo + "-" + boxNo);
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
        throw new ServiceException("该仓库入库单不允许修改，请取消入库单后重新创建");
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

    /**
     * 创建 IML 出库单。
     *
     * <p>预发实测：相同 platformOrderNo（WFHD）重复提交直接返回 success + 原 orderNo（天然幂等）。
     * 优先用创建响应中的 orderNo；缺失、响应为空或创建失败时再按 platformOrderNo 反查，
     * 反查命中则按成功处理。</p>
     */
    @Override
    public ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        ImlCreateOutboundReq imlCreateOutboundReq = this.buildOutboundDto(createOutboundReq);
        String referenceNo = createOutboundReq.getReferenceNo();
        log.warn("{}创建出库单请求:{}", getPlatForm().getName(), JSONUtil.toJsonStr(createOutboundReq));
        ImlBaseResp<ImlOutboundResp> resp = imlService.createOutboundBill(imlCreateOutboundReq);
        log.warn("{}创建出库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));

        if (resp == null) {
            log.warn("{}创建出库单响应为空，按 platformOrderNo 反查, platformOrderNo={}",
                    getPlatForm().getName(), referenceNo);
            ApiResult<ThirdWarehouseQueryOutboundResponse> queried = queryExistingOutboundByPlatformOrderNo(referenceNo);
            if (isQueriedOutboundHit(queried)) {
                return queried;
            }
            return failure("IML创建出库单响应结果为空");
        }

        if (isSuccess(resp.getCode())) {
            String orderNo = resp.getData() == null ? null : resp.getData().getOrderNo();
            if (CharSequenceUtil.isNotBlank(orderNo)) {
                return success(ThirdWarehouseQueryOutboundResponse.builder()
                        .shippingOrderNo(orderNo)
                        .build());
            }
            log.warn("{}创建出库单成功但未返回 orderNo，按 platformOrderNo 反查, platformOrderNo={}",
                    getPlatForm().getName(), referenceNo);
            ApiResult<ThirdWarehouseQueryOutboundResponse> queried = queryExistingOutboundByPlatformOrderNo(referenceNo);
            if (queried != null && queried.isSuccess()) {
                return queried;
            }
            // 创建已成功，反查暂无单号时不降级为失败
            return success(ThirdWarehouseQueryOutboundResponse.builder().build());
        }

        log.warn("{}建单失败，先按 platformOrderNo 反查是否已有出库单, platformOrderNo={}, msg={}",
                getPlatForm().getName(), referenceNo, resp.getMessage());
        ApiResult<ThirdWarehouseQueryOutboundResponse> queried = queryExistingOutboundByPlatformOrderNo(referenceNo);
        if (isQueriedOutboundHit(queried)) {
            log.warn("{}反查命中已有订单，按幂等成功处理, shippingOrderNo={}",
                    getPlatForm().getName(), queried.getData().getShippingOrderNo());
            return queried;
        }
        return failure(CharSequenceUtil.blankToDefault(resp.getMessage(), "IML创建出库单失败"));
    }

    /**
     * 按 platformOrderNo（WFHD）反查仓侧出库单；未命中返回 failure，不抛异常。
     */
    private ApiResult<ThirdWarehouseQueryOutboundResponse> queryExistingOutboundByPlatformOrderNo(String platformOrderNo) {
        if (CharSequenceUtil.isBlank(platformOrderNo)) {
            return failure("IML出库单参考号不能为空");
        }
        try {
            ThirdWarehouseQueryOutboundReq queryReq = new ThirdWarehouseQueryOutboundReq();
            queryReq.setErpOrderCode(platformOrderNo);
            return queryOutboundBill(queryReq);
        } catch (Exception e) {
            log.warn("{}按 platformOrderNo 反查异常, platformOrderNo={}, err={}",
                    getPlatForm().getName(), platformOrderNo, e.getMessage());
            return failure(CharSequenceUtil.blankToDefault(e.getMessage(), "IML出库单反查失败"));
        }
    }

    private boolean isQueriedOutboundHit(ApiResult<ThirdWarehouseQueryOutboundResponse> queried) {
        return queried != null && queried.isSuccess()
                && queried.getData() != null
                && CharSequenceUtil.isNotBlank(queried.getData().getShippingOrderNo());
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
            return failure(response.getMessage());
        }
        return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        ImlCancelOutboundReq imlCancelOutboundReq = ImlCancelOutboundReq.builder().build();
        imlCancelOutboundReq.setOrderNo(cancelOutboundReq.getOrderCode());
        ImlBaseResp<String> response = imlService.cancelOutboundBill(imlCancelOutboundReq);
        if(!isSuccess(response.getCode())){
            return failure(response.getMessage());
        }
        return success(B2bThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq) {
        ImlQueryOutboundReq imlQueryOutboundReq = ImlQueryOutboundReq.builder()
                .platformOrderNo(queryOutboundReq.getErpOrderCode())
                .build();
        log.warn("{}查询出库单请求:{}", getPlatForm().getName(), JSONUtil.toJsonStr(imlQueryOutboundReq));
        ImlBaseResp<ImlQueryOutboundResp> resp = imlService.queryOutboundBill(imlQueryOutboundReq);
        log.warn("{}查询出库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (resp == null) {
            return failure("IML获取出库单数据失败: 响应结果为空");
        }
        if (!isSuccess(resp.getCode()) || resp.getData() == null
                || CharSequenceUtil.isBlank(resp.getData().getOrderNo())) {
            log.warn("{}获取出库单失败或无单号，code:{},msg:{}",
                    getPlatForm().getName(), resp.getCode(), resp.getMessage());
            return failure(CharSequenceUtil.blankToDefault(resp.getMessage(), "IML出库单不存在"));
        }
        return success(ThirdWarehouseQueryOutboundResponse.builder()
                .shippingOrderNo(resp.getData().getOrderNo())
                .trackNo(resp.getData().getTrackNumber())
                .build());
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        List<ThirdWarehouseQueryFbaOutboundResponse> responseList = new ArrayList<>();
        for (String code : req.getErpOrderCodeList()) {
            ImlQueryOutboundReq imlQueryOutboundReq = ImlQueryOutboundReq.builder()
                    .platformOrderNo(code)
                    .build();
            ImlBaseResp<ImlQueryOutboundResp> imlQueryOutboundRespImlBaseResp = imlService.queryOutboundBill(imlQueryOutboundReq);
            if(isSuccess(imlQueryOutboundRespImlBaseResp.getCode())){
                ImlQueryOutboundResp imlQueryOutboundResp = imlQueryOutboundRespImlBaseResp.getData();
                ThirdWarehouseQueryFbaOutboundResponse response = new ThirdWarehouseQueryFbaOutboundResponse();
                response.setCode(code);
                response.setTrackNo(imlQueryOutboundResp.getTrackNumber());
                if(Objects.nonNull(imlQueryOutboundResp.getOutTime())){
                    Integer outTime = imlQueryOutboundResp.getOutTime();
                    Instant instant = Instant.ofEpochSecond(outTime);
                    // 2. 转换为 LocalDateTime
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    // 3. 格式化
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String deliveryTimeStr = dateTime.format(formatter);
                    response.setDeliveryTimeStr(deliveryTimeStr);
                }
                response.setPlatformOriginalStatus(imlQueryOutboundResp.getOrderStatus());
                response.setStatus(ImlEnums.B2BOrderStatusEnum.getErpOrderStatus(imlQueryOutboundResp.getOrderStatus()));
                responseList.add(response);
            }else{
                return failure(imlQueryOutboundRespImlBaseResp.getMessage());
            }
        }
        return success(responseList);
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        ImlBaseResp<String> response = imlService.getWarehouse();
        if(!isSuccess(response.getCode())){
            throw new ServiceException("授权失败,"+response.getMessage());
        }
        return true;
    }

    public boolean isSuccess(Integer code) {
        return code != null && code.equals(0);
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        String type = uploadHandoverFileReq.getDictPlatform();
        if(PlatformDictEnum.WILDBERRIES.getCode().equals(uploadHandoverFileReq.getDictPlatform())){
            type = PlatformDictEnum.WILDBERRIES.getName();
        }
        ImlUploadFileReq imlUploadLabelReq = ImlUploadFileReq.builder()
                .platformCustomerCode(uploadHandoverFileReq.getOwnerCode())
                .fileNumber(uploadHandoverFileReq.getFileName())
                .fileName(uploadHandoverFileReq.getFileName())
                .filePath(uploadHandoverFileReq.getFileUrl())
                .type(type)
                .orderList( Arrays.asList(
                        ImlUploadFileReq.OrderListDTO.builder()
                                .orderNo(uploadHandoverFileReq.getOrderCode())
                                .build()
                ))
                .build();
        ImlBaseResp<String> resp = imlService.uploadFile(imlUploadLabelReq);
        if (!isSuccess(resp.getCode())) {
            if (isHandoverAlreadyExistsMessage(resp.getMessage())) {
                log.warn("IML交接文件已存在，按幂等成功处理，orderNo={}，msg={}",
                        uploadHandoverFileReq.getOrderCode(), resp.getMessage());
                return success(new ThirdWarehouseUploadHandoverFileResponse(uploadHandoverFileReq.getOrderCode()));
            }
            return failure(resp.getMessage());
        }
        return success(new ThirdWarehouseUploadHandoverFileResponse(uploadHandoverFileReq.getOrderCode()));
    }

    /**
     * IML 重复上传交接文件时返回「面单号已存在其它交接单中」，表示该面单已绑定交接单，可视为幂等成功。
     */
    static boolean isHandoverAlreadyExistsMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return false;
        }
        return message.contains("已存在其它交接单");
    }
    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        ImlCreateOutboundReq imlCreateOutboundReq =  this.buildB2BOutboundDto(createOutboundReq);
        ImlBaseResp<ImlOutboundResp> imlInboundRespImlBaseResp = imlService.createOutboundBill(imlCreateOutboundReq);
        if(!isSuccess(imlInboundRespImlBaseResp.getCode())){
            return failure(imlInboundRespImlBaseResp.getMessage());
        }
        return success(imlInboundRespImlBaseResp.getData().getOrderNo());
    }

    private ImlCreateOutboundReq buildB2BOutboundDto(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        ImlCreateOutboundReq imlCreateOutboundReq = new ImlCreateOutboundReq();
        imlCreateOutboundReq.setPlatformOrderNo(createOutboundReq.getReferenceNo());
        imlCreateOutboundReq.setLogisticsCode(createOutboundReq.getChannelCode());
        imlCreateOutboundReq.setBizType("TOC");
        imlCreateOutboundReq.setWarehouseCode(createOutboundReq.getThirdWarehouseCode());
        imlCreateOutboundReq.setBuyerCountry(createOutboundReq.getReceiverCountryCode());
        imlCreateOutboundReq.setBuyerProvince(createOutboundReq.getProvince());
        imlCreateOutboundReq.setBuyerCity(createOutboundReq.getCity());
        imlCreateOutboundReq.setBuyerAddress(createOutboundReq.getAddress1());
        imlCreateOutboundReq.setBuyerName(createOutboundReq.getReceiverName());
        imlCreateOutboundReq.setBuyerPhone(createOutboundReq.getTelNumber());
        imlCreateOutboundReq.setBuyerPostcode(createOutboundReq.getPostCode());
        imlCreateOutboundReq.setRemark(createOutboundReq.getRemark());
        imlCreateOutboundReq.setInsuranceService(createOutboundReq.getIsInsurance()?"Y":"N");
        List<ImlCreateOutboundReq.DetailListDTO> detailListDTOS = new ArrayList<>();
        createOutboundReq.getItems().forEach(item -> {
            ImlCreateOutboundReq.DetailListDTO detailListDTO = new ImlCreateOutboundReq.DetailListDTO();
            detailListDTO.setSkuBarcode(item.getWarehousePlatformSku());
            detailListDTO.setSkuCount(item.getDeliveryQty());
            detailListDTOS.add(detailListDTO);
        });
        imlCreateOutboundReq.setDetailList(detailListDTOS);
        return imlCreateOutboundReq;
    }
}
