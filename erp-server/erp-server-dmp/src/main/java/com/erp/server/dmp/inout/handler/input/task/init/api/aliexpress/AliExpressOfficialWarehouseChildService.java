package com.erp.server.dmp.inout.handler.input.task.init.api.aliexpress;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.enums.AliexpressDeliveryOrderStatusEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.dto.response.AliExpressWarehouseInventoryDTO.InventoryLogDTO;
import com.erp.oms.aliexpress.dto.response.AliExpressWarehouseInventoryDTO.ScItemDTO;
import com.erp.oms.aliexpress.dto.response.AliExpressWarehouseInventoryDTO.ScItemRelationDTO;
import com.erp.oms.aliexpress.dto.response.AliExpressWarehouseInventoryDTO.ShopItemRelationDTO;
import com.erp.oms.aliexpress.service.AliExpressWarehouseInventoryService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 速卖通海外托管官方仓发货子任务服务。
 */
@Slf4j
@Service
public class AliExpressOfficialWarehouseChildService {

    private static final int MAX_TRADE_IDS_PER_REQUEST = 5;
    private static final String OUTSTOCK_ITEMS = "official_outstock_items";
    private static final String CAINIAO_LOGISTICS_PREFIX = "CAINIAO_";
    private static final String AE_OFFICIAL_WAREHOUSE_LOGISTICS_PREFIX = "AE_LOCAL_";

    @Resource
    private AliExpressWarehouseInventoryService warehouseInventoryService;

    /**
     * 根据订单主任务数据查询官方仓库存扣减流水，并转换为现有发货主单结构。
     *
     * @param shopId 海外托管订单主任务授权店铺 ID
     * @param parentOrderData 订单列表主任务 Mongo 数据
     * @param orderDetailData 订单详情子任务 Mongo 数据
     * @param beginTime 主任务开始时间
     * @param endTime 主任务结束时间
     * @return 可由现有 soOutstock 链路处理的发货主单
     */
    public JSONArray queryOutstock(String shopId,
                                   List<Map<String, Object>> parentOrderData,
                                   List<Map<String, Object>> orderDetailData,
                                   LocalDateTime beginTime,
                                   LocalDateTime endTime) {
        if (StrUtil.isBlank(shopId) || CollUtil.isEmpty(parentOrderData)) {
            return new JSONArray();
        }
        ShopItemRelationDTO shopRelation = warehouseInventoryService.queryShopItemRelation(shopId);
        List<OrderContext> orderContexts = buildOrderContexts(
                parentOrderData, orderDetailData, shopRelation.getScItemList());
        if (CollUtil.isEmpty(orderContexts)) {
            return new JSONArray();
        }

        LocalDateTime queryBeginTime = Objects.isNull(beginTime)
                ? LocalDateTime.now().minusDays(30) : beginTime.minusDays(1);
        LocalDateTime queryEndTime = Objects.isNull(endTime)
                ? LocalDateTime.now().plusDays(1) : endTime.plusDays(1);
        JSONArray result = new JSONArray();
        for (int start = 0; start < orderContexts.size(); start += MAX_TRADE_IDS_PER_REQUEST) {
            int end = Math.min(start + MAX_TRADE_IDS_PER_REQUEST, orderContexts.size());
            result.addAll(queryBatch(
                    shopId,
                    shopRelation.getChannelSellerId(),
                    orderContexts.subList(start, end),
                    queryBeginTime,
                    queryEndTime));
        }
        log.info("速卖通海外托管发货子任务查询完成, shopId={}, orderCount={}, outstockCount={}",
                shopId, orderContexts.size(), result.size());
        return result;
    }

