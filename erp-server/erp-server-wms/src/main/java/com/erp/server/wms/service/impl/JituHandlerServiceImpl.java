package com.erp.server.wms.service.impl;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.erp.server.wms.service.B2bThirdDeliveryService;
import com.sdk.wms.jitu.dto.request.StockOutOrderRequest;
import com.sdk.wms.jitu.dto.request.WarehouseRequest;
import com.sdk.wms.jitu.dto.response.StockOutOrderResponse;
import com.sdk.wms.jitu.dto.response.WarehouseResponse;
import com.sdk.wms.jitu.service.JituService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author zdy
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class JituHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private JituService jituService;

    @Resource
    private B2bThirdDeliveryService b2bThirdDeliveryService;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;

    @Value("${openApi.jitu.key:}")
    private String key;

    @Value("${openApi.jitu.eccompanyid:}")
    private String eccompanyid;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.JI_TU;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return null;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return null;
    }

    /***
     * B2C发货单下推海外仓
     * @param createInboundReq
     * @return
     */
    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
//        createInboundReq.setReceivingCode(null);
//        // 众包推送需要默认ERP的头程发货单号-HH+MM+SS
//        String timeFormatter = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
//        createInboundReq.setReferenceNo(CharSequenceUtil.format("{}_{}",createInboundReq.getReferenceNo(),timeFormatter));
//        OverseasInboundCreateRequest overseasInboundCreateRequest = this.buildInboundDto(createInboundReq);
//        // 创建入库单
//        BaseResponse<OverseasInboundApproveResponse> responseBaseResponse = zhongbaoService.overseasInboundApprove(overseasInboundCreateRequest);
//        return responseBaseResponse.getSuccess() ? success(responseBaseResponse.getData().getOrderNo()) : failure(responseBaseResponse.getMessage() + ":" + String.join(", ", responseBaseResponse.getErrors()));
        return null;
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        return failure(getPlatForm().getName() + "不支持编辑入库单，请先取消入库单后，重新创建");
//        OverseasInboundCreateRequest overseasInboundCreateRequest = this.buildInboundDto(createInboundReq);
//        BaseResponse<OverseasInboundUpdateResponse> responseBaseResponse = zhongbaoService.overseasInboundUpdate(overseasInboundCreateRequest);
//        return responseBaseResponse.getSuccess() ? success(responseBaseResponse.getData().getOrderNo()) : failure(responseBaseResponse.getMessage());
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
//        OverseasInboundCancelRequest overseasInboundCancelRequest = OverseasInboundCancelRequest.builder()
//                .orderNos(Collections.singletonList(cancelInboundReq.getReceivingCode()))
//                .cancelRemark(cancelInboundReq.getRemark())
//                .build();
//        BaseResponse<OverseasInboundCancelResponse> responseBaseResponse = zhongbaoService.overseasInboundCancel(overseasInboundCancelRequest);
//        return responseBaseResponse.getSuccess() ? success(responseBaseResponse.getData().getSuccessList().get(0).getOrderNo()) : failure(responseBaseResponse.getData().getFailList().get(0).getMessage());
        return null;
    }

    @Override
    public ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(@Valid ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return ApiResult.error(getPlatForm().getName() + "不支持查询运费");
    }

    @Override
    public ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
//        OutboundB2cCreateRequest createRequest = OverseasWarehouseInboundConverter.INSTANCE.b2coutboundDtoToZhongbao(createOutboundReq);
//        //设置拣货类型
//        setPickType(createOutboundReq, createRequest);
//        //设置附件
//        setAttachment(createOutboundReq, createRequest);
//        //重置地址
//        setAddress(createRequest);
//        log.warn(getPlatForm().getName() + "创建出库单请求:{}", JSONUtil.toJsonStr(createRequest));
//        BaseResponse<OutboundB2cCreateResponse> response = zhongbaoService.createB2cOutboundBill(createRequest);
//        log.warn(getPlatForm().getName() + "创建出库单结果:{}", JSONUtil.toJsonStr(response));
////        if (response.getMessage().contains("参考号重复")) {
////            GoodCangResponse<String> orderCode = goodCangService.getOutboundCode(createOutboundReq.getReferenceNo());
////            return success(orderCode.getData());
////        }
//        return response.getSuccess() ? success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getData().getOrderNo()).trackNo(response.getData().getTrackingNo()).build()) : failure(response.getMessage() + ":" + String.join(", ", response.getErrors()));
        return null;
    }

