package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelInboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateInboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCalculateFeeReq;
import com.erp.model.wms.dto.third.ThirdWarehouseCalculateFeeResponse;
import com.erp.model.wms.dto.third.ThirdWarehouseProductReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryFbaOutboundResponse;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryOutboundReq;
import com.erp.model.wms.dto.third.ThirdWarehouseQueryOutboundResponse;
import com.erp.model.wms.dto.third.ThirdWarehouseSkuResp;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadFileReq;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadFileResponse;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadHandoverFileReq;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadHandoverFileResponse;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadOrderLabelReq;
import com.erp.model.wms.dto.third.ThirdWarehouseUploadOrderLabelResponse;
import com.erp.model.wms.resolver.B2bThirdDeliveryStatusResolver;
import com.erp.model.wms.enums.OverseasInstockTypeEnum;
import com.erp.model.wms.enums.B2bThirdWarehouseCancelResultEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.handler.TongYouB2bOperationResolver;
import com.erp.server.wms.convert.TongYouCreateHbOutboundConverter;
import com.erp.server.wms.convert.TongYouCreateInboundConverter;
import com.erp.server.wms.convert.TongYouCreateOutboundConverter;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.sdk.wms.tongyou.dto.request.TongYouCreateHbOutboundReq;
import com.sdk.wms.tongyou.dto.request.TongYouCreateInboundReq;
import com.sdk.wms.tongyou.dto.request.TongYouCreateOutboundReq;
import com.sdk.wms.tongyou.dto.response.TongYouBaseResp;
import com.sdk.wms.tongyou.dto.response.TongYouCancelOutboundResp;
import com.sdk.wms.tongyou.dto.response.TongYouInboundResp;
import com.sdk.wms.tongyou.dto.response.TongYouOutboundResp;
import com.sdk.wms.tongyou.dto.response.TongYouQueryOutboundBillResp;
import com.sdk.wms.tongyou.dto.response.TongYouQueryOutboundResp;
import com.sdk.wms.tongyou.enums.TongYouOutboundStatusEnum;
import com.sdk.wms.tongyou.service.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通邮处理服务实现类
 * @author will
 * @date 2025/11/11 15:21
 */
@Slf4j
@Service
@Validated
public class TongYouHandlerServiceImpl extends AbstractThirdWarehouseHandler {
    private static final String TONGYOU_ORDER_NOT_EXISTS = "订单不存在";
    private static final String TONGYOU_ORDER_DELETED = "已删除";

