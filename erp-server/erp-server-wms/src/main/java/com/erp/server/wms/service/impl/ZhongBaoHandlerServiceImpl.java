package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.enums.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.convert.ThirdWarehouseConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.erp.server.wms.service.B2bThirdDeliveryService;
import com.sdk.wms.goodcang.dto.request.GoodCangGetSkuReq;
import com.sdk.wms.zhongbao.dto.request.*;
import com.sdk.wms.zhongbao.dto.response.*;
import com.sdk.wms.zhongbao.service.ZhongbaoService;
import com.sdk.wms.zhongbao.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
/**
 * @author liuruipeng
 * @date 2023年11月17日 10:51
 */
@Slf4j
@Service
public class ZhongBaoHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private ZhongbaoService zhongbaoService;

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

    @Value("${openApi.zhongbao.appId:}")
    private String apiKey;

    @Value("${openApi.zhongbao.apiSecret:}")
    private String apiSecret;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.ZHONG_BAO;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return null;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        ProductRequest productRequest = ProductRequest.builder().commonParam(CommonRequest.builder()
                .pageParam(PageRequest.builder().pageSize(100).pageNum(0).build()).build()).status(3).build();
        List<ProductResponse.Product> respList = new ArrayList<>();
        int pageNum = 1;
        while (true) {
         productRequest.getCommonParam().setPageParam(PageRequest.builder().pageSize(100).pageNum(pageNum).build());
            BaseResponse<ProductResponse> response = zhongbaoService.productList(productRequest);
            if (!response.getSuccess()) {
                log.error(getPlatForm() +"查询产品信息异常" + response);
                return failure(response.getMessage());
            }
            respList.addAll(response.getData().getList());
            if (Integer.parseInt(response.getData().getTotalCount()) <= pageNum * 100) {
                break;
            }
            pageNum++;
        }
        List<ThirdWarehouseSkuResp> thirdWarehouseSkuRespList = ThirdWarehouseConverter.INSTANCE.convertZhongbaoSku(respList);
        return success(thirdWarehouseSkuRespList);
    }

    /***
     * B2C发货单下推海外仓
     * @param createInboundReq
     * @return
     */
    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        createInboundReq.setReceivingCode(null);
        // 众包推送需要默认ERP的头程发货单号-HH+MM+SS
        String timeFormatter = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        createInboundReq.setReferenceNo(CharSequenceUtil.format("{}_{}",createInboundReq.getReferenceNo(),timeFormatter));
        OverseasInboundCreateRequest overseasInboundCreateRequest = this.buildInboundDto(createInboundReq);
        // 创建入库单
        BaseResponse<OverseasInboundApproveResponse> responseBaseResponse = zhongbaoService.overseasInboundApprove(overseasInboundCreateRequest);
        return responseBaseResponse.getSuccess() ? success(responseBaseResponse.getData().getOrderNo()) : failure(responseBaseResponse.getMessage() + ":" + String.join(", ", responseBaseResponse.getErrors()));
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
        OverseasInboundCancelRequest overseasInboundCancelRequest = OverseasInboundCancelRequest.builder()
                .orderNos(Collections.singletonList(cancelInboundReq.getReceivingCode()))
                .cancelRemark(cancelInboundReq.getRemark())
                .build();
        BaseResponse<OverseasInboundCancelResponse> responseBaseResponse = zhongbaoService.overseasInboundCancel(overseasInboundCancelRequest);
        return responseBaseResponse.getSuccess() ? success(responseBaseResponse.getData().getSuccessList().get(0).getOrderNo()) : failure(responseBaseResponse.getData().getFailList().get(0).getMessage());
    }

    @Override
    public ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(@Valid ThirdWarehouseCalculateFeeReq calculateFeeReq) {
       return ApiResult.error(getPlatForm().getName() + "不支持查询运费");
    }

    @Override
    public ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        OutboundB2cCreateRequest createRequest = OverseasWarehouseInboundConverter.INSTANCE.b2coutboundDtoToZhongbao(createOutboundReq);
        //设置拣货类型
        setPickType(createOutboundReq, createRequest);
        //设置附件
        setAttachment(createOutboundReq, createRequest);
        //重置地址
        setAddress(createRequest);
        log.warn(getPlatForm().getName() + "创建出库单请求:{}", JSONUtil.toJsonStr(createRequest));
        BaseResponse<OutboundB2cCreateResponse> response = zhongbaoService.createB2cOutboundBill(createRequest);
        log.warn(getPlatForm().getName() + "创建出库单结果:{}", JSONUtil.toJsonStr(response));
