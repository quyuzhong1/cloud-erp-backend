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
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WegoInOrderSaveDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.erp.server.wms.service.FirstMileDeliveryService;
import com.erp.server.wms.service.WmsCartonDetailService;
import com.sdk.wms.wego.service.WegoOpenApiService;
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
        log.warn("{}创建入库单请求:{}", getPlatForm().getName(), JSONUtil.toJsonStr(request));
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
        log.warn("{}修改入库单请求:{}", getPlatForm().getName(), JSONUtil.toJsonStr(request));
        JSONObject resp = wegoOpenApiService.saveInorder(request);
        log.warn("{}修改入库单结果:{}", getPlatForm().getName(), JSONUtil.toJsonStr(resp));
        if (!isSuccess(resp)) {
            return failure(buildErrorMessage(resp));
        }
        return success(CharSequenceUtil.blankToDefault(extractOrderNo(resp), createInboundReq.getReceivingCode()));
    }

    /**
     * 构造 WEGO inorder.save 请求 DTO。
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
        if (CollUtil.isNotEmpty(packingItems)) {
            return buildDetailsFromPackingList(packingItems);
        }
        log.warn("[WEGO入库] 装箱清单为空，回退到 items 兜底, referenceNo={}", createInboundReq.getReferenceNo());
        return buildDetailsFromItems(createInboundReq);
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
            WegoInOrderSaveDTO.Detail detail = WegoInOrderSaveDTO.Detail.builder()
                    .inOrderDetailId(null)
                    .boxQty(1)
                    .boxLabel(null)
                    .boxLength(toIntegerCm(first.getBoxLength()))
                    .boxWidth(toIntegerCm(first.getBoxWidth()))
                    .boxHeight(toIntegerCm(first.getBoxHeight()))
                    .boxWeight(toIntegerKg(first.getPackageWeight(), first.getWeightUnit()))
                    .deletedFlag(false)
                    .products(buildProductsFromPackingList(list))
                    .build();
            details.add(detail);
        });
        return details;
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
     * 兜底：用上层 {@link ThirdWarehouseCreateInboundReq#getItems()} 构造 details。
     * <p>
     * 仅在装箱清单为空时使用，逻辑与装箱清单分支等价：按 {@code boxNo} 分组、每箱一条、多 SKU 进 products。
     */
    private List<WegoInOrderSaveDTO.Detail> buildDetailsFromItems(ThirdWarehouseCreateInboundReq createInboundReq) {
        List<ThirdWarehouseCreateInboundReq.Item> items = createInboundReq.getItems();
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<ThirdWarehouseCreateInboundReq.Item> sortedItems = new ArrayList<>(items);
        sortedItems.sort(Comparator.comparing(ThirdWarehouseCreateInboundReq.Item::getBoxNo,
                Comparator.nullsLast(Comparator.naturalOrder())));
        Map<Integer, List<ThirdWarehouseCreateInboundReq.Item>> boxItemMap = sortedItems.stream()
                .collect(Collectors.groupingBy(
                        ThirdWarehouseCreateInboundReq.Item::getBoxNo,
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<WegoInOrderSaveDTO.Detail> details = new ArrayList<>(boxItemMap.size());
        boxItemMap.forEach((boxNo, itemList) -> {
            ThirdWarehouseCreateInboundReq.Item firstItem = itemList.get(0);
            WegoInOrderSaveDTO.Detail detail = WegoInOrderSaveDTO.Detail.builder()
                    .inOrderDetailId(null)
                    .boxQty(1)
                    .boxLabel(null)
                    .boxLength(toIntegerCm(firstItem.getBoxLength()))
                    .boxWidth(toIntegerCm(firstItem.getBoxWidth()))
                    .boxHeight(toIntegerCm(firstItem.getBoxHeight()))
                    .boxWeight(toIntegerKg(firstItem.getPackageWeight(), firstItem.getWeightUnit()))
                    .deletedFlag(false)
                    .products(buildProductsFromItems(itemList))
                    .build();
            details.add(detail);
        });
        return details;
    }

    /**
     * 兜底：箱内 items → products 聚合。
     */
    private List<WegoInOrderSaveDTO.Product> buildProductsFromItems(List<ThirdWarehouseCreateInboundReq.Item> itemList) {
        Map<String, Integer> skuQtyMap = new LinkedHashMap<>();
        for (ThirdWarehouseCreateInboundReq.Item item : itemList) {
            if (CharSequenceUtil.isBlank(item.getProductSku()) || item.getQuantity() == null) {
                continue;
            }
            skuQtyMap.merge(item.getProductSku(), item.getQuantity(), Integer::sum);
        }
        List<WegoInOrderSaveDTO.Product> products = new ArrayList<>(skuQtyMap.size());
        skuQtyMap.forEach((sku, qty) -> products.add(WegoInOrderSaveDTO.Product.builder()
                .sku(sku)
                .qty(qty)
                .build()));
        return products;
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
            kg = value.divide(new BigDecimal(1000), 4, RoundingMode.HALF_UP);
        }
        return kg.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    @Override
    protected ApiResult<String> cancelInboundBill(@Valid ThirdWarehouseCancelInboundReq cancelInboundReq) {
        return failure("WEGO暂不支持取消入库单，请前往WEGO后台操作");
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
        return failure("WEGO暂不支持创建2C出库单");
    }

    @Override
    protected ApiResult<String> createFbaOutboundBill(ThirdWarehouseCreateFbaOutboundReq createOutboundReq) {
        return failure("WEGO暂不支持创建B2B出库单");
    }

    @Override
    protected ApiResult<String> cancelOutboundBill(@Valid ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        return failure("WEGO暂不支持取消2C出库单");
    }

    @Override
    protected ApiResult<String> cancelFbaOutboundBill(ThirdWarehouseCancelFbaOutboundReq cancelOutboundReq) {
        return failure("WEGO暂不支持取消B2B出库单");
    }

    @Override
    protected ApiResult<ThirdWarehouseQueryOutboundResponse> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq) {
        return failure("WEGO暂不支持查询2C出库单");
    }

    @Override
    protected ApiResult<List<ThirdWarehouseQueryFbaOutboundResponse>> queryFbaOutboundBill(ThirdWarehouseQueryFbaOutboundReq req) {
        return failure("WEGO暂不支持查询B2B出库单");
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
        return Boolean.TRUE.equals(resp.getBoolean("success"));
    }

    /**
     * 拼装 WEGO 接口错误信息：优先取 errorMsg，缺失时回退到 errorCode。
     */
    private String buildErrorMessage(JSONObject resp) {
        if (resp == null) {
            return "WEGO接口返回为空";
        }
        String errorMsg = resp.getString("errorMsg");
        if (CharSequenceUtil.isNotBlank(errorMsg)) {
            return errorMsg;
        }
        String errorCode = resp.getString("errorCode");
        return CharSequenceUtil.isNotBlank(errorCode) ? "WEGO接口错误码: " + errorCode : "WEGO接口调用失败";
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
        Object result = resp.get("result");
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
}
