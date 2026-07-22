package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.OmsPlatformEnum;
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
import com.sdk.wms.weishi.dto.request.*;
import com.sdk.wms.weishi.dto.response.WeiShiBaseResp;
import com.sdk.wms.weishi.dto.response.WeiShiCreateOutboundResp;
import com.sdk.wms.weishi.dto.response.WeiShiOutboundResp;
import com.sdk.wms.weishi.dto.response.WeiShiTokenResp;
import com.sdk.wms.weishi.enums.WeiShiEnums;
import com.sdk.wms.weishi.service.WeiShiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class WeiShiHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    /**
     * 纬狮历史失败文案：相同 referNo 重复提交时偶发返回（code=1）。
     * 当前测试环境更多表现为直接 success 并带回原 orderNo。
     */
    private static final String WEISHI_ERROR_ORDER_ALREADY_EXISTS = "ERP order number already exists";

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
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return success();
    }

    /**
     * 创建纬狮出库单。
     *
     * <p>测试环境实测：相同 referNo 重复提交常直接返回 success + 原 orderNo（天然幂等）。
     * 历史环境也可能返回失败文案 {@code ERP order number already exists}。
     * 因此：优先用创建响应中的 orderNo；缺失或创建失败时再按 referNo 反查，
     * 反查命中则按成功处理，避免误失败。</p>
     */
    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        WeiShiCreateOutboundRequest weiShiCreateOutboundRequest = this.buildOutboundDto(createOutboundReq);
        log.warn(getPlatForm().getName() + "创建出库单请求:{}", JSONUtil.toJsonStr(createOutboundReq));
        WeiShiBaseResp<WeiShiCreateOutboundResp> resp = weiShiService.createOutbound(
                weiShiCreateOutboundRequest, ThirdWarehouseContext.getAuthMap());
        log.warn(getPlatForm().getName() + "创建出库单结果:{}", JSONUtil.toJsonStr(resp));

        String referenceNo = createOutboundReq.getReferenceNo();
        if (Objects.isNull(resp)) {
            // 响应丢失时仓侧可能已建单，先反查再失败
            log.warn("{}创建出库单响应为空，按 referNo 反查, referNo={}",
                    getPlatForm().getName(), referenceNo);
            ApiResult<ThirdWarehouseQueryOutboundResponse> queried = queryExistingOutboundByReferNo(referenceNo);
            if (queried != null && queried.isSuccess()
                    && queried.getData() != null
                    && CharSequenceUtil.isNotBlank(queried.getData().getShippingOrderNo())) {
                return queried;
            }
            return failure("纬狮创建出库单响应结果为空");
        }

        if (isSuccess(resp)) {
            // 含重复提交直接 success 并带回原 orderNo 的场景
            String orderNo = resp.getData() == null ? null : resp.getData().getOrderNo();
            String trackNo = resp.getData() == null ? null : resp.getData().getTrackingNumber();
            if (CharSequenceUtil.isNotBlank(orderNo)) {
                return success(ThirdWarehouseQueryOutboundResponse.builder()
                        .shippingOrderNo(orderNo)
                        .trackNo(trackNo)
                        .build());
            }
            log.warn("{}创建出库单成功但未返回 orderNo，按 referNo 反查, referNo={}",
                    getPlatForm().getName(), referenceNo);
            ApiResult<ThirdWarehouseQueryOutboundResponse> queried = queryExistingOutboundByReferNo(referenceNo);
            if (queried != null && queried.isSuccess()) {
                return queried;
            }
            // 创建已成功，反查暂无单号时不降级为失败
            return success(ThirdWarehouseQueryOutboundResponse.builder().trackNo(trackNo).build());
        }

        // 创建失败：先反查是否仓侧已有单（覆盖 already exists 及未知重复文案）
        if (isOrderAlreadyExists(resp)) {
            log.warn("{}建单返回[ERP order number already exists]，按 referNo 反查, referNo={}",
                    getPlatForm().getName(), referenceNo);
        } else {
            log.warn("{}建单失败，先按 referNo 反查是否已有出库单, referNo={}, msg={}",
                    getPlatForm().getName(), referenceNo, resp.getMsg());
        }
        ApiResult<ThirdWarehouseQueryOutboundResponse> queried = queryExistingOutboundByReferNo(referenceNo);
        if (queried != null && queried.isSuccess()
                && queried.getData() != null
                && CharSequenceUtil.isNotBlank(queried.getData().getShippingOrderNo())) {
            log.warn("{}反查命中已有订单，按幂等成功处理, shippingOrderNo={}",
                    getPlatForm().getName(), queried.getData().getShippingOrderNo());
            return queried;
        }
        return failure(CharSequenceUtil.blankToDefault(resp.getMsg(), "纬狮创建出库单失败"));
    }

    private boolean isOrderAlreadyExists(WeiShiBaseResp<?> resp) {
        return resp != null && CharSequenceUtil.containsIgnoreCase(resp.getMsg(), WEISHI_ERROR_ORDER_ALREADY_EXISTS);
    }

    /**
     * 按 referNo（WFHD）反查仓侧出库单；未命中返回 failure，不抛异常。
     */
    private ApiResult<ThirdWarehouseQueryOutboundResponse> queryExistingOutboundByReferNo(String referenceNo) {
        if (CharSequenceUtil.isBlank(referenceNo)) {
            return failure("纬狮出库单参考号不能为空");
        }
        try {
            ThirdWarehouseQueryOutboundReq queryReq = new ThirdWarehouseQueryOutboundReq();
            queryReq.setErpOrderCode(referenceNo);
            return queryOutboundBill(queryReq);
        } catch (Exception e) {
            log.warn("{}按 referNo 反查异常, referNo={}, err={}",
                    getPlatForm().getName(), referenceNo, e.getMessage());
            return failure(CharSequenceUtil.blankToDefault(e.getMessage(), "纬狮出库单反查失败"));
        }
    }

    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        return failure("ERP功能暂不支持");
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
                .platformCode(createOutboundReq.getPlatformCode())
                .orderType(createOutboundReq.isOnlineFlag()?2:0)
                .productCode(createOutboundReq.getShippingMethod())
                .remark(StringUtils.isNotBlank(createOutboundReq.getPlatformCode())?createOutboundReq.getPlatformCode():createOutboundReq.getSoCode())
                .useSpecifiedMaterial("false")
                .labelFile(createOutboundReq.getLabelData())
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
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        return failure("ERP功能暂不支持");
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq) {
        WeiShiGetOutboundRequest weiShiGetOutboundRequest = new WeiShiGetOutboundRequest();
        weiShiGetOutboundRequest.setReferNo(queryOutboundReq.getErpOrderCode());
        log.warn(getPlatForm().getName() + "查询出库单请求:{}", JSONUtil.toJsonStr(weiShiGetOutboundRequest));
        WeiShiBaseResp<WeiShiOutboundResp> resp = weiShiService.getOutbound(
                weiShiGetOutboundRequest, ThirdWarehouseContext.getAuthMap());
        log.warn(getPlatForm().getName() + "查询出库单结果:{}", JSONUtil.toJsonStr(resp));
        if (resp == null) {
            return failure("纬狮获取出库单数据失败: 响应结果为空");
        }
        if (!isSuccess(resp) || resp.getData() == null || CharSequenceUtil.isBlank(resp.getData().getOrderNo())) {
            log.warn("{}获取出库单失败或无单号，code:{},msg:{}",
                    getPlatForm().getName(), resp.getCode(), resp.getMsg());
            return failure(CharSequenceUtil.blankToDefault(resp.getMsg(), "纬狮出库单不存在"));
        }
        return success(ThirdWarehouseQueryOutboundResponse.builder()
                .shippingOrderNo(resp.getData().getOrderNo())
                .trackNo(resp.getData().getTrackingNo())
                .build());
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        return failure("ERP功能暂不支持");
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