//    private void setAddress(OutboundB2cCreateRequest createRequest) {
//        if (CharSequenceUtil.isBlank(createRequest.getAddress())) {
//            createRequest.setAddress(createRequest.getAddress2());
//        }
//        if (CharSequenceUtil.isBlank(createRequest.getAddress())) {
//            createRequest.setAddress(createRequest.getAddress3());
//        }
//    }
//
//    private void setAttachment(ThirdWarehouseCreateOutboundReq createOutboundReq, OutboundB2cCreateRequest createRequest) {
//        List<OutboundB2cCreateRequest.Attachment> attachments = new ArrayList<>();
//        if (CharSequenceUtil.isNotBlank(createOutboundReq.getLabelData())) {
//               attachments.add(OutboundB2cCreateRequest.Attachment.builder().base64(dropPrefix(createOutboundReq.getLabelData())).fileName(createOutboundReq.getReferenceNo()+"面单.pdf").build());
//        }
//        if (CharSequenceUtil.isNotBlank(createOutboundReq.getInvoiceData())) {
//            attachments.add(OutboundB2cCreateRequest.Attachment.builder().base64(dropPrefix(createOutboundReq.getInvoiceData())).fileName(createOutboundReq.getReferenceNo()+"发票.pdf").build());
//        }
//        createRequest.setAttachmentOpenDTOs(attachments);
//    }

    /**
     * 根据，去掉参数base64需去掉前缀
     */