    /**
     * 将发货主单中携带的库存流水明细展开为现有发货明细结构。
     *
     * @param outstockData 发货主单 Mongo 数据
     * @return 可由现有 soOutstockDetail 链路处理的发货明细
     */
    public JSONArray flattenOutstockDetails(List<Map<String, Object>> outstockData) {
        JSONArray result = new JSONArray();
        if (CollUtil.isEmpty(outstockData)) {
            return result;
        }
        for (Map<String, Object> outstock : outstockData) {
            String fulfillmentOrderNo = text(outstock.get("fulfillment_order_no"));
            for (Map<String, Object> item : toMapList(outstock.get(OUTSTOCK_ITEMS))) {
                Map<String, Object> detail = new LinkedHashMap<>(item);
                detail.put("fulfillment_order_no", fulfillmentOrderNo);
                result.add(detail);
            }
        }
        return result;
    }

    /**
     * 从海外托管订单中筛选全部明细均为菜鸟仓的订单。
     *
     * @param parentOrderData 订单列表主任务 Mongo 数据
     * @param orderDetailData 订单详情子任务 Mongo 数据
     * @return 应使用菜鸟仓授权调用 FFO 接口的订单
     */
    public List<Map<String, Object>> selectCainiaoWarehouseOrders(
            List<Map<String, Object>> parentOrderData,
            List<Map<String, Object>> orderDetailData) {
        return selectWarehouseOrders(parentOrderData, orderDetailData, true);
    }

    /**
     * 从海外托管订单中筛选全部明细均非菜鸟仓的 AE 官方仓订单。
     *
     * @param parentOrderData 订单列表主任务 Mongo 数据
     * @param orderDetailData 订单详情子任务 Mongo 数据
     * @return 应使用海外托管授权查询库存流水的订单
     */
    public List<Map<String, Object>> selectOfficialWarehouseOrders(
            List<Map<String, Object>> parentOrderData,
            List<Map<String, Object>> orderDetailData) {
        return selectWarehouseOrders(parentOrderData, orderDetailData, false);
    }

