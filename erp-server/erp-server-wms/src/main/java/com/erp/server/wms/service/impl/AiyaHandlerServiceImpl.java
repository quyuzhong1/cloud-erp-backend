package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AiyaInboundCancelDTO;
import com.erp.model.wms.dto.AiyaInboundSaveDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.erp.server.wms.service.FirstMileDeliveryService;
import com.erp.server.wms.service.WmsCartonDetailService;
import com.sdk.wms.aiya.service.AiyaOpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AIYA（爱亚/百世 GLINK）三方海外仓处理服务实现类。
 * <p>
 * 仅在 {@link OmsPlatformEnum#AI_YA} 渠道下生效，入库单创建/修改统一调用爱亚
 * {@code GLINK_CREATE_ASN_NOTIFY} 接口（创建/修改合一，按 {@code asnNumber} 幂等 upsert）。
 * 结构参照 {@link WegoHandlerServiceImpl}：授权信息取自 {@link ThirdWarehouseContext#getAuthMap()}，
 * 入库明细按装箱清单（{@code wms_carton_detail}）逐箱组装。
 */
@Slf4j
@Service
public class AiyaHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    /**
     * 爱亚授权 JSON 中的 partnerId（客户ID）字段；兼容历史 appKey 写法。
     */
    private static final String AUTH_KEY_PARTNER_ID = "partnerId";
    private static final String AUTH_KEY_APP_KEY = "appKey";

    /**
     * 爱亚授权 JSON 中的客户编码字段（所有接口必填业务参数）。
     */
    private static final String AUTH_KEY_CUSTOMER_CODE = "customerCode";

    /**
     * 爱亚授权 JSON 中的 partnerKey（合作方密钥，仅用于本地签名）；兼容历史 appSecret 写法。
     */
    private static final String AUTH_KEY_PARTNER_KEY = "partnerKey";
    private static final String AUTH_KEY_APP_SECRET = "appSecret";

    /**
     * 默认入库单类型：供应商入库。
     */
    private static final String DEFAULT_ASN_TYPE = "SUPPLIER_RECEIPT";

    /**
     * SKU 良品状态。
     */
    private static final String SKU_STATUS_GOOD = "GOOD";

    /**
     * 默认长度单位。
     */
    private static final String DEFAULT_LENGTH_UNIT = "cm";

    /**
     * 爱亚重量单位固定 kg。
     */
    private static final String WEIGHT_UNIT_KG = "kg";

    /**
     * 预计到货日期格式。
     */
    private static final DateTimeFormatter ETA_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * g → kg 换算除数。
     */
    private static final BigDecimal G_TO_KG_DIVISOR = new BigDecimal("1000");

    /**
     * 爱亚响应 JSON 字段：是否成功。
     */
    private static final String RESP_FIELD_SUCCESS = "success";

    /**
     * 爱亚响应 JSON 字段：状态码。
     */
    private static final String RESP_FIELD_CODE = "code";

    /**
     * 爱亚响应 JSON 字段：提示信息。
     */
    private static final String RESP_FIELD_MESSAGE = "message";

    /**
     * 爱亚响应 JSON 字段：入库单信息（JSON 字符串）。
     */
    private static final String RESP_FIELD_DATA = "data";

    /**
     * 爱亚返回 data 内的仓库分配入库单号字段。
     */
    private static final String DATA_FIELD_WMS_ASN_NUMBER = "wmsAsnNumber";

    /**
     * 爱亚返回 data 内的客户入库单号字段。
     */
    private static final String DATA_FIELD_ASN_NUMBER = "asnNumber";

    @Resource
    private AiyaOpenApiService aiyaOpenApiService;

    @Resource
    private WmsCartonDetailService wmsCartonDetailService;

    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.AI_YA;
    }

    @Override
    protected ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        AiyaAuth auth = resolveAuth();
        AiyaInboundSaveDTO request = buildInboundSaveDto(createInboundReq);
        log.warn("{}创建入库单请求:{}", getPlatForm().getName(), JSONUtil.toJsonStr(request));
        JSONObject resp = aiyaOpenApiService.saveInorder(auth.partnerId, auth.partnerKey, auth.customerCode, toBizParams(request));
        log.warn("{}创建入库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (!isSuccess(resp)) {
            return failure(buildErrorMessage(resp));
        }
        // ERP.code 统一采用我方下发的 asnNumber(=发货单号/referenceNo)：爱亚入库验货明细查询接口
        // GLINK_QUERY_ASN_INSPECT_DETAIL_NOTIFY 按 putawayCompletedTime 时间窗口回传，asnInfo.asnNumber 即回显
        // 我方下发的 asnNumber；回传侧 InitHandler 以 asnNumber 作为 receivingCode/sourceCode，故创建/修改一律以
        // asnNumber 作为 ERP 单据编号，保证创建/回传(按 receivingCode 匹配主表)/取消三段以同一键关联。
        log.warn("{}创建入库单成功, asnNumber={}, wmsAsnNumber={}", getPlatForm().getName(),
                request.getAsnNumber(), extractAsnNumber(resp));
        return success(request.getAsnNumber());
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        if (CharSequenceUtil.isBlank(createInboundReq.getReceivingCode())) {
            throw new ServiceException(ApiError.WH_AIYA_INBOUND_CODE_REQUIRED);
        }
        AiyaAuth auth = resolveAuth();
        // 爱亚 GLINK_CREATE_ASN_NOTIFY 创建/修改合一，按 asnNumber 幂等 upsert，修改同样传全量报文。
        AiyaInboundSaveDTO request = buildInboundSaveDto(createInboundReq);
        log.warn("{}修改入库单请求:{}", getPlatForm().getName(), JSONUtil.toJsonStr(request));
        JSONObject resp = aiyaOpenApiService.saveInorder(auth.partnerId, auth.partnerKey, auth.customerCode, toBizParams(request));
        log.warn("{}修改入库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (!isSuccess(resp)) {
            return failure(buildErrorMessage(resp));
        }
        // 同创建：以我方下发的 asnNumber(=发货单号) 作为 ERP 单据编号，保持回传关联一致。
        log.warn("{}修改入库单成功, asnNumber={}, wmsAsnNumber={}", getPlatForm().getName(),
                request.getAsnNumber(), extractAsnNumber(resp));
        return success(request.getAsnNumber());
    }

    @Override
    protected ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        if (CharSequenceUtil.isBlank(cancelInboundReq.getReceivingCode())) {
            throw new ServiceException(ApiError.WH_AIYA_INBOUND_CODE_REQUIRED);
        }
        AiyaAuth auth = resolveAuth();
        // 取消入库单 GLINK_CANCEL_ASN_NOTIFY 必填 asnNumbers[]，ERP 单据 code(=asnNumber=发货单号) 单条取消
        log.warn("{}取消入库单请求:asnNumber={}", getPlatForm().getName(), cancelInboundReq.getReceivingCode());
        AiyaInboundCancelDTO request = AiyaInboundCancelDTO.builder()
                .asnNumbers(Collections.singletonList(cancelInboundReq.getReceivingCode()))
                .build();
        JSONObject resp = aiyaOpenApiService.cancelInorder(auth.partnerId, auth.partnerKey, auth.customerCode, request);
        log.warn("{}取消入库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (!isSuccess(resp)) {
            return failure(buildErrorMessage(resp));
        }
        return success(cancelInboundReq.getReceivingCode());
    }

    /**
     * 构造爱亚 ASN 创建/修改请求。
     * <p>
     * {@code asnNumber}（必填）取发货单号（{@link ThirdWarehouseCreateInboundReq#getReferenceNo()}），
     * 作为 ERP 侧唯一号，创建/修改以此为幂等键；入库明细按装箱清单逐箱组装。
     */
    private AiyaInboundSaveDTO buildInboundSaveDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        List<WmsCartonSpecDTO.PackingItemDTO> packingItems = loadPackingList(createInboundReq.getReferenceNo());
        if (CollUtil.isEmpty(packingItems)) {
            log.warn("[AIYA入库] 装箱清单为空，referenceNo={}", createInboundReq.getReferenceNo());
            throw new ServiceException(ApiError.WH_AIYA_PACKING_LIST_EMPTY);
        }
        List<AiyaInboundSaveDTO.MarkList> markList = new ArrayList<>();
        List<AiyaInboundSaveDTO.AsnLineItem> lineItems = new ArrayList<>();
        buildBoxAndLines(packingItems, markList, lineItems);
        if (CollUtil.isEmpty(lineItems)) {
            log.warn("[AIYA入库] 装箱清单缺少有效箱号或明细，referenceNo={}", createInboundReq.getReferenceNo());
            throw new ServiceException(ApiError.WH_AIYA_PACKING_LIST_MISSING_BOX_NO);
        }
        int skuTypeCount = (int) lineItems.stream().map(AiyaInboundSaveDTO.AsnLineItem::getSku).distinct().count();
        String trackingNumber = CharSequenceUtil.blankToDefault(createInboundReq.getTrackingNumber(), createInboundReq.getDeliveryCode());
        return AiyaInboundSaveDTO.builder()
                .asnNumber(createInboundReq.getReferenceNo())
                .extAsnNumber(createInboundReq.getReferenceNo())
                .refNumber(createInboundReq.getReferenceNo())
                .referenceNumber(createInboundReq.getReferenceNo())
                .warehouseCode(createInboundReq.getWarehouseCode())
                .warehouseNotes(createInboundReq.getRemark())
                .trackingNumber(trackingNumber)
                .containerNumber(createInboundReq.getContainerType())
                .asnType(DEFAULT_ASN_TYPE)
                .expectedReceiptDate(createInboundReq.getEtaDate() == null ? null
                        : createInboundReq.getEtaDate().format(ETA_DATE_FORMATTER))
                .itemLineQty(skuTypeCount)
                .cartonQty(String.valueOf(markList.size()))
                .markList(markList)
                .asnLineItems(lineItems)
                .build();
    }

    /**
     * 装箱清单 → 爱亚箱信息(markList) + 发运明细(asnLineItems)。
     * <p>
     * 按 {@code boxNo} 分组：每箱一条 markList；箱内相同 SKU 数量累加后按「箱唛 + SKU」生成一条明细，
     * 明细通过 {@code markCode}（箱唛）关联到对应箱。
     */
    private void buildBoxAndLines(List<WmsCartonSpecDTO.PackingItemDTO> packingItems,
                                  List<AiyaInboundSaveDTO.MarkList> markList,
                                  List<AiyaInboundSaveDTO.AsnLineItem> lineItems) {
        Map<String, List<WmsCartonSpecDTO.PackingItemDTO>> boxMap = packingItems.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getBoxNo()))
                .collect(Collectors.groupingBy(
                        WmsCartonSpecDTO.PackingItemDTO::getBoxNo,
                        LinkedHashMap::new,
                        Collectors.toList()));
        int[] lineNo = {0};
        boxMap.forEach((boxNo, items) -> {
            WmsCartonSpecDTO.PackingItemDTO first = items.get(0);
            String boxLabel = buildBoxLabel(first.getSourceCode(), boxNo);
            Map<String, Integer> skuQtyMap = new LinkedHashMap<>();
            for (WmsCartonSpecDTO.PackingItemDTO item : items) {
                if (CharSequenceUtil.isBlank(item.getPlatformSkuNo()) || item.getPackQty() == null) {
                    continue;
                }
                skuQtyMap.merge(item.getPlatformSkuNo(), item.getPackQty(), Integer::sum);
            }
            if (skuQtyMap.isEmpty() || skuQtyMap.values().stream().mapToInt(Integer::intValue).sum() <= 0) {
                log.warn("[AIYA入库] 箱内无有效SKU明细, 发货单号={}, boxNo={}", first.getSourceCode(), boxNo);
                throw new ServiceException(ApiError.WH_AIYA_PACKING_BOX_NO_VALID_SKU, first.getSourceCode(), boxNo);
            }
            markList.add(AiyaInboundSaveDTO.MarkList.builder()
                    .markCode(boxLabel)
                    .length(scale(first.getBoxLength()))
                    .width(scale(first.getBoxWidth()))
                    .height(scale(first.getBoxHeight()))
                    .weight(toKg(first.getPackageWeight(), first.getWeightUnit()))
                    .lengthUnit(CharSequenceUtil.blankToDefault(first.getSizeUnit(), DEFAULT_LENGTH_UNIT))
                    .weightUnit(WEIGHT_UNIT_KG)
                    .build());
            skuQtyMap.forEach((sku, qty) -> lineItems.add(AiyaInboundSaveDTO.AsnLineItem.builder()
                    .lineNo(String.valueOf(++lineNo[0]))
                    .sku(sku)
                    .quantity(qty)
                    .markCode(boxLabel)
                    .skuStatus(SKU_STATUS_GOOD)
                    .build()));
        });
    }

    /**
     * 箱唛：发货单号 + "-" + 箱号；发货单号为空时退化为仅用箱号。
     */
    private String buildBoxLabel(String sourceCode, String boxNo) {
        if (CharSequenceUtil.isBlank(boxNo)) {
            return CharSequenceUtil.isBlank(sourceCode) ? null : sourceCode;
        }
        if (CharSequenceUtil.isBlank(sourceCode)) {
            return boxNo;
        }
        return sourceCode + "-" + boxNo;
    }

    /**
     * 通过 {@code referenceNo}（发货单 code）反查发货单，并按 sourceId 拉装箱清单。
     * 与 {@code OverseasWarehouseInboundServiceImpl#pullThirdOverseasPlatform} 保持一致，
     * 同时按 requisitionId（上游来源 id）与发货单自身 id 两个维度查。
     */
    private List<WmsCartonSpecDTO.PackingItemDTO> loadPackingList(String referenceNo) {
        if (CharSequenceUtil.isBlank(referenceNo)) {
            return Collections.emptyList();
        }
        FirstMileDeliveryEntity delivery = firstMileDeliveryService.getByCode(referenceNo);
        if (delivery == null) {
            log.warn("[AIYA入库] 未找到发货单, referenceNo={}", referenceNo);
            return Collections.emptyList();
        }
        String requisitionId = CharSequenceUtil.blankToDefault(delivery.getSourceId(), "");
        List<String> sourceIds = CharSequenceUtil.isBlank(requisitionId)
                ? Collections.singletonList(delivery.getId())
                : Arrays.asList(requisitionId, delivery.getId());
        return wmsCartonDetailService.boxInfoBySourceIds(sourceIds);
    }

    /**
     * 尺寸取两位小数；为空返回 null（fastjson 序列化时忽略）。
     */
    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 箱重换算为 kg（两位小数）。存储单位为 g 时先除以 1000。
     */
    private BigDecimal toKg(BigDecimal value, String weightUnit) {
        if (value == null) {
            return null;
        }
        BigDecimal kg = value;
        if (CharSequenceUtil.equals(weightUnit, UnitEnum.WeightUnitEnum.G.code)) {
            kg = value.divide(G_TO_KG_DIVISOR, 4, RoundingMode.HALF_UP);
        }
        return kg.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 将强类型请求转换为 SDK 需要的 bizParams（fastjson 默认忽略 null 字段）。
     */
    private Map<String, Object> toBizParams(AiyaInboundSaveDTO request) {
        return (JSONObject) JSON.toJSON(request);
    }

    /**
     * 解析授权信息：partnerId（回退 appKey）/ customerCode / partnerKey（回退 appSecret）。
     */
    private AiyaAuth resolveAuth() {
        Map<String, Object> authMap = ThirdWarehouseContext.getAuthMap();
        if (authMap == null || authMap.isEmpty()) {
            throw new ServiceException(ApiError.WH_AIYA_AUTH_INFO_NOT_FOUND);
        }
        String partnerId = CharSequenceUtil.blankToDefault(toStr(authMap.get(AUTH_KEY_PARTNER_ID)), toStr(authMap.get(AUTH_KEY_APP_KEY)));
        String customerCode = toStr(authMap.get(AUTH_KEY_CUSTOMER_CODE));
        String partnerKey = CharSequenceUtil.blankToDefault(toStr(authMap.get(AUTH_KEY_PARTNER_KEY)), toStr(authMap.get(AUTH_KEY_APP_SECRET)));
        if (CharSequenceUtil.hasBlank(partnerId, customerCode, partnerKey)) {
            throw new ServiceException(ApiError.WH_AIYA_AUTH_TOKEN_SECRET_MISSING);
        }
        return new AiyaAuth(partnerId, customerCode, partnerKey);
    }

    private boolean isSuccess(JSONObject resp) {
        return resp != null && Boolean.TRUE.equals(resp.getBoolean(RESP_FIELD_SUCCESS));
    }

    /**
     * 爱亚 ASN 响应错误信息：优先 message，其次状态码。
     */
    private String buildErrorMessage(JSONObject resp) {
        if (resp == null) {
            return "AIYA接口返回为空";
        }
        String message = resp.getString(RESP_FIELD_MESSAGE);
        if (CharSequenceUtil.isNotBlank(message)) {
            return message;
        }
        String code = resp.getString(RESP_FIELD_CODE);
        return CharSequenceUtil.isNotBlank(code) ? "AIYA接口错误码: " + code : "AIYA接口调用失败";
    }

    /**
     * 从响应 data（JSON 字符串）提取仓库分配入库单号，优先 wmsAsnNumber，回退 asnNumber。
     */
    private String extractAsnNumber(JSONObject resp) {
        if (resp == null) {
            return "";
        }
        String data = resp.getString(RESP_FIELD_DATA);
        if (CharSequenceUtil.isBlank(data)) {
            return "";
        }
        try {
            JSONObject dataObj = JSON.parseObject(data);
            String wmsAsnNumber = dataObj.getString(DATA_FIELD_WMS_ASN_NUMBER);
            if (CharSequenceUtil.isNotBlank(wmsAsnNumber)) {
                return wmsAsnNumber;
            }
            return CharSequenceUtil.blankToDefault(dataObj.getString(DATA_FIELD_ASN_NUMBER), "");
        } catch (Exception e) {
            log.warn("[AIYA入库] 解析 data 字段失败, data={}", data, e);
            return "";
        }
    }

    private String toStr(Object value) {
        return value == null ? null : value.toString();
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        // 爱亚授权无需调用第三方，仅在落库前校验必填字段。
        Map<String, Object> authJson = dto.getAuthJson();
        if (authJson == null || authJson.isEmpty()) {
            throw new ServiceException(ApiError.WH_AIYA_AUTH_INFO_NOT_FOUND);
        }
        String partnerId = CharSequenceUtil.blankToDefault(toStr(authJson.get(AUTH_KEY_PARTNER_ID)), toStr(authJson.get(AUTH_KEY_APP_KEY)));
        String customerCode = toStr(authJson.get(AUTH_KEY_CUSTOMER_CODE));
        String partnerKey = CharSequenceUtil.blankToDefault(toStr(authJson.get(AUTH_KEY_PARTNER_KEY)), toStr(authJson.get(AUTH_KEY_APP_SECRET)));
        if (CharSequenceUtil.hasBlank(partnerId, customerCode, partnerKey)) {
            throw new ServiceException(ApiError.WH_AIYA_AUTH_TOKEN_SECRET_MISSING);
        }
        return true;
    }

    // ===================== 出库 / SKU / 计费 / 上传等暂不支持 =====================

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return null;
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

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        return failure("AIYA暂不支持创建2C出库单");
    }

    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        return failure("AIYA暂不支持创建B2B出库单");
    }

    @Override
    protected ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        return failure("AIYA暂不支持取消2C出库单");
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(@Valid ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        return failure("AIYA暂不支持取消B2B出库单");
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq) {
        return failure("AIYA暂不支持查询2C出库单");
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(@Valid ThirdWarehouseQueryFbaOutboundReq req) {
        return failure("AIYA暂不支持查询B2B出库单");
    }

    /**
     * 爱亚授权信息（partnerId / customerCode / partnerKey）。
     */
    private static class AiyaAuth {
        private final String partnerId;
        private final String customerCode;
        private final String partnerKey;

        private AiyaAuth(String partnerId, String customerCode, String partnerKey) {
            this.partnerId = partnerId;
            this.customerCode = customerCode;
            this.partnerKey = partnerKey;
        }
    }
}
