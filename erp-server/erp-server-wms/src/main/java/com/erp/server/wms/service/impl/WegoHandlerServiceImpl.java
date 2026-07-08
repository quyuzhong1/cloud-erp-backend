package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.erp.server.wms.service.FirstMileDeliveryService;
import com.erp.server.wms.service.WmsCartonDetailService;
import com.sdk.wms.wego.dto.response.WegoOutboundResp;
import com.sdk.wms.wego.service.WegoOpenApiService;
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
 * WEGO 三方海外仓处理服务实现类
 * <p>
 * 仅在 {@link OmsPlatformEnum#WE_GO} 渠道下生效，
 * 入库单创建/修改统一调用 WEGO {@code inorder.save} 接口。
 */
@Slf4j
@Service
public class WegoHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    /**
     * WEGO 授权 JSON 中的 accessToken 字段
     */
    private static final String AUTH_KEY_APP_TOKEN = "appToken";

    /**
     * WEGO 授权 JSON 中的 secret 字段，仅用于本地签名
     */
    private static final String AUTH_KEY_APP_SECRET = "appSecret";

    /**
     * WEGO 请求 DTO 中的敏感字段：第三方授权 Token，禁止落日志
     */
    private static final String DTO_FIELD_ACCESS_TOKEN = "accessToken";

    /**
     * WEGO 请求 DTO 中的敏感字段：本地签名密钥，禁止落日志
     */
    private static final String DTO_FIELD_SECRET = "secret";

    /**
     * 日志脱敏占位符
     */
    private static final String LOG_MASK = "***";

    /**
     * queryPage 降级查询的时间窗口（小时）。
     * 幂等重试通常在建单后数秒到数分钟内触发，24 小时窗口足够覆盖所有重试场景，
     * 同时相比 3 天窗口大幅减少 queryPage 返回的无关订单数，降低漏匹配概率。
     */
    private static final int REFERENCE_CODE_FALLBACK_HOURS = 24;

    /**
     * queryPage 降级查询的最大翻页数。
     * 防止 WEGO 在时间窗口内存在大量订单时发起过多 HTTP 请求导致超时或触发限速。
     */
    private static final int REFERENCE_CODE_FALLBACK_MAX_PAGES = 50;

    /**
     * WEGO "订单已存在" 错误关键词（errorCode=2000 时出现在 result/errorMsg 字段中）。
     */
    private static final String WEGO_ERROR_ORDER_ALREADY_EXISTS = "订单已存在";

    /**
     * 默认库存类型：0=2C库存
     */
    private static final Integer DEFAULT_INVENTORY_TYPE = 0;

    /**
     * 默认到货方式：8=海运/空运散货
     */
    private static final String DEFAULT_WAREHOUSE_DELIVERY = "8";

    /**
     * 预计到货日期格式
     */
    private static final DateTimeFormatter ETA_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * queryPage 降级查询的时间格式（精确到秒）
     */
    private static final DateTimeFormatter QUERY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * g → kg 换算除数
     */
    private static final BigDecimal G_TO_KG_DIVISOR = new BigDecimal("1000");

    /**
     * WEGO 响应 JSON 字段：是否成功
     */
    private static final String RESP_FIELD_SUCCESS = "success";

    /**
     * WEGO 响应 JSON 字段：错误码
     */
    private static final String RESP_FIELD_ERROR_CODE = "errorCode";

    /**
     * WEGO 响应 JSON 字段：错误信息
     */
    private static final String RESP_FIELD_ERROR_MSG = "errorMsg";

    /**
     * WEGO 响应 JSON 字段：业务结果
     */
    private static final String RESP_FIELD_RESULT = "result";

    /**
     * WEGO "订单已存在" 的 errorCode（2c.order.save 幂等重试时返回）
     */
    private static final int WEGO_ERROR_CODE_ORDER_EXISTS = 2000;

    /**
     * WEGO "订单已截单" 的 errorCode（重复截单时返回）
     */
    private static final int WEGO_ERROR_CODE_INTERCEPTED = 2003;

    /**
     * WEGO 截单幂等关键词：在 WEGO 后台手动取消订单后再截单时，
     * WEGO 返回 success=false 但 errorMsg 包含此关键词，视为幂等成功。
     */
    private static final String WEGO_INTERCEPT_IDEMPOTENT_KEYWORD = "操作成功";

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    /**
     * 装箱清单查询服务（按 sourceId 维度返回每箱-每 SKU 行级数据）。
     * <p>
     * WEGO inorder.save 要求按箱推送（一箱一条 details），箱内多 SKU 进 products 数组，
     * 因此 wego 在 handler 内部直接拉装箱清单原始数据，避免上层 items 转换造成的字段失真。
     */
    @Resource
    private WmsCartonDetailService wmsCartonDetailService;

    /**
     * 通过 {@link ThirdWarehouseCreateInboundReq#getReferenceNo()}（发货单 code）
     * 反查 {@link FirstMileDeliveryEntity}，取得 sourceId 后查装箱清单。
     */
    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.WE_GO;
    }

    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return null;
    }

    @Override
    protected ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        WegoInOrderSaveDTO.SaveReqDTO request = buildInorderSaveDto(createInboundReq, null);
        log.warn("{}创建入库单请求:{}", getPlatForm().getName(), toLogSafeJson(request));
        JSONObject resp = wegoOpenApiService.saveInorder(request);
        log.warn("{}创建入库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (!isSuccess(resp)) {
            return failure(buildErrorMessage(resp));
        }
        return success(extractOrderNo(resp));
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        if (CharSequenceUtil.isBlank(createInboundReq.getReceivingCode())) {
            throw new ServiceException("WEGO入库单号不能为空");
        }
        WegoInOrderSaveDTO.SaveReqDTO request = buildInorderSaveDto(createInboundReq, createInboundReq.getReceivingCode());
        log.warn("{}修改入库单请求:{}", getPlatForm().getName(), toLogSafeJson(request));
        JSONObject resp = wegoOpenApiService.saveInorder(request);
        log.warn("{}修改入库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (!isSuccess(resp)) {
            return failure(buildErrorMessage(resp));
        }
        return success(CharSequenceUtil.blankToDefault(extractOrderNo(resp), createInboundReq.getReceivingCode()));
    }

    /**
     * 构造 WEGO inorder.save 请求 DTO。
     * <p>
     * details 字段的传输策略：
     * <ul>
     *     <li>新增场景（{@code no} 为空）：完整传 details，由 WEGO 落库为入库单明细；</li>
     *     <li>修改场景（{@code no} 非空）：<b>不传 details</b>。
     *         {@code OverseasWarehouseInboundServiceImpl#update} 仅允许修改单据头部信息（如运输方式、跟踪号等），
     *         ERP 侧入库单明细一旦生成即不可修改，故修改请求中不携带 details 字段，
     *         避免与 WEGO 服务端已有明细产生冲突或被全量覆盖。</li>
     * </ul>
     * 入参 details 在 SDK 层（{@code WegoOpenApiService#saveInorder}）通过
     * {@code putIfNotNull} 控制，null 时不参与签名、不出现在请求体。
     *
     * @param createInboundReq ERP 统一入库单请求
     * @param no               WEGO 单号；新增传 null，修改传已有单号
     */
    private WegoInOrderSaveDTO.SaveReqDTO buildInorderSaveDto(ThirdWarehouseCreateInboundReq createInboundReq, String no) {
        Map<String, Object> authMap = ThirdWarehouseContext.getAuthMap();
        if (authMap == null || authMap.isEmpty()) {
            throw new ServiceException("WEGO授权信息为空");
        }
        String accessToken = toStr(authMap.get(AUTH_KEY_APP_TOKEN));
        String secret = toStr(authMap.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(accessToken, secret)) {
            throw new ServiceException("WEGO授权信息appToken/appSecret缺失");
        }
        boolean isCreate = CharSequenceUtil.isBlank(no);
        return WegoInOrderSaveDTO.SaveReqDTO.builder()
                .accessToken(accessToken)
                .secret(secret)
                .no(no)
                .warehouseBusiness(getPlatForm().getName())
                .warehouseCode(createInboundReq.getWarehouseCode())
                .warehouseDelivery(resolveWarehouseDelivery(createInboundReq))
                .inventoryType(DEFAULT_INVENTORY_TYPE)
                .expectedArrivalDate(createInboundReq.getEtaDate() == null ? null
                        : createInboundReq.getEtaDate().format(ETA_DATE_FORMATTER))
                .trackNumber(CharSequenceUtil.blankToDefault(createInboundReq.getTrackingNumber(),
                        createInboundReq.getDeliveryCode()))
                .referenceNumber(createInboundReq.getReferenceNo())
                .notes(createInboundReq.getRemark())
                .details(isCreate ? buildDetails(createInboundReq) : null)
                .build();
    }

    /**
     * 根据 ERP 物流方式与柜型推导 WEGO 到货方式。
     * <p>
     * WEGO warehouseDelivery 取值：
     * <ul>
     *     <li>4 - 20FT(打板货)</li>
     *     <li>5 - 20FT(散装货)</li>
     *     <li>6 - 40FT(打板货)</li>
     *     <li>7 - 40FT(散装货)</li>
     *     <li>8 - 海运/空运散货（默认）</li>
     * </ul>
     * 当前 ERP 未存储「是否打板」标识，整柜场景统一按散装货推导（5/7），其余统一回退到 8。
     */
    private String resolveWarehouseDelivery(ThirdWarehouseCreateInboundReq createInboundReq) {
        LogisticsMethodEnum methodEnum = LogisticsMethodEnum.getByCode(createInboundReq.getReceivingShippingType());
        if (methodEnum == null) {
            return DEFAULT_WAREHOUSE_DELIVERY;
        }
        boolean isFcl = methodEnum == LogisticsMethodEnum.OCEAN_FREIGHT_FCL
                || methodEnum == LogisticsMethodEnum.RAILWAY_TRANSPORTATION_FCL;
        if (!isFcl) {
            return DEFAULT_WAREHOUSE_DELIVERY;
        }
        String containerType = CharSequenceUtil.blankToDefault(createInboundReq.getContainerType(), "");
        if (containerType.contains("20")) {
            return "5";
        }
        if (containerType.contains("40")) {
            return "7";
        }
        return DEFAULT_WAREHOUSE_DELIVERY;
    }

    /**
     * 构造 WEGO inorder.save 的 details 列表。
     * <p>
     * WEGO 入库单按箱推送：一条 {@link WegoInOrderSaveDTO.Detail} = 一箱（{@code boxQty=1}），
     * 箱内不同 SKU 聚合到 {@code products}。
     * <p>
     * 数据来源优先级：
     * <ol>
     *     <li>装箱清单（{@code wms_carton_detail}）— 通过发货单 code 反查 sourceId 后调用
     *         {@link WmsCartonDetailService#boxInfoBySourceIds(List)}；</li>
     * </ol>
     */
    private List<WegoInOrderSaveDTO.Detail> buildDetails(ThirdWarehouseCreateInboundReq createInboundReq) {
        List<WmsCartonSpecDTO.PackingItemDTO> packingItems = loadPackingList(createInboundReq.getReferenceNo());
        if (CollUtil.isEmpty(packingItems)) {
            log.warn("[WEGO入库] 装箱清单为空，referenceNo={}", createInboundReq.getReferenceNo());
            throw new ServiceException("装箱清单为空");
        }
        return buildDetailsFromPackingList(packingItems);
    }

    /**
     * 通过 {@code referenceNo}（发货单 code）反查发货单，并按 sourceId 拉装箱清单。
     * <p>
     * 与 {@code OverseasWarehouseInboundServiceImpl#pullThirdOverseasPlatform} 保持一致：
     * 同时按 {@code requisitionId}（上游来源 id）和发货单自身 id 两个维度查，覆盖调拨/请购等场景。
     *
     * @param referenceNo 发货单 code（即 {@code OverseasWarehouseInboundEntity#getSourceCode}）
     * @return 装箱清单行集合；发货单不存在或 referenceNo 为空时返回空列表
     */
    private List<WmsCartonSpecDTO.PackingItemDTO> loadPackingList(String referenceNo) {
        if (CharSequenceUtil.isBlank(referenceNo)) {
            return Collections.emptyList();
        }
        FirstMileDeliveryEntity delivery = firstMileDeliveryService.getByCode(referenceNo);
        if (delivery == null) {
            log.warn("[WEGO入库] 未找到发货单, referenceNo={}", referenceNo);
            return Collections.emptyList();
        }
        String requisitionId = CharSequenceUtil.blankToDefault(delivery.getSourceId(), "");
        List<String> sourceIds = CharSequenceUtil.isBlank(requisitionId)
                ? Collections.singletonList(delivery.getId())
                : Arrays.asList(requisitionId, delivery.getId());
        return wmsCartonDetailService.boxInfoBySourceIds(sourceIds);
    }

    /**
     * 装箱清单 → WEGO details 的转换：按 {@code boxNo} 分组，每箱一条 Detail。
     * <p>
     * 同一 boxNo 内的多条 PackingItemDTO 代表箱内不同 SKU，进 products 数组并按 SKU 累加数量。
     * 箱尺寸与重量取该箱第一条记录的值（同一箱所有行应一致）。
     */
    private List<WegoInOrderSaveDTO.Detail> buildDetailsFromPackingList(List<WmsCartonSpecDTO.PackingItemDTO> packingItems) {
        Map<String, List<WmsCartonSpecDTO.PackingItemDTO>> boxMap = packingItems.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getBoxNo()))
                .collect(Collectors.groupingBy(
                        WmsCartonSpecDTO.PackingItemDTO::getBoxNo,
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<WegoInOrderSaveDTO.Detail> details = new ArrayList<>(boxMap.size());
        boxMap.forEach((boxNo, list) -> {
            WmsCartonSpecDTO.PackingItemDTO first = list.get(0);
            List<WegoInOrderSaveDTO.Product> products = buildProductsFromPackingList(list);
            if (CollUtil.isEmpty(products) || sumSkuQty(products) <= 0) {
                log.warn("[WEGO入库] 箱内无有效SKU明细, 发货单号={}, boxNo={}", first.getSourceCode(), boxNo);
                throw new ServiceException("装箱清单箱内无有效SKU明细, 发货单号="
                        + first.getSourceCode() + ", 箱号=" + boxNo);
            }
            WegoInOrderSaveDTO.Detail detail = WegoInOrderSaveDTO.Detail.builder()
                    .inOrderDetailId(null)
                    .boxQty(1)
                    .skuQty(sumSkuQty(products))
                    .boxLabel(buildBoxLabel(first.getSourceCode(), boxNo))
                    .boxLength(toIntegerCm(first.getBoxLength()))
                    .boxWidth(toIntegerCm(first.getBoxWidth()))
                    .boxHeight(toIntegerCm(first.getBoxHeight()))
                    .boxWeight(toIntegerKg(first.getPackageWeight(), first.getWeightUnit()))
                    .deletedFlag(false)
                    .products(products)
                    .build();
            details.add(detail);
        });
        if (CollUtil.isEmpty(details)) {
            log.warn("[WEGO入库] 装箱清单缺少有效箱号，无法构造入库明细，packingItems={}", packingItems.size());
            throw new ServiceException("装箱清单缺少箱号");
        }
        return details;
    }

    /**
     * 构造 WEGO 箱唛（boxLabel）：发货单号（{@code packing_task.source_code}）+ "-" + 箱号。
     * <p>
     * sourceCode 为空时退化为仅用箱号，避免出现以 "-" 开头的无效箱唛。
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
     * 装箱清单 → 箱内 products：相同 platformSkuNo 数量累加。
     */
    private List<WegoInOrderSaveDTO.Product> buildProductsFromPackingList(List<WmsCartonSpecDTO.PackingItemDTO> items) {
        Map<String, Integer> skuQtyMap = new LinkedHashMap<>();
        for (WmsCartonSpecDTO.PackingItemDTO item : items) {
            if (CharSequenceUtil.isBlank(item.getPlatformSkuNo()) || item.getPackQty() == null) {
                continue;
            }
            skuQtyMap.merge(item.getPlatformSkuNo(), item.getPackQty(), Integer::sum);
        }
        List<WegoInOrderSaveDTO.Product> products = new ArrayList<>(skuQtyMap.size());
        skuQtyMap.forEach((sku, qty) -> products.add(WegoInOrderSaveDTO.Product.builder()
                .sku(sku)
                .qty(qty)
                .build()));
        return products;
    }

    /**
     * 汇总单箱 SKU 总件数：对应 WEGO Detail.skuQty 字段。
     * <p>
     * 取 products 列表中所有 {@link WegoInOrderSaveDTO.Product#getQty()} 之和，
     * 空列表或全空 qty 时返回 0。
     */
    private Integer sumSkuQty(List<WegoInOrderSaveDTO.Product> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (WegoInOrderSaveDTO.Product product : products) {
            if (product != null && product.getQty() != null) {
                total += product.getQty();
            }
        }
        return total;
    }

    /**
     * 将 cm 尺寸（可能为 BigDecimal）转换为整数；为空时返回 0。
     */
    private Integer toIntegerCm(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        return value.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * 将箱重转换为 kg 整数。
     * <p>
     * 当 ERP 存储单位为 g 时，先除以 1000 再向上取整；否则按 kg 取整。
     */
    private Integer toIntegerKg(BigDecimal value, String weightUnit) {
        if (value == null) {
            return 0;
        }
        BigDecimal kg = value;
        if (CharSequenceUtil.equals(weightUnit, UnitEnum.WeightUnitEnum.G.code)) {
            kg = value.divide(G_TO_KG_DIVISOR, 4, RoundingMode.HALF_UP);
        }
        return kg.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    @Override
    protected ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        if (CharSequenceUtil.isBlank(cancelInboundReq.getReceivingCode())) {
            throw new ServiceException("WEGO入库单号不能为空");
        }
        WegoInOrderCancelDTO.CancelReqDTO request = buildInorderCancelDto(cancelInboundReq);
        log.warn("{}取消入库单请求:{}", getPlatForm().getName(), toLogSafeJson(request));
        JSONObject resp = wegoOpenApiService.cancelInorder(request);
        log.warn("{}取消入库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (!isSuccess(resp)) {
            return failure(buildErrorMessage(resp));
        }
        return success(cancelInboundReq.getReceivingCode());
    }

    /**
     * 构造 WEGO inorder.cancel 请求 DTO。
     * <p>
     * 授权信息 {@code accessToken / secret} 取自 {@link ThirdWarehouseContext#getAuthMap()}，
     * {@code no} 取 {@link ThirdWarehouseCancelInboundReq#getReceivingCode()}（即 ERP 侧落库的 WEGO 单号）。
     */
    private WegoInOrderCancelDTO.CancelReqDTO buildInorderCancelDto(ThirdWarehouseCancelInboundReq cancelInboundReq) {
        Map<String, Object> authMap = ThirdWarehouseContext.getAuthMap();
        if (authMap == null || authMap.isEmpty()) {
            throw new ServiceException("WEGO授权信息为空");
        }
        String accessToken = toStr(authMap.get(AUTH_KEY_APP_TOKEN));
        String secret = toStr(authMap.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(accessToken, secret)) {
            throw new ServiceException("WEGO授权信息appToken/appSecret缺失");
        }
        return WegoInOrderCancelDTO.CancelReqDTO.builder()
                .accessToken(accessToken)
                .secret(secret)
                .no(cancelInboundReq.getReceivingCode())
                .build();
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
     * 创建 WEGO 2C 出库单（2c.order.save）。
     *
     * <p>WEGO 不支持幂等建单：相同 {@code referenceCode} 重复提交会返回
     * {@code success=false, errorCode=2000, result="订单已存在:WFHD-xxx"}。
     * 为避免将"已存在"误判为真实失败（导致 WFHD 被删除、订单回退），
     * 在收到该错误时立即通过 {@link #queryByReferenceCodeFallback} 反查真实 WEGO 单号
     * 并返回 success，使幂等重试链路安全落地。</p>
     */
    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        Map<String, Object> authMap = ThirdWarehouseContext.getAuthMap();
        if (authMap == null || authMap.isEmpty()) {
            throw new ServiceException("WEGO授权信息为空");
        }
        String accessToken = toStr(authMap.get(AUTH_KEY_APP_TOKEN));
        String secret = toStr(authMap.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(accessToken, secret)) {
            throw new ServiceException("WEGO授权信息appToken/appSecret缺失");
        }

        WegoOutboundSaveDTO.SaveReqDTO request = buildOutboundSaveDto(createOutboundReq, null, accessToken, secret);
        if (CollUtil.isEmpty(request.getProducts())) {
            log.warn("{}创建出库单明细为空, referenceNo={}", getPlatForm().getName(), createOutboundReq.getReferenceNo());
            throw new ServiceException("出库明细不能为空");
        }
        log.warn("{}创建出库单请求:{}", getPlatForm().getName(), toLogSafeJson(request));
        JSONObject resp = wegoOpenApiService.save2cOrder(request);
        log.warn("{}创建出库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));

        if (!isSuccess(resp)) {
            // WEGO 不幂等：相同 referenceCode 重复提交返回 "订单已存在"。
            // 此时订单实际已在 WEGO 侧创建成功，通过 queryPage 反查真实单号并返回 success，
            // 避免上层误删 WFHD 并将订单回退到"配货中"。
            if (isOrderAlreadyExistsError(resp)) {
                String referenceNo = createOutboundReq.getReferenceNo();
                log.warn("{}建单返回[订单已存在]（非重单，属幂等重试），按 referenceCode 反查 WEGO 单号, referenceNo={}",
                        getPlatForm().getName(), referenceNo);
                try {
                    ApiResult<ThirdWarehouseQueryOutboundResponse> fallback =
                            queryByReferenceCodeFallback(accessToken, secret, referenceNo);
                    if (fallback.isSuccess()) {
                        log.info("{}反查成功，幂等重试命中已有订单, wegoNo={}",
                                getPlatForm().getName(), fallback.getData().getShippingOrderNo());
                        return fallback;
                    }
                    log.warn("{}反查失败（referenceCode={}, msg={}），以原始错误返回",
                            getPlatForm().getName(), referenceNo, fallback.getMsg());
                } catch (ServiceException e) {
                    // 幂等重试路径下反查接口临时失败（响应为空/success=false）不应向上抛出，
                    // 否则上层会将其当作系统异常处理，导致 WFHD 状态与预期不符。
                    // 此处降级为原始 WEGO 错误信息返回，保证幂等分支始终返回可控的 ApiResult。
                    log.warn("{}反查接口异常（referenceCode={}, err={}），以原始错误返回",
                            getPlatForm().getName(), referenceNo, e.getMessage());
                }
            }
            return failure(buildErrorMessage(resp));
        }

        String wegoOrderNo = extractStringResult(resp);
        if (CharSequenceUtil.isBlank(wegoOrderNo)) {
            log.error("{}创建出库单接口返回 success 但未提取到出库单号, resp={}",
                    getPlatForm().getName(), JSONUtil.toJsonStr(resp));
            return failure("WEGO创建出库单成功但未返回出库单号，请检查接口响应或联系WEGO排查");
        }
        return success(ThirdWarehouseQueryOutboundResponse.builder().shippingOrderNo(wegoOrderNo).build());
    }

    /**
     * 判断 WEGO 是否因"相同 referenceCode 已存在"而拒绝建单。
     * <p>
     * WEGO 返回特征：{@code success=false, errorCode=2000, result="订单已存在:WFHD-xxx"}。
     * 同时兜底检查 {@code errorMsg} 字段，应对 WEGO 未来调整字段位置的情况。
     * errorCode 与关键词双重匹配，防止其他错误场景误触发。
     * </p>
     */
    private boolean isOrderAlreadyExistsError(JSONObject resp) {
        if (resp == null) {
            return false;
        }
        Integer errorCode = resp.getInteger(RESP_FIELD_ERROR_CODE);
        boolean codeMatch = Integer.valueOf(WEGO_ERROR_CODE_ORDER_EXISTS).equals(errorCode);
        if (!codeMatch) {
            return false;
        }
        String result = resp.getString(RESP_FIELD_RESULT);
        if (CharSequenceUtil.isNotBlank(result) && result.contains(WEGO_ERROR_ORDER_ALREADY_EXISTS)) {
            return true;
        }
        String errorMsg = resp.getString(RESP_FIELD_ERROR_MSG);
        return CharSequenceUtil.isNotBlank(errorMsg) && errorMsg.contains(WEGO_ERROR_ORDER_ALREADY_EXISTS);
    }

    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        return failure("WEGO暂不支持创建B2B出库单");
    }

    @Override
    protected ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        if (CharSequenceUtil.isBlank(cancelOutboundReq.getOrderCode())) {
            throw new ServiceException("WEGO出库单号不能为空");
        }
        WegoOutboundInterceptDTO.InterceptReqDTO request = buildInterceptDto(cancelOutboundReq);
        log.warn("{}截单请求:{}", getPlatForm().getName(), toLogSafeJson(request));
        JSONObject resp = wegoOpenApiService.intercept2cOrder(request);
        log.warn("{}截单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        // Case B：WEGO 正常拦截成功（success=true）
        if (isSuccess(resp)) {
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }
        // Case A：订单已在 WEGO 侧截单/取消，重复截单属于幂等成功，透传 WEGO 说明供上层展示
        if (isInterceptAlreadySuccessful(resp)) {
            String wegoMsg = CharSequenceUtil.blankToDefault(resp.getString(RESP_FIELD_ERROR_MSG), "WEGO订单已取消");
            log.info("{}截单幂等命中，视为拦截成功, orderNo={}, wegoMsg={}",
                    getPlatForm().getName(), cancelOutboundReq.getOrderCode(), wegoMsg);
            return ApiResult.success("WEGO订单已截单/取消，视为拦截成功（" + wegoMsg + "）",
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }
        // Case C：其他错误，透传 WEGO 原始错误信息
        return failure(buildErrorMessage(resp));
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        return failure("WEGO暂不支持取消B2B出库单");
    }

    /**
     * 查询 WEGO 2C 出库单（按 referenceCode 反查）。
     *
     * <p>{@code erpOrderCode} 在所有调用路径上均为 ERP 侧的 WFHD 参考号（非 WEGO 内部单号），
     * 直接通过 {@code 2c.order.queryPage} 按近 {@value REFERENCE_CODE_FALLBACK_HOURS} 小时
     * 时间窗口拉取，再按 {@code referenceCode} 过滤定位目标订单。</p>
     *
     * <p>这样即使建单后因网络超时未能拿到 WEGO 单号，幂等重试时也能感知订单已存在，
     * 从而避免重复建单。</p>
     */
    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq) {
        String referenceCode = queryOutboundReq.getErpOrderCode();
        if (CharSequenceUtil.isBlank(referenceCode)) {
            return failure("WEGO查询出库单参考号不能为空");
        }
        Map<String, Object> authMap = ThirdWarehouseContext.getAuthMap();
        if (authMap == null || authMap.isEmpty()) {
            throw new ServiceException("WEGO授权信息为空");
        }
        String accessToken = toStr(authMap.get(AUTH_KEY_APP_TOKEN));
        String secret = toStr(authMap.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(accessToken, secret)) {
            throw new ServiceException("WEGO授权信息appToken/appSecret缺失");
        }

        log.info("{}查询出库单（按 referenceCode），referenceCode={}", getPlatForm().getName(), referenceCode);
        return queryByReferenceCodeFallback(accessToken, secret, referenceCode);
    }

    /**
     * 通过 {@code 2c.order.queryPage} 在近 {@value REFERENCE_CODE_FALLBACK_HOURS} 小时订单中
     * 按 {@code referenceCode} 反查出库单，自动翻页直到找到匹配项或全部扫描完毕。
     *
     * <p>每页最多 {@link WegoOutboundQueryPageDTO#MAX_PAGE_SIZE} 条（WEGO 上限 100）；
     * 第一页响应的 {@code pages} 字段决定总页数，后续逐页拉取直到命中或超出范围。
     * DMP 轮询等已知 WEGO 单号的场景不走此路径（已在第一段按单号找到）。</p>
     */
    private ApiResult<ThirdWarehouseQueryOutboundResponse> queryByReferenceCodeFallback(
            String accessToken, String secret, String referenceCode) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime begin = now.minusHours(REFERENCE_CODE_FALLBACK_HOURS);
        String orderDateBegin = begin.format(QUERY_DATE_FORMATTER);
        String orderDateEnd = now.format(QUERY_DATE_FORMATTER);

        int pageNum = 1;
        int totalPages = 1;
        int totalScanned = 0;

        do {
            WegoOutboundQueryPageDTO.QueryReqDTO pageReq = WegoOutboundQueryPageDTO.QueryReqDTO.builder()
                    .accessToken(accessToken)
                    .secret(secret)
                    .orderDateBegin(orderDateBegin)
                    .orderDateEnd(orderDateEnd)
                    .pageNum(pageNum)
                    .pageSize(WegoOutboundQueryPageDTO.MAX_PAGE_SIZE)
                    .build();

            WegoOutboundResp pageResp = wegoOpenApiService.query2cOrderPage(pageReq);
            if (pageResp == null) {
                log.error("{}queryPage 降级查询接口响应为空, referenceCode={}, pageNum={}",
                        getPlatForm().getName(), referenceCode, pageNum);
                throw new ServiceException("WEGO查询出库单降级查询接口响应为空，referenceCode=" + referenceCode);
            }
            if (!Boolean.TRUE.equals(pageResp.getSuccess())) {
                log.error("{}queryPage 降级查询接口返回失败: errorCode={}, errorMsg={}, referenceCode={}, pageNum={}",
                        getPlatForm().getName(), pageResp.getErrorCode(), pageResp.getErrorMsg(), referenceCode, pageNum);
                throw new ServiceException("WEGO查询出库单降级查询接口返回失败: errorCode=" + pageResp.getErrorCode()
                        + ", errorMsg=" + pageResp.getErrorMsg());
            }
            if (pageResp.getResult() == null || CollUtil.isEmpty(pageResp.getResult().getList())) {
                log.warn("{}queryPage 降级查询第{}页无数据，停止翻页, referenceCode={}",
                        getPlatForm().getName(), pageNum, referenceCode);
                break;
            }

            WegoOutboundResp.PageResultDTO page = pageResp.getResult();
            // 首页时读取总页数
            if (pageNum == 1 && page.getPages() != null && page.getPages() > 0) {
                totalPages = page.getPages();
            }
            totalScanned += page.getList().size();

            WegoOutboundResp.OutboundOrderDTO matched = page.getList().stream()
                    .filter(o -> referenceCode.equals(o.getReferenceCode()))
                    .findFirst()
                    .orElse(null);

            if (matched != null) {
                String trackNo = extractTrackNo(matched.getLogisticsList());
                log.info("{}queryPage 降级查询成功（第{}/{}页）, referenceCode={}, wegoNo={}, trackNo={}",
                        getPlatForm().getName(), pageNum, totalPages, referenceCode, matched.getNo(), trackNo);
                return success(ThirdWarehouseQueryOutboundResponse.builder()
                        .shippingOrderNo(matched.getNo())
                        .trackNo(trackNo)
                        .build());
            }

            pageNum++;
        } while (pageNum <= totalPages && pageNum <= REFERENCE_CODE_FALLBACK_MAX_PAGES);

        if (pageNum > REFERENCE_CODE_FALLBACK_MAX_PAGES) {
            log.warn("{}queryPage 降级查询达到最大翻页上限({})仍未命中, referenceCode={}, 近{}小时共扫描{}条",
                    getPlatForm().getName(), REFERENCE_CODE_FALLBACK_MAX_PAGES,
                    referenceCode, REFERENCE_CODE_FALLBACK_HOURS, totalScanned);
        } else {
            log.warn("{}queryPage 降级查询完成但未匹配, referenceCode={}, 近{}小时共扫描{}条（{}页）",
                    getPlatForm().getName(), referenceCode, REFERENCE_CODE_FALLBACK_HOURS,
                    totalScanned, pageNum - 1);
        }
        return failure("WEGO未查询到对应出库单（referenceCode=" + referenceCode + "）");
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        return failure("WEGO暂不支持查询B2B出库单");
    }

    /**
     * 将 {@link ThirdWarehouseCreateOutboundReq} 转换为 WEGO {@code 2c.order.save} 请求 DTO。
     *
     * <p>字段映射说明：</p>
     * <ul>
     *     <li>{@code referenceNo}（WFHD 三方仓发货单号）→ {@code referenceCode}，供 WEGO 回传时关联 ERP 单据；</li>
     *     <li>{@code receiverInfo} → 收件人相关字段，address1/2/3 拼接为单一 {@code receiverAddress}；</li>
     *     <li>有 {@code labelUrl} 时 {@code wayBillType=1}（平台指定面单），否则 {@code wayBillType=0}（WEGO 自动生成）；</li>
     *     <li>{@code needSendFlag=0}（需要派送）、{@code needPackFlag=1}（不需要包装）为默认值；</li>
     *     <li>{@code products} 取 {@code items[].productSku}（平台 SKU，已由上层 sku_mapping 转换完成）。</li>
     * </ul>
     *
     * @param req         ERP 统一出库单请求
     * @param wegoOrderNo WEGO 出库单号：新增传 null，修改传已有单号
     * @param accessToken WEGO appToken
     * @param secret      WEGO appSecret
     */
    private WegoOutboundSaveDTO.SaveReqDTO buildOutboundSaveDto(
            ThirdWarehouseCreateOutboundReq req, String wegoOrderNo,
            String accessToken, String secret) {

        if (CollUtil.isEmpty(req.getItems())) {
            throw new ServiceException("出库明细不能为空");
        }

        ThirdWarehouseCreateOutboundReq.ReceiverInfo receiver = req.getReceiverInfo();

        // 有 labelUrl 时用平台指定面单（wayBillType=1），否则由 WEGO 生成（wayBillType=0）
        boolean hasPlatformLabel = CharSequenceUtil.isNotBlank(req.getLabelUrl());
        int wayBillType = hasPlatformLabel ? 1 : 0;
        WegoOutboundSaveDTO.WayBillUrl wayBillUrl = null;
        if (hasPlatformLabel) {
            List<WegoOutboundSaveDTO.WayBillFile> files = Collections.singletonList(
                    WegoOutboundSaveDTO.WayBillFile.builder().fileName("面单信息").fileUrl(req.getLabelUrl()).build());
            wayBillUrl = WegoOutboundSaveDTO.WayBillUrl.builder()
                    .logisticsName(req.getShippingMethodName())
                    .trackingNum(req.getTrackingNo())
                    .files(files)
                    .build();
        }

        List<WegoOutboundSaveDTO.Product> products = new ArrayList<>();
        if (CollUtil.isNotEmpty(req.getItems())) {
            req.getItems().forEach(item -> products.add(
                    WegoOutboundSaveDTO.Product.builder()
                            .sku(item.getProductSku())
                            .qty(item.getQuantity())
                            .build()));
        }

        return WegoOutboundSaveDTO.SaveReqDTO.builder()
                .accessToken(accessToken)
                .secret(secret)
                .no(wegoOrderNo)
                .warehouseBusiness(getPlatForm().getName())
                .warehouseCode(req.getWarehouseCode())
                .receiver(receiver != null ? receiver.getName() : null)
                .receiverPhone(receiver != null ? receiver.getPhone() : null)
                .receiverPostCode(receiver != null ? receiver.getZipCode() : null)
                .receiverEmail(receiver != null ? receiver.getEmail() : null)
                .receiverProvince(receiver != null ? receiver.getProvince() : null)
                .receiverCity(receiver != null ? receiver.getCity() : null)
                .receiverArea(receiver != null ? receiver.getDistrict() : null)
                .receiverAddress(buildReceiverAddress(receiver))
                .referenceCode(req.getReferenceNo())
                .shopName(req.getShopName())
                .remark(req.getBuyerRemark())
                .needSendFlag(0)
                .needPackFlag(1)
                .wayBillType(wayBillType)
                .wayBillUrl(wayBillUrl)
                .logisticsName(req.getShippingMethodName())
                .products(products)
                .build();
    }

    /**
     * 拼接收件人地址：address1 + address2 + address3，以空格分隔，去除首尾空白。
     */
    private String buildReceiverAddress(ThirdWarehouseCreateOutboundReq.ReceiverInfo receiver) {
        if (receiver == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        appendIfNotBlank(sb, receiver.getAddress1());
        appendIfNotBlank(sb, receiver.getAddress2());
        appendIfNotBlank(sb, receiver.getAddress3());
        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private void appendIfNotBlank(StringBuilder sb, String value) {
        if (CharSequenceUtil.isNotBlank(value)) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(value.trim());
        }
    }

    /**
     * 构造 WEGO {@code 2c.order.intercept} 截单请求 DTO。
     */
    private WegoOutboundInterceptDTO.InterceptReqDTO buildInterceptDto(ThirdWarehouseCancelOutboundReq cancelReq) {
        Map<String, Object> authMap = ThirdWarehouseContext.getAuthMap();
        if (authMap == null || authMap.isEmpty()) {
            throw new ServiceException("WEGO授权信息为空");
        }
        String accessToken = toStr(authMap.get(AUTH_KEY_APP_TOKEN));
        String secret = toStr(authMap.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(accessToken, secret)) {
            throw new ServiceException("WEGO授权信息appToken/appSecret缺失");
        }
        return WegoOutboundInterceptDTO.InterceptReqDTO.builder()
                .accessToken(accessToken)
                .secret(secret)
                .no(cancelReq.getOrderCode())
                .build();
    }

    /**
     * 从 {@code logisticsList} 中取第一条非空物流跟踪号（即 ERP trackNo）。
     */
    private String extractTrackNo(List<WegoOutboundResp.LogisticsDTO> logisticsList) {
        if (CollUtil.isEmpty(logisticsList)) {
            return null;
        }
        return logisticsList.stream()
                .filter(l -> CharSequenceUtil.isNotBlank(l.getTrackingNum()))
                .map(WegoOutboundResp.LogisticsDTO::getTrackingNum)
                .findFirst()
                .orElse(null);
    }

    /**
     * 从 WEGO 响应中提取 {@code result} 字符串字段（2c.order.save 成功时为出库单号）。
     */
    private String extractStringResult(JSONObject resp) {
        if (resp == null) {
            return "";
        }
        Object result = resp.get(RESP_FIELD_RESULT);
        if (result instanceof String && CharSequenceUtil.isNotBlank((String) result)) {
            return (String) result;
        }
        return "";
    }

    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        // WEGO 授权属于特殊场景，无需调用第三方授权接口，仅在落库前校验必填字段。
        Map<String, Object> authJson = dto.getAuthJson();
        if (authJson == null || authJson.isEmpty()) {
            throw new ServiceException("WEGO授权信息不能为空");
        }
        if (CharSequenceUtil.hasBlank(toStr(authJson.get(AUTH_KEY_APP_TOKEN)),
                toStr(authJson.get(AUTH_KEY_APP_SECRET)))) {
            throw new ServiceException("WEGO授权信息appToken/appSecret不能为空");
        }
        return true;
    }

    /**
     * WEGO 响应统一以 success=true 判定调用成功。
     */
    private boolean isSuccess(JSONObject resp) {
        if (resp == null) {
            return false;
        }
        return Boolean.TRUE.equals(resp.getBoolean(RESP_FIELD_SUCCESS));
    }

    /**
     * 拼装 WEGO 接口错误信息。
     * <p>
     * WEGO 在部分场景下（如派送渠道/SKU 校验失败）会将详细原因放在 {@code result} 字符串字段，
     * 而 {@code errorMsg} 仅返回无意义的内部错误码（如 {@code tocOrder.error.dataError}）。
     * 因此优先将 {@code result} 字符串拼入返回信息，确保调用方能看到具体原因。
     * </p>
     * 优先级：errorMsg + result（detail）> errorMsg > errorCode > 默认文案
     */
    private String buildErrorMessage(JSONObject resp) {
        if (resp == null) {
            return "WEGO接口返回为空";
        }
        String errorMsg = resp.getString(RESP_FIELD_ERROR_MSG);
        // result 在报错时常含详细原因（如"派送渠道错误：xxx,sku错误：xxx"），优先直接返回
        Object resultObj = resp.get(RESP_FIELD_RESULT);
        if (resultObj instanceof String && CharSequenceUtil.isNotBlank((String) resultObj)) {
            return (String) resultObj;
        }
        if (CharSequenceUtil.isNotBlank(errorMsg)) {
            return errorMsg;
        }
        String errorCode = resp.getString(RESP_FIELD_ERROR_CODE);
        return CharSequenceUtil.isNotBlank(errorCode) ? "WEGO接口错误码: " + errorCode : "WEGO接口调用失败";
    }

    /**
     * 判断 WEGO 截单是否因订单已处于截单/取消终态而返回幂等响应（Case A）。
     *
     * <p>已知的两种幂等场景：</p>
     * <ul>
     *   <li><b>errorCode=2003</b>：{@code "errorMsg":"订单已截单"} —— 通过 ERP 已截单后重复调用</li>
     *   <li><b>errorMsg 含"操作成功"</b>：{@code "errorMsg":"操作成功!"} —— 在 WEGO 后台手动取消后调用</li>
     * </ul>
     * <p>以上两种情况订单均已进入终态，应视为幂等成功（Case A）。</p>
     */
    private boolean isInterceptAlreadySuccessful(JSONObject resp) {
        if (resp == null || Boolean.TRUE.equals(resp.getBoolean(RESP_FIELD_SUCCESS))) {
            return false;
        }
        // 场景1：已截单（errorCode=2003）
        Integer errorCode = resp.getInteger(RESP_FIELD_ERROR_CODE);
        if (Integer.valueOf(WEGO_ERROR_CODE_INTERCEPTED).equals(errorCode)) {
            return true;
        }
        // 场景2：WEGO 后台手动取消后返回 success=false + errorMsg包含"操作成功!"
        String errorMsg = resp.getString(RESP_FIELD_ERROR_MSG);
        return CharSequenceUtil.isNotBlank(errorMsg) && errorMsg.contains(WEGO_INTERCEPT_IDEMPOTENT_KEYWORD);
    }

    /**
     * 从 WEGO 响应中提取入库单号。
     * <p>
     * 响应结构: {success, result: {no | inboundNo | ...}}，按常见字段顺序回退。
     */
    private String extractOrderNo(JSONObject resp) {
        if (resp == null) {
            return "";
        }
        Object result = resp.get(RESP_FIELD_RESULT);
        if (result instanceof JSONObject) {
            JSONObject resultObj = (JSONObject) result;
            String no = resultObj.getString("no");
            if (CharSequenceUtil.isNotBlank(no)) {
                return no;
            }
        }
        if (result instanceof String && CharSequenceUtil.isNotBlank((String) result)) {
            return (String) result;
        }
        return "";
    }

    private String toStr(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * 把 WEGO 请求 DTO 序列化为日志可输出的 JSON，
     * 对 accessToken（第三方授权）与 secret（本地签名密钥）做掩码处理，
     * 避免敏感凭据明文落到日志文件中。
     */
    private String toLogSafeJson(Object request) {
        if (request == null) {
            return "";
        }
        String raw = JSONUtil.toJsonStr(request);
        JSONObject json = JSONObject.parseObject(raw);
        if (json == null) {
            return raw;
        }
        if (json.containsKey(DTO_FIELD_ACCESS_TOKEN)) {
            json.put(DTO_FIELD_ACCESS_TOKEN, LOG_MASK);
        }
        if (json.containsKey(DTO_FIELD_SECRET)) {
            json.put(DTO_FIELD_SECRET, LOG_MASK);
        }
        return json.toJSONString();
    }
}
