package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.UnitEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
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
import com.sdk.wms.wego.enums.WegoEnums;
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
 * 仅在 {@link OmsPlatformEnum#WE_GO} 渠道下生效。
 * 入库单创建调用 WEGO {@code inorder.save}（autoCommit=true 自动提交）；
 * 提交后不支持编辑，需先取消再重新创建。
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

    /**
     * 截单前查询出库单最大尝试次数（含首次）
     */
    private static final int CANCEL_SEARCH_MAX_RETRY = 3;

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
        WegoInOrderSaveDTO.SaveReqDTO request = buildInorderSaveDto(createInboundReq);
        log.warn("{}创建入库单请求:{}", getPlatForm().getName(), toLogSafeJson(request));
        JSONObject resp = wegoOpenApiService.saveInorder(request);
        log.warn("{}创建入库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (!isSuccess(resp)) {
            return failure(buildErrorMessage(resp));
        }
        return success(extractOrderNo(resp));
    }

    /**
     * WEGO 创建入库单时已 autoCommit=true 自动提交，提交后不允许修改。
     * 需要变更时请先取消入库单再重新创建。
     */
    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        return failure(getPlatForm().getName() + "不支持编辑入库单，请先取消入库单后，重新创建");
    }

    /**
     * 构造 WEGO inorder.save 创建请求 DTO。
     * <p>
     * 固定传 {@code autoCommit=true}，创建后由 WEGO 自动提交且不可再改；
     * details 完整传箱明细，由 WEGO 落库。
     *
     * @param createInboundReq ERP 统一入库单请求
     * @return WEGO inorder.save 请求体
     */
    private WegoInOrderSaveDTO.SaveReqDTO buildInorderSaveDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        Map<String, Object> authMap = ThirdWarehouseContext.getAuthMap();
        if (authMap == null || authMap.isEmpty()) {
            throw new ServiceException(ApiError.WH_WEGO_AUTH_INFO_EMPTY);
        }
        String accessToken = toStr(authMap.get(AUTH_KEY_APP_TOKEN));
        String secret = toStr(authMap.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(accessToken, secret)) {
            throw new ServiceException(ApiError.WH_WEGO_AUTH_TOKEN_SECRET_MISSING);
        }
        return WegoInOrderSaveDTO.SaveReqDTO.builder()
                .accessToken(accessToken)
                .secret(secret)
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
                .autoCommit(Boolean.TRUE)
                .details(buildDetails(createInboundReq))
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
     *     <li>兜底：上层 {@link ThirdWarehouseCreateInboundReq#getItems()}（来自
     *         {@code OverseasWarehouseInboundServiceImpl#entityToCreateInboundBill}）。</li>
     * </ol>
     */
    private List<WegoInOrderSaveDTO.Detail> buildDetails(ThirdWarehouseCreateInboundReq createInboundReq) {
        List<WmsCartonSpecDTO.PackingItemDTO> packingItems = loadPackingList(createInboundReq.getReferenceNo());
        if (CollUtil.isEmpty(packingItems)) {
            log.warn("[WEGO入库] 装箱清单为空，referenceNo={}", createInboundReq.getReferenceNo());
            throw new ServiceException(ApiError.WH_WEGO_PACKING_LIST_EMPTY);
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
                throw new ServiceException(ApiError.WH_WEGO_PACKING_BOX_NO_VALID_SKU, first.getSourceCode(), boxNo);
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
            throw new ServiceException(ApiError.WH_WEGO_PACKING_LIST_MISSING_BOX_NO);
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
            throw new ServiceException(ApiError.WH_WEGO_INBOUND_CODE_REQUIRED);
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
            throw new ServiceException(ApiError.WH_WEGO_AUTH_INFO_EMPTY);
        }
        String accessToken = toStr(authMap.get(AUTH_KEY_APP_TOKEN));
        String secret = toStr(authMap.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(accessToken, secret)) {
            throw new ServiceException(ApiError.WH_WEGO_AUTH_TOKEN_SECRET_MISSING);
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
            throw new ServiceException(ApiError.WH_WEGO_AUTH_INFO_EMPTY);
        }
        String accessToken = toStr(authMap.get(AUTH_KEY_APP_TOKEN));
        String secret = toStr(authMap.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(accessToken, secret)) {
            throw new ServiceException(ApiError.WH_WEGO_AUTH_TOKEN_SECRET_MISSING);
        }

        WegoOutboundSaveDTO.SaveReqDTO request = buildOutboundSaveDto(createOutboundReq, null, accessToken, secret);
        if (CollUtil.isEmpty(request.getProducts())) {
            log.warn("{}创建出库单明细为空, referenceNo={}", getPlatForm().getName(), createOutboundReq.getReferenceNo());
            throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_DETAIL_EMPTY);
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

    /**
     * 取消/截单 WEGO 2C 出库单。
     * <p>
     * 按出库单当前状态分流：
     * <ul>
     *     <li>提交失败 / 出库异常：调用 {@code 2c.order.errorHandle} 异常出库取消，
     *         再按查询结果返回成功 / 拦截中 / 失败（对齐异步确认流程）；</li>
     *     <li>已取消：幂等成功；已出库/已签收：拦截失败；</li>
     *     <li>其余状态：沿用 {@code 2c.order.intercept} 普通截单。</li>
     * </ul>
     */
    @Override
    protected ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        if (CharSequenceUtil.isBlank(cancelOutboundReq.getOrderCode())) {
            throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_CODE_REQUIRED);
        }
        String[] auth = resolveAccessTokenAndSecret();
        String accessToken = auth[0];
        String secret = auth[1];
        String orderNo = cancelOutboundReq.getOrderCode();

        // 截单前必须拿到目标单精确状态，再决定 errorHandle / intercept；
        // 查询异常或未命中时禁止降级普通截单，避免异常单误走 2c.order.intercept。
        WegoOutboundResp.OutboundOrderDTO currentOrder;
        try {
            currentOrder = searchOutboundByNoWithRetry(accessToken, secret, orderNo);
        } catch (Exception e) {
            log.warn("{}截单前查询出库单失败（已重试），返回可重试失败, orderNo={}",
                    getPlatForm().getName(), orderNo, e);
            return failure("WEGO截单前查询出库单失败，请稍后重试");
        }
        if (currentOrder == null) {
            log.warn("{}截单前未查询到目标出库单, orderNo={}", getPlatForm().getName(), orderNo);
            return failure("WEGO未查询到出库单（" + orderNo + "），无法确定状态，请稍后重试");
        }
        if (currentOrder.getOrderStatus() == null) {
            log.warn("{}截单前出库单状态为空, orderNo={}", getPlatForm().getName(), orderNo);
            return failure("WEGO出库单状态为空，无法确定取消方式，请稍后重试");
        }
        String orderStatus = String.valueOf(currentOrder.getOrderStatus());

        // 已取消：幂等成功
        if (WegoEnums.OrderStatusEnum.CANCELLED.getCode().equals(orderStatus)) {
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }
        // 已出库/已签收：无法线上拦截
        if (WegoEnums.OrderStatusEnum.SHIPPED.getCode().equals(orderStatus)
                || WegoEnums.OrderStatusEnum.SIGNED.getCode().equals(orderStatus)) {
            return ApiResult.success("出库单已发货/已签收，无法线上拦截",
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }

        // 提交失败 / 出库异常：走异常出库取消接口
        if (isExceptionOutboundStatus(orderStatus)) {
            return cancelExceptionOutbound(accessToken, secret, orderNo);
        }

        // 已明确为非异常态：普通截单
        return cancelNormalOutbound(cancelOutboundReq);
    }

    /**
     * 调用 {@code 2c.order.errorHandle} 取消异常态出库单，并按回查结果确认拦截终态。
     *
     * @param accessToken WEGO accessToken
     * @param secret      WEGO secret
     * @param orderNo     WEGO 出库单号
     * @return 拦截成功 / 拦截中 / 拦截失败
     */
    private ApiResult<String> cancelExceptionOutbound(String accessToken, String secret, String orderNo) {
        WegoOutboundErrorHandleDTO.ErrorHandleReqDTO request = WegoOutboundErrorHandleDTO.ErrorHandleReqDTO.builder()
                .accessToken(accessToken)
                .secret(secret)
                .no(orderNo)
                .build();
        log.warn("{}异常出库取消请求:{}", getPlatForm().getName(), toLogSafeJson(request));
        JSONObject resp = wegoOpenApiService.errorHandle2cOrder(request);
        log.warn("{}异常出库取消结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));

        if (!isSuccess(resp) && !isInterceptAlreadySuccessful(resp)) {
            return failure(buildErrorMessage(resp));
        }
        // 取消接口受理后回查确认：已取消=成功，已出库=失败，仍异常/处理中=拦截中（待 DMP 轮询裁决）
        return resolveCancelResultByQuery(accessToken, secret, orderNo, true);
    }

    /**
     * 调用 {@code 2c.order.intercept} 普通截单（非异常态）。
     *
     * @param cancelOutboundReq 取消请求
     * @return 拦截成功或失败
     */
    private ApiResult<String> cancelNormalOutbound(ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        WegoOutboundInterceptDTO.InterceptReqDTO request = buildInterceptDto(cancelOutboundReq);
        log.warn("{}截单请求:{}", getPlatForm().getName(), toLogSafeJson(request));
        JSONObject resp = wegoOpenApiService.intercept2cOrder(request);
        log.warn("{}截单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (isSuccess(resp)) {
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }
        if (isInterceptAlreadySuccessful(resp)) {
            String wegoMsg = CharSequenceUtil.blankToDefault(resp.getString(RESP_FIELD_ERROR_MSG), "WEGO订单已取消");
            log.warn("{}截单幂等命中，视为拦截成功, orderNo={}, wegoMsg={}",
                    getPlatForm().getName(), cancelOutboundReq.getOrderCode(), wegoMsg);
            return ApiResult.success("WEGO订单已截单/取消，视为拦截成功（" + wegoMsg + "）",
                    ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }
        return failure(buildErrorMessage(resp));
    }

    /**
     * 取消受理后按出库单最新状态确认拦截结果。
     *
     * @param accessToken         WEGO accessToken
     * @param secret              WEGO secret
     * @param orderNo             WEGO 出库单号
     * @param defaultIntercepting 回查失败或状态未决时是否按「拦截中」处理
     * @return 拦截成功 / 拦截中 / 拦截失败
     */
    private ApiResult<String> resolveCancelResultByQuery(String accessToken, String secret,
                                                         String orderNo, boolean defaultIntercepting) {
        WegoOutboundResp.OutboundOrderDTO order;
        try {
            order = searchOutboundByNo(accessToken, secret, orderNo);
        } catch (Exception e) {
            log.warn("{}取消后回查出库单异常, orderNo={}", getPlatForm().getName(), orderNo, e);
            if (defaultIntercepting) {
                return success(ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
            }
            return failure("WEGO取消后回查出库单失败，请稍后重试");
        }
        if (order == null || order.getOrderStatus() == null) {
            return defaultIntercepting
                    ? success(ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode())
                    : failure("WEGO取消后未查询到出库单");
        }
        String status = String.valueOf(order.getOrderStatus());
        if (WegoEnums.OrderStatusEnum.CANCELLED.getCode().equals(status)) {
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        }
        if (WegoEnums.OrderStatusEnum.SHIPPED.getCode().equals(status)
                || WegoEnums.OrderStatusEnum.SIGNED.getCode().equals(status)) {
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_FAILED.getCode());
        }
        // 仍为提交失败/出库异常或其他处理中状态：等待 DMP 轮询确认
        return success(ThirdWarehouseCancelResultEnum.INTERCEPTING.getCode());
    }

    /**
     * 截单前查询出库单，失败时有限次重试。
     *
     * @param accessToken WEGO accessToken
     * @param secret      WEGO secret
     * @param orderNo     WEGO 出库单号
     * @return 精确匹配的出库单；接口成功但无匹配时返回 null
     * @throws Exception 重试耗尽后仍查询失败
     */
    private WegoOutboundResp.OutboundOrderDTO searchOutboundByNoWithRetry(String accessToken, String secret,
                                                                          String orderNo) throws Exception {
        Exception last = null;
        for (int attempt = 1; attempt <= CANCEL_SEARCH_MAX_RETRY; attempt++) {
            try {
                return searchOutboundByNo(accessToken, secret, orderNo);
            } catch (Exception e) {
                last = e;
                log.warn("{}截单前查询出库单第{}次失败, orderNo={}",
                        getPlatForm().getName(), attempt, orderNo, e);
            }
        }
        throw last;
    }

    /**
     * 按 WEGO 出库单号精确查询单条出库单。
     * <p>
     * 仅返回 {@code no} 完全匹配的记录；列表为空或无精确匹配时返回 {@code null}，
     * 禁止回退 {@code list.get(0)}，避免用其他订单状态做截单路由。
     *
     * @param accessToken WEGO accessToken
     * @param secret      WEGO secret
     * @param orderNo     WEGO 出库单号
     * @return 出库单；未精确命中返回 null
     */
    private WegoOutboundResp.OutboundOrderDTO searchOutboundByNo(String accessToken, String secret, String orderNo) {
        WegoOutboundSearchDTO.SearchReqDTO searchReq = WegoOutboundSearchDTO.SearchReqDTO.builder()
                .accessToken(accessToken)
                .secret(secret)
                .noList(Collections.singletonList(orderNo))
                .build();
        List<WegoOutboundResp.OutboundOrderDTO> list = wegoOpenApiService.search2cOrder(searchReq);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return list.stream()
                .filter(o -> o != null && orderNo.equals(o.getNo()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 是否为需走 {@code 2c.order.errorHandle} 的异常出库状态（提交失败 / 出库异常）。
     *
     * @param orderStatus WEGO orderStatus 字符串
     * @return true=异常态
     */
    private boolean isExceptionOutboundStatus(String orderStatus) {
        return WegoEnums.OrderStatusEnum.SUBMIT_FAIL.getCode().equals(orderStatus)
                || WegoEnums.OrderStatusEnum.OUTBOUND_EXCEPTION.getCode().equals(orderStatus);
    }

    /**
     * 从上下文解析 WEGO accessToken / secret。
     *
     * @return [accessToken, secret]
     */
    private String[] resolveAccessTokenAndSecret() {
        Map<String, Object> authMap = ThirdWarehouseContext.getAuthMap();
        if (authMap == null || authMap.isEmpty()) {
            throw new ServiceException(ApiError.WH_WEGO_AUTH_INFO_EMPTY);
        }
        String accessToken = toStr(authMap.get(AUTH_KEY_APP_TOKEN));
        String secret = toStr(authMap.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(accessToken, secret)) {
            throw new ServiceException(ApiError.WH_WEGO_AUTH_TOKEN_SECRET_MISSING);
        }
        return new String[]{accessToken, secret};
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
            throw new ServiceException(ApiError.WH_WEGO_AUTH_INFO_EMPTY);
        }
        String accessToken = toStr(authMap.get(AUTH_KEY_APP_TOKEN));
        String secret = toStr(authMap.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(accessToken, secret)) {
            throw new ServiceException(ApiError.WH_WEGO_AUTH_TOKEN_SECRET_MISSING);
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
                throw new ServiceException(ApiError.WH_WEGO_QUERY_FALLBACK_EMPTY_RESPONSE, referenceCode);
            }
            if (!Boolean.TRUE.equals(pageResp.getSuccess())) {
                log.error("{}queryPage 降级查询接口返回失败: errorCode={}, errorMsg={}, referenceCode={}, pageNum={}",
                        getPlatForm().getName(), pageResp.getErrorCode(), pageResp.getErrorMsg(), referenceCode, pageNum);
                throw new ServiceException(ApiError.WH_WEGO_QUERY_FALLBACK_FAILED, pageResp.getErrorCode(), pageResp.getErrorMsg());
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
            throw new ServiceException(ApiError.WH_WEGO_OUTBOUND_DETAIL_EMPTY);
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
            // 同一 productSku 可能出现在多行明细中，需按 SKU 聚合数量后再传给 WEGO，避免重复条目导致拒单或数量统计异常
            Map<String, Integer> skuQtyMap = new LinkedHashMap<>();
            for (ThirdWarehouseCreateOutboundReq.Item item : req.getItems()) {
                if (CharSequenceUtil.isBlank(item.getProductSku()) || item.getQuantity() == null) {
                    continue;
                }
                skuQtyMap.merge(item.getProductSku(), item.getQuantity(), Integer::sum);
            }
            skuQtyMap.forEach((sku, qty) -> products.add(
                    WegoOutboundSaveDTO.Product.builder()
                            .sku(sku)
                            .qty(qty)
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
        String[] auth = resolveAccessTokenAndSecret();
        return WegoOutboundInterceptDTO.InterceptReqDTO.builder()
                .accessToken(auth[0])
                .secret(auth[1])
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
            throw new ServiceException(ApiError.WH_WEGO_AUTH_INFO_EMPTY);
        }
        if (CharSequenceUtil.hasBlank(toStr(authJson.get(AUTH_KEY_APP_TOKEN)),
                toStr(authJson.get(AUTH_KEY_APP_SECRET)))) {
            throw new ServiceException(ApiError.WH_WEGO_AUTH_TOKEN_SECRET_MISSING);
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
        //已截单（errorCode=2003） 已讨论直接用文档里面的code进行判断是否已截单
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
