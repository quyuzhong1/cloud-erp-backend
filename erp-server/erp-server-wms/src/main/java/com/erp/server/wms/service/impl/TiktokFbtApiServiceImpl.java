package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.model.wms.dto.TiktokFbtDTO;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.erp.server.wms.service.TiktokFbtApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TiktokFbtApiServiceImpl implements TiktokFbtApiService {

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Override
    public List<TiktokFbtDTO.InboundOrderDTO> queryInboundOrders(String shopId, List<String> inboundOrderIds, LocalDateTime updatedAfter) {
        TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(shopId);
        if (shopInfoDTO == null) {
            return Collections.emptyList();
        }
        List<String> normalizedInboundOrderIds = normalizeInboundOrderIds(inboundOrderIds);
        Map<String, Object> resultMap = executeWithRetry(
                () -> tikTokSdkClientService.getInboundOrder(shopInfoDTO, normalizedInboundOrderIds),
                "queryInboundOrders");
        Object rows = resultMap.get("inbound_orders");
        if (!(rows instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> inboundOrders = (List<Map<String, Object>>) rows;
        List<TiktokFbtDTO.InboundOrderDTO> result = new ArrayList<>();
        for (Map<String, Object> item : inboundOrders) {
            TiktokFbtDTO.InboundOrderDTO dto = new TiktokFbtDTO.InboundOrderDTO();
            dto.setShopId(shopId);
            dto.setInboundOrderId(firstNotBlank(item.get("inbound_order_id"), item.get("id")));
            dto.setStatus(resolveStatus(item));
            dto.setUpdatedTime(resolveUpdatedTime(item));

            Map<String, Object> warehouse = mapVal(item.get("warehouse"));
            if (warehouse != null) {
                dto.setWarehouseCode(stringVal(warehouse.get("fbt_warehouse_id")));
                if (warehouse.get("warehouse_ids") instanceof List) {
                    dto.setFbtWarehouseIds((List<String>) warehouse.get("warehouse_ids"));
                }
            } else {
                dto.setWarehouseCode(stringVal(item.get("fbt_warehouse_id")));
            }

            List<TiktokFbtDTO.PlannedGoodDTO> plannedGoods = parsePlannedGoods(item.get("planned_goods"));
            dto.setPlannedGoods(plannedGoods);
            dto.setCarriers(parseCarriers(item.get("carriers")));
            dto.setReceivedBatches(parseReceivedBatches(item.get("received_batches")));
            dto.setShipmentName(resolveShipmentName(dto.getInboundOrderId(), plannedGoods));
            if (CollUtil.isNotEmpty(plannedGoods)) {
                List<String> goodsIds = plannedGoods.stream()
                        .map(TiktokFbtDTO.PlannedGoodDTO::getGoodsId)
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.toList());
                dto.setGoodsIds(goodsIds);
            } else if (CollUtil.isNotEmpty((List<?>) item.get("goods_ids"))) {
                dto.setGoodsIds((List<String>) item.get("goods_ids"));
            }
            if (CollUtil.isEmpty(dto.getFbtWarehouseIds())) {
                List<String> warehouseIds = new ArrayList<>();
                if (dto.getWarehouseCode() != null) {
                    warehouseIds.add(dto.getWarehouseCode());
                }
                dto.setFbtWarehouseIds(warehouseIds);
            }
            if (updatedAfter == null || dto.getUpdatedTime() == null || !dto.getUpdatedTime().isBefore(updatedAfter)) {
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public List<TiktokFbtDTO.InventoryRecordDTO> queryInventoryRecords(String shopId,
                                                                       List<String> goodsIds,
                                                                       List<String> warehouseIds,
                                                                       Long startTime,
                                                                       Long endTime) {
        TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(shopId);
        if (shopInfoDTO == null) {
            return Collections.emptyList();
        }
        Map<String, Object> resultMap = executeWithRetry(
                () -> tikTokSdkClientService.searchFbtInventoryRecord(shopInfoDTO, goodsIds, warehouseIds, startTime, endTime),
                "queryInventoryRecords");
        Object rows = resultMap.get("inventory_records");
        if (!(rows instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> inventoryRecords = (List<Map<String, Object>>) rows;
        List<TiktokFbtDTO.InventoryRecordDTO> result = new ArrayList<>();
        for (Map<String, Object> item : inventoryRecords) {
            TiktokFbtDTO.InventoryRecordDTO dto = new TiktokFbtDTO.InventoryRecordDTO();
            dto.setShopId(shopId);
            dto.setInboundOrderId(stringVal(item.get("inbound_order_id")));
            dto.setRecordId(stringVal(item.get("record_id")));
            dto.setWarehouseCode(stringVal(item.get("fbt_warehouse_id")));
            dto.setSkuCode(stringVal(item.get("seller_sku")));
            dto.setGoodsId(stringVal(item.get("goods_id")));
            dto.setDeltaQty(intVal(item.get("change_quantity")));
            dto.setEventTime(parseTime(item.get("event_time")));
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<TiktokFbtDTO.InventorySnapshotDTO> queryInventorySnapshots(String shopId,
                                                                           List<String> goodsIds,
                                                                           List<String> warehouseIds) {
        TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(shopId);
        if (shopInfoDTO == null) {
            return Collections.emptyList();
        }
        Map<String, Object> resultMap = executeWithRetry(
                () -> tikTokSdkClientService.searchFbtInventory(shopInfoDTO, goodsIds, warehouseIds),
                "queryInventorySnapshots");
        Object rows = resultMap.get("inventory_list");
        if (!(rows instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> inventoryList = (List<Map<String, Object>>) rows;
        List<TiktokFbtDTO.InventorySnapshotDTO> result = new ArrayList<>();
        for (Map<String, Object> item : inventoryList) {
            Map<String, Object> warehouseObj = mapVal(item.get("warehouse"));
            if (warehouseObj == null) {
                warehouseObj = mapVal(item.get("fbt_warehouse"));
            }
            Map<String, Object> goodsObj = mapVal(item.get("goods"));
            Map<String, Object> skuObj = mapVal(item.get("sku"));
            Map<String, Object> onHandObj = mapVal(item.get("on_hand_detail"));
            TiktokFbtDTO.InventorySnapshotDTO dto = new TiktokFbtDTO.InventorySnapshotDTO();
            dto.setShopId(shopId);
            dto.setWarehouseCode(firstNotBlank(
                    item.get("fbt_warehouse_id"),
                    item.get("warehouse_id"),
                    warehouseObj == null ? null : warehouseObj.get("fbt_warehouse_id"),
                    warehouseObj == null ? null : warehouseObj.get("warehouse_id"),
                    warehouseObj == null ? null : warehouseObj.get("id")));
            dto.setWarehouseName(firstNotBlank(
                    item.get("fbt_warehouse_name"),
                    item.get("warehouse_name"),
                    warehouseObj == null ? null : warehouseObj.get("name"),
                    warehouseObj == null ? null : warehouseObj.get("warehouse_name"),
                    dto.getWarehouseCode()));
            dto.setSkuCode(firstNotBlank(
                    item.get("seller_sku"),
                    item.get("sku"),
                    item.get("reference_code"),
                    goodsObj == null ? null : goodsObj.get("reference_code"),
                    skuObj == null ? null : skuObj.get("seller_sku"),
                    skuObj == null ? null : skuObj.get("sku"),
                    skuObj == null ? null : skuObj.get("code")));
            dto.setGoodsId(firstNotBlank(
                    item.get("goods_id"),
                    item.get("id"),
                    goodsObj == null ? null : goodsObj.get("id")));
            dto.setGoodsName(firstNotBlank(
                    item.get("goods_name"),
                    item.get("name"),
                    goodsObj == null ? null : goodsObj.get("name")));
            dto.setAvailableQty(intVal(firstNotBlank(
                    item.get("available_quantity"),
                    item.get("sellable_quantity"),
                    onHandObj == null ? null : onHandObj.get("available_quantity"))));
            dto.setReservedQty(intVal(firstNotBlank(
                    item.get("reserved_quantity"),
                    onHandObj == null ? null : onHandObj.get("reserved_quantity"))));
            dto.setUnfulfillableQty(intVal(firstNotBlank(
                    item.get("unfulfillable_quantity"),
                    item.get("unsellable_quantity"),
                    onHandObj == null ? null : onHandObj.get("unfulfillable_quantity"))));
            dto.setInTransitQty(intVal(firstNotBlank(item.get("in_transit_quantity"), item.get("deliver_onway_quantity"))));
            dto.setUpdatedTime(parseTime(firstNotBlank(item.get("update_time"), item.get("updated_time"), item.get("event_time"))));
            result.add(dto);
        }
        return result;
    }

    private <T> T executeWithRetry(java.util.concurrent.Callable<T> callable, String action) {
        int maxRetry = 3;
        long backoffMs = 500L;
        Exception lastEx = null;
        for (int i = 1; i <= maxRetry; i++) {
            try {
                return callable.call();
            } catch (Exception ex) {
                lastEx = ex;
                log.warn("TikTok调用失败, action={}, attempt={}/{}, msg={}", action, i, maxRetry, ex.getMessage());
                if (i == maxRetry) {
                    break;
                }
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试中断");
                }
                backoffMs = backoffMs * 2;
            }
        }
        throw new RuntimeException("TikTok调用失败: " + action + ", error=" + (lastEx == null ? "" : lastEx.getMessage()));
    }

    private String stringVal(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            String text = normalizeTextValue(value);
            if (StrUtil.isNotBlank(text)) {
                return text;
            }
        }
        return null;
    }

    private String resolveStatus(Map<String, Object> item) {
        Map<String, Object> latestLog = latestOperationLog(item.get("order_operation_logs"));
        if (latestLog != null) {
            String logStatus = stringVal(latestLog.get("order_status"));
            if (StrUtil.isNotBlank(logStatus)) {
                return logStatus;
            }
        }
        return null;
    }

    private LocalDateTime resolveUpdatedTime(Map<String, Object> item) {
        Map<String, Object> latestLog = latestOperationLog(item.get("order_operation_logs"));
        if (latestLog != null) {
            LocalDateTime logTime = parseTime(latestLog.get("operate_time"));
            if (logTime != null) {
                return logTime;
            }
        }
        return null;
    }

    private String resolveShipmentName(String inboundOrderId, List<TiktokFbtDTO.PlannedGoodDTO> plannedGoods) {
        if (CollUtil.isNotEmpty(plannedGoods)) {
            String firstName = plannedGoods.get(0).getName();
            if (StrUtil.isNotBlank(firstName)) {
                return firstName;
            }
        }
        return "FBT-" + inboundOrderId;
    }

    private List<TiktokFbtDTO.PlannedGoodDTO> parsePlannedGoods(Object plannedGoodsObj) {
        if (!(plannedGoodsObj instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> plannedGoods = (List<Map<String, Object>>) plannedGoodsObj;
        List<TiktokFbtDTO.PlannedGoodDTO> result = new ArrayList<>();
        for (Map<String, Object> item : plannedGoods) {
            TiktokFbtDTO.PlannedGoodDTO dto = new TiktokFbtDTO.PlannedGoodDTO();
            dto.setGoodsId(stringVal(item.get("id")));
            dto.setReferenceCode(stringVal(item.get("reference_code")));
            dto.setName(stringVal(item.get("name")));
            dto.setQuantity(intVal(item.get("quantity")));
            if (item.get("sku_ids") instanceof List) {
                dto.setSkuIds((List<String>) item.get("sku_ids"));
            }
            result.add(dto);
        }
        return result;
    }

    private List<TiktokFbtDTO.ReceivedBatchDTO> parseReceivedBatches(Object receivedBatchesObj) {
        if (!(receivedBatchesObj instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> receivedBatches = (List<Map<String, Object>>) receivedBatchesObj;
        List<TiktokFbtDTO.ReceivedBatchDTO> result = new ArrayList<>();
        for (Map<String, Object> item : receivedBatches) {
            TiktokFbtDTO.ReceivedBatchDTO dto = new TiktokFbtDTO.ReceivedBatchDTO();
            dto.setBatchId(stringVal(item.get("id")));
            dto.setGoodsId(stringVal(item.get("goods_id")));
            dto.setNormalQuantity(intVal(item.get("normal_quantity")));
            dto.setDefectiveQuantity(intVal(item.get("defective_quantity")));
            dto.setTotalQuantity(intVal(item.get("total_quantity")));
            if (item.get("product_ids") instanceof List) {
                dto.setProductIds((List<String>) item.get("product_ids"));
            }
            if (item.get("sku_ids") instanceof List) {
                dto.setSkuIds((List<String>) item.get("sku_ids"));
            }
            result.add(dto);
        }
        return result;
    }

    private List<TiktokFbtDTO.CarrierDTO> parseCarriers(Object carriersObj) {
        if (!(carriersObj instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> carriers = (List<Map<String, Object>>) carriersObj;
        List<TiktokFbtDTO.CarrierDTO> result = new ArrayList<>();
        for (Map<String, Object> item : carriers) {
            TiktokFbtDTO.CarrierDTO dto = new TiktokFbtDTO.CarrierDTO();
            dto.setCarrierName(firstNotBlank(
                    item.get("carrier_name"),
                    item.get("carrierName"),
                    item.get("name"),
                    item.get("carrier")));
            dto.setTrackingNumber(firstNotBlank(
                    item.get("tracking_number"),
                    item.get("trackingNumber"),
                    item.get("track_no"),
                    item.get("trackNo")));
            if (StrUtil.isBlank(dto.getCarrierName()) && StrUtil.isBlank(dto.getTrackingNumber())) {
                continue;
            }
            result.add(dto);
        }
        return result;
    }

    private String normalizeTextValue(Object obj) {
        String text = stringVal(obj);
        if (StrUtil.isBlank(text)) {
            return null;
        }
        text = text.trim();
        if ("0".equals(text) || "null".equalsIgnoreCase(text) || "undefined".equalsIgnoreCase(text)) {
            return null;
        }
        return text;
    }

    private Map<String, Object> mapVal(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    private Map<String, Object> latestOperationLog(Object logsObj) {
        if (!(logsObj instanceof List)) {
            return null;
        }
        List<Map<String, Object>> logs = (List<Map<String, Object>>) logsObj;
        Map<String, Object> latest = null;
        long maxOperateTime = Long.MIN_VALUE;
        for (Map<String, Object> log : logs) {
            long operateTime = longVal(log == null ? null : log.get("operate_time"));
            if (operateTime > maxOperateTime) {
                maxOperateTime = operateTime;
                latest = log;
            }
        }
        if (latest != null) {
            return latest;
        }
        for (Map<String, Object> log : logs) {
            if (log != null) {
                return log;
            }
        }
        return null;
    }

    private Integer intVal(Object obj) {
        if (obj == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (Exception ignore) {
            return 0;
        }
    }

    private long longVal(Object obj) {
        if (obj == null) {
            return Long.MIN_VALUE;
        }
        try {
            return Long.parseLong(String.valueOf(obj));
        } catch (Exception ignore) {
            return Long.MIN_VALUE;
        }
    }

    private LocalDateTime parseTime(Object obj) {
        if (obj == null) {
            return null;
        }
        String value = String.valueOf(obj);
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignore) {
        }
        try {
            long epoch = Long.parseLong(value);
            // 兼容毫秒时间戳
            if (epoch > 99999999999L) {
                epoch = epoch / 1000;
            }
            return LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.ofHours(8));
        } catch (Exception ignore) {
        }
        return null;
    }

    private List<String> normalizeInboundOrderIds(List<String> inboundOrderIds) {
        if (CollUtil.isEmpty(inboundOrderIds)) {
            return inboundOrderIds;
        }
        return inboundOrderIds.stream()
                .map(this::normalizeInboundOrderId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeInboundOrderId(String inboundOrderId) {
        if (StrUtil.isBlank(inboundOrderId)) {
            return null;
        }
        String value = inboundOrderId.trim();
        if (StrUtil.startWithIgnoreCase(value, "IBR") && value.length() > 3) {
            value = value.substring(3);
        }
        return StrUtil.blankToDefault(value, null);
    }
}