    @Resource
    private TongYouService tongYouService;


    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.TONG_YOU;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return success();
    }

    @Override
    public ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        // 通邮推送需要默认ERP的头程发货单号-HH+MM+SS
        String timeFormatter = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        createInboundReq.setReceivingCode(CharSequenceUtil.format("{}_{}",createInboundReq.getReferenceNo(),timeFormatter));

        TongYouCreateInboundReq tongYouCreateInboundReq =  this.buildInboundDto(createInboundReq);
        TongYouBaseResp<TongYouInboundResp> TongYouInboundRespTongYouBaseResp = tongYouService.createInboundBill(tongYouCreateInboundReq);
        if(!isSuccess(TongYouInboundRespTongYouBaseResp.getError())){
            return failure(TongYouInboundRespTongYouBaseResp.getContent());
        }
        //通邮无单号返回直接给空字符串
        return success(createInboundReq.getReceivingCode());
    }


    /**
     * 处理request信息
     * @author will
     * @date 2025/11/17 17:35
     * @param createInboundReq
     * @return TongYouCreateInboundReq
     */
    private TongYouCreateInboundReq buildInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        TongYouCreateInboundReq request = new TongYouCreateInboundReq();
        //主表信息
        TongYouCreateInboundReq.AddDTO addDTO = TongYouCreateInboundConverter.INSTANCE.inboundToThird(createInboundReq);
        if (CharSequenceUtil.equals(createInboundReq.getReceivingType(), OverseasInstockTypeEnum.SELF_HEADWAY.getCode())) {
            addDTO.setJhfs("693");
            addDTO.setOrder_types("718");
        } else if (CharSequenceUtil.equals(createInboundReq.getReceivingType(), OverseasInstockTypeEnum.TRANSFER_AGENT.getCode())) {
            addDTO.setJhfs("692");
            addDTO.setOrder_types("717");
            addDTO.setTcck(createInboundReq.getTransitWarehouseCode());
            addDTO.setChqd(createInboundReq.getLogisticsChannel());
        }
        addDTO.setWaybill(createInboundReq.getReceivingCode());

        //明细信息
        List<TongYouCreateInboundReq.AddDetailDTO> addDetailDTOList = TongYouCreateInboundConverter.INSTANCE.inboundDetailToThird(createInboundReq.getItems());
        addDTO.setOrder_products(addDetailDTOList);
        request.setOrder_list(Collections.singletonList(addDTO));
        return request;
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        throw new ServiceException("该仓库入库单不允许修改，请取消入库单后重新创建");
    }

    @Override
    public ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        //通邮不支持直接取消，需要判断通邮入库单是否已取消状态，是则允许取消，否则不允许取消
        Map<String, Object> authJson = new HashMap<>();
        //密钥
        Object object = ThirdWarehouseContext.getAuthMap().get("appToken");
        authJson.put("token",ObjectUtil.isEmpty(object) ? "" : object.toString());
        authJson.put("waybill",cancelInboundReq.getReceivingCode());

        TongYouBaseResp<List<TongYouInboundResp>> resp = tongYouService.getInboundBill(authJson);
        if(!isSuccess(resp.getError())){
            //没有数据默认已取消
            if (CharSequenceUtil.equals("没有数据",resp.getContent())) {
                log.warn("通邮入库单{}未查询到数据，默认已取消",cancelInboundReq.getReceivingCode());
                return success();
            }
            throw new ServiceException("查询通邮入库单失败,"+resp.getContent());
        }
        if (CollUtil.isEmpty(resp.getData())) {
            return failure("未查询到对应通邮入库单信息");
        }
        TongYouInboundResp tongYouInboundResp = resp.getData().get(0);
        if (!CharSequenceUtil.equals(tongYouInboundResp.getStatus(),"7")) {
            throw new ServiceException("三方仓单据未取消，ERP不允许取消");
        }
        return success();
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return success(Collections.singletonList(new ThirdWarehouseCalculateFeeResponse()));
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return success(new ThirdWarehouseUploadFileResponse());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return success(new ThirdWarehouseUploadOrderLabelResponse());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return null;
    }

    @Override
    public ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        TongYouCreateOutboundReq TongYouCreateOutboundReq =  this.buildOutboundDto(createOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单请求:{}", JSONUtil.toJsonStr(TongYouCreateOutboundReq));
        TongYouBaseResp<TongYouOutboundResp> tongYouBaseResp = tongYouService.createOutboundBill(TongYouCreateOutboundReq);
        log.warn(getPlatForm().getName()+"创建出库单结果:{}", JSONUtil.toJsonStr(tongYouBaseResp));
        if(!isSuccess(tongYouBaseResp.getError())){
            return failure(tongYouBaseResp.getContent());
        }
        return success(ThirdWarehouseQueryOutboundResponse.builder().build());
    }

    /** 通邮查询接口仅支持单号；批量查询串行调用，限制单次数量避免任务拖死 */
    private static final int QUERY_FBA_OUTBOUND_MAX_SIZE = 50;

    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        TongYouCreateHbOutboundReq hbOutboundReq = buildHbOutboundDto(createOutboundReq);
        // 不打印完整请求体（含收件人/地址/联系方式/附件 URL）
        log.warn("{}创建B2B出库单请求, referenceNo={}, deliver_no={}",
                getPlatForm().getName(), createOutboundReq.getReferenceNo(), hbOutboundReq.getDeliver_no());
        TongYouBaseResp<TongYouOutboundResp> tongYouBaseResp = tongYouService.createHbOutboundBill(hbOutboundReq);
        log.warn("{}创建B2B出库单结果, referenceNo={}, error={}, content={}",
                getPlatForm().getName(), createOutboundReq.getReferenceNo(),
                tongYouBaseResp == null ? null : tongYouBaseResp.getError(),
                tongYouBaseResp == null ? null : tongYouBaseResp.getContent());
        if (tongYouBaseResp == null) {
            return failure("通邮创建出库单未收到有效响应");
        }
        if (!isSuccess(tongYouBaseResp.getError())) {
            return failure(CharSequenceUtil.blankToDefault(tongYouBaseResp.getContent(), "通邮创建出库单失败"));
        }
        return success(createOutboundReq.getReferenceNo());
    }

    private TongYouCreateHbOutboundReq buildHbOutboundDto(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        // 推送路径强校验，避免历史单/重试缺操作指令时静默按「否」推送
        TongYouB2bOperationResolver.validateRequiredOperations(createOutboundReq);
        Boolean relabel = TongYouB2bOperationResolver.resolveRelabel(createOutboundReq);
        Boolean mixedPacking = TongYouB2bOperationResolver.resolveMixedPacking(createOutboundReq);
        TongYouCreateHbOutboundReq request = TongYouCreateHbOutboundConverter.INSTANCE.toHbOutboundReq(createOutboundReq);
        request.setIs_hb(Boolean.TRUE.equals(relabel) ? "2" : "1");
        request.setIs_hz(Boolean.TRUE.equals(mixedPacking) ? "1" : "2");
        if (CollUtil.isNotEmpty(request.getDeliver_products())) {
            for (TongYouCreateHbOutboundReq.DeliverProductDTO product : request.getDeliver_products()) {
                if (product.getNums() != null) {
                    product.setNums(String.valueOf(product.getNums()));
                }
                // add_order_hb requires non-empty sku_news; when not relabeling, fallback to warehouse SKU
                if (CharSequenceUtil.isBlank(product.getSku_news())) {
                    product.setSku_news(CharSequenceUtil.blankToDefault(product.getSku(), ""));
                }
            }
        }
        return request;
    }

    /**
     * 海外仓出库数据格式化
     * @author will
     * @date 2025/11/21 16:57
     * @param createOutboundReq
     * @return TongYouCreateOutboundReq
     */
    private TongYouCreateOutboundReq buildOutboundDto(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        //主表信息
        TongYouCreateOutboundReq addDTO = TongYouCreateOutboundConverter.INSTANCE.outboundToThird(createOutboundReq,createOutboundReq.getReceiverInfo());
        if (CharSequenceUtil.equals(createOutboundReq.getIsApiSignName(),"是")) {
            addDTO.setQmfw("AdultSignature");
        } else {
            addDTO.setQmfw("NoSignature");
        }
        addDTO.setZip(createOutboundReq.getReceiverInfo().getZipCode());
        //收货人电话
        String phone = createOutboundReq.getReceiverInfo().getPhone();
        //买家电话
        String buyerNumber = createOutboundReq.getReceiverInfo().getBuyerNumber();
        addDTO.setMobile(StrUtil.isBlank(phone) ? buyerNumber : phone);
        addDTO.setPhone(StrUtil.isBlank(buyerNumber) ? phone : buyerNumber);
        //明细信息
        List<TongYouCreateOutboundReq.AddDetailDTO> addDetailDTOList = TongYouCreateOutboundConverter.INSTANCE.outboundDetailToThird(createOutboundReq.getItems());
        addDTO.setDeliver_products(addDetailDTOList);
        return addDTO;

    }

    @Override
    public ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        log.warn(getPlatForm().getName()+"取消出库单请求:{}", JSONUtil.toJsonStr(cancelOutboundReq));
        TongYouCancelOutboundResp tongYouBaseResp = tongYouService.cancelOutboundBill(cancelOutboundReq);
        log.warn(getPlatForm().getName()+"取消出库单结果:{}", JSONUtil.toJsonStr(tongYouBaseResp));
        if (tongYouBaseResp == null) {
            return failure("通邮取消出库单未收到有效响应");
        }
        if(!isSuccess(tongYouBaseResp.getError())){
            return failure(CharSequenceUtil.blankToDefault(tongYouBaseResp.getContent(), "通邮取消出库单失败"));
        }
        return resolveCancelOutboundResult(cancelOutboundReq, tongYouBaseResp);

    }

    private ApiResult<String> resolveCancelOutboundResult(ThirdWarehouseCancelOutboundReq cancelOutboundReq,
                                                          TongYouCancelOutboundResp cancelResp) {
        if (Boolean.TRUE.equals(cancelOutboundReq.getConfirmInterceptResult())) {
            return resolveInterceptCancelOutboundResult(cancelOutboundReq, cancelResp);
        }

        String erpOrderCode = cancelOutboundReq.getErpOrderCode();
        if (containsCancelOrder(cancelResp.getSuccArray(), erpOrderCode)) {
            return success(CharSequenceUtil.format("通邮取消出库单成功：{}", erpOrderCode),
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }

        String errorMsg = getCancelErrorMessage(cancelResp.getErrorArray(), erpOrderCode);
        if (CharSequenceUtil.isNotBlank(errorMsg)) {
            if (isCancelDeletedResult(errorMsg)) {
                return success(CharSequenceUtil.format("通邮取消出库单成功，通邮返回出库单不存在或已删除：{}", errorMsg),
                        ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
            }
            return success(CharSequenceUtil.format("通邮取消出库单失败：{}", errorMsg),
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }

        if (ObjectUtil.isNotEmpty(cancelResp.getSuccArray()) || ObjectUtil.isNotEmpty(cancelResp.getErrorArray())) {
            return success(CharSequenceUtil.format("通邮取消接口已返回处理明细，但未包含当前出库单{}，按拦截中处理", erpOrderCode),
                    ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
        }

        // 通邮取消接口的“请求成功”只代表请求被接收；响应未带明细时，再查出库单状态兜底确认。
        TongYouQueryOutboundBillResp queryResp;
        try {
            queryResp = queryOutboundAfterCancel(cancelOutboundReq);
        } catch (Exception e) {
            log.warn(getPlatForm().getName() + "取消后查询出库单异常, erpOrderCode:{}", erpOrderCode, e);
            return success("通邮取消请求已受理，取消后查询异常，按拦截中处理",
                    ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
        }
        log.warn(getPlatForm().getName()+"取消后查询出库单结果:{}", JSONUtil.toJsonStr(queryResp));
        if (ObjectUtil.isEmpty(queryResp)) {
            return success("通邮取消请求已受理，取消后查询无返回，按拦截中处理",
                    ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
        }
        String queryErrorMsg = getCancelErrorMessage(queryResp.getErrorArray(), erpOrderCode);
        if (CharSequenceUtil.isNotBlank(queryErrorMsg)) {
            if (isCancelDeletedResult(queryErrorMsg)) {
                return success(CharSequenceUtil.format("通邮取消请求已受理，取消后查询返回出库单不存在或已删除：{}",
                                queryErrorMsg),
                        ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
            }
            return success(CharSequenceUtil.format("通邮取消请求已受理，取消后查询返回异常明细，按拦截中处理：{}",
                            queryErrorMsg),
                    ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
        }
        if(!isSuccess(queryResp.getError())){
            return success(CharSequenceUtil.format("通邮取消请求已受理，取消后查询失败，按拦截中处理：{}",
                            CharSequenceUtil.blankToDefault(queryResp.getContent(), getCancelResultText(cancelResp))),
                    ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
        }
        if (CollUtil.isEmpty(queryResp.getData())) {
            return success(CharSequenceUtil.format("通邮取消请求已受理，取消后查询无出库单，按拦截成功处理：{}",
                            getCancelResultText(cancelResp)),
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }

        TongYouQueryOutboundResp outboundResp = queryResp.getData().get(0);
        String status = ObjectUtil.isEmpty(outboundResp) ? "" : CharSequenceUtil.trim(outboundResp.getPb());
        String statusText = getOutboundStatusText(status);
        if (isShippedOutboundStatus(status)) {
            return success(CharSequenceUtil.format("通邮取消请求已受理，取消后出库单状态为{}，按拦截失败处理", statusText),
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        return success(CharSequenceUtil.format("通邮取消请求已受理，取消后出库单状态仍为{}，按拦截中处理", statusText),
                ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
    }

    private ApiResult<String> resolveInterceptCancelOutboundResult(ThirdWarehouseCancelOutboundReq cancelOutboundReq,
                                                                   TongYouCancelOutboundResp cancelResp) {
        TongYouQueryOutboundBillResp queryResp;
        try {
            queryResp = queryOutboundAfterCancel(cancelOutboundReq, true);
        } catch (Exception e) {
            log.warn(getPlatForm().getName() + "发货拦截后查询出库单异常, erpOrderCode:{}", cancelOutboundReq.getErpOrderCode(), e);
            return success("通邮取消请求已受理，按order_status=7查询拦截结果异常，按拦截失败处理",
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        log.warn(getPlatForm().getName()+"发货拦截后查询出库单结果:{}", JSONUtil.toJsonStr(queryResp));
        if (ObjectUtil.isEmpty(queryResp)) {
            return success("通邮取消请求已受理，按order_status=7查询拦截结果无返回，按拦截失败处理",
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        if(!isSuccess(queryResp.getError())){
            return success(CharSequenceUtil.format("通邮取消请求已受理，按order_status=7查询拦截结果失败，按拦截失败处理：{}",
                            CharSequenceUtil.blankToDefault(queryResp.getContent(), getCancelResultText(cancelResp))),
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        if (CollUtil.isEmpty(queryResp.getData())) {
            return success(CharSequenceUtil.format("通邮取消请求已受理，按order_status=7查询无出库单，按拦截失败处理：{}",
                            getCancelResultText(cancelResp)),
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        if (hasInterceptedOutbound(queryResp.getData())) {
            return success(CharSequenceUtil.format("通邮取消请求已受理，查询返回PB=7，按拦截成功处理：{}",
                            cancelOutboundReq.getErpOrderCode()),
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }

        String statusText = getOutboundStatusText(CharSequenceUtil.trim(queryResp.getData().get(0).getPb()));
        return success(CharSequenceUtil.format("通邮取消请求已受理，查询未返回PB=7，当前状态为{}，按拦截失败处理", statusText),
                ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
    }

    private TongYouQueryOutboundBillResp queryOutboundAfterCancel(ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        return queryOutboundAfterCancel(cancelOutboundReq, false);
    }

    private TongYouQueryOutboundBillResp queryOutboundAfterCancel(ThirdWarehouseCancelOutboundReq cancelOutboundReq,
                                                                  boolean queryInterceptStatus) {
        Map<String, Object> authJson = new HashMap<>();
        Object object = ThirdWarehouseContext.getAuthMap().get("appToken");
        authJson.put("token",ObjectUtil.isEmpty(object) ? "" : object.toString());
        authJson.put("deliver_no",cancelOutboundReq.getErpOrderCode());
        // 通邮拦截结果不能用取消接口受理响应判断，拦截场景必须带 order_status=7 查询确认返回PB=7。
        if (queryInterceptStatus) {
            authJson.put("order_status", TongYouOutboundStatusEnum.INTERCEPTED.getCode());
        }
        return tongYouService.getOutboundBill(authJson);
    }

    private boolean hasInterceptedOutbound(List<TongYouQueryOutboundResp> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return false;
        }
        return dataList.stream()
                .map(TongYouQueryOutboundResp::getPb)
                .filter(CharSequenceUtil::isNotBlank)
                .map(CharSequenceUtil::trim)
                .anyMatch(status -> TongYouOutboundStatusEnum.INTERCEPTED.getCode().equals(status));
    }

    private boolean isCancelDeletedResult(String errorMsg) {
        // 通邮查询接口在已取消/已删除场景按文案返回，当前无独立错误码可用。
        return containsAny(errorMsg, TONGYOU_ORDER_NOT_EXISTS, TONGYOU_ORDER_DELETED);
    }

    private String getCancelResultText(TongYouBaseResp<String> cancelResp) {
        if (ObjectUtil.isEmpty(cancelResp)) {
            return "取消接口请求成功";
        }
        String content = CharSequenceUtil.trim(cancelResp.getContent());
        String data = CharSequenceUtil.trim(cancelResp.getData());
        return CharSequenceUtil.blankToDefault(CharSequenceUtil.blankToDefault(content, data), "取消接口请求成功");
    }

    private String getOutboundStatusText(String status) {
        return TongYouOutboundStatusEnum.getDisplayText(status);
    }

    private boolean isShippedOutboundStatus(String status) {
        return TongYouOutboundStatusEnum.isShippedStatus(status);
    }

    private boolean containsCancelOrder(Object result, String orderCode) {
        if (ObjectUtil.isEmpty(result) || CharSequenceUtil.isBlank(orderCode)) {
            return false;
        }
        if (result instanceof Map) {
            Map<?, ?> resultMap = (Map<?, ?>) result;
            return resultMap.containsKey(orderCode) || resultMap.containsValue(orderCode);
        }
        if (result instanceof Collection) {
            Collection<?> resultList = (Collection<?>) result;
            for (Object item : resultList) {
                if (containsCancelOrder(item, orderCode)) {
                    return true;
                }
            }
            return false;
        }
        return CharSequenceUtil.contains(JSONUtil.toJsonStr(result), orderCode);
    }

    private String getCancelErrorMessage(Object errorArray, String orderCode) {
        if (ObjectUtil.isEmpty(errorArray) || CharSequenceUtil.isBlank(orderCode)) {
            return "";
        }
        if (errorArray instanceof Map) {
            Object errorMsg = ((Map<?, ?>) errorArray).get(orderCode);
            if (ObjectUtil.isNotEmpty(errorMsg)) {
                return String.valueOf(errorMsg);
            }
        }
        if (containsCancelOrder(errorArray, orderCode)) {
            return JSONUtil.toJsonStr(errorArray);
        }
        return "";
    }

    private boolean containsAny(String value, String... keywords) {
        if (CharSequenceUtil.isBlank(value)) {
            return false;
        }
        for (String keyword : keywords) {
            if (CharSequenceUtil.contains(value, keyword)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        // B2B 拦截走 cancelFba → 复用通邮取消出库；须带齐 auth 与单号，否则 @Valid 在 SDK 层直接失败
        ThirdWarehouseCancelOutboundReq cancelReq = new ThirdWarehouseCancelOutboundReq();
        cancelReq.setAuthId(cancelOutboundReq.getAuthId());
        cancelReq.setThirdWarehouseProvideCode(cancelOutboundReq.getThirdWarehouseProvideCode());
        cancelReq.setErpOrderCode(CharSequenceUtil.blankToDefault(cancelOutboundReq.getErpOrderCode(), cancelOutboundReq.getOrderCode()));
        cancelReq.setOrderCode(CharSequenceUtil.blankToDefault(cancelOutboundReq.getOrderCode(), cancelOutboundReq.getErpOrderCode()));
        cancelReq.setOwnerCode(cancelOutboundReq.getOwnerCode());
        cancelReq.setReason(CharSequenceUtil.blankToDefault(cancelOutboundReq.getReason(), cancelOutboundReq.getRemark()));
        ApiResult<String> cancelResult = cancelOutboundBill(cancelReq);
        if (!cancelResult.isSuccess()) {
            return failure(cancelResult.getMsg());
        }
        String resultCode = cancelResult.getData();
        if (ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode().equals(resultCode)) {
            return success(B2bThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }
        if (ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode().equals(resultCode)) {
            return success(B2bThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        return success(B2bThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq){
        Map<String, Object> authJson = new HashMap<>();
        //密钥
        Object object = ThirdWarehouseContext.getAuthMap().get("appToken");
        authJson.put("token",ObjectUtil.isEmpty(object) ? "" : object.toString());
        authJson.put("deliver_no",queryOutboundReq.getErpOrderCode());

        log.warn(getPlatForm().getName()+"查询出库单请求:{}", JSONUtil.toJsonStr(authJson));
        TongYouBaseResp<List<TongYouQueryOutboundResp>> tongYouBaseResp = tongYouService.getOutboundBill(authJson);
        log.warn(getPlatForm().getName()+"查询出库单结果:{}", JSONUtil.toJsonStr(tongYouBaseResp));
        if(!isSuccess(tongYouBaseResp.getError())){
            return failure(tongYouBaseResp.getContent());
        }
        if (CollUtil.isEmpty(tongYouBaseResp.getData())) {
            return failure("未查询到对应通邮出库单信息");
        }
        return success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(tongYouBaseResp.getData().get(0).getWaybill()).build());
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(@Valid ThirdWarehouseQueryFbaOutboundReq req) {
        if (CollUtil.isEmpty(req.getErpOrderCodeList())) {
            return success(Collections.emptyList());
        }
        // 通邮 hwc_order.php 仅支持单号查询；去重后限流，失败写入 errorReason 不静默丢弃
        Set<String> orderCodes = new LinkedHashSet<>();
        for (String erpOrderCode : req.getErpOrderCodeList()) {
            if (CharSequenceUtil.isNotBlank(erpOrderCode)) {
                orderCodes.add(erpOrderCode.trim());
            }
        }
        if (orderCodes.isEmpty()) {
            return success(Collections.emptyList());
        }
        if (orderCodes.size() > QUERY_FBA_OUTBOUND_MAX_SIZE) {
            return failure("通邮批量查询出库单单次最多支持" + QUERY_FBA_OUTBOUND_MAX_SIZE + "条");
        }
        List<ThirdWarehouseQueryFbaOutboundResponse> resultList = new ArrayList<>(orderCodes.size());
        Object tokenObj = ThirdWarehouseContext.getAuthMap().get("appToken");
        String token = ObjectUtil.isEmpty(tokenObj) ? "" : tokenObj.toString();
        for (String erpOrderCode : orderCodes) {
            Map<String, Object> authJson = new HashMap<>(4);
            authJson.put("token", token);
            authJson.put("deliver_no", erpOrderCode);
            TongYouQueryOutboundBillResp queryResp;
            try {
                queryResp = tongYouService.getOutboundBill(authJson);
            } catch (Exception e) {
                log.warn("{}查询B2B出库单异常, deliver_no={}", getPlatForm().getName(), erpOrderCode, e);
                resultList.add(buildQueryFbaFailResponse(erpOrderCode, "通邮查询出库单异常"));
                continue;
            }
            if (ObjectUtil.isEmpty(queryResp) || !isSuccess(queryResp.getError()) || CollUtil.isEmpty(queryResp.getData())) {
                String failMsg = ObjectUtil.isEmpty(queryResp)
                        ? "通邮查询出库单未收到有效响应"
                        : CharSequenceUtil.blankToDefault(queryResp.getContent(), "未查询到对应通邮出库单信息");
                log.warn("{}查询B2B出库单失败, deliver_no={}, msg={}", getPlatForm().getName(), erpOrderCode, failMsg);
                resultList.add(buildQueryFbaFailResponse(erpOrderCode, failMsg));
                continue;
            }
            TongYouQueryOutboundResp outboundResp = queryResp.getData().get(0);
            ThirdWarehouseQueryFbaOutboundResponse response = new ThirdWarehouseQueryFbaOutboundResponse();
            response.setCode(CharSequenceUtil.blankToDefault(outboundResp.getDeliver_no(), erpOrderCode));
            response.setPlatformOrderCode(CharSequenceUtil.blankToDefault(outboundResp.getDeliver_no(), erpOrderCode));
            response.setTrackNo(outboundResp.getWaybill());
            response.setPlatformOriginalStatus(outboundResp.getPb());
            response.setStatus(B2bThirdDeliveryStatusResolver.resolveErpStatus(PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode(), outboundResp.getPb()));
            response.setDeliveryTimeStr(outboundResp.getCk_time());
            response.setPlatform(PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode());
            resultList.add(response);
        }
        return success(resultList);
    }

    /**
     * 组装通邮 B2B 出库单查询失败占位结果。
     * <p>不写 platformOrderCode：上游 createFbaOutboundBill 会把非空平台单号当成「通邮已有单」而跳过真实创建。
     *
     * @param erpOrderCode ERP 单号（deliver_no）
     * @param errorReason  失败原因
     * @return 仅含 code / errorReason 的失败项
     */
    private ThirdWarehouseQueryFbaOutboundResponse buildQueryFbaFailResponse(String erpOrderCode, String errorReason) {
        ThirdWarehouseQueryFbaOutboundResponse response = new ThirdWarehouseQueryFbaOutboundResponse();
        response.setCode(erpOrderCode);
        response.setPlatform(PlatformDictEnum.TONG_YOU_WAREHOUSE.getCode());
        response.setErrorReason(errorReason);
        return response;
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        dto.getAuthJson().put("token",dto.getAuthJson().get("appToken"));
        TongYouBaseResp<String> response = tongYouService.getWarehouse(dto.getAuthJson());
        if(!isSuccess(response.getError())){
            throw new ServiceException("授权失败,"+response.getContent());
        }
        return true;
    }

    public boolean isSuccess(String code){
        return CharSequenceUtil.equals(code,"T");
    }

}