//    private String dropPrefix(String base64) {
//        if (CharSequenceUtil.isBlank(base64)) {
//            return base64;
//        }
//        //根据逗号去掉前缀，不存在逗号直接返回
//        if (!base64.contains(",")) {
//            return base64;
//        }else {
//            return base64.split(",")[1];
//        }
//    }
//    private static void setPickType(ThirdWarehouseCreateOutboundReq createOutboundReq, OutboundB2cCreateRequest createRequest) {
//        List<ThirdWarehouseCreateOutboundReq.Item> items = createOutboundReq.getItems();
//        /**
//         单SKU且数量为1，传1：一票一件
//         非单SKU，传3：一票多件
//         */
//        if (items.size() == 1 && items.get(0).getQuantity() == 1) {
//            createRequest.setPickType(1);
//        }else {
//            createRequest.setPickType(3);
//        }
//    }
    @Override
    public ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@Valid ThirdWarehouseUploadFileReq uploadFileReq) {
        return ApiResult.error(getPlatForm().getName() + "不支持上传文件");
    }

    @Override
    public ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(@Valid ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return ApiResult.error("暂不支持上传面单");
    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
//        BaseResponse<OutboundB2cCancelResponse> response = zhongbaoService.cancelB2cOutboundBill(OutboundB2cCancelRequest.builder().orderNos(Collections.singletonList(cancelOutboundReq.getOrderCode())).cancelRemark(cancelOutboundReq.getReason()).build());
//        if (!response.getSuccess()) {
//            return failure(response.getMessage());
//        }else {
//            if (Objects.nonNull(response.getData().getFailQty()) && response.getData().getFailQty() > 0){
//                return failure(response.getData().getFailList().get(0).getMessage());
//            }
//            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
//        }
        return null;
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        try {
            // 获取B2B三方发货单信息
            B2bThirdDeliveryEntity b2bThirdDelivery = b2bThirdDeliveryService.getById(cancelOutboundReq.getSourceId());
            if (b2bThirdDelivery == null) {
                return failure("B2B三方发货单不存在");
            }
            
            // 构建授权信息
            Map<String, Object> authMap = new HashMap<>();
            authMap.put("key", key);
            authMap.put("eccompanyid", eccompanyid);
            
            // 构建取消订单请求参数
            Map<String, Object> request = new HashMap<>();
            request.put("customerid", authMap.get("eccompanyid")); // 取发货仓库在三方仓配置绑定的货主编码
            request.put("warehouseCode", b2bThirdDelivery.getThirdWarehouseCode()); // 取发货仓库在三方仓配置绑定的三方仓仓库编码
            request.put("orderType", "XSCK"); // 默认XSCK-销售出库
            request.put("orderCode", cancelOutboundReq.getOrderCode()); // 出库单类型时，传txlogisticid字段的单号
            request.put("cancelReason", cancelOutboundReq.getRemark()); // 取操作拦截时填写的拦截原因
            
            // 调用极兔API取消订单
            StockOutOrderResponse response = jituService.cancelOrder(authMap, request);
            
            // 处理返回结果
            if (response != null && response.getResponseitems() != null && !response.getResponseitems().isEmpty()) {
                StockOutOrderResponse.ResponseItem item = response.getResponseitems().get(0);
                if (item.getSuccess()) {
                    return success("SUCCESS");
                } else {
                    return failure(item.getMessage());
                }
            } else {
                return failure("极兔接口返回异常");
            }
        } catch (Exception e) {
            log.error("取消极兔B2B出库单失败", e);
            return failure("取消极兔B2B出库单失败: " + e.getMessage());
        }
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq) {
//        OutboundB2cQueryRequest queryRequest = OutboundB2cQueryRequest.builder().referenceNo(queryOutboundReq.getErpOrderCode()).build();
//        BaseResponse<OutboundB2cQueryResponse> response = zhongbaoService.queryB2cOutboundBill(queryRequest);
//        return response.getSuccess() ?
//                success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getData().getList().get(0).getOrderNo()).trackNo(response.getData().getList().get(0).getTrackingNo()).build())
//                : failure(response.getMessage() + ":" + String.join(", ", response.getErrors()));
        return null;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
//        OverseasOutboundQueryRequest overseasOutboundQueryRequest = new OverseasOutboundQueryRequest();
//        List<ThirdWarehouseQueryFbaOutboundResponse> thirdWarehouseQueryFbaOutboundResponses = new ArrayList<>();
//        overseasOutboundQueryRequest.setReferenceNos(req.getErpOrderCodeList());
//
//        log.warn(getPlatForm().getName() + "查询b2b出库单请求:{}", JSONUtil.toJsonStr(overseasOutboundQueryRequest));
//        OverseasOutboundQueryResponse response = zhongbaoService.queryOutboundBill(overseasOutboundQueryRequest);
//        log.warn(getPlatForm().getName() + "查询b2b出库单结果:{}", JSONUtil.toJsonStr(response));
//        if (response.getCode().equals("20000")) {
//            if (Objects.nonNull(response.getResponseData())
//                    && !response.getResponseData().getList().isEmpty()) {
//                for (OverseasOutboundQueryResponse.DataList dataList : response.getResponseData().getList()) {
//                    ThirdWarehouseQueryFbaOutboundResponse thirdWarehouseQueryFbaOutboundResponse = new ThirdWarehouseQueryFbaOutboundResponse();
//                    thirdWarehouseQueryFbaOutboundResponse.setPlatformOrderCode(dataList.getOrderNo());
//                    thirdWarehouseQueryFbaOutboundResponse.setCode(dataList.getReferenceNo());
//                    thirdWarehouseQueryFbaOutboundResponse.setTrackNo(dataList.getTrackingNo());
//                    thirdWarehouseQueryFbaOutboundResponse.setStatus(dataList.getStatus().toString());
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    Calendar now = Calendar.getInstance();
//                    String delievery = sdf.format(now.getTime());
//                    String isoFormatStr = delievery.replace(" ", "T");
//                    thirdWarehouseQueryFbaOutboundResponse.setDeliveryTimeStr(isoFormatStr);
//                    thirdWarehouseQueryFbaOutboundResponse.setErrorReason(dataList.getErrorReason());
//                    thirdWarehouseQueryFbaOutboundResponse.setPlatform(PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode());
//                    thirdWarehouseQueryFbaOutboundResponses.add(thirdWarehouseQueryFbaOutboundResponse);
//                }
//                return success(thirdWarehouseQueryFbaOutboundResponses);
//            } else {
//                ThirdWarehouseQueryFbaOutboundResponse thirdWarehouseQueryFbaOutboundResponse = new ThirdWarehouseQueryFbaOutboundResponse();
//                thirdWarehouseQueryFbaOutboundResponse.setPlatform(PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode());
//                thirdWarehouseQueryFbaOutboundResponses.add(thirdWarehouseQueryFbaOutboundResponse);
//                return failure(thirdWarehouseQueryFbaOutboundResponses);
//            }
//        }
//        return !thirdWarehouseQueryFbaOutboundResponses.isEmpty() ? success(thirdWarehouseQueryFbaOutboundResponses) : failure(response.getMessage());
        return null;
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        Map<String, Object> authJson = dto.getAuthJson();
        WarehouseRequest request = WarehouseRequest.builder().customerid((String) authJson.getOrDefault("customerid", "")).build();
        WarehouseResponse response = jituService.warehouseList(authJson, request);
        if ("true".equals(response.getResponseitems().get(0).getSuccess())) {
            return true;
        } else {
            throw new ServiceException("授权失败," + response.getResponseitems().get(0).getErrorMsg());
        }
    }