//        if (response.getMessage().contains("参考号重复")) {
//            GoodCangResponse<String> orderCode = goodCangService.getOutboundCode(createOutboundReq.getReferenceNo());
//            return success(orderCode.getData());
//        }
        return response.getSuccess() ? success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getData().getOrderNo()).trackNo(response.getData().getTrackingNo()).build()) : failure(response.getMessage() + ":" + String.join(", ", response.getErrors()));
    }

    private void setAddress(OutboundB2cCreateRequest createRequest) {
        if (CharSequenceUtil.isBlank(createRequest.getAddress())) {
            createRequest.setAddress(createRequest.getAddress2());
        }
        if (CharSequenceUtil.isBlank(createRequest.getAddress())) {
            createRequest.setAddress(createRequest.getAddress3());
        }
    }

    private void setAttachment(ThirdWarehouseCreateOutboundReq createOutboundReq, OutboundB2cCreateRequest createRequest) {
        List<OutboundB2cCreateRequest.Attachment> attachments = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(createOutboundReq.getLabelData())) {
               attachments.add(OutboundB2cCreateRequest.Attachment.builder().base64(dropPrefix(createOutboundReq.getLabelData())).fileName(createOutboundReq.getReferenceNo()+"面单.pdf").build());
        }
        if (CharSequenceUtil.isNotBlank(createOutboundReq.getInvoiceData())) {
            attachments.add(OutboundB2cCreateRequest.Attachment.builder().base64(dropPrefix(createOutboundReq.getInvoiceData())).fileName(createOutboundReq.getReferenceNo()+"发票.pdf").build());
        }
        createRequest.setAttachmentOpenDTOs(attachments);
    }

    /**
     * 根据，去掉参数base64需去掉前缀
     */
    private String dropPrefix(String base64) {
        if (CharSequenceUtil.isBlank(base64)) {
            return base64;
        }
        //根据逗号去掉前缀，不存在逗号直接返回
        if (!base64.contains(",")) {
            return base64;
        }else {
            return base64.split(",")[1];
        }
    }
    private static void setPickType(ThirdWarehouseCreateOutboundReq createOutboundReq, OutboundB2cCreateRequest createRequest) {
        List<ThirdWarehouseCreateOutboundReq.Item> items = createOutboundReq.getItems();
        /**
         单SKU且数量为1，传1：一票一件
         非单SKU，传3：一票多件
         */
        if (items.size() == 1 && items.get(0).getQuantity() == 1) {
            createRequest.setPickType(1);
        }else {
            createRequest.setPickType(3);
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
        BaseResponse<OutboundB2cCancelResponse> response = zhongbaoService.cancelB2cOutboundBill(OutboundB2cCancelRequest.builder().orderNos(Collections.singletonList(cancelOutboundReq.getOrderCode())).cancelRemark(cancelOutboundReq.getReason()).build());
        if (!response.getSuccess()) {
            return failure(response.getMessage());
        }else {
            if (Objects.nonNull(response.getData().getFailQty()) && response.getData().getFailQty() > 0){
                return failure(response.getData().getFailList().get(0).getMessage());
            }
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        OverseasOutboundCancelRequest overseasOutboundCancelRequest = new OverseasOutboundCancelRequest();
        overseasOutboundCancelRequest.setCancelRemark(cancelOutboundReq.getRemark());
        overseasOutboundCancelRequest.setOrderNos(Collections.singletonList(cancelOutboundReq.getOrderCode()));
        log.warn(getPlatForm().getName() + "取消出库单请求:{}", JSONUtil.toJsonStr(overseasOutboundCancelRequest));
        BaseResponse<OverseasOutboundCancelResponse> response = zhongbaoService.cancelOutboundBill(overseasOutboundCancelRequest);
        log.warn(getPlatForm().getName() + "取消出库单结果:{}", JSONUtil.toJsonStr(response));
        if (!response.getData().getResponseData().getSuccessList().isEmpty()) {
            return success(B2bThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        } else {
            return failure(response.getData().getMessage());
        }
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq) {
        OutboundB2cQueryRequest queryRequest = OutboundB2cQueryRequest.builder().referenceNo(queryOutboundReq.getErpOrderCode()).build();
        BaseResponse<OutboundB2cQueryResponse> response = zhongbaoService.queryB2cOutboundBill(queryRequest);
        return response.getSuccess() ?
                success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getData().getList().get(0).getOrderNo()).trackNo(response.getData().getList().get(0).getTrackingNo()).build())
                : failure(response.getMessage() + ":" + String.join(", ", response.getErrors()));
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        OverseasOutboundQueryRequest overseasOutboundQueryRequest = new OverseasOutboundQueryRequest();
        List<ThirdWarehouseQueryFbaOutboundResponse> thirdWarehouseQueryFbaOutboundResponses = new ArrayList<>();
//        //一个半小时到现在的订单
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Calendar now = Calendar.getInstance();
//        String endUpdateTime = sdf.format(now.getTime());
//        Calendar oneAndHalfHourAgo = Calendar.getInstance();
//        oneAndHalfHourAgo.add(Calendar.MINUTE, -90);
//        String startUpdateTime = sdf.format(oneAndHalfHourAgo.getTime());
//        overseasOutboundQueryRequest.setStartUpdateTime(startUpdateTime);
//        overseasOutboundQueryRequest.setEndUpdateTime(endUpdateTime);
        overseasOutboundQueryRequest.setReferenceNos(req.getErpOrderCodeList());

        log.warn(getPlatForm().getName() + "查询b2b出库单请求:{}", JSONUtil.toJsonStr(overseasOutboundQueryRequest));
        OverseasOutboundQueryResponse response = zhongbaoService.queryOutboundBill(overseasOutboundQueryRequest);
        log.warn(getPlatForm().getName() + "查询b2b出库单结果:{}", JSONUtil.toJsonStr(response));
        if (response.getCode().equals("20000")) {
            if (Objects.nonNull(response.getResponseData())
                    && !response.getResponseData().getList().isEmpty()) {
                for (OverseasOutboundQueryResponse.DataList dataList : response.getResponseData().getList()) {
                    thirdWarehouseQueryFbaOutboundResponses.add(buildQueryFbaOutboundResponse(dataList, false));
                }
                return success(thirdWarehouseQueryFbaOutboundResponses);
            } else {
                ThirdWarehouseQueryFbaOutboundResponse thirdWarehouseQueryFbaOutboundResponse = new ThirdWarehouseQueryFbaOutboundResponse();
                thirdWarehouseQueryFbaOutboundResponse.setPlatform(PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode());
                thirdWarehouseQueryFbaOutboundResponses.add(thirdWarehouseQueryFbaOutboundResponse);
                return failure(thirdWarehouseQueryFbaOutboundResponses);
            }
        }
        return !thirdWarehouseQueryFbaOutboundResponses.isEmpty() ? success(thirdWarehouseQueryFbaOutboundResponses) : failure(response.getMessage());
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryFbaOutboundPageResponse> queryFbaOutboundBillPage(ThirdWarehouseQueryFbaOutboundPageReq req) {
        OverseasOutboundQueryRequest queryRequest = new OverseasOutboundQueryRequest();
        queryRequest.setStartCreateTime(req.getStartCreateTime());
        queryRequest.setEndCreateTime(req.getEndCreateTime());
        queryRequest.setStartUpdateTime(req.getStartUpdateTime());
        queryRequest.setEndUpdateTime(req.getEndUpdateTime());
        queryRequest.setCommonParam(CommonRequest.builder()
                .pageParam(PageRequest.builder()
                        .pageNum(Objects.nonNull(req.getPageNum()) ? req.getPageNum() : 1)
                        .pageSize(Objects.nonNull(req.getPageSize()) ? req.getPageSize() : 100)
                        .build())
                .build());
        log.warn(getPlatForm().getName() + "分页查询b2b出库单请求:{}", JSONUtil.toJsonStr(queryRequest));
        OverseasOutboundQueryResponse response = zhongbaoService.queryOutboundBill(queryRequest);
        log.warn(getPlatForm().getName() + "分页查询b2b出库单结果:{}", JSONUtil.toJsonStr(response));
        if (Objects.isNull(response)) {
            return failure("众包查询B2B出库单响应为空");
        }
        if (!StringUtils.equals(response.getCode(), "20000") || !response.isSuccess()) {
            return failure(response.getMessage());
        }
        ThirdWarehouseQueryFbaOutboundPageResponse pageResponse = new ThirdWarehouseQueryFbaOutboundPageResponse();
        OverseasOutboundQueryResponse.ResponseData responseData = response.getResponseData();
        if (Objects.isNull(responseData)) {
            pageResponse.setList(Collections.emptyList());
            return success(pageResponse);
        }
        pageResponse.setPageNum(responseData.getPageNum());
        pageResponse.setPageSize(responseData.getPageSize());
        pageResponse.setTotalCount(responseData.getTotalCount());
        pageResponse.setTotalPage(responseData.getTotalPage());
        List<ThirdWarehouseQueryFbaOutboundResponse> list = new ArrayList<>();
        if (CollUtil.isNotEmpty(responseData.getList())) {
            for (OverseasOutboundQueryResponse.DataList dataList : responseData.getList()) {
                list.add(buildQueryFbaOutboundResponse(dataList, true));
            }
        }
        pageResponse.setList(list);
        return success(pageResponse);
    }

    private ThirdWarehouseQueryFbaOutboundResponse buildQueryFbaOutboundResponse(OverseasOutboundQueryResponse.DataList dataList, boolean rawStatus) {
        ThirdWarehouseQueryFbaOutboundResponse response = new ThirdWarehouseQueryFbaOutboundResponse();
        response.setPlatformOrderCode(dataList.getOrderNo());
        response.setCode(dataList.getReferenceNo());
        response.setTrackNo(dataList.getTrackingNo());
        response.setStatus(rawStatus
                ? String.valueOf(dataList.getStatus())
                : ZhongBaoB2BDeliveryStatusEnum.getErpOrderStatus(String.valueOf(dataList.getStatus())));
        if (rawStatus) {
            response.setDeliveryTimeStr(dataList.getOutboundTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar now = Calendar.getInstance();
            String delievery = sdf.format(now.getTime());
            response.setDeliveryTimeStr(delievery.replace(" ", "T"));
        }
        response.setErrorReason(dataList.getErrorReason());
        response.setPlatform(PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode());
        return response;
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        Map<String, Object> authJson = dto.getAuthJson();
        BaseResponse open = zhongbaoService.open(zhongbaoService.getToken(authJson));
        if (open.getSuccess()) {
            return true;
        } else {
            throw new ServiceException("授权失败," + open.getMessage());
        }
    }

    private OverseasInboundCreateRequest buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        OverseasInboundCreateRequest inboundCreateRequest = OverseasWarehouseInboundConverter.INSTANCE.inboundDtoToZhongbao(createInboundReq);
        if (CollUtil.isNotEmpty(createInboundReq.getAttachmentList())){
            List<OverseasInboundCreateRequest.Attachment> attachmentOpenDTOs = new ArrayList<>();
            createInboundReq.getAttachmentList().forEach(attachment -> {
                byte[] bytes = fileFeign.downloadFile(attachment.getAttachUrl());
                if (bytes != null) {
                    attachmentOpenDTOs.add(OverseasInboundCreateRequest.Attachment.builder().base64(Base64.getEncoder().encodeToString(bytes)).fileName(attachment.getAttachName()).build());
                }
            });
            inboundCreateRequest.setAttachmentOpenDTOs(attachmentOpenDTOs);
        }
        return inboundCreateRequest;
    }

    @Override
    public ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        OverseasOutboundCreateRequest overseasOutboundCreateRequest = buildCreateFbaOutboundDto(createOutboundReq);
        log.warn(getPlatForm().getName() + "创建b2b出库单请求:{}", JSONUtil.toJsonStr(overseasOutboundCreateRequest));
        OverseasOutboundCreateResponse response = zhongbaoService.createOutboundBill(overseasOutboundCreateRequest);
        log.warn(getPlatForm().getName() + "创建b2b出库单结果:{}", JSONUtil.toJsonStr(response));
        return response.getSuccess() ? success(response.getResponseData().getOrderNo()) : failure(response.getMessage());
    }

    public OverseasOutboundCreateRequest buildCreateFbaOutboundDto(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {

        OverseasOutboundCreateRequest overseasOutboundCreateRequest = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToZhongBao(createOutboundReq);
        List<ThirdWarehouseCreateFbaOutboundReq.Item> items = createOutboundReq.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("订单明细不能为空");
        }

        Set<String> skuIds = new HashSet<>();
        B2bThirdDeliveryEntity b2bThirdDelivery = b2bThirdDeliveryService.getById(createOutboundReq.getSourceId());
        if (Objects.nonNull(b2bThirdDelivery)) {
            LogisticsChannelEntity logisticsChannel = logisticsFeign.getChannelById(b2bThirdDelivery.getLogisticsChannelId());
            overseasOutboundCreateRequest.setShippingMethodCode(b2bThirdDelivery.getLogisticsChannelCode());
            if (Objects.nonNull(logisticsChannel)) {
                overseasOutboundCreateRequest.setIsSign(logisticsChannel.getIsApiSign() ? 1 : -1);
                overseasOutboundCreateRequest.setIsInsure(logisticsChannel.getIsApiInsurance() ? 1 : -1);
            }

            //取订单金额和汇率（明细行取第一行汇率）换算成人民币金额，在取系统最新美元汇率换算成美元
            SoInfoEntity soInfo = soInfoFeign.getSoInfoById(b2bThirdDelivery.getSoId());
            if (Objects.nonNull(soInfo)) {
                List<SoDetailEntity> soDetails = soInfoFeign.listSoDetailByMainId(soInfo.getId());
                if (!soDetails.isEmpty()) {
                    SoDetailEntity soDetail = soDetails.get(0);
                    BigDecimal exchangeRate = soDetail.getExchangeRate();
                    if (exchangeRate.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal cnAmount = MathUtil.multiplyWithTwo(soInfo.getOrderAmount(), exchangeRate);
                        exchangeRate = dmpTaskFeign.getRate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), CurrencyEnum.USD.getCurrencyCode());
                        BigDecimal usAmount = cnAmount.divide(exchangeRate,2, RoundingMode.HALF_UP);
                        overseasOutboundCreateRequest.setInsurePrice(usAmount);
                    }
                }
            }

        }

        for (ThirdWarehouseCreateFbaOutboundReq.Item item : items) {
            if (item.getSkuId() != null) {
                skuIds.add(item.getSkuId());
            }
        }

        int totalQuantity = items.stream()
                .mapToInt(item -> {
                    Integer boxQty = item.getBoxQty() != null ? item.getBoxQty() : 0;
                    Integer perBoxQty = item.getPerBoxQty() != null ? item.getPerBoxQty() : 0;
                    return boxQty * perBoxQty;
                })
                .sum();

        // 拣货类型
        if (skuIds.size() == 1 && totalQuantity == 1) {
//            // 只有一个SKU
//            if (totalQuantity == 1) {
//                //一票一件
//                overseasOutboundCreateRequest.setPickType(1);
//            } else {
//                //一票一件多个
//                overseasOutboundCreateRequest.setPickType(2);
//            }
            overseasOutboundCreateRequest.setPickType(1);
        } else {
            //一票多件
            overseasOutboundCreateRequest.setPickType(3);
        }

        //仓库操作指令类型
        List<ThirdWarehouseCreateFbaOutboundReq.WarehouseOperationTypeDTO> warehouseOperationTypeDTOList =
                createOutboundReq.getWarehouseOperationTypeDTOList();

        //明细
        List<OverseasOutboundCreateRequest.ItemDTOs> itemDTOs = new ArrayList<>();
        for (ThirdWarehouseCreateFbaOutboundReq.Item item : createOutboundReq.getItems()) {
            OverseasOutboundCreateRequest.ItemDTOs itemDTO = new OverseasOutboundCreateRequest.ItemDTOs();
            itemDTO.setProductSku(item.getPlatformSkuNo());
            itemDTO.setQty(item.getBoxQty());
            itemDTO.setPlatformSku(item.getSkuNo());
            itemDTOs.add(itemDTO);
        }
        overseasOutboundCreateRequest.setItemDTOs(itemDTOs);

        //仓库操作指令
        OverseasOutboundCreateRequest.B2bDto b2bDto = new OverseasOutboundCreateRequest.B2bDto();
        if (!warehouseOperationTypeDTOList.isEmpty()) {
            for (ThirdWarehouseCreateFbaOutboundReq.WarehouseOperationTypeDTO warehouseOperationTypeDTO : warehouseOperationTypeDTOList) {
                if (StringUtils.isNotBlank(warehouseOperationTypeDTO.getWarehouseOperationType())
                        && StringUtils.isNotBlank(warehouseOperationTypeDTO.getOperationDesc())) {
                    String type = warehouseOperationTypeDTO.getWarehouseOperationType();
                    String desc = warehouseOperationTypeDTO.getOperationDesc();
                    try {
                        WarehouseOperationTypeEnum operationTypeEnum = WarehouseOperationTypeEnum.fromCode(type);
                        if (Objects.nonNull(operationTypeEnum)) {
                            // 根据枚举值设置 b2bDto 的不同属性
                            switch (operationTypeEnum) {
                                case IS_CHANGE_PACKAGE:
                                    b2bDto.setIsChangePackage(Integer.parseInt(WarehouseOperationTypeValueEnum.getValueByCode(desc)));
                                    break;
                                case CHANGE_BARCODE_TYPE:
                                    b2bDto.setChangeBarcodeType(Integer.parseInt(WarehouseOperationTypeValueEnum.getValueByCode(desc)));
                                    break;
                                case IS_COVER_BARCODE:
                                    b2bDto.setIsCoverBarcode(Integer.parseInt(WarehouseOperationTypeValueEnum.getValueByCode(desc)));
                                    break;
                                case CHANGE_SHIPPING_MARK_TYPE:
                                    b2bDto.setChangeShippingMarkType(Integer.parseInt(WarehouseOperationTypeValueEnum.getValueByCode(desc)));
                                    break;
                                case IS_COVER_SHIPPING_MARK:
                                    b2bDto.setIsCoverShippingMark(Integer.parseInt(WarehouseOperationTypeValueEnum.getValueByCode(desc)));
                                    break;
                                case IS_PALLET:
                                    b2bDto.setIsPallet(Integer.parseInt(WarehouseOperationTypeValueEnum.getValueByCode(desc)));
                                    break;
                                case IS_DOUBLE_PALLET:
                                    b2bDto.setIsDoublePallet(Integer.parseInt(WarehouseOperationTypeValueEnum.getValueByCode(desc)));
                                    break;
                                case IS_MIXED_PALLET:
                                    b2bDto.setIsMixedPallet(Integer.parseInt(WarehouseOperationTypeValueEnum.getValueByCode(desc)));
                                    break;
                                case PASTE_CARTON_MARK_TYPE:
                                    b2bDto.setPasteCartonMarkType(Integer.parseInt(WarehouseOperationTypeValueEnum.getValueByCode(desc)));
                                    break;
                                case IS_PALLET_SCHEME:
                                    b2bDto.setIsPalletScheme(Integer.parseInt(WarehouseOperationTypeValueEnum.getValueByCode(desc)));
                                    break;
                                case LIMIT_PLATE_NUM:
                                    b2bDto.setLimitPlateNum(Integer.parseInt(desc));
                                    break;
                                case LIMIT_PLATE_HEIGHT:
                                    b2bDto.setLimitPlateHeight(new BigDecimal(desc));
                                    break;
                                case LIMIT_PLATE_WEIGHT:
                                    b2bDto.setLimitPlateWeight(new BigDecimal(desc));
                                    break;
                                default:
                                    // 不处理未知枚举
                                    break;
                            }
                        }

                    } catch (IllegalArgumentException e) {
                        log.info("未知的仓库操作类型: {}",type);
                    }
                }
            }
        }
        overseasOutboundCreateRequest.setB2bDto(b2bDto);


        //附件
        List<OverseasOutboundCreateRequest.AttachmentOpenDTOs> attachments = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(createOutboundReq.getFileBase64())
                && CharSequenceUtil.isNotBlank(createOutboundReq.getFileUrl())) {
            String fileName = createOutboundReq.getFileName();
            if (CharSequenceUtil.isBlank(fileName)) {
                int lastSlashIndex = createOutboundReq.getFileUrl().lastIndexOf('/');
                fileName = createOutboundReq.getFileUrl().substring(lastSlashIndex + 1);
            }

            attachments.add(OverseasOutboundCreateRequest.AttachmentOpenDTOs.builder()
                    .attachmentType("OTHER")
                    .base64(createOutboundReq.getFileBase64())
                    .fileName(fileName)
                    .build());
        }
        overseasOutboundCreateRequest.setAttachmentOpenDTOs(attachments);

        return overseasOutboundCreateRequest;
    }
}
