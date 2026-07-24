package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.AsnTypeEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.erp.server.wms.service.FirstMileDeliveryService;
import com.erp.server.wms.service.WmsCartonDetailService;
import com.sdk.wms.aiya.dto.response.AiyaOutboundResp;
import com.sdk.wms.aiya.service.AiyaOpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
     * ERP 单据编号时间后缀格式（与通邮一致：发货单号_HHmmss）。
     */
    private static final DateTimeFormatter RECEIVING_CODE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

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

    /**
     * 2C出库单 orderTime 官方格式：{@code yyyy-MM-dd'T'HH:mm:ssZ}，示例 {@code 2017-05-01T16:00:00+0800}。
     */
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    /**
     * 承运商服务等级：官方文档"无特殊要求填 STD"，本项目暂无更细的服务等级映射数据源，固定传 STD。
     */
    private static final String DEFAULT_CARRIER_SERVICE = "STD";

    /**
     * 发货标签来源：{@code ATTACHMENT}（平台自带面单，随单下发 trackingNumber+files）。
     */
    private static final String SHIPPING_LABEL_SOURCE_ATTACHMENT = "ATTACHMENT";

    /**
     * 发货标签来源：{@code API}（由爱亚/GWMS 向快递取号）。
     */
    private static final String SHIPPING_LABEL_SOURCE_API = "API";

    /**
     * shipFrom 占位常量：ERP 目前无任何仓库/服务商维度的寄件人数据源
     * （{@code WarehouseEntity}/{@code OverseasProviderEntity} 均无相关字段）。
     * TODO：待产品确认真实寄件人信息来源后替换为动态数据，详见
     * docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」。
     */
    private static final String SHIP_FROM_PLACEHOLDER_NAME = "TODO-寄件人占位";
    private static final String SHIP_FROM_PLACEHOLDER_STREET_LINE1 = "TODO-寄件地址占位";
    private static final String SHIP_FROM_PLACEHOLDER_CITY = "Shenzhen";
    private static final String SHIP_FROM_PLACEHOLDER_STATE = "Guangdong";
    private static final String SHIP_FROM_PLACEHOLDER_POSTAL_CODE = "518000";
    private static final String SHIP_FROM_PLACEHOLDER_COUNTRY_CODE = "CN";

    /**
     * Handler 侧按单号反查时使用的创建时间回溯天数（查询接口不支持按 orderNumber 精确查）。
     */
    private static final int QUERY_OUTBOUND_FALLBACK_DAYS = 7;

    /**
     * 创建/发运时间格式（请求侧 {@code yyyy-MM-dd HH:mm:ss}）。
     */
    private static final DateTimeFormatter SHIPPING_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        // ERP 单据编号(=下发爱亚的 asnNumber) 采用「头程发货单号_HHmmss」，避免同一发货单号重复入库时冲突；
        // 该 asnNumber 同时作为爱亚 upsert/回传(asnInfo.asnNumber 回显)/取消 三段匹配的幂等键，故下发即为最终 code。
        String timeFormatter = LocalDateTime.now().format(RECEIVING_CODE_TIME_FORMATTER);
        createInboundReq.setReceivingCode(CharSequenceUtil.format("{}_{}", createInboundReq.getReferenceNo(), timeFormatter));
        AiyaInboundSaveDTO request = buildInboundSaveDto(createInboundReq);
        log.warn("{}创建入库单请求:{}", getPlatForm().getName(), JSONUtil.toJsonStr(request));
        JSONObject resp = aiyaOpenApiService.saveInorder(auth.partnerId, auth.partnerKey, auth.customerCode, request);
        log.warn("{}创建入库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (!isSuccess(resp)) {
            return failure(buildErrorMessage(resp));
        }
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
        JSONObject resp = aiyaOpenApiService.saveInorder(auth.partnerId, auth.partnerKey, auth.customerCode, request);
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
     * {@code asnNumber}（必填，幂等键）取 ERP 单据编号 {@link ThirdWarehouseCreateInboundReq#getReceivingCode()}
     * （= 发货单号_HHmmss）：创建时由 {@link #createInboundBill} 生成，修改时为已存在的 code；
     * {@code extAsnNumber}/{@code refNumber}/{@code referenceNumber} 仍取发货单号（{@code referenceNo}）便于溯源；
     * 入库明细按装箱清单逐箱组装。
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
                .asnNumber(createInboundReq.getReceivingCode())
                .extAsnNumber(createInboundReq.getReferenceNo())
                .refNumber(createInboundReq.getReferenceNo())
                .referenceNumber(createInboundReq.getReferenceNo())
                .warehouseCode(createInboundReq.getWarehouseCode())
                .warehouseNotes(createInboundReq.getRemark())
                .trackingNumber(trackingNumber)
                .containerNumber(createInboundReq.getContainerType())
                .asnType(AsnTypeEnum.SUPPLIER_RECEIPT.getCode())
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

    /**
     * 创建/修改 AIYA 2C 出库单（{@code GLINK_CREATE_ORDER_NOTIFY}，创建/修改合一，按 {@code orderNumber} 幂等 upsert）。
     * <p>
     * {@code orderNumber} 直接取 {@code referenceNo}（ERP 发货单号）作为幂等键：与 WEGO 不同，
     * AIYA 官方文档明确"客户系统保证唯一"即按此号 upsert，故不需要像 WEGO 一样再拼时间戳后缀，
     * 也不需要 WEGO 那套"订单已存在"幂等反查兜底逻辑。
     * <p>
     * {@code shipFrom} 当前使用占位常量（见类常量注释 TODO），{@code shippingLabelSource} 按
     * {@code isPushLabel}+{@code labelUrl} 二态映射（{@code WMS_GEN} 第三态本次不使用）。
     */
    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        AiyaAuth auth = resolveAuth();
        AiyaOutboundSaveDTO request = buildOutboundSaveDto(createOutboundReq);
        log.warn("{}创建出库单请求:{}", getPlatForm().getName(), JSONUtil.toJsonStr(request));
        JSONObject resp = aiyaOpenApiService.save2cOrder(auth.partnerId, auth.partnerKey, auth.customerCode, request);
        log.warn("{}创建出库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (!isSuccess(resp)) {
            return failure(buildErrorMessage(resp));
        }
        // 2026-07-22 联调确认：成功响应为 {success:true,code:SUCCESS,message:null,data:null}，
        // 不回传独立出库单号；orderNumber（=referenceNo）即最终单号，查询/取消均以此为 key。
        return success(ThirdWarehouseQueryOutboundResponse.builder()
                .shippingOrderNo(request.getOrderNumber())
                .build());
    }

    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        return failure("AIYA暂不支持创建B2B出库单");
    }

    /**
     * 取消（截单）AIYA 2C 出库单（{@code GLINK_CANCEL_ORDER_NOTIFY}）。
     * <p>
     * 参照 {@code TongYouHandlerServiceImpl} 的三态处理范式：{@code success=true} → 拦截成功；
     * 明确"已出库"类错误 → 拦截失败；其余（含异常/无法判断）→ 拦截中，交由 DMP 轮询链路
     * 后续用真实单据状态收敛（{@link ThirdWarehouseCancelResultEnum#INTERCEPTING} 落地方式与
     * 现有平台一致，不需要新机制）。
     * <p>
     * TODO：AIYA 截单接口真实响应结构/错误码未有真实样例验证，"已出库"判定条件为推测，
     * 详见 docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」。
     */
    @Override
    protected ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        if (CharSequenceUtil.isBlank(cancelOutboundReq.getOrderCode())) {
            throw new ServiceException(ApiError.WH_AIYA_OUTBOUND_CODE_REQUIRED);
        }
        AiyaAuth auth = resolveAuth();
        log.warn("{}截单请求:orderNumber={}", getPlatForm().getName(), cancelOutboundReq.getOrderCode());
        JSONObject resp = aiyaOpenApiService.intercept2cOrder(auth.partnerId, auth.partnerKey, auth.customerCode,
                Collections.singletonList(cancelOutboundReq.getOrderCode()));
        log.warn("{}截单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (isSuccess(resp)) {
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }
        if (isOutboundAlreadyShippedError(resp)) {
            log.warn("{}截单失败：订单已出库，orderNumber={}, msg={}",
                    getPlatForm().getName(), cancelOutboundReq.getOrderCode(), buildErrorMessage(resp));
            return ApiResult.success(buildErrorMessage(resp), ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        // 其余场景（含接口异常/无法判断）视为拦截中，由下游 DMP 轮询按订单真实状态（AiyaEnums.OrderStatusEnum）收敛。
        log.warn("{}截单结果未知，暂按拦截中处理，orderNumber={}, msg={}",
                getPlatForm().getName(), cancelOutboundReq.getOrderCode(), buildErrorMessage(resp));
        return ApiResult.success(buildErrorMessage(resp), ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(@Valid ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        return failure("AIYA暂不支持取消B2B出库单");
    }

    /**
     * 查询 AIYA 2C 出库单（{@code GLINK_QUERY_ORDER_NOTIFY}）。
     * <p>
     * 查询接口必填 {@code warehouseCode}；状态反查用创建时间 {@code createdTimeFrom}/
     * {@code createdTimeTo}（对齐 WEGO {@code orderDate*}，可覆盖已提交未发货），
     * <b>不支持</b>按 {@code orderNumber} 精确查。
     * {@link ThirdWarehouseQueryOutboundReq} 仅有 {@code erpOrderCode}（=orderNumber），无仓库编码，
     * 因此本方法按授权服务商下全部可用仓库、近 {@value #QUERY_OUTBOUND_FALLBACK_DAYS} 天创建时间窗口拉取，
     * 再在本地按 {@code orderNumber} 过滤。日常状态同步仍以 DMP {@code AiyaOutboundInitHandler} 为准。
     */
    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq) {
        String orderNumber = queryOutboundReq.getErpOrderCode();
        if (CharSequenceUtil.isBlank(orderNumber)) {
            return failure("AIYA查询出库单参考号不能为空");
        }
        AiyaAuth auth = resolveAuth();
        List<OverseasProviderWarehouseEntity> warehouseList = FeignQuery.create(OverseasProviderWarehouseEntity.class)
                .eq(OverseasProviderWarehouseEntity::getMainId, ThirdWarehouseContext.getAuthId())
                .eq(OverseasProviderWarehouseEntity::getDisabled, Boolean.FALSE)
                .list();
        if (CollUtil.isEmpty(warehouseList)) {
            return failure("AIYA查询出库单失败：当前授权下无可用仓库");
        }

        LocalDateTime now = LocalDateTime.now();
        String createdTimeFrom = now.minusDays(QUERY_OUTBOUND_FALLBACK_DAYS).format(SHIPPING_TIME_FORMATTER);
        String createdTimeTo = now.format(SHIPPING_TIME_FORMATTER);

        for (OverseasProviderWarehouseEntity warehouse : warehouseList) {
            String warehouseCode = warehouse.getPlatformWarehouseCode();
            if (CharSequenceUtil.isBlank(warehouseCode)) {
                continue;
            }
            AiyaOutboundQueryDTO.QueryReqDTO req = AiyaOutboundQueryDTO.QueryReqDTO.builder()
                    .accessToken(auth.partnerId)
                    .secret(auth.partnerKey)
                    .customerCode(auth.customerCode)
                    .warehouseCode(warehouseCode)
                    .createdTimeFrom(createdTimeFrom)
                    .createdTimeTo(createdTimeTo)
                    .pageNum(1)
                    .pageSize(AiyaOutboundQueryDTO.DEFAULT_PAGE_SIZE)
                    .build();
            List<AiyaOutboundResp.OutboundOrderDTO> page = aiyaOpenApiService.query2cOrder(req);
            if (CollUtil.isEmpty(page)) {
                continue;
            }
            AiyaOutboundResp.OutboundOrderDTO matched = page.stream()
                    .filter(o -> orderNumber.equals(o.getOrderNumber()))
                    .findFirst()
                    .orElse(null);
            if (matched != null) {
                return success(ThirdWarehouseQueryOutboundResponse.builder()
                        .shippingOrderNo(matched.getOrderNumber())
                        .trackNo(matched.getTrackingNumber())
                        .build());
            }
        }
        return failure("AIYA未查询到对应出库单（orderNumber=" + orderNumber
                + "，近" + QUERY_OUTBOUND_FALLBACK_DAYS + "天创建窗口）");
    }

    /**
     * 构造 AIYA {@code GLINK_CREATE_ORDER_NOTIFY} 建单请求。
     * <p>
     * 字段映射说明：
     * <ul>
     *     <li>{@code orderNumber} = {@code referenceNo}（ERP 发货单号，直接做幂等键）；</li>
     *     <li>{@code shippingInstructions.carrier} 取 {@code shippingMethodName}，{@code carrierService}
     *         固定 {@value #DEFAULT_CARRIER_SERVICE}；{@code shippingLabelSource} 按 {@code isPushLabel}+
     *         {@code labelUrl} 二态映射：有面单 → {@code ATTACHMENT}（连带 trackingNumber+files 传面单），
     *         否则 → {@code API}（由 AIYA 自动生成，{@code WMS_GEN} 第三态本次不使用）；</li>
     *     <li>{@code shipTo} 取 {@code receiverInfo}（address1→streetLine1、address2→streetLine2、
     *         district、city、province→state、zipCode→postalCode、countryCode）；</li>
     *     <li>{@code items[]} 按 {@code productSku} 聚合数量，防重复 SKU 行；</li>
     *     <li>{@code shipFrom} 使用占位常量（TODO，见类常量注释）。</li>
     * </ul>
     */
    private AiyaOutboundSaveDTO buildOutboundSaveDto(ThirdWarehouseCreateOutboundReq req) {
        if (CollUtil.isEmpty(req.getItems())) {
            log.warn("{}创建出库单明细为空, referenceNo={}", getPlatForm().getName(), req.getReferenceNo());
            throw new ServiceException(ApiError.WH_AIYA_OUTBOUND_DETAIL_EMPTY);
        }

        ThirdWarehouseCreateOutboundReq.ReceiverInfo receiver = req.getReceiverInfo();

        boolean hasPlatformLabel = CharSequenceUtil.isNotBlank(req.getLabelUrl())
                && Boolean.TRUE.equals(req.getIsPushLabel());
        AiyaOutboundSaveDTO.ShippingInstructions.ShippingInstructionsBuilder instructionsBuilder =
                AiyaOutboundSaveDTO.ShippingInstructions.builder()
                        .carrier(req.getShippingMethodName())
                        .carrierService(DEFAULT_CARRIER_SERVICE);
        List<AiyaOutboundSaveDTO.FileItem> files = null;
        if (hasPlatformLabel) {
            instructionsBuilder.shippingLabelSource(SHIPPING_LABEL_SOURCE_ATTACHMENT)
                    .trackingNumber(req.getTrackingNo());
            files = Collections.singletonList(AiyaOutboundSaveDTO.FileItem.builder()
                    .fileType(AiyaOutboundSaveDTO.FileItem.FILE_TYPE_SHIPPING_LABEL)
                    .fileName("面单信息")
                    .fileUrl(req.getLabelUrl())
                    .build());
        } else {
            instructionsBuilder.shippingLabelSource(SHIPPING_LABEL_SOURCE_API);
        }

        List<AiyaOutboundSaveDTO.Item> items = new ArrayList<>();
        // 同一 productSku 可能出现在多行明细中，需按 SKU 聚合数量，避免重复条目导致拒单或数量统计异常
        Map<String, Integer> skuQtyMap = new LinkedHashMap<>();
        for (ThirdWarehouseCreateOutboundReq.Item item : req.getItems()) {
            if (CharSequenceUtil.isBlank(item.getProductSku()) || item.getQuantity() == null) {
                continue;
            }
            skuQtyMap.merge(item.getProductSku(), item.getQuantity(), Integer::sum);
        }
        skuQtyMap.forEach((sku, qty) -> items.add(
                AiyaOutboundSaveDTO.Item.builder().sku(sku).quantity(qty).build()));
        if (items.isEmpty()) {
            log.warn("{}创建出库单明细为空, referenceNo={}", getPlatForm().getName(), req.getReferenceNo());
            throw new ServiceException(ApiError.WH_AIYA_OUTBOUND_DETAIL_EMPTY);
        }

        String orderTime = req.getPayTime() != null
                ? ZonedDateTime.of(req.getPayTime(), ZoneOffset.ofHours(8)).format(ORDER_TIME_FORMATTER)
                : ZonedDateTime.now(ZoneOffset.ofHours(8)).format(ORDER_TIME_FORMATTER);

        return AiyaOutboundSaveDTO.builder()
                .orderNumber(req.getReferenceNo())
                .warehouseCode(req.getWarehouseCode())
                .extOrderNumber(req.getPlatformCode())
                .orderTime(orderTime)
                .salesChannel(req.getPlatform())
                .storeNumber(req.getShopName())
                .shippingInstructions(instructionsBuilder.build())
                .shipTo(AiyaOutboundSaveDTO.ShipTo.builder()
                        .name(receiver != null ? receiver.getName() : null)
                        .mobileNumber(receiver != null ? receiver.getPhone() : null)
                        .email(receiver != null ? receiver.getEmail() : null)
                        .streetLine1(receiver != null ? receiver.getAddress1() : null)
                        .streetLine2(receiver != null ? receiver.getAddress2() : null)
                        .district(receiver != null ? receiver.getDistrict() : null)
                        .city(receiver != null ? receiver.getCity() : null)
                        .state(receiver != null ? receiver.getProvince() : null)
                        .postalCode(receiver != null ? receiver.getZipCode() : null)
                        .countryCode(receiver != null ? receiver.getCountryCode() : null)
                        .build())
                .items(items)
                .shipFrom(buildShipFromPlaceholder())
                .files(files)
                .build();
    }

    /**
     * shipFrom 占位实现：ERP 目前无任何仓库/服务商维度的寄件人数据源，见类常量注释 TODO。
     */
    private AiyaOutboundSaveDTO.ShipFrom buildShipFromPlaceholder() {
        return AiyaOutboundSaveDTO.ShipFrom.builder()
                .name(SHIP_FROM_PLACEHOLDER_NAME)
                .streetLine1(SHIP_FROM_PLACEHOLDER_STREET_LINE1)
                .city(SHIP_FROM_PLACEHOLDER_CITY)
                .state(SHIP_FROM_PLACEHOLDER_STATE)
                .postalCode(SHIP_FROM_PLACEHOLDER_POSTAL_CODE)
                .countryCode(SHIP_FROM_PLACEHOLDER_COUNTRY_CODE)
                .build();
    }

    /**
     * 判断截单失败是否因订单已出库导致（AIYA 真实错误码/文案未有样例验证，暂按关键词匹配，纯推测实现）。
     * TODO：需联调真实接口确认，详见 docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」。
     */
    private boolean isOutboundAlreadyShippedError(JSONObject resp) {
        if (resp == null) {
            return false;
        }
        String message = resp.getString(RESP_FIELD_MESSAGE);
        return CharSequenceUtil.isNotBlank(message) && message.contains("已出库");
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
