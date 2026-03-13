package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.B2bThirdWarehouseCancelResultEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.wms.convert.OverseasWarehouseInboundConverter;
import com.erp.server.wms.convert.ThirdWarehouseConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.goodcang.dto.request.*;
import com.sdk.wms.goodcang.dto.response.*;
import com.sdk.wms.goodcang.service.GoodCangService;
import com.sdk.wms.zhongbao.dto.request.*;
import com.sdk.wms.zhongbao.dto.response.BaseResponse;
import com.sdk.wms.zhongbao.dto.response.OverseasOutboundCancelResponse;
import com.sdk.wms.zhongbao.dto.response.OverseasOutboundCreateResponse;
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
    private GoodCangService goodCangService;

    @Resource
    private ZhongbaoService zhongbaoService;
    @Resource
    private FileFeign fileFeign;

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
        GoodCangGetSkuReq goodCangGetSkuReq = GoodCangGetSkuReq.builder()
                .page(1)
                .pageSize(100)
                .productSkuArr(productReq.getSkuNoList())
                .build();
        List<GoodCangSkuResp> respList = new ArrayList<>();
        int page = 1;
        while (true) {
            goodCangGetSkuReq.setPage(page);
            GoodCangResponse<List<GoodCangSkuResp>> goodCangResponse = goodCangService.getSkuList(goodCangGetSkuReq);
            if (!isSuccess(goodCangResponse.getAsk(), "")) {
                log.error("谷仓查询产品信息异常" + goodCangResponse);
                return failure(goodCangResponse.getMessage());
            }
            respList.addAll(goodCangResponse.getData());
            if (goodCangResponse.getCount() <= page * 100) {
                break;
            }
            page++;
        }
        List<ThirdWarehouseSkuResp> thirdWarehouseSkuRespList = BeanUtil.copyToList(respList, ThirdWarehouseSkuResp.class);
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
        GoodCangCalculateDeliveryFeeReq goodCangCalculateDeliveryFeeReq = ThirdWarehouseConverter.INSTANCE.reqToGucangCalculateFeeReq(calculateFeeReq);
        GoodCangResponse<List<GoodCangCalculateDeliveryFeeResp>> response = goodCangService.getCalculateDeliveryFee(goodCangCalculateDeliveryFeeReq);
        List<GoodCangCalculateDeliveryFeeResp> goodCangCalculateDeliveryFeeRespList = response.getData();
        String currency = response.getCurrency();
        List<ThirdWarehouseCalculateFeeResponse> dataList = convertCalculateDeliveryFeeResp(currency, goodCangCalculateDeliveryFeeRespList);
        return isSuccess(response.getAsk(), "") ? success(dataList) : failure(response.getMessage());
    }

    private List<ThirdWarehouseCalculateFeeResponse> convertCalculateDeliveryFeeResp(String currency, List<GoodCangCalculateDeliveryFeeResp> goodCangCalculateDeliveryFeeRespList) {
        if (CollUtil.isEmpty(goodCangCalculateDeliveryFeeRespList)) {
            return Collections.emptyList();
        }
        List<ThirdWarehouseCalculateFeeResponse> list = new ArrayList<>();
        for (GoodCangCalculateDeliveryFeeResp resp : goodCangCalculateDeliveryFeeRespList) {
            ThirdWarehouseCalculateFeeResponse response = ThirdWarehouseConverter.INSTANCE.gucangResToThirdWarehouseResponse(resp);
            response.setCurrency(currency);
            List<GoodCangCalculateDeliveryFeeResp.Income> income = resp.getIncome();
            //设置其他费用
            setOtherCostByIncome(response, income);
            list.add(response);
        }
        return list;
    }

    private void setOtherCostByIncome(ThirdWarehouseCalculateFeeResponse response, List<GoodCangCalculateDeliveryFeeResp.Income> income) {
        if (CollUtil.isEmpty(income)) {
            response.setShippingCost(BigDecimal.ZERO);
            response.setDeclareCost(BigDecimal.ZERO);
            response.setOtherCost(BigDecimal.ZERO);
            response.setRegistrationCost(BigDecimal.ZERO);
            response.setOperatingCost(BigDecimal.ZERO);
        } else {
            for (GoodCangCalculateDeliveryFeeResp.Income cost : income) {
                if (cost.getName().contains("运输费")) {
                    response.setShippingCost(new BigDecimal(cost.getAmount()));
                } else if (cost.getName().contains("关税") || cost.getName().contains("报关费") || cost.getName().contains("偏远住宅费") || cost.getName().contains("附加费")) {
                    BigDecimal amount = new BigDecimal(cost.getAmount());
                    BigDecimal declareCost = Objects.nonNull(response.getDeclareCost()) ? response.getDeclareCost() : BigDecimal.ZERO;
                    response.setDeclareCost(MathUtil.add(amount, declareCost));
                } else if (cost.getName().contains("操作费")) {
                    response.setOperatingCost(new BigDecimal(cost.getAmount()));
                }
            }
        }
    }

    @Override
    public ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        OutboundB2cCreateRequest createRequest = OverseasWarehouseInboundConverter.INSTANCE.b2coutboundDtoToZhongbao(createOutboundReq);
        //设置拣货类型
        setPickType(createOutboundReq, createRequest);
        //设置附件
        setAttachment(createOutboundReq, createRequest);
        log.warn(getPlatForm().getName() + "创建出库单请求:{}", JSONUtil.toJsonStr(createRequest));
        BaseResponse<OutboundB2cCreateResponse> response = zhongbaoService.createB2cOutboundBill(createRequest);
        log.warn(getPlatForm().getName() + "创建出库单结果:{}", JSONUtil.toJsonStr(response));
