package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.enums.JituDeliveryTypeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.erp.server.wms.service.B2bThirdDeliveryService;
import com.sdk.wms.jitu.dto.request.*;
import com.sdk.wms.jitu.dto.response.*;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * 头程发货单下推海外仓
     * @param createInboundReq
     * @return
     */
    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        createInboundReq.setReceivingCode(null);
        // 众包推送需要默认ERP的头程发货单号-HH+MM+SS
        String timeFormatter = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        createInboundReq.setReferenceNo(CharSequenceUtil.format("{}_{}",createInboundReq.getReferenceNo(),timeFormatter));
        JituOverseasInboundCreateRequest jituOverseasInboundCreateRequest = this.buildInboundDto(createInboundReq);
        // 创建入库单
        OverseasInboundCreateResponse responseBaseResponse = jituService.overseasInboundCreate(jituOverseasInboundCreateRequest);
        OverseasInboundCreateResponse.Response response = responseBaseResponse.getResponseitems().get(0);
        return "true".equals(response.getSuccess()) ? success(response.getEntryOrderId()) : failure(response.getMessage());
    }

    private JituOverseasInboundCreateRequest buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        JituOverseasInboundCreateRequest inboundCreateRequest = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToJitu(createInboundReq);
        return inboundCreateRequest;
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        return failure(getPlatForm().getName() + "不支持编辑入库单，请先取消入库单后，重新创建");
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        JituOverseasInboundCancelRequest overseasInboundCancelRequest = JituOverseasInboundCancelRequest.builder()
                .customerid(cancelInboundReq.getOwnerCode())
                .warehouseCode(cancelInboundReq.getWarehouseCode())
                .orderType("CGRK")
                .orderCode(cancelInboundReq.getSourceCode())
                .cancelReason(cancelInboundReq.getRemark())
                .build();
        OverseasInboundCancelResponse responseBaseResponse = jituService.overseasInboundCancel(overseasInboundCancelRequest);
        OverseasInboundCancelResponse.Response response = responseBaseResponse.getResponseitems().get(0);
        return "true".equals(response.getSuccess()) ? success(cancelInboundReq.getSourceCode()) : failure(response.getMessage());
    }

    @Override
    public ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(@Valid ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return ApiResult.error(getPlatForm().getName() + "不支持查询运费");
    }

    @Override
    public ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        StockOutOrderCreateRequest createRequest = OverseasWarehouseInboundConverter.INSTANCE.b2cOutboundDtoToJitu(createOutboundReq);
        //平台订单号
        setPlatformNumber(createOutboundReq, createRequest);
        //设置付款时间
        setPayTime(createOutboundReq, createRequest);
        //重置地址
        setAddress(createOutboundReq,createRequest);
        log.warn(getPlatForm().getName() + "创建出库单请求:{}", JSONUtil.toJsonStr(createRequest));
        StockOutOrderCreateResponse response = jituService.createStockOutOrder(createRequest);
        log.warn(getPlatForm().getName() + "创建出库单结果:{}", JSONUtil.toJsonStr(response));
        StockOutOrderCreateResponse.ResponseItem responseItem = response.getResponseitems().get(0);
        return "true".equals(responseItem.getSuccess()) ? success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(responseItem.getDeliveryOrderCode()).trackNo(responseItem.getMailno()).build()) : failure(responseItem.getMessage() + ":" + responseItem.getReason());
    }

    private void setAddress(ThirdWarehouseCreateOutboundReq createOutboundReq, StockOutOrderCreateRequest createRequest) {
        StockOutOrderCreateRequest.Receiver receiver = createRequest.getReceiver();
        if (CharSequenceUtil.isNotBlank(createOutboundReq.getReceiverInfo().getAddress1())){
            receiver.setAddress(createOutboundReq.getReceiverInfo().getAddress1());
        }else if (CharSequenceUtil.isNotBlank(createOutboundReq.getReceiverInfo().getAddress2())){
            receiver.setAddress(createOutboundReq.getReceiverInfo().getAddress2());
        }else if (CharSequenceUtil.isNotBlank(createOutboundReq.getReceiverInfo().getAddress3())) {
            receiver.setAddress(createOutboundReq.getReceiverInfo().getAddress3());
        }
        //取B2C订单的买家电话，为空时取收货人电话
        if (CharSequenceUtil.isBlank(createRequest.getReceiver().getMobile())) {
            receiver.setMobile(createOutboundReq.getReceiverInfo().getPhone());
        }
    }

    //取B2C销售订单的付款时间，如无取当前系统时间，时间格式（默认北京时间）2024-11-14 22:04:22
    private void setPayTime(ThirdWarehouseCreateOutboundReq createOutboundReq, StockOutOrderCreateRequest createRequest) {
        if (Objects.isNull(createRequest.getPayTime())) {
            createRequest.setPayTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }
    //取B2C销售订单的平台订单编号，如无可与客户订单号保持一致
    private void setPlatformNumber(ThirdWarehouseCreateOutboundReq createOutboundReq, StockOutOrderCreateRequest createRequest) {
        if (CharSequenceUtil.isBlank(createRequest.getPlatformNumber())) {
            createRequest.setPlatformNumber(createOutboundReq.getReferenceNo());
        }
    }


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
        StockOutOrderCancelRequest request = StockOutOrderCancelRequest.builder()
                .customerid(cancelOutboundReq.getOwnerCode())
                .warehouseCode(cancelOutboundReq.getWarehouseCode())
                .orderType("XSCK")
                .orderCode(cancelOutboundReq.getOrderCode())
                .cancelReason(cancelOutboundReq.getReason())
                .build();
        StockOutOrderCancelResponse stockOutOrderCreateResponse = jituService.cancelOrder(request);
        StockOutOrderCancelResponse.ResponseItem responseItem = stockOutOrderCreateResponse.getResponseitems().get(0);
        return "true".equals(responseItem.getSuccess()) ? success(cancelOutboundReq.getOrderCode()) : failure(responseItem.getMessage() + ":" + responseItem.getReason());
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        try {
            // 获取B2B三方发货单信息
            B2bThirdDeliveryEntity b2bThirdDelivery = b2bThirdDeliveryService.getById(cancelOutboundReq.getSourceId());
            if (b2bThirdDelivery == null) {
                return failure("B2B三方发货单不存在");
            }
            // 构建取消订单请求参数
            StockOutOrderCancelRequest request = new StockOutOrderCancelRequest();
            request.setCustomerid(cancelOutboundReq.getOwnerCode()); // 取发货仓库在三方仓配置绑定的货主编码
            request.setWarehouseCode(b2bThirdDelivery.getThirdWarehouseCode()); // 取发货仓库在三方仓配置绑定的三方仓仓库编码
            request.setOrderType("XSCK"); // 默认XSCK-销售出库
            request.setOrderCode(cancelOutboundReq.getOrderCode()); // 出库单类型时，传txlogisticid字段的单号
            request.setCancelReason(cancelOutboundReq.getRemark()); // 取操作拦截时填写的拦截原因
            log.warn(getPlatForm().getName() + "取消B2B出库单请求:{}", JSONUtil.toJsonStr(request));
            // 调用极兔API取消订单
            StockOutOrderCancelResponse response = jituService.cancelOrder(request);

            // 处理返回结果
            if (response != null && response.getResponseitems() != null && !response.getResponseitems().isEmpty()) {
                log.warn(getPlatForm().getName() + "取消B2B出库单结果:{}", JSONUtil.toJsonStr(response));
                StockOutOrderCancelResponse.ResponseItem item = response.getResponseitems().get(0);
                if ("true".equals(item.getSuccess())) {
                    return success("SUCCESS");
                } else {
                    return failure(item.getErrorMsg());
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
        return failure("暂不支持查询出库单");
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

//    private JituOverseasInboundCreateRequest buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
//        JituOverseasInboundCreateRequest inboundCreateRequest = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToZhongbao(createInboundReq);
//        if (CollUtil.isNotEmpty(createInboundReq.getAttachmentList())){
//            List<JituOverseasInboundCreateRequest.Attachment> attachmentOpenDTOs = new ArrayList<>();
//            createInboundReq.getAttachmentList().forEach(attachment -> {
//                byte[] bytes = fileFeign.downloadFile(attachment.getAttachUrl());
//                if (bytes != null) {
//                    attachmentOpenDTOs.add(JituOverseasInboundCreateRequest.Attachment.builder().base64(Base64.getEncoder().encodeToString(bytes)).fileName(attachment.getAttachName()).build());
//                }
//            });
//            inboundCreateRequest.setAttachmentOpenDTOs(attachmentOpenDTOs);
//        }
//        return inboundCreateRequest;
//    }

    @Override
    public ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        try {
            // 构建极兔出库单请求
            StockOutOrderCreateRequest request = buildB2BStockOutOrderRequest(createOutboundReq);
            log.warn(getPlatForm().getName() + "创建B2B出库单请求:{}", JSONUtil.toJsonStr(request));
            // 调用极兔API创建出库单
            StockOutOrderCreateResponse response = jituService.createStockOutOrder(request);

            // 处理返回结果
            if (response != null && response.getResponseitems() != null && !response.getResponseitems().isEmpty()) {
                log.warn(getPlatForm().getName() + "创建B2B出库单结果:{}", JSONUtil.toJsonStr(response));
                StockOutOrderCreateResponse.ResponseItem item = response.getResponseitems().get(0);
                if ("true".equals(item.getSuccess())) {
                    return success(item.getDeliveryOrderCode());
                } else {
                    return failure(item.getErrorMsg());
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
    private StockOutOrderCreateRequest buildB2BStockOutOrderRequest(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        StockOutOrderCreateRequest request = new StockOutOrderCreateRequest();
        StockOutOrderCreateRequest.Receiver receiver = new StockOutOrderCreateRequest.Receiver();
        Map<String, BigDecimal> skuPriceMap = new HashMap<>();
        B2bThirdDeliveryEntity b2bThirdDelivery = b2bThirdDeliveryService.getById(createOutboundReq.getSourceId());
        LogisticsChannelEntity logisticsChannel = logisticsFeign.getChannelById(b2bThirdDelivery.getLogisticsChannelId());
        if (Objects.nonNull(b2bThirdDelivery)) {
            // 收件人信息
            receiver.setCountrycode(b2bThirdDelivery.getCountryId());
            if (StringUtils.isNotBlank(b2bThirdDelivery.getAddress2())) {
                receiver.setAddress(b2bThirdDelivery.getAddress2());
            } else {
                if (StringUtils.isNotBlank(b2bThirdDelivery.getAddress3())) {
                    receiver.setAddress2(b2bThirdDelivery.getAddress3());
                } else {
                    receiver.setAddress2(b2bThirdDelivery.getReceiveAddress());
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append(b2bThirdDelivery.getAddress3());
            sb.append(b2bThirdDelivery.getReceiveAddress());
            receiver.setAddress2(sb.toString());
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
                    payTimeStr = soInfo.getReceiveDate().atStartOfDay().format(formatter);
                } else if(Objects.nonNull(soInfo.getBillDate())) {
                    payTimeStr = soInfo.getBillDate().atStartOfDay().format(formatter);
                }
                request.setPayTime(payTimeStr);

                // 构建SKU价格映射
                for (SoDetailEntity soDetail : soDetails) {
                    skuPriceMap.put(soDetail.getSkuId(), soDetail.getPrice());
                }
            }
            // 配送方式=平台物流/商家自联快递时必填
            if (StringUtils.isNotBlank(logisticsChannel.getUndeliverableDecision())){
                if (Objects.equals(logisticsChannel.getUndeliverableDecision(), JituDeliveryTypeEnum.PLATFORM_LOGISTICS.getCode())
                        || Objects.equals(logisticsChannel.getUndeliverableDecision(),JituDeliveryTypeEnum.SHOP_SELF_DELIVERY.getCode())) {
                    if (StringUtils.isNotBlank(b2bThirdDelivery.getTrackNo())) {
                        request.setMailno(b2bThirdDelivery.getTrackNo());
                    } else {
                        throw new ServiceException("配送方式为平台物流/商家自联快递时物流跟踪号必填");
                    }
                }
            }
        }

        // 基本信息
        request.setWarehouseCode(createOutboundReq.getThirdWarehouseCode());
        request.setCustomerid(createOutboundReq.getOwnerCode());
        request.setTxlogisticid(createOutboundReq.getReferenceNo());
        request.setOrderType("B2BXSCK"); // B2B订单默认B2BXSCK
        request.setSource("OTHER"); // B2B订单默认OTHER
        request.setSourceSystem("ERP");
        request.setBusinessMode("B2B");
        request.setOutBizNo(createOutboundReq.getReferenceNo());

        // 物流信息
        // 配送方式=平台物流时必填，渠道是否需要同步面单标识
        if (StringUtils.isNotBlank(logisticsChannel.getUndeliverableDecision())){
            request.setTransportMode(logisticsChannel.getUndeliverableDecision());
            if (Objects.equals(logisticsChannel.getUndeliverableDecision(),JituDeliveryTypeEnum.PLATFORM_LOGISTICS.getCode())
                    && StringUtils.isBlank(createOutboundReq.getFileUrl())) {
                throw new ServiceException("配送方式为平台物流时必填，渠道需要同步面单");
            }
            // 配送方式=平台物流/商家自联快递/仓配快递 时必填
            if (Objects.equals(logisticsChannel.getUndeliverableDecision(),JituDeliveryTypeEnum.PLATFORM_LOGISTICS.getCode())
                    || Objects.equals(logisticsChannel.getUndeliverableDecision(),JituDeliveryTypeEnum.SHOP_SELF_DELIVERY.getCode())
                    || Objects.equals(logisticsChannel.getUndeliverableDecision(),JituDeliveryTypeEnum.WAREHOUSE_DELIVERY.getCode())){
                if (StringUtils.isBlank(logisticsChannel.getLastMileCarrier())) {
                    throw new ServiceException("配送方式为平台物流/商家自联快递/仓配快递时尾程服务商必填");
                }
            }
        }
        request.setLabel(createOutboundReq.getFileUrl());
        request.setCarrier(logisticsChannel.getLastMileCarrier());
        request.setRouteid(createOutboundReq.getChannelCode());
        request.setDeliveryNote(createOutboundReq.getRemark());
        request.setIsCod(0); // 默认0否
        request.setStoreCode("-"); // 默认-
        request.setStoreName(""); // 默认空

        //明细
        List<StockOutOrderCreateRequest.Item> items = new ArrayList<>();
        for (ThirdWarehouseCreateFbaOutboundReq.Item item : createOutboundReq.getItems()) {
            StockOutOrderCreateRequest.Item stockOutItem = new StockOutOrderCreateRequest.Item();
            stockOutItem.setItemCode(item.getPlatformSkuNo());
            stockOutItem.setNumber(item.getDeliveryQty());
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
