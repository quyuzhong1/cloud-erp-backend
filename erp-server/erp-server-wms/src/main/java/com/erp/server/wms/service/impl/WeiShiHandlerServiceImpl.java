package com.erp.server.wms.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.erp.wms.aliexpress.util.Constants;
import com.sdk.wms.jifeng.dto.request.JiFengAuthRequest;
import com.sdk.wms.jifeng.dto.request.JiFengCreateInboundRequest;
import com.sdk.wms.jifeng.dto.request.JiFengCreateOutboundRequest;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import com.sdk.wms.jifeng.dto.response.JiFengCreateInboundResp;
import com.sdk.wms.jifeng.dto.response.JiFengTokenResp;
import com.sdk.wms.jifeng.service.JiFengService;
import com.sdk.wms.weishi.dto.request.*;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiCreateOutboundResp;
import com.sdk.wms.weishi.dto.response.WeiShiReturnOrderResp;
import com.sdk.wms.weishi.dto.response.WeiShiTokenResp;
import com.sdk.wms.weishi.enums.WeiShiEnums;
import com.sdk.wms.weishi.service.WeiShiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class WeiShiHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private WeiShiService weiShiService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.WEI_SHI;
    }


    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return null;
    }

    @Override
    protected ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        WeiShiCreateInboundRequest weiShiCreateInboundRequest = this.buildInboundDto(createInboundReq);
        WeiShiBaseResp<String> resp = weiShiService.createInbound(weiShiCreateInboundRequest,ThirdWarehouseContext.getAuthMap());
        if(!isSuccess(resp)){
            return failure(resp.getMsg());
        }
        return success(resp.getData());
    }

    private WeiShiCreateInboundRequest buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        List<ThirdWarehouseCreateInboundReq.Item> items = createInboundReq.getItems();
        Map<Integer, List<ThirdWarehouseCreateInboundReq.Item>> itemMap = items.stream()
                .collect(Collectors.groupingBy(
                        ThirdWarehouseCreateInboundReq.Item::getBoxNo,
                        LinkedHashMap::new,  // 指定有序 Map 实现
                        Collectors.toList()
                ));
        List<WeiShiCreateInboundRequest.InboundBoxListDTO> boxList = new ArrayList<>();
        itemMap.forEach((boxNo,itemList) -> {
            WeiShiCreateInboundRequest.InboundBoxListDTO boxListDTO = new WeiShiCreateInboundRequest.InboundBoxListDTO();
            ThirdWarehouseCreateInboundReq.Item firstItem = itemList.get(0);
            if(firstItem.getWeightUnit().equals(UnitEnum.WeightUnitEnum.G.code)){
                boxListDTO.setBoxWeight(firstItem.getPackageWeight().divide(new BigDecimal(1000),4, RoundingMode.HALF_UP).toString());
            }else{
                boxListDTO.setBoxWeight(firstItem.getPackageWeight().toString());
            }
            boxListDTO.setBoxLength(firstItem.getBoxLength().toString());
            boxListDTO.setBoxWidth(firstItem.getBoxWidth().toString());
            boxListDTO.setBoxHeight(firstItem.getBoxHeight().toString());
            boxListDTO.setBoxCode(createInboundReq.getReferenceNo() + "-" + boxNo);
            boxListDTO.setSysBoxSeq(boxNo);
            List<WeiShiCreateInboundRequest.InboundBoxListDTO.InboundSkuListDTO> skuVosDTOS = new ArrayList<>();
            for (ThirdWarehouseCreateInboundReq.Item item : itemList) {
                WeiShiCreateInboundRequest.InboundBoxListDTO.InboundSkuListDTO skuVosDTO = new WeiShiCreateInboundRequest.InboundBoxListDTO.InboundSkuListDTO();
                skuVosDTO.setSkuCode(item.getProductSku());
                skuVosDTO.setQuantity(item.getQuantity());
                skuVosDTOS.add(skuVosDTO);
            }
            boxListDTO.setInboundSkuList(skuVosDTOS);
            boxList.add(boxListDTO);
        });
        WeiShiCreateInboundRequest request = WeiShiCreateInboundRequest.builder()
                .inboundType("SKU")
                .inboundMode(WeiShiEnums.TransitTypeEnum.getCodeByErp(createInboundReq.getReceivingType()))
                .transportType(createInboundReq.getReceivingType().equals(OverseasInstockTypeEnum.TRANSFER_AGENT.getCode())?WeiShiEnums.ProductCodeEnum.LOCAL_DELIVERY.getCode():WeiShiEnums.ProductCodeEnum.getCodeByErp(createInboundReq.getReceivingShippingType()))
                .contact(WeiShiCreateInboundRequest.ContactDTO.builder()
                        .city(createInboundReq.getCollect().getCollectCityName())
                        .contactName(createInboundReq.getCollect().getContacterName())
                        .countryCode(createInboundReq.getCollect().getCollectCountryCode())
                        .phone(createInboundReq.getCollect().getContactPhone())
                        .state(createInboundReq.getCollect().getCollectStateName())
                        .street(createInboundReq.getCollect().getCollectStreet())
                        .build())
                .trackingNo(createInboundReq.getTrackingNumber())
                .destWarehouseCode(createInboundReq.getWarehouseCode())
                .expectedArriveDate(createInboundReq.getEtaDate() == null?"":createInboundReq.getEtaDate().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT)))
                .remark(createInboundReq.getRemark())
                .appointmentPickingStartTime(createInboundReq.getCollectStartTime() == null?"":createInboundReq.getCollectStartTime().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT)))
                .appointmentPickingEndTime(createInboundReq.getCollectEndTime() == null?"":createInboundReq.getCollectEndTime().format(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT)))
                .deliveryVoucherBase64(createInboundReq.getFileBase64())
                .inboundBoxList(boxList)
                .transportSize(createInboundReq.getContainerType())
                .build();
        return request;
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        WeiShiCreateInboundRequest weiShiCreateInboundRequest = this.buildInboundDto(createInboundReq);
        weiShiCreateInboundRequest.setOrderNo(createInboundReq.getReceivingCode());
        WeiShiBaseResp<String> resp = weiShiService.updateInbound(weiShiCreateInboundRequest,ThirdWarehouseContext.getAuthMap());
        if(!isSuccess(resp)){
            return failure(resp.getMsg());
        }
        return success(resp.getData());
    }

    @Override
    protected ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq) {
        WeiShiCancelInboundRequest weiShiCancelInboundRequest = new WeiShiCancelInboundRequest();
        weiShiCancelInboundRequest.setOrderNo(cancelInboundReq.getReceivingCode());
        WeiShiBaseResp<String> resp = weiShiService.cancelInbound(weiShiCancelInboundRequest,ThirdWarehouseContext.getAuthMap());
        if(!isSuccess(resp)){
            return failure(resp.getMsg());
        }
        return success(resp.getData());
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return null;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return success(new ThirdWarehouseUploadFileResponse());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return success();
    }

    @Override
    protected ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        WeiShiCreateOutboundRequest weiShiCreateOutboundRequest = this.buildOutboundDto(createOutboundReq);
        WeiShiBaseResp<WeiShiCreateOutboundResp> resp = weiShiService.createOutbound(weiShiCreateOutboundRequest,ThirdWarehouseContext.getAuthMap());
        if(!isSuccess(resp)){
            return failure(resp.getMsg());
        }
        return success(resp.getData().getOrderNo());
    }

    private WeiShiCreateOutboundRequest buildOutboundDto(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        List<WeiShiCreateOutboundRequest.SkuListDTO> skuListDTOS = new ArrayList<>();
        for (ThirdWarehouseCreateOutboundReq.Item item : createOutboundReq.getItems()) {
            WeiShiCreateOutboundRequest.SkuListDTO skuListDTO = new WeiShiCreateOutboundRequest.SkuListDTO();
            skuListDTO.setSkuCode(item.getProductSku());
            skuListDTO.setQuantity(item.getQuantity());
            skuListDTOS.add(skuListDTO);
        }
        WeiShiCreateOutboundRequest weiShiCreateOutboundRequest = WeiShiCreateOutboundRequest.builder()
                .referNo(createOutboundReq.getReferenceNo())
                .warehouseCode(createOutboundReq.getWarehouseCode())
                .platformCode(createOutboundReq.getShippingMethod())
                .orderType(createOutboundReq.isOnlineFlag()?2:0)
                .productCode(createOutboundReq.getShippingMethod())
                .remark(StringUtils.isNotBlank(createOutboundReq.getPlatformCode())?createOutboundReq.getPlatformCode():createOutboundReq.getSoCode())
                .useSpecifiedMaterial("false")
                .labelFile(createOutboundReq.getFileData())
                .skuList(skuListDTOS)
                .recipient(WeiShiCreateOutboundRequest.RecipientDTO.builder()
                        .name(createOutboundReq.getReceiverInfo().getName())
                        .taxno(createOutboundReq.getReceiverInfo().getTaxNumber())
                        .postcode(createOutboundReq.getReceiverInfo().getZipCode())
                        .mobile(createOutboundReq.getReceiverInfo().getPhone())
                        .email(createOutboundReq.getReceiverInfo().getEmail())
                        .state(createOutboundReq.getReceiverInfo().getProvince())
                        .city(createOutboundReq.getReceiverInfo().getCity())
                        .street(createOutboundReq.getReceiverInfo().getAddress1()+createOutboundReq.getReceiverInfo().getAddress2())
                        .countrycode(createOutboundReq.getReceiverInfo().getCountryCode())
                        .build())
                .build();
        return weiShiCreateOutboundRequest;
    }

    @Override
    protected ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        WeiShiCancelOutboundRequest weiShiCancelOutboundRequest = new WeiShiCancelOutboundRequest();
        weiShiCancelOutboundRequest.setOrderNo(cancelOutboundReq.getOrderCode());
        weiShiCancelOutboundRequest.setReason(cancelOutboundReq.getReason());
        WeiShiBaseResp<String> resp = weiShiService.cancelOutbound(weiShiCancelOutboundRequest,ThirdWarehouseContext.getAuthMap());
        if(!isSuccess(resp)){
            return failure(resp.getMsg());
        }
        return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        Map<String, Object> authJson = dto.getAuthJson();
        WeiShiBaseResp<WeiShiTokenResp> authResp = weiShiService.accessToken(authJson);
        if(!isSuccess(authResp)){
            throw new ServiceException("授权失败,"+authResp.getMsg());
        }
        WeiShiTokenResp weiShiTokenResp = authResp.getData();
        authJson.put("accessToken",weiShiTokenResp.getAccessToken());
        Long expireInSecond = weiShiTokenResp.getExpiresIn();
        LocalDateTime expireIn = LocalDateTime.now().plusSeconds(expireInSecond);
        authJson.put("expireIn",expireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setAuthJson(authJson);
        return true;
    }
    @Override
    protected  ApiResult<String> refreshToken(Map<String,Object> map){
        WeiShiBaseResp<WeiShiTokenResp> authResp = weiShiService.accessToken(map);
        if(!isSuccess(authResp)){
            throw new ServiceException("授权失败,"+authResp.getMsg());
        }
        WeiShiTokenResp weiShiTokenResp = authResp.getData();
        map.put("accessToken",weiShiTokenResp.getAccessToken());
        Long expireInSecond = weiShiTokenResp.getExpiresIn();
        LocalDateTime expireIn = LocalDateTime.now().plusSeconds(expireInSecond);
        map.put("expireIn",expireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return success();
    }

    public <T> boolean isSuccess(WeiShiBaseResp<T> resp){
        return resp.getCode()==200;
    }

}