//    private OverseasInboundCreateRequest buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
//        OverseasInboundCreateRequest inboundCreateRequest = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToZhongbao(createInboundReq);
//        if (CollUtil.isNotEmpty(createInboundReq.getAttachmentList())){
//            List<OverseasInboundCreateRequest.Attachment> attachmentOpenDTOs = new ArrayList<>();
//            createInboundReq.getAttachmentList().forEach(attachment -> {
//                byte[] bytes = fileFeign.downloadFile(attachment.getAttachUrl());
//                if (bytes != null) {
//                    attachmentOpenDTOs.add(OverseasInboundCreateRequest.Attachment.builder().base64(Base64.getEncoder().encodeToString(bytes)).fileName(attachment.getAttachName()).build());
//                }
//            });
//            inboundCreateRequest.setAttachmentOpenDTOs(attachmentOpenDTOs);
//        }
//        return inboundCreateRequest;
//    }

    @Override
    public ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        try {
            Map<String, Object> authMap = new HashMap<>();
            // 构建极兔出库单请求
            StockOutOrderRequest request = buildB2BStockOutOrderRequest(createOutboundReq,authMap);
            
            // 调用极兔API创建出库单
            StockOutOrderResponse response = jituService.createStockOutOrder(authMap,request);
            
            // 处理返回结果
            if (response != null && response.getResponseitems() != null && !response.getResponseitems().isEmpty()) {
                StockOutOrderResponse.ResponseItem item = response.getResponseitems().get(0);
                if (item.getSuccess()) {
                    return success(item.getDeliveryOrderCode());
                } else {
                    return failure(item.getMessage());
                }
            } else {
                return failure("极兔接口返回异常");
            }
        } catch (Exception e) {
            log.error("创建极兔B2B出库单失败", e);
            return failure("创建极兔B2B出库单失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建B2B极兔出库单请求
     */
    private StockOutOrderRequest buildB2BStockOutOrderRequest(ThirdWarehouseCreateFbaOutboundReq createOutboundReq,Map<String, Object> authMap) {
        StockOutOrderRequest request = new StockOutOrderRequest();
        StockOutOrderRequest.Receiver receiver = new StockOutOrderRequest.Receiver();
        authMap.put("key",key);
        authMap.put("eccompanyid",eccompanyid);
        Map<String, BigDecimal> skuPriceMap = new HashMap<>();
        B2bThirdDeliveryEntity b2bThirdDelivery = b2bThirdDeliveryService.getById(createOutboundReq.getSourceId());
        if (Objects.nonNull(b2bThirdDelivery)) {
            // 收件人信息
            receiver.setCountrycode(b2bThirdDelivery.getCountryId());
            receiver.setShortAddress(b2bThirdDelivery.getReceiveAddress());
            if (StringUtils.isNotBlank(b2bThirdDelivery.getAddress2())) {
                receiver.setAddress(b2bThirdDelivery.getAddress2());
            } else {
                if (StringUtils.isNotBlank(b2bThirdDelivery.getAddress3())) {
                    receiver.setAddress(b2bThirdDelivery.getAddress3());
                } else {
                    receiver.setAddress(b2bThirdDelivery.getReceiveAddress());
                }
            }
            receiver.setAddress2(b2bThirdDelivery.getAddress3());
            receiver.setArea(b2bThirdDelivery.getCity());
            receiver.setCity(b2bThirdDelivery.getCity());
            receiver.setProv(b2bThirdDelivery.getProvince());
            receiver.setPostcode(b2bThirdDelivery.getPostCode());
            receiver.setName(b2bThirdDelivery.getCustomerName());
            receiver.setPhone(b2bThirdDelivery.getTelNumber());
            receiver.setMobile(b2bThirdDelivery.getTelNumber());
            receiver.setDoorNo("");
            request.setReceiver(receiver);

            SoInfoEntity soInfo = soInfoFeign.getSoInfoById(b2bThirdDelivery.getSoId());
            if (Objects.nonNull(soInfo)) {
                List<SoDetailEntity> soDetails = soInfoFeign.listSoDetailByMainId(soInfo.getId());
                String payTimeStr = "";
                request.setPlatformNumber(StringUtils.isNotBlank(soInfo.getPlatformOrderCode()) ? soInfo.getPlatformOrderCode() : soInfo.getCustomerOrderNo());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                if(Objects.nonNull(soInfo.getReceiveDate())) {
                    payTimeStr = soInfo.getReceiveDate().format(formatter);
                } else {
                    payTimeStr = soInfo.getBillDate().format(formatter);
                }
                request.setPayTime(payTimeStr);
                
                // 构建SKU价格映射
                for (SoDetailEntity soDetail : soDetails) {
                    skuPriceMap.put(soDetail.getSkuId(), soDetail.getPrice());
                }
            }
            //TODO 配送方式=平台物流/商家自联快递时必填
            request.setMailno(b2bThirdDelivery.getTrackNo());
        }
        // 基本信息
        request.setWarehouseCode(createOutboundReq.getThirdWarehouseCode());
        request.setTxlogisticid(createOutboundReq.getReferenceNo());
        request.setOrderType("B2BXSCK"); // B2B订单默认B2BXSCK
        request.setSource("OTHER"); // B2B订单默认OTHER
        request.setSourceSystem("ERP");
        request.setBusinessMode("B2B");
        request.setOutBizNo(createOutboundReq.getReferenceNo());
        
        // 物流信息
        //TODO 等4.5开发完
        request.setTransportMode("PTWL");
        request.setCarrier("ABF");
        request.setRouteid(createOutboundReq.getChannelCode());
        //TODO 配送方式=平台物流时必填，渠道是否需要同步面单标识
        request.setLabel(createOutboundReq.getFileUrl());
        request.setDeliveryNote(createOutboundReq.getRemark());
        request.setIsCod("0"); // 默认0否
        request.setStoreCode("-"); // 默认-
        request.setStoreName(""); // 默认空

        //明细
        List<StockOutOrderRequest.Item> items = new ArrayList<>();
        for (ThirdWarehouseCreateFbaOutboundReq.Item item : createOutboundReq.getItems()) {
            StockOutOrderRequest.Item stockOutItem = new StockOutOrderRequest.Item();
            stockOutItem.setItemCode(item.getSkuNo());
            stockOutItem.setNumber(item.getBoxQty());
            // 从销售订单明细获取销售单价
            BigDecimal price = skuPriceMap.get(item.getSkuId());
            stockOutItem.setItemvalue(price != null ? price : BigDecimal.ZERO);
            stockOutItem.setInventoryType("ZP"); // 默认ZP
            stockOutItem.setSkuId(""); // B2B订单：默认为空
            stockOutItem.setIsGift("0"); // 默认0否
            items.add(stockOutItem);
        }
        request.setItems(items);
        request.setPricecurrency("CNY"); // 默认CNY

        return request;
    }
}