//        if (response.getMessage().contains("参考号重复")) {
//            GoodCangResponse<String> orderCode = goodCangService.getOutboundCode(createOutboundReq.getReferenceNo());
//            return success(orderCode.getData());
//        }
        return response.getSuccess() ? success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getData().getOrderNo()).trackNo(response.getData().getTrackingNo()).build()) : failure(response.getMessage());
    }

    private void setAttachment(ThirdWarehouseCreateOutboundReq createOutboundReq, OutboundB2cCreateRequest createRequest) {
        List<OutboundB2cCreateRequest.Attachment> attachments = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(createOutboundReq.getLabelData())) {
            attachments.add(OutboundB2cCreateRequest.Attachment.builder().base64(createOutboundReq.getLabelData()).fileName(createOutboundReq.getReferenceNo()+"面单.pdf").build());
        }
        if (CharSequenceUtil.isNotBlank(createOutboundReq.getInvoiceData())) {
            attachments.add(OutboundB2cCreateRequest.Attachment.builder().base64(createOutboundReq.getInvoiceData()).fileName(createOutboundReq.getReferenceNo()+"发票.pdf").build());
        }
        createRequest.setAttachmentOpenDTOs(attachments);
    }

    private static void setPickType(ThirdWarehouseCreateOutboundReq createOutboundReq, OutboundB2cCreateRequest createRequest) {
        List<ThirdWarehouseCreateOutboundReq.Item> items = createOutboundReq.getItems();
        /**
         * 订单只有一个SKU，数量为1时：1=>一票一件
         * 订单只有一个SKU，数量大于1时：2=>一票一件多个
         * 订单SKU大于1个时：3=>一票多件
         */
        if (CollUtil.isEmpty(items)) {
            createRequest.setPickType(1);
        } else if (items.size() == 1 && items.get(0).getQuantity() == 1) {
            createRequest.setPickType(1);
        } else if (items.size() == 1 && items.get(0).getQuantity() > 1) {
            createRequest.setPickType(2);
        } else {
            createRequest.setPickType(3);
        }
    }

    @Override
    public ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(@Valid ThirdWarehouseUploadFileReq uploadFileReq) {
        GoodCangUploadFileReq goodCangUploadFileReq = ThirdWarehouseConverter.INSTANCE.reqToGoodCangUploadFileReq(uploadFileReq);
        if (CharSequenceUtil.isNotBlank(uploadFileReq.getFileType())) {
            goodCangUploadFileReq.setUseFor(uploadFileReq.getFileType());
        }
        GoodCangResponse<GoodCangUploadFileResp> response = goodCangService.uploadFile(goodCangUploadFileReq);
        GoodCangUploadFileResp goodCangUploadFileResp = response.getData();
        ThirdWarehouseUploadFileResponse resToThirdWarehouseResponse = ThirdWarehouseConverter.INSTANCE.goodCangResToThirdWarehouseUploadFileResponse(goodCangUploadFileResp);
        return isSuccess(response.getAsk(), response.getMessage()) ? success(resToThirdWarehouseResponse) : failure(response.getMessage());

    }

    @Override
    public ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(@Valid ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        GoodCangUploadOrderLabelReq goodCangUploadFileReq = ThirdWarehouseConverter.INSTANCE.reqToGoodCangUploadOrderLabelReq(uploadFileReq);
        GoodCangResponse<GoodCangUploadOrderLabelResp> response = goodCangService.uploadOrderLabel(goodCangUploadFileReq);
        GoodCangUploadOrderLabelResp resp = response.getData();
        ThirdWarehouseUploadOrderLabelResponse uploadOrderLabelResponse = ThirdWarehouseConverter.INSTANCE.googCangResToThirdWarehouseUploadOrderLabelResponse(resp);
        if (response.getMessage().contains("订单状态已确认")) {
            return success(new ThirdWarehouseUploadOrderLabelResponse(uploadFileReq.getOrderCode()));
        }
        return isSuccess(response.getAsk(), response.getMessage()) ? success(uploadOrderLabelResponse) : failure(response.getMessage());

    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        GoodCangResponse<String> response = goodCangService.cancelOutboundBill(cancelOutboundReq.getOrderCode(), cancelOutboundReq.getReason());
        if (Objects.isNull(response.getCancelStatus())) {
            return failure(response.getMessage());
        }
        if (response.getCancelStatus().equals(3)) {
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        OverseasOutboundCancelRequest overseasOutboundCancelRequest = new OverseasOutboundCancelRequest();
        String token = AuthUtils.getToken(apiKey, apiSecret);
        overseasOutboundCancelRequest.setCancelRemark(cancelOutboundReq.getRemark());
        overseasOutboundCancelRequest.setOrderNos(Collections.singletonList(cancelOutboundReq.getErpOrderCode()));
        log.warn(getPlatForm().getName() + "取消出库单请求:{}", JSONUtil.toJsonStr(overseasOutboundCancelRequest));
        BaseResponse<OverseasOutboundCancelResponse> response = zhongbaoService.cancelOutboundBill(token, overseasOutboundCancelRequest);
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
        BaseResponse<List<OutboundB2cQueryResponse>> response = zhongbaoService.queryB2cOutboundBill(queryRequest);
        return response.getSuccess() ?
                success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(response.getData().get(0).getOrderNo()).trackNo(response.getData().get(0).getTrackingNo()).build())
                : failure(response.getMessage() + ":" + String.join(", ", response.getErrors()));
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        OverseasOutboundQueryRequest overseasOutboundQueryRequest = new OverseasOutboundQueryRequest();
        List<ThirdWarehouseQueryFbaOutboundResponse> thirdWarehouseQueryFbaOutboundResponses = new ArrayList<>();
        //一个半小时到现在的订单
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar now = Calendar.getInstance();
        String endUpdateTime = sdf.format(now.getTime());
        Calendar oneAndHalfHourAgo = Calendar.getInstance();
        oneAndHalfHourAgo.add(Calendar.MINUTE, -90);
        String startUpdateTime = sdf.format(oneAndHalfHourAgo.getTime());
        overseasOutboundQueryRequest.setStartUpdateTime(startUpdateTime);
        overseasOutboundQueryRequest.setEndUpdateTime(endUpdateTime);

        String token = AuthUtils.getToken(apiKey, apiSecret);
        log.warn(getPlatForm().getName() + "创建b2b出库单请求:{}", JSONUtil.toJsonStr(overseasOutboundQueryRequest));
        BaseResponse<OverseasOutboundQueryResponse> response = zhongbaoService.queryOutboundBill(token, overseasOutboundQueryRequest);
        log.warn(getPlatForm().getName() + "创建b2b出库单结果:{}", JSONUtil.toJsonStr(response));
        if (response.getData().getCode().equals("20000")  && !response.getData().getResponseData().getList().isEmpty()) {
            for (OverseasOutboundQueryResponse.DataList dataList : response.getData().getResponseData().getList()) {
                ThirdWarehouseQueryFbaOutboundResponse thirdWarehouseQueryFbaOutboundResponse = new ThirdWarehouseQueryFbaOutboundResponse();
                thirdWarehouseQueryFbaOutboundResponse.setCode(dataList.getOrderNo());
                thirdWarehouseQueryFbaOutboundResponse.setTrackNo(dataList.getTrackingNo());
                thirdWarehouseQueryFbaOutboundResponse.setStatus(dataList.getStatus().toString());
                thirdWarehouseQueryFbaOutboundResponse.setErrorReason(dataList.getErrorReason());
                thirdWarehouseQueryFbaOutboundResponses.add(thirdWarehouseQueryFbaOutboundResponse);
            }
        }

        return !thirdWarehouseQueryFbaOutboundResponses.isEmpty() ? success(thirdWarehouseQueryFbaOutboundResponses) : failure(response.getMessage());
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
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        OverseasOutboundCreateRequest overseasOutboundCreateRequest = buildCreateFbaOutboundDto(createOutboundReq);
        String token = AuthUtils.getToken(apiKey, apiSecret);
        log.warn(getPlatForm().getName() + "创建b2b出库单请求:{}", JSONUtil.toJsonStr(overseasOutboundCreateRequest));
        BaseResponse<OverseasOutboundCreateResponse> response = zhongbaoService.createOutboundBill(token, overseasOutboundCreateRequest);
        log.warn(getPlatForm().getName() + "创建b2b出库单结果:{}", JSONUtil.toJsonStr(response));
        return response.getSuccess() ? success(response.getData().getResponseData().getOrderNo()) : failure(response.getMessage());
    }

    public OverseasOutboundCreateRequest buildCreateFbaOutboundDto(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        OverseasOutboundCreateRequest overseasOutboundCreateRequest = OverseasWarehouseInboundConverter.INSTANCE.outboundDtoToZhongBao(createOutboundReq);
        List<ThirdWarehouseCreateFbaOutboundReq.Item> items = createOutboundReq.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("订单明细不能为空");
        }

        Set<String> skuIds = new HashSet<>();
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
        if (skuIds.size() == 1) {
            // 只有一个SKU
            if (totalQuantity == 1) {
                //一票一件
                overseasOutboundCreateRequest.setPickType(3);
            } else {
                //一票一件多个
                overseasOutboundCreateRequest.setPickType(2);
            }
        } else {
            //一票多件
            overseasOutboundCreateRequest.setPickType(3);
        }
        List<OverseasOutboundCreateRequest.ItemDTOs> itemDTOs = new ArrayList<>();
        for (ThirdWarehouseCreateFbaOutboundReq.Item item : createOutboundReq.getItems()) {
            OverseasOutboundCreateRequest.ItemDTOs itemDTO = new OverseasOutboundCreateRequest.ItemDTOs();
            itemDTO.setProductSku(item.getSkuNo());
            itemDTO.setQty(item.getBoxQty());
            itemDTO.setPlatformSku(item.getPlatformSkuNo());
            itemDTOs.add(itemDTO);
        }

        overseasOutboundCreateRequest.setItemDTOs(itemDTOs);

        return overseasOutboundCreateRequest;
    }

    public boolean isSuccess(String ask, String message) {
        return "Success".equals(ask) || "success".equals(message);
    }
}