    /**
     * 按订单明细仓型筛选海外托管订单。
     *
     * @param parentOrderData 订单列表主任务 Mongo 数据
     * @param orderDetailData 订单详情子任务 Mongo 数据
     * @param cainiaoWarehouse true 选择菜鸟仓，false 选择 AE 官方仓
     * @return 符合仓型的订单
     */
    private List<Map<String, Object>> selectWarehouseOrders(
            List<Map<String, Object>> parentOrderData,
            List<Map<String, Object>> orderDetailData,
            boolean cainiaoWarehouse) {
        if (CollUtil.isEmpty(parentOrderData) || CollUtil.isEmpty(orderDetailData)) {
            return Collections.emptyList();
        }
        Map<String, Map<String, Object>> detailByOrderId = orderDetailData.stream()
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isNotBlank(text(item.get("order_id"))))
                .collect(Collectors.toMap(
                        item -> text(item.get("order_id")),
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new));
        return parentOrderData.stream()
                .filter(Objects::nonNull)
                .filter(parentOrder -> {
                    Map<String, Object> detail = detailByOrderId.get(
                            text(parentOrder.get("order_id")));
                    List<Map<String, Object>> childOrders = detail == null
                            ? Collections.emptyList()
                            : toMapList(detail.get("child_order_list"));
                    if (CollUtil.isEmpty(childOrders)) {
                        return false;
                    }
                    boolean cainiaoFfoOrder = isCainiaoFfoOrder(detail, childOrders);
                    return cainiaoWarehouse == cainiaoFfoOrder;
                })
                .collect(Collectors.toList());
    }

    /**
     * 从混合发货主单中筛选由 FFO 接口返回的记录。
     *
     * @param outstockData 发货主单 Mongo 数据
     * @return 需要继续调用 FFO 明细接口的记录
     */
    public List<Map<String, Object>> selectFfoOutstockRows(
            List<Map<String, Object>> outstockData) {
        if (CollUtil.isEmpty(outstockData)) {
            return Collections.emptyList();
        }
        return outstockData.stream()
                .filter(Objects::nonNull)
                .filter(item -> !item.containsKey(OUTSTOCK_ITEMS))
                .collect(Collectors.toList());
    }

    /**
     * 构造订单与速卖通货品关系上下文。
     *
     * @param parentOrderData 订单列表数据
     * @param orderDetailData 订单详情数据
     * @param scItems 速卖通货品关系
     * @return 可查询库存流水的订单上下文
     */
    private List<OrderContext> buildOrderContexts(List<Map<String, Object>> parentOrderData,
                                                  List<Map<String, Object>> orderDetailData,
                                                  List<ScItemDTO> scItems) {
        List<Map<String, Object>> safeOrderDetailData = CollUtil.isEmpty(orderDetailData)
                ? Collections.emptyList() : orderDetailData;
        Map<String, Map<String, Object>> detailByOrderId = safeOrderDetailData.stream()
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isNotBlank(text(item.get("order_id"))))
                .collect(Collectors.toMap(
                        item -> text(item.get("order_id")),
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new));
        List<OrderContext> result = new ArrayList<>();
        for (Map<String, Object> parentOrder : parentOrderData) {
            if (Objects.isNull(parentOrder)) {
                continue;
            }
            String orderId = text(parentOrder.get("order_id"));
            if (StrUtil.isBlank(orderId)) {
                continue;
            }
            try {
                Map<String, Object> detail = detailByOrderId.get(orderId);
                List<Map<String, Object>> childOrders = detail == null
                        ? Collections.emptyList() : toMapList(detail.get("child_order_list"));
                if (CollUtil.isEmpty(childOrders)) {
                    throw new ServiceException("速卖通海外托管订单缺少订单详情，orderId:" + orderId);
                }
                List<Map<String, Object>> childOrderExtInfoList = toMapList(
                        detail.get("child_order_ext_info_list"));
                if (!isOfficialWarehouseShippedOrder(
                        orderId, parentOrder, detail, childOrders)) {
                    continue;
                }
                List<OrderItemContext> orderItems = new ArrayList<>();
                for (int index = 0; index < childOrders.size(); index++) {
                    Map<String, Object> childOrder = childOrders.get(index);
                    Map<String, Object> childOrderExtInfo =
                            index < childOrderExtInfoList.size()
                                    ? childOrderExtInfoList.get(index)
                                    : Collections.emptyMap();
                    childOrderExtInfo = Objects.isNull(childOrderExtInfo)
                            ? Collections.emptyMap() : childOrderExtInfo;
                    OrderItemContext itemContext = toOrderItemContext(
                            orderId, childOrder, childOrderExtInfo);
                    itemContext.setScItem(resolveScItem(itemContext, scItems));
                    orderItems.add(itemContext);
                }
                result.add(new OrderContext(orderId, orderItems));
            } catch (ServiceException e) {
                log.error("速卖通海外托管订单构造发货查询参数失败, orderId={}, reason={}",
                        orderId, e.getMessage());
            }
        }
        return result;
    }

    /**
     * 判断订单是否属于已发货的速卖通官方仓订单。
     *
     * @param orderId 平台主订单号
     * @param parentOrder 订单列表数据
     * @param detail 订单详情数据
     * @param childOrders 子订单列表
     * @return 全部子订单均非菜鸟仓且订单状态换算为已发货时返回 true
     */
    private boolean isOfficialWarehouseShippedOrder(String orderId,
                                                     Map<String, Object> parentOrder,
                                                     Map<String, Object> detail,
                                                     List<Map<String, Object>> childOrders) {
        boolean officialWarehouseOrder = !isCainiaoFfoOrder(detail, childOrders);
        if (!officialWarehouseOrder) {
            log.debug("跳过非官方仓速卖通海外托管订单, orderId={}", orderId);
            return false;
        }

        AliExpressOrder sourceOrder = JSON.parseObject(
                JSON.toJSONString(parentOrder), AliExpressOrder.class);
        String billStatus = sourceOrder.convertBillStatus(
                Boolean.TRUE, detail.get("logistic_info_list"));
        if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(billStatus)) {
            log.debug("跳过非已发货速卖通海外托管订单, orderId={}, billStatus={}",
                    orderId, billStatus);
            return false;
        }
        return true;
    }

    /**
     * 判断订单是否应使用菜鸟仓 FFO 接口。
     *
     * <p>海外托管订单的 {@code logistics_warehouse_type} 可能统一返回
     * {@code cainiaoInternationalWarehouse}，因此优先按实际物流服务代码区分。
     * {@code CAINIAO_*} 使用 FFO，{@code AE_LOCAL_*} 使用官方仓库存流水；
     * 平台未返回物流服务代码时才兼容旧仓型字段。</p>
     *
     * @param detail 订单详情
     * @param childOrders 子订单列表
     * @return 是否使用菜鸟仓 FFO 接口
     */
    private boolean isCainiaoFfoOrder(Map<String, Object> detail,
                                      List<Map<String, Object>> childOrders) {
        boolean hasCainiaoService = false;
        boolean hasAeOfficialWarehouseService = false;
        for (Map<String, Object> logisticInfo :
                toMapList(detail.get("logistic_info_list"))) {
            String serviceName = text(logisticInfo.get("logistics_service_name"));
            String serviceCode = text(logisticInfo.get("logistics_type_code"));
            hasCainiaoService = hasCainiaoService
                    || startsWithIgnoreCase(serviceName, CAINIAO_LOGISTICS_PREFIX)
                    || startsWithIgnoreCase(serviceCode, CAINIAO_LOGISTICS_PREFIX);
            hasAeOfficialWarehouseService = hasAeOfficialWarehouseService
                    || startsWithIgnoreCase(
                    serviceName, AE_OFFICIAL_WAREHOUSE_LOGISTICS_PREFIX)
                    || startsWithIgnoreCase(
                    serviceCode, AE_OFFICIAL_WAREHOUSE_LOGISTICS_PREFIX);
        }
        if (hasAeOfficialWarehouseService) {
            return false;
        }
        if (hasCainiaoService) {
            return true;
        }
        return childOrders.stream().allMatch(this::isCainiaoWarehouseItem);
    }

    /**
     * 忽略大小写判断物流服务代码前缀。
     *
     * @param value 物流服务代码
     * @param prefix 目标前缀
     * @return 是否匹配
     */
    private boolean startsWithIgnoreCase(String value, String prefix) {
        return StrUtil.isNotBlank(value)
                && value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * 判断订单明细是否标记为菜鸟仓。
     *
     * @param childOrder 平台订单明细
     * @return 是否为菜鸟国际仓
     */
    private boolean isCainiaoWarehouseItem(Map<String, Object> childOrder) {
        return childOrder != null
                && AliexpressConstants.CAINIAO_INTERNATIONAL_WAREHOUSE.equals(
                text(childOrder.get("logistics_warehouse_type")));
    }

    /**
     * 查询一批订单的库存流水。
     *
     * @param shopId 海外托管授权店铺 ID
     * @param channelSellerId 全托管渠道卖家 ID
     * @param batch 订单上下文，最多五单
     * @param beginTime 查询开始时间
     * @param endTime 查询结束时间
     * @return 发货主单
     */
    private List<Map<String, Object>> queryBatch(String shopId,
                                                 String channelSellerId,
                                                 List<OrderContext> batch,
                                                 LocalDateTime beginTime,
                                                 LocalDateTime endTime) {
        List<String> tradeIds = batch.stream()
                .map(OrderContext::getOrderId)
                .distinct()
                .collect(Collectors.toList());
        List<String> scItemIds = batch.stream()
                .flatMap(item -> item.getItems().stream())
                .map(item -> item.getScItem().getScItemId())
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<InventoryLogDTO> inventoryLogs = warehouseInventoryService.queryInventoryLogs(
                shopId, channelSellerId, tradeIds, scItemIds, beginTime, endTime);
        Map<String, List<InventoryLogDTO>> logsByOrderId = inventoryLogs.stream()
                .collect(Collectors.groupingBy(
                        InventoryLogDTO::getBizTradeId,
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderContext context : batch) {
            List<InventoryLogDTO> orderLogs = logsByOrderId.getOrDefault(
                    context.getOrderId(), Collections.emptyList());
            if (CollUtil.isEmpty(orderLogs)) {
                log.info("速卖通海外托管订单未查询到官方仓发货流水, shopId={}, orderId={}",
                        shopId, context.getOrderId());
                continue;
            }
            try {
                result.addAll(buildOutstockRows(
                        context, matchInventoryLogs(context, orderLogs)));
            } catch (ServiceException e) {
                log.error("速卖通海外托管订单匹配官方仓发货流水失败, shopId={}, orderId={}, reason={}",
                        shopId, context.getOrderId(), e.getMessage());
            }
        }
        return result;
    }

    /**
     * 将订单明细转换为匹配上下文。
     *
     * @param orderId 平台主订单号
     * @param childOrder 子订单数据
     * @param childOrderExtInfo 子订单扩展信息
     * @return 子订单上下文
     */
    private OrderItemContext toOrderItemContext(String orderId,
                                                Map<String, Object> childOrder,
                                                Map<String, Object> childOrderExtInfo) {
        String childOrderId = firstNotBlank(
                childOrder.get("child_order_id"), childOrder.get("id"));
        Integer quantity = integer(childOrder.get("product_count"));
        if (StrUtil.isBlank(childOrderId) || Objects.isNull(quantity) || quantity <= 0) {
            throw new ServiceException("速卖通海外托管订单明细参数不完整，orderId:" + orderId);
        }
        String unitPrice = requireUnitPrice(
                orderId, childOrderId, childOrder.get("product_price"));
        return new OrderItemContext(
                childOrderId,
                firstNotBlank(childOrder.get("product_id"), childOrder.get("item_id")),
                firstNotBlank(
                        childOrder.get("sku_id"),
                        childOrder.get("skuId"),
                        childOrder.get("platformSkuId"),
                        childOrderExtInfo.get("sku_id"),
                        childOrderExtInfo.get("skuId")),
                firstNotBlank(childOrder.get("sku_code"), childOrder.get("skuCode")),
                quantity,
                unitPrice,
                null);
    }

    /**
     * 从订单详情读取官方仓发货明细单价。
     *
     * <p>库存流水不返回金额，必须复用订单详情的 product_price；字段缺失时拒绝生成，
     * 避免销售出库单落成 0 CNY。</p>
     *
     * @param orderId 平台主订单号
     * @param childOrderId 平台子订单号
     * @param productPrice 订单详情 product_price
     * @return 现有速卖通发货转换器可识别的“金额(币种)”格式
     */
    String requireUnitPrice(String orderId,
                            String childOrderId,
                            Object productPrice) {
        if (!(productPrice instanceof Map)) {
            throw missingPriceException(orderId, childOrderId);
        }
        Map<?, ?> price = (Map<?, ?>) productPrice;
        String amount = text(price.get("amount"));
        String currency = text(price.get("currency_code"));
        if (StrUtil.isBlank(amount) || StrUtil.isBlank(currency)) {
            throw missingPriceException(orderId, childOrderId);
        }
        try {
            return new BigDecimal(amount).toPlainString()
                    + "(" + currency.trim() + ")";
        } catch (NumberFormatException e) {
            throw new ServiceException(StrUtil.format(
                    "速卖通海外托管订单{}子订单{}商品金额格式错误，amount={}",
                    orderId, childOrderId, amount));
        }
    }

    /**
     * 构造官方仓订单明细缺少价格时的业务异常。
     *
     * @param orderId 平台主订单号
     * @param childOrderId 平台子订单号
     * @return 价格缺失异常
     */
    private static ServiceException missingPriceException(String orderId,
                                                          String childOrderId) {
        return new ServiceException(StrUtil.format(
                "速卖通海外托管订单{}子订单{}缺少商品金额或币种",
                orderId, childOrderId));
    }

    /**
     * 按 SKU ID、商品 ID + SKU 编码、唯一 SKU 编码的顺序匹配货品。
     *
     * @param itemContext 订单明细
     * @param scItems 速卖通货品关系
     * @return 唯一货品
     */
    private ScItemDTO resolveScItem(OrderItemContext itemContext, List<ScItemDTO> scItems) {
        if (CollUtil.isEmpty(scItems)) {
            throw new ServiceException("速卖通海外托管货品关系为空");
        }
        List<ScItemDTO> candidates = findByRelation(scItems,
                relation -> StrUtil.isNotBlank(itemContext.getSkuId())
                        && itemContext.getSkuId().equals(relation.getSkuId()),
                item -> true);
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        assertNotAmbiguous(itemContext, candidates, "skuId");

        candidates = findByRelation(scItems,
                relation -> StrUtil.isNotBlank(itemContext.getProductId())
                        && itemContext.getProductId().equals(relation.getItemId()),
                item -> matchesSkuCode(itemContext.getSkuCode(), item));
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        assertNotAmbiguous(itemContext, candidates, "productId+skuCode");

        candidates = scItems.stream()
                .filter(item -> matchesSkuCode(itemContext.getSkuCode(), item))
                .collect(Collectors.toList());
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        assertNotAmbiguous(itemContext, candidates, "skuCode");
        throw new ServiceException(StrUtil.format(
                "速卖通海外托管子订单{}未匹配到货品，productId={}, skuId={}, skuCode={}",
                itemContext.getChildOrderId(),
                itemContext.getProductId(),
                itemContext.getSkuId(),
                itemContext.getSkuCode()));
    }

    /**
     * 使用平台可能返回的供应商 SKU、货品编码或仓库条码精确匹配订单 SKU。
     *
     * @param skuCode 订单 SKU 编码
     * @param scItem 速卖通货品
     * @return 任一货品编码与订单 SKU 完全一致时返回 true
     */
    private boolean matchesSkuCode(String skuCode, ScItemDTO scItem) {
        if (StrUtil.isBlank(skuCode) || Objects.isNull(scItem)) {
            return false;
        }
        return skuCode.equals(scItem.getSupplierSkuCode())
                || skuCode.equals(scItem.getItemCode())
                || skuCode.equals(scItem.getWhcBarCode());
    }

    /**
     * 按货品及绑定关系筛选候选项。
     *
     * @param scItems 速卖通货品
     * @param relationPredicate 绑定关系条件
     * @param itemPredicate 货品条件
     * @return 候选货品
     */
    private List<ScItemDTO> findByRelation(List<ScItemDTO> scItems,
                                           Predicate<ScItemRelationDTO> relationPredicate,
                                           Predicate<ScItemDTO> itemPredicate) {
        return scItems.stream()
                .filter(itemPredicate)
                .filter(item -> CollUtil.isNotEmpty(item.getRelationList())
                        && item.getRelationList().stream().anyMatch(relationPredicate))
                .collect(Collectors.toList());
    }

    /**
     * 拒绝无法唯一确定的货品关系。
     *
     * @param itemContext 订单明细
     * @param candidates 候选货品
     * @param rule 匹配规则
     */
    private void assertNotAmbiguous(OrderItemContext itemContext,
                                    List<ScItemDTO> candidates,
                                    String rule) {
        if (candidates.size() > 1) {
            throw new ServiceException(StrUtil.format(
                    "速卖通海外托管子订单{}按{}匹配到多个货品",
                    itemContext.getChildOrderId(), rule));
        }
    }

    /**
     * 将库存流水匹配到唯一子订单并校验整单数量。
     *
     * @param context 订单上下文
     * @param inventoryLogs 库存流水
     * @return 已匹配流水
     */
    private List<MatchedInventoryLog> matchInventoryLogs(OrderContext context,
                                                          List<InventoryLogDTO> inventoryLogs) {
        List<MatchedInventoryLog> matched = new ArrayList<>();
        for (InventoryLogDTO inventoryLog : inventoryLogs) {
            List<OrderItemContext> candidates = context.getItems().stream()
                    .filter(item -> matches(item, inventoryLog))
                    .collect(Collectors.toList());
            if (candidates.size() != 1) {
                throw new ServiceException(StrUtil.format(
                        "库存流水{}匹配子订单数量为{}，bizSubTradeId={}, scItemId={}",
                        inventoryLog.getWhOrderCode(),
                        candidates.size(),
                        inventoryLog.getBizSubTradeId(),
                        inventoryLog.getScItemInfo().getScItemId()));
            }
            matched.add(new MatchedInventoryLog(candidates.get(0), inventoryLog));
        }
        Map<String, Integer> expected = context.getItems().stream()
                .collect(Collectors.toMap(
                        this::orderItemKey,
                        OrderItemContext::getQuantity,
                        Integer::sum,
                        LinkedHashMap::new));
        Map<String, Integer> actual = matched.stream()
                .collect(Collectors.groupingBy(
                        item -> orderItemKey(item.getOrderItem()),
                        LinkedHashMap::new,
                        Collectors.summingInt(item -> Math.abs(
                                item.getInventoryLog().getChangeQuantity()))));
        if (!expected.equals(actual)) {
            throw new ServiceException(StrUtil.format(
                    "速卖通海外托管订单{}发货数量不一致，订单数量={}，流水数量={}",
                    context.getOrderId(), expected, actual));
        }
        return matched;
    }

    /**
     * 判断库存流水是否对应指定子订单和货品。
     *
     * @param item 订单明细
     * @param inventoryLog 库存流水
     * @return 是否匹配
     */
    private boolean matches(OrderItemContext item, InventoryLogDTO inventoryLog) {
        return Objects.nonNull(inventoryLog)
                && Objects.nonNull(inventoryLog.getScItemInfo())
                && item.getChildOrderId().equals(inventoryLog.getBizSubTradeId())
                && item.getScItem().getScItemId().equals(
                inventoryLog.getScItemInfo().getScItemId());
    }

    /**
     * 按履约单和仓库分组构造发货主单。
     *
     * @param context 订单上下文
     * @param matchedLogs 已匹配库存流水
     * @return 发货主单
     */
    private List<Map<String, Object>> buildOutstockRows(
            OrderContext context, List<MatchedInventoryLog> matchedLogs) {
        Map<String, List<MatchedInventoryLog>> groups = matchedLogs.stream()
                .collect(Collectors.groupingBy(
                        item -> outstockGroupKey(item.getInventoryLog()),
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (List<MatchedInventoryLog> group : groups.values()) {
            InventoryLogDTO first = group.get(0).getInventoryLog();
            Long outstockTime = group.stream()
                    .map(item -> item.getInventoryLog().getOperateTime())
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElseThrow(() -> new ServiceException("速卖通海外托管发货流水缺少操作时间"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("trade_order_no", context.getOrderId());
            row.put("fulfillment_order_no", first.getWhOrderCode());
            row.put("order_status",
                    AliexpressDeliveryOrderStatusEnum.SHIPPED.getName());
            row.put("send_fulfill_time", outstockTime);
            row.put("out_bound_time", outstockTime);
            row.put("tracking_no", first.getWhOrderCode());
            row.put("lbx_no", first.getWhOrderCode());
            row.put("warehouse_name", first.getStoreInfo().getStoreName());
            row.put("warehouse_code", StrUtil.blankToDefault(
                    first.getStoreInfo().getStoreCode(), first.getStoreInfo().getStoreName()));
            row.put(OUTSTOCK_ITEMS, buildOutstockItems(first.getWhOrderCode(), group));
            result.add(row);
        }
        return result;
    }

    /**
     * 汇总同一履约单下的发货明细。
     *
     * @param fulfillmentOrderNo 履约单号
     * @param matchedLogs 已匹配库存流水
     * @return 发货明细
     */
    private List<Map<String, Object>> buildOutstockItems(
            String fulfillmentOrderNo, List<MatchedInventoryLog> matchedLogs) {
        Map<String, List<MatchedInventoryLog>> groups = matchedLogs.stream()
                .collect(Collectors.groupingBy(
                        item -> orderItemKey(item.getOrderItem()),
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (List<MatchedInventoryLog> group : groups.values()) {
            OrderItemContext item = group.get(0).getOrderItem();
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("fulfillment_order_no", fulfillmentOrderNo);
            detail.put("item_id", item.getChildOrderId());
            detail.put("sc_item_id", item.getScItem().getScItemId());
            detail.put("sku_id", item.getSkuId());
            detail.put("platform_sku", StrUtil.blankToDefault(
                    item.getSkuCode(), item.getScItem().getSupplierSkuCode()));
            detail.put("unit_price", item.getUnitPrice());
            detail.put("order_line_qty", group.stream()
                    .mapToInt(value -> Math.abs(
                            value.getInventoryLog().getChangeQuantity()))
                    .sum());
            result.add(detail);
        }
        return result;
    }

    /**
     * 生成发货主单分组键。
     *
     * @param inventoryLog 库存流水
     * @return 履约单和仓库组合键
     */
    private String outstockGroupKey(InventoryLogDTO inventoryLog) {
        return inventoryLog.getWhOrderCode() + '\u0001'
                + StrUtil.blankToDefault(
                inventoryLog.getStoreInfo().getStoreCode(),
                inventoryLog.getStoreInfo().getStoreName());
    }

    /**
     * 生成子订单货品组合键。
     *
     * @param item 订单明细
     * @return 子订单和货品组合键
     */
    private String orderItemKey(OrderItemContext item) {
        return item.getChildOrderId() + '\u0001' + item.getScItem().getScItemId();
    }

    /**
     * 将对象转换为 Map 列表。
     *
     * @param value 原始对象
     * @return Map 列表
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toMapList(Object value) {
        if (!(value instanceof Collection)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : (Collection<?>) value) {
            if (item instanceof Map) {
                result.add((Map<String, Object>) item);
            } else if (item != null) {
                result.add(JSONObject.parseObject(JSONObject.toJSONString(item)));
            }
        }
        return result;
    }

    /**
     * 返回首个非空文本。
     *
     * @param values 候选值
     * @return 非空文本
     */
    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            String result = text(value);
            if (StrUtil.isNotBlank(result)) {
                return result;
            }
        }
        return null;
    }

    /**
     * 安全转换文本。
     *
     * @param value 原始值
     * @return 文本
     */
    private String text(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        String result = String.valueOf(value);
        return StrUtil.isBlank(result) || "null".equalsIgnoreCase(result)
                ? null : result.trim();
    }

    /**
     * 安全转换整数。
     *
     * @param value 原始值
     * @return 整数，无法转换时返回 null
     */
    private Integer integer(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return new java.math.BigDecimal(String.valueOf(value)).intValueExact();
        } catch (NumberFormatException e) {
            return null;
        } catch (ArithmeticException e) {
            return null;
        }
    }

    /**
     * 单个订单的库存流水查询上下文。
     */
    @Getter
    @AllArgsConstructor
    private static class OrderContext {
        private final String orderId;
        private final List<OrderItemContext> items;
    }

    /**
     * 单个子订单的货品匹配上下文。
     */
    @Getter
    @AllArgsConstructor
    private static class OrderItemContext {
        private final String childOrderId;
        private final String productId;
        private final String skuId;
        private final String skuCode;
        private final Integer quantity;
        private final String unitPrice;
        private ScItemDTO scItem;

        /**
         * 设置匹配到的速卖通货品。
         *
         * @param scItem 速卖通货品
         */
        private void setScItem(ScItemDTO scItem) {
            this.scItem = scItem;
        }
    }

    /**
     * 已匹配的订单明细和库存流水。
     */
    @Getter
    @AllArgsConstructor
    private static class MatchedInventoryLog {
        private final OrderItemContext orderItem;
        private final InventoryLogDTO inventoryLog;
    }
}
