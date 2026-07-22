package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.B2bThirdWarehouseCancelResultEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.antu.dto.request.AntuGetOutboundRefReq;
import com.sdk.wms.antu.dto.response.AntuOutboundResp;
import com.sdk.wms.antu.dto.response.AntuResponse;
import com.sdk.wms.antu.enums.AntuEnums;
import com.sdk.wms.jifeng.dto.request.JiFengAuthRequest;
import com.sdk.wms.jifeng.dto.request.JiFengCreateB2BOutboundRequest;
import com.sdk.wms.jifeng.dto.request.JiFengCreateInboundRequest;
import com.sdk.wms.jifeng.dto.request.JiFengCreateOutboundRequest;
import com.sdk.wms.jifeng.dto.response.*;
import com.sdk.wms.jifeng.enums.JiFengEnums;
import com.sdk.wms.jifeng.service.JiFengService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
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
public class JiFengHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    /**
     * 极风重复建单：相同 erpNo（WFHD）再提交返回 code=50019。
     * 实测文案：ERP order number already exists（无新单产生）。
     */
    private static final int JIFENG_CODE_ORDER_ALREADY_EXISTS = 50019;
    private static final String JIFENG_ERROR_ORDER_ALREADY_EXISTS = "ERP order number already exists";

    @Resource
    private JiFengService jiFengService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.JIFENG;
    }


    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return null;
    }

    @Override
    protected ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        JiFengCreateInboundRequest jiFengCreateInboundRequest = this.buildInboundDto(createInboundReq);
        JiFengBaseResp<JiFengCreateInboundResp> resp = jiFengService.createInbound(ThirdWarehouseContext.getAuthMap(),jiFengCreateInboundRequest);
        if(!isSuccess(resp)){
            return failure(resp.getMessage());
        }
        return success(resp.getData().getInboundNo());
    }

    private JiFengCreateInboundRequest buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        JiFengCreateInboundRequest jiFengCreateInboundRequest = JiFengCreateInboundRequest.builder()
                .erpNo(createInboundReq.getReferenceNo())
                .trackingNo(createInboundReq.getTrackingNumber())
                .expectedTime(createInboundReq.getEtaDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .warehouse(createInboundReq.getWarehouseCode())
                .remark(createInboundReq.getRemark())
                .receiptType(2)
                .build();
        //封装箱子明细
        List<JiFengCreateInboundRequest.SkuListDTO> skuListDTOS = new ArrayList<>();
        //汇总的sku
        Map<String,Integer> skuTotalMap = new HashMap<>();
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
        List<JiFengCreateInboundRequest.BoxListDTO> boxList = new ArrayList<>();
        itemMap.forEach((boxNo,itemList) -> {
            JiFengCreateInboundRequest.BoxListDTO boxListDTO = new JiFengCreateInboundRequest.BoxListDTO();
            ThirdWarehouseCreateInboundReq.Item firstItem = itemList.get(0);
            if(firstItem.getWeightUnit().equals(UnitEnum.WeightUnitEnum.KG.code)){
                boxListDTO.setWeight(firstItem.getPackageWeight().multiply(new BigDecimal(1000)).intValue());
            }else{
                boxListDTO.setWeight(firstItem.getPackageWeight().intValue());
            }
            boxListDTO.setLength(firstItem.getBoxLength().intValue());
            boxListDTO.setWidth(firstItem.getBoxWidth().intValue());
            boxListDTO.setHeight(firstItem.getBoxHeight().intValue());
            List<JiFengCreateInboundRequest.BoxListDTO.SkuVosDTO> skuVosDTOS = new ArrayList<>();
            for (ThirdWarehouseCreateInboundReq.Item item : itemList) {
                JiFengCreateInboundRequest.BoxListDTO.SkuVosDTO skuVosDTO = new JiFengCreateInboundRequest.BoxListDTO.SkuVosDTO();
                skuVosDTO.setSku(item.getProductSku());
                skuVosDTO.setCount(item.getQuantity());
                skuVosDTOS.add(skuVosDTO);
                if(skuTotalMap.containsKey(item.getProductSku())) {
                    skuTotalMap.put(item.getProductSku(), skuTotalMap.get(item.getProductSku()) + item.getQuantity());
                }else{
                    skuTotalMap.put(item.getProductSku(), item.getQuantity());
                }
            }
            boxListDTO.setSkuVos(skuVosDTOS);
            boxList.add(boxListDTO);
        });
        //skuTotalMap 转 skuListDTOS
        skuTotalMap.forEach((sku,count) -> {
            JiFengCreateInboundRequest.SkuListDTO skuListDTO = new JiFengCreateInboundRequest.SkuListDTO();
            skuListDTO.setSku(sku);
            skuListDTO.setCount(count);
            skuListDTOS.add(skuListDTO);
        });
        jiFengCreateInboundRequest.setSkuList(skuListDTOS);
        jiFengCreateInboundRequest.setBoxList(boxList);
        return jiFengCreateInboundRequest;
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        throw new ServiceException("该仓库入库单不允许修改，请取消入库单后重新创建");
    }

    @Override
    protected ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq) {
        JiFengBaseResp<JiFengCreateInboundResp>  resp = jiFengService.cancelInbound(ThirdWarehouseContext.getAuthMap(), cancelInboundReq.getReceivingCode(),cancelInboundReq.getSourceCode());
        if(!isSuccess(resp)){
            return failure(resp.getMessage());
        }
        return success();
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
        return null;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return null;
    }

    /**
     * 创建极风出库单。
     *
     * <p>仓侧实测：相同 erpNo 重复提交返回 {@code code=50019} /
     * {@code ERP order number already exists}，此时按 erpNo 反查仓侧 orderNo，
     * 命中则按成功处理。建单失败时也先反查，覆盖超时后仓侧已成功的场景。</p>
     */
    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        JiFengCreateOutboundRequest jiFengCreateOutboundRequest = this.buildOutboundDto(createOutboundReq);
        String referenceNo = createOutboundReq.getReferenceNo();
        log.warn("{}创建出库单请求:{}", getPlatForm().getName(), JSONUtil.toJsonStr(jiFengCreateOutboundRequest));
        JiFengBaseResp<String> resp = jiFengService.createOutbound(
                ThirdWarehouseContext.getAuthMap(), jiFengCreateOutboundRequest);
        log.warn("{}创建出库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));

        if (resp == null) {
            log.warn("{}创建出库单响应为空，按 erpNo 反查, erpNo={}", getPlatForm().getName(), referenceNo);
            ApiResult<ThirdWarehouseQueryOutboundResponse> queried = queryExistingOutboundByErpNo(referenceNo);
            if (isQueriedOutboundHit(queried)) {
                return queried;
            }
            return failure("极风创建出库单响应结果为空");
        }

        if (isSuccess(resp)) {
            // 历史成功路径以 WFHD 作为 shippingOrderNo，保持兼容
            return success(ThirdWarehouseQueryOutboundResponse.builder()
                    .shippingOrderNo(referenceNo)
                    .build());
        }

        if (isOrderAlreadyExists(resp)) {
            log.warn("{}建单返回[ERP order number already exists]，按 erpNo 反查, erpNo={}",
                    getPlatForm().getName(), referenceNo);
        } else {
            log.warn("{}建单失败，先按 erpNo 反查是否已有出库单, erpNo={}, code={}, msg={}",
                    getPlatForm().getName(), referenceNo, resp.getCode(), resp.getMessage());
        }
        ApiResult<ThirdWarehouseQueryOutboundResponse> queried = queryExistingOutboundByErpNo(referenceNo);
        if (isQueriedOutboundHit(queried)) {
            log.warn("{}反查命中已有订单，按幂等成功处理, shippingOrderNo={}",
                    getPlatForm().getName(), queried.getData().getShippingOrderNo());
            return queried;
        }
        return failure(CharSequenceUtil.blankToDefault(resp.getMessage(), "极风创建出库单失败"));
    }

    private boolean isOrderAlreadyExists(JiFengBaseResp<?> resp) {
        if (resp == null) {
            return false;
        }
        if (resp.getCode() != null && resp.getCode() == JIFENG_CODE_ORDER_ALREADY_EXISTS) {
            return true;
        }
        return CharSequenceUtil.containsIgnoreCase(resp.getMessage(), JIFENG_ERROR_ORDER_ALREADY_EXISTS);
    }

    /**
     * 按 erpNo（WFHD）反查仓侧出库单；未命中返回 failure，不抛异常。
     */
    private ApiResult<ThirdWarehouseQueryOutboundResponse> queryExistingOutboundByErpNo(String erpNo) {
        if (CharSequenceUtil.isBlank(erpNo)) {
            return failure("极风出库单参考号不能为空");
        }
        try {
            ThirdWarehouseQueryOutboundReq queryReq = new ThirdWarehouseQueryOutboundReq();
            queryReq.setErpOrderCode(erpNo);
            return queryOutboundBill(queryReq);
        } catch (Exception e) {
            log.warn("{}按 erpNo 反查异常, erpNo={}, err={}",
                    getPlatForm().getName(), erpNo, e.getMessage());
            return failure(CharSequenceUtil.blankToDefault(e.getMessage(), "极风出库单反查失败"));
        }
    }

    private boolean isQueriedOutboundHit(ApiResult<ThirdWarehouseQueryOutboundResponse> queried) {
        return queried != null && queried.isSuccess()
                && queried.getData() != null
                && CharSequenceUtil.isNotBlank(queried.getData().getShippingOrderNo());
    }

    private JiFengCreateOutboundRequest buildOutboundDto(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        JiFengCreateOutboundRequest jiFengCreateOutboundRequest = new JiFengCreateOutboundRequest();
        jiFengCreateOutboundRequest.setWarehouse(createOutboundReq.getWarehouseCode());
        jiFengCreateOutboundRequest.setErpNo(createOutboundReq.getReferenceNo());
        jiFengCreateOutboundRequest.setPlatform(PlatformDictEnum.getNameByCode(createOutboundReq.getPlatform()));
        jiFengCreateOutboundRequest.setPlatformOrderNo(createOutboundReq.getPlatformCode());
        jiFengCreateOutboundRequest.setBuyerName(createOutboundReq.getReceiverInfo().getName());
        jiFengCreateOutboundRequest.setBuyerPhone(createOutboundReq.getReceiverInfo().getPhone());
        jiFengCreateOutboundRequest.setRecipientCountry(createOutboundReq.getReceiverInfo().getCountryCode());
        jiFengCreateOutboundRequest.setRecipientProvince(createOutboundReq.getReceiverInfo().getProvince());
        jiFengCreateOutboundRequest.setRecipientCity(createOutboundReq.getReceiverInfo().getCity());
        jiFengCreateOutboundRequest.setRecipientArea(createOutboundReq.getReceiverInfo().getDistrict());
        jiFengCreateOutboundRequest.setRecipientAddress(createOutboundReq.getReceiverInfo().getAddress1());
        jiFengCreateOutboundRequest.setRecipientAddress2(createOutboundReq.getReceiverInfo().getAddress2() + (StringUtils.isBlank(createOutboundReq.getReceiverInfo().getAddress3())?"":createOutboundReq.getReceiverInfo().getAddress3()));
        jiFengCreateOutboundRequest.setRecipientEmail(createOutboundReq.getReceiverInfo().getEmail());
        jiFengCreateOutboundRequest.setZipCode(createOutboundReq.getReceiverInfo().getZipCode());
        jiFengCreateOutboundRequest.setTaxId(createOutboundReq.getReceiverInfo().getTaxNumber());
        jiFengCreateOutboundRequest.setType(createOutboundReq.isOnlineFlag()?1:2);
        jiFengCreateOutboundRequest.setLogisticsId(Integer.valueOf(createOutboundReq.getShippingMethodId()));
        jiFengCreateOutboundRequest.setLogisticsName(createOutboundReq.getShippingMethodName());
        jiFengCreateOutboundRequest.setTrackingNo(createOutboundReq.getTrackingNo());
        jiFengCreateOutboundRequest.setPackageType(3);
        jiFengCreateOutboundRequest.setLabelUrl(createOutboundReq.getLabelUrl());
        List<JiFengCreateOutboundRequest.SkuListDTO> skuListDTOS = new ArrayList<>();
        createOutboundReq.getItems().forEach(item -> {
            JiFengCreateOutboundRequest.SkuListDTO skuListDTO = new JiFengCreateOutboundRequest.SkuListDTO();
            skuListDTO.setSku(item.getProductSku());
            skuListDTO.setNum(item.getQuantity());
            skuListDTO.setHsCode(item.getHsCode());
            skuListDTOS.add(skuListDTO);
        });
        jiFengCreateOutboundRequest.setSkuList(skuListDTOS);
        return jiFengCreateOutboundRequest;
    }

    @Override
    protected ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        JiFengBaseResp<String> resp = jiFengService.cancelOutbound(ThirdWarehouseContext.getAuthMap(), cancelOutboundReq.getOrderCode());
        if(!isSuccess(resp)){
            if(StringUtils.isNotBlank(resp.getMessage()) && resp.getMessage().contains("Order canceled")){
                return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
            }
            //拦截中
            if(resp.getCode().equals(20023)){
                return success(ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
            }
            return failure(resp.getMessage());
        }
        return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        String erpNo = StringUtils.isNotBlank(cancelOutboundReq.getErpOrderCode())
                ? cancelOutboundReq.getErpOrderCode()
                : cancelOutboundReq.getOrderCode();
        if (StringUtils.isBlank(erpNo)) {
            return failure("取消B2B出库单失败: ERP参考号为空");
        }
        JiFengBaseResp<String>  resp = jiFengService.cancelB2BOutbound(ThirdWarehouseContext.getAuthMap(), erpNo);
        if(!isSuccess(resp)){
            return failure(resp.getMessage());
        }
        return success(B2bThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq) {
        log.warn("{}查询出库单请求:{}", getPlatForm().getName(), queryOutboundReq.getErpOrderCode());
        JiFengBaseResp<JiFengOutboundResp> outBound = jiFengService.getOutBound(
                ThirdWarehouseContext.getAuthMap(), queryOutboundReq.getErpOrderCode());
        log.warn("{}查询出库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(outBound));
        if (outBound == null) {
            return failure("极风获取出库单数据失败: 响应结果为空");
        }
        if (!isSuccess(outBound) || outBound.getData() == null
                || CharSequenceUtil.isBlank(outBound.getData().getOrderNo())) {
            return failure(CharSequenceUtil.blankToDefault(outBound.getMessage(), "极风出库单不存在"));
        }
        return success(ThirdWarehouseQueryOutboundResponse.builder()
                .shippingOrderNo(outBound.getData().getOrderNo())
                .trackNo(outBound.getData().getTrackingNo())
                .build());
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        List<ThirdWarehouseQueryFbaOutboundResponse> resultList = new ArrayList<>();
        req.getErpOrderCodeList().forEach(code -> {
            JiFengBaseResp<JiFengB2BOutboundResp> resp = jiFengService.getB2BOrder(ThirdWarehouseContext.getAuthMap(), code);
            if (!isSuccess(resp)) {
                throw new ServiceException("查询B2B订单失败," + resp.getMessage());
            }
            if (Objects.nonNull(resp.getData())) {
                JiFengB2BOutboundResp jiFengB2BOutboundResp = resp.getData();
                ThirdWarehouseQueryFbaOutboundResponse res = new ThirdWarehouseQueryFbaOutboundResponse();
                res.setCode(code);
                res.setTrackNo(jiFengB2BOutboundResp.getTrackingNo());
                if(Objects.nonNull(jiFengB2BOutboundResp.getShippedTime())){
                    res.setDeliveryTimeStr(jiFengB2BOutboundResp.getShippedTime());
                }
                res.setPlatformOriginalStatus(jiFengB2BOutboundResp.getStatus().toString());
                res.setStatus(JiFengEnums.B2BOrderStatusEnum.getErpOrderStatus(jiFengB2BOutboundResp.getStatus().toString()));
                resultList.add(res);
            }
        });
        return success(resultList);
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        Map<String, Object> authJson = dto.getAuthJson();
        JiFengAuthRequest jiFengAuthRequest = JiFengAuthRequest.builder()
                .email(authJson.get("email").toString())
                .token(authJson.get("token").toString())
                .domain(authJson.get("domain").toString())
                .clientId(authJson.get("appKey").toString())
                .clientSecret(authJson.get("appToken").toString())
                .build();
        JiFengBaseResp<String> authResp = jiFengService.authorize(jiFengAuthRequest);
        if(!isSuccess(authResp)){
            throw new ServiceException("授权失败,"+authResp.getMessage());
        }
        jiFengAuthRequest.setKey(authResp.getData());
        JiFengBaseResp<JiFengTokenResp> tokenResp = jiFengService.accessToken(jiFengAuthRequest);
        if(!isSuccess(tokenResp)){
            throw new ServiceException("授权失败,"+tokenResp.getMessage());
        }
        JiFengTokenResp jiFengTokenResp = tokenResp.getData();
        authJson.put("accessToken",jiFengTokenResp.getAccessToken());
        authJson.put("refreshToken",jiFengTokenResp.getRefreshToken());
        authJson.put("userId",jiFengTokenResp.getUserId());
        LocalDateTime expireIn = Instant.ofEpochMilli(jiFengTokenResp.getExpireIn())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        authJson.put("expireIn",expireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        LocalDateTime refreshExpireIn = Instant.ofEpochMilli(jiFengTokenResp.getRefreshExpireIn())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        authJson.put("refreshExpireIn",refreshExpireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.setAuthJson(authJson);
        return true;
    }

    @Override
    protected  ApiResult<String> refreshToken(Map<String,Object> map){
        JiFengAuthRequest jiFengAuthRequest = JiFengAuthRequest.builder()
                .domain(map.get("domain").toString())
                .clientId(map.get("appKey").toString())
                .clientSecret(map.get("appToken").toString())
                .refreshToken(map.get("refreshToken").toString())
                .userId(Integer.valueOf(map.get("userId").toString()))
                .build();
        JiFengBaseResp<JiFengTokenResp> resp = jiFengService.refreshToken(jiFengAuthRequest);
        if(!isSuccess(resp)){
            return failure(resp.getMessage());
        }
        JiFengTokenResp jiFengTokenResp = resp.getData();
        map.put("accessToken",jiFengTokenResp.getAccessToken());
        LocalDateTime expireIn = Instant.ofEpochMilli(jiFengTokenResp.getExpireIn())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        map.put("expireIn",expireIn.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return success();
    };
    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        JiFengCreateB2BOutboundRequest jiFengCreateB2BOutboundRequest = this.buildB2BOrderDTO(createOutboundReq);
        JiFengBaseResp<JiFengCreateB2bOrderResp> resp = jiFengService.createB2BOutbound(ThirdWarehouseContext.getAuthMap(), jiFengCreateB2BOutboundRequest);
        if(!isSuccess(resp)){
            return failure(resp.getMessage());
        }
        return success(resp.getData().getOutboundNo());
    }

    private JiFengCreateB2BOutboundRequest buildB2BOrderDTO(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        JiFengCreateB2BOutboundRequest jiFengCreateB2BOutboundRequest = new JiFengCreateB2BOutboundRequest();
        jiFengCreateB2BOutboundRequest.setErpNo(createOutboundReq.getReferenceNo());
        jiFengCreateB2BOutboundRequest.setReferenceNo(createOutboundReq.getReferenceNo());
        jiFengCreateB2BOutboundRequest.setWarehouse(createOutboundReq.getThirdWarehouseCode());
        jiFengCreateB2BOutboundRequest.setDestination(2);
        jiFengCreateB2BOutboundRequest.setLogisticType(Integer.valueOf(createOutboundReq.getChannelCode()));
        jiFengCreateB2BOutboundRequest.setRemark(createOutboundReq.getRemark());
        jiFengCreateB2BOutboundRequest.setOutboundType(2);
        jiFengCreateB2BOutboundRequest.setDispatchType(1);
        jiFengCreateB2BOutboundRequest.setAddressVo(JiFengCreateB2BOutboundRequest.AddressVoDTO.builder()
                        .buyerName(createOutboundReq.getReceiverName())
                        .buyerPhone(createOutboundReq.getTelNumber())
                        .recipientCountry(createOutboundReq.getReceiverCountryCode())
                        .recipientProvince(createOutboundReq.getProvince())
                        .recipientCity(createOutboundReq.getCity())
                        .recipientArea(createOutboundReq.getCity())
                        .recipientAddress(createOutboundReq.getAddress1())
                        .zipCode(createOutboundReq.getPostCode())
                .build());
        List<JiFengCreateB2BOutboundRequest.SkuInfoListDTO> skuListDTOS = new ArrayList<>();
        createOutboundReq.getItems().forEach(item -> {
            JiFengCreateB2BOutboundRequest.SkuInfoListDTO skuListDTO = new JiFengCreateB2BOutboundRequest.SkuInfoListDTO();
            skuListDTO.setSku(item.getWarehousePlatformSku());
            skuListDTO.setCount(item.getDeliveryQty());
            skuListDTOS.add(skuListDTO);
        });
        jiFengCreateB2BOutboundRequest.setSkuInfoList(skuListDTOS);
        if(StringUtils.isNotBlank(createOutboundReq.getFileUrl())) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOnce("url", createOutboundReq.getFileUrl());
            jsonObject.putOnce("name", createOutboundReq.getFileName());
            jiFengCreateB2BOutboundRequest.setFileInfo(JiFengCreateB2BOutboundRequest.FileInfoDTO.builder()
                            .otherJson(jsonObject.toString())
                    .build());
        }
        return jiFengCreateB2BOutboundRequest;
    }

    public <T> boolean isSuccess(JiFengBaseResp<T> resp) {
        return resp != null && resp.getCode() != null && resp.getCode() == 0;
    }

}
