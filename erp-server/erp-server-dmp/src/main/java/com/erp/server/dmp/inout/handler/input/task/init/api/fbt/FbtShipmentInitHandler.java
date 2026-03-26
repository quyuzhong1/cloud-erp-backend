package com.erp.server.dmp.inout.handler.input.task.init.api.fbt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.exception.ServiceException;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FBT货件初始化处理器
 */
@Slf4j
@Service
@Scope("prototype")
public class FbtShipmentInitHandler extends DmpInputInitHandler {

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;
    @Resource
    private FbtAuthorizedShopResolver fbtAuthorizedShopResolver;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        FbtAuthorizedShopResolver.ResolvedAuthContext resolvedAuthContext =
                fbtAuthorizedShopResolver.resolveByAuthId(dmpInputTaskEntity.getNextLevelId());
        String authId = resolvedAuthContext.getProvider().getId();
        String resolvedShopId = resolvedAuthContext.getShopId();

        JSONObject extendJson = parseExtendJson();
        List<String> shipmentCodeList = parseShipmentCodeList(extendJson);
        if (CollUtil.isEmpty(shipmentCodeList)) {
            log.info("FBT货件手动拉取未提供shipmentCodeList, authId={}", authId);
            return Collections.emptyList();
        }

        String requestShopId = extendJson.getString("shopId");
        String shopId = StrUtil.blankToDefault(requestShopId, resolvedShopId);
        if (StrUtil.isNotBlank(requestShopId) && !StrUtil.equals(requestShopId, resolvedShopId)) {
            log.warn("FBT货件手动拉取入参shopId与授权解析结果不一致, requestShopId={}, resolvedShopId={}, authId={}",
                    requestShopId, resolvedShopId, authId);
            shopId = resolvedShopId;
        }

        TikTokShopInfoDTO shopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(shopId);
        if (shopInfoDTO == null) {
            throw new ServiceException("TikTok店铺授权信息为空, shopId:" + shopId);
        }

        Map<String, Object> response = tikTokSdkClientService.getInboundOrder(shopInfoDTO, shipmentCodeList);
        Object inboundOrdersObj = response == null ? null : response.get("inbound_orders");
        if (!(inboundOrdersObj instanceof List)) {
            log.info("FBT货件返回为空, authId={}, shopId={}, shipmentCodeList={}", authId, shopId, shipmentCodeList);
            return Collections.emptyList();
        }

        List<Map<String, Object>> inboundOrders = (List<Map<String, Object>>) inboundOrdersObj;
        if (CollUtil.isEmpty(inboundOrders)) {
            return Collections.emptyList();
        }

        JSONArray rows = new JSONArray();
        for (Map<String, Object> inboundOrder : inboundOrders) {
            rows.add(normalizeInboundOrder(inboundOrder, authId, shopId));
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(rows.toJSONString()));
    }

    private JSONObject parseExtendJson() {
        if (StrUtil.isBlank(dmpInputTaskEntity.getExtendJson())) {
            return new JSONObject();
        }
        return JSON.parseObject(dmpInputTaskEntity.getExtendJson());
    }

    private List<String> parseShipmentCodeList(JSONObject extendJson) {
        JSONArray jsonArray = extendJson.getJSONArray("shipmentCodeList");
        if (CollUtil.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        return jsonArray.stream()
                .map(String::valueOf)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<String, Object> normalizeInboundOrder(Map<String, Object> inboundOrder, String authId, String shopId) {
        Map<String, Object> row = new LinkedHashMap<>();
        String inboundOrderId = firstNotBlank(inboundOrder.get("inbound_order_id"), inboundOrder.get("id"));
        Map<String, Object> warehouse = asMap(inboundOrder.get("warehouse"));
        List<Map<String, Object>> plannedGoods = parsePlannedGoods(inboundOrder.get("planned_goods"));
        List<Map<String, Object>> carriers = parseCarriers(inboundOrder.get("carriers"));
        List<Map<String, Object>> receivedBatches = parseReceivedBatches(inboundOrder.get("received_batches"));

        row.put("sourcePlatform", OmsPlatformEnum.FBT.getCode());
        row.put("sourceSystem", OmsPlatformEnum.FBT.getCode());
        row.put("authId", authId);
        row.put("shopId", shopId);
        row.put("nextLevelId", authId);
        row.put("inboundOrderId", inboundOrderId);
        row.put("shipmentName", resolveShipmentName(inboundOrderId, plannedGoods));
        row.put("platformWarehouseCode", firstNotBlank(
                inboundOrder.get("fbt_warehouse_id"),
                warehouse == null ? null : warehouse.get("fbt_warehouse_id"),
                warehouse == null ? null : warehouse.get("id")));
        row.put("platformWarehouseName", firstNotBlank(
                inboundOrder.get("warehouse_name"),
                warehouse == null ? null : warehouse.get("name"),
                row.get("platformWarehouseCode")));
        row.put("platformShipmentStatus", resolveStatus(inboundOrder));
        row.put("platformUpdateTimeRaw", formatDateTime(resolveUpdatedTime(inboundOrder)));
        row.put("carriers", carriers);
        row.put("plannedGoods", plannedGoods);
        row.put("receivedBatches", receivedBatches);
        return row;
    }

    private List<Map<String, Object>> parsePlannedGoods(Object plannedGoodsObj) {
        if (!(plannedGoodsObj instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object itemObj : (List<?>) plannedGoodsObj) {
            Map<String, Object> item = asMap(itemObj);
            if (item == null) {
                continue;
            }
            Map<String, Object> normalized = new LinkedHashMap<>();
            normalized.put("goodsId", firstNotBlank(item.get("id")));
            normalized.put("referenceCode", firstNotBlank(item.get("reference_code")));
            normalized.put("name", firstNotBlank(item.get("name")));
            normalized.put("quantity", intVal(item.get("quantity")));
            Object skuIdsObj = item.get("sku_ids");
            normalized.put("skuIds", skuIdsObj instanceof List ? skuIdsObj : Collections.emptyList());
            result.add(normalized);
        }
        return result;
    }

    private List<Map<String, Object>> parseCarriers(Object carriersObj) {
        if (!(carriersObj instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object itemObj : (List<?>) carriersObj) {
            Map<String, Object> item = asMap(itemObj);
            if (item == null) {
                continue;
            }
            String carrierName = firstNotBlank(
                    item.get("carrier_name"),
                    item.get("carrierName"),
                    item.get("name"),
                    item.get("carrier"));
            String trackingNumber = firstNotBlank(
                    item.get("tracking_number"),
                    item.get("trackingNumber"),
                    item.get("track_no"),
                    item.get("trackNo"));
            if (StrUtil.isBlank(carrierName) && StrUtil.isBlank(trackingNumber)) {
                continue;
            }
            Map<String, Object> normalized = new LinkedHashMap<>();
            normalized.put("carrierName", carrierName);
            normalized.put("trackingNumber", trackingNumber);
            result.add(normalized);
        }
        return result;
    }

    private List<Map<String, Object>> parseReceivedBatches(Object receivedBatchesObj) {
        if (!(receivedBatchesObj instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object itemObj : (List<?>) receivedBatchesObj) {
            Map<String, Object> item = asMap(itemObj);
            if (item == null) {
                continue;
            }
            Map<String, Object> normalized = new LinkedHashMap<>();
            normalized.put("batchId", firstNotBlank(item.get("id")));
            normalized.put("goodsId", firstNotBlank(item.get("goods_id")));
            normalized.put("normalQuantity", intVal(item.get("normal_quantity")));
            normalized.put("defectiveQuantity", intVal(item.get("defective_quantity")));
            normalized.put("totalQuantity", intVal(item.get("total_quantity")));
            normalized.put("productIds", item.get("product_ids") instanceof List ? item.get("product_ids") : Collections.emptyList());
            normalized.put("skuIds", item.get("sku_ids") instanceof List ? item.get("sku_ids") : Collections.emptyList());
            result.add(normalized);
        }
        return result;
    }

    private String resolveShipmentName(String inboundOrderId, List<Map<String, Object>> plannedGoods) {
        if (CollUtil.isNotEmpty(plannedGoods)) {
            String firstName = firstNotBlank(plannedGoods.get(0).get("name"));
            if (StrUtil.isNotBlank(firstName)) {
                return firstName;
            }
        }
        return "FBT-" + inboundOrderId;
    }

    private String resolveStatus(Map<String, Object> inboundOrder) {
        Map<String, Object> latestLog = latestOperationLog(inboundOrder.get("order_operation_logs"));
        if (latestLog != null) {
            String status = firstNotBlank(latestLog.get("order_status"));
            if (StrUtil.isNotBlank(status)) {
                return status;
            }
        }
        return firstNotBlank(inboundOrder.get("status"));
    }

    private LocalDateTime resolveUpdatedTime(Map<String, Object> inboundOrder) {
        Map<String, Object> latestLog = latestOperationLog(inboundOrder.get("order_operation_logs"));
        if (latestLog != null) {
            LocalDateTime operateTime = parseDateTime(latestLog.get("operate_time"));
            if (operateTime != null) {
                return operateTime;
            }
        }
        return parseDateTime(firstNotBlank(
                inboundOrder.get("update_time"),
                inboundOrder.get("updated_time"),
                inboundOrder.get("ship_time"),
                inboundOrder.get("create_time")));
    }

    private Map<String, Object> latestOperationLog(Object logsObj) {
        if (!(logsObj instanceof List)) {
            return null;
        }
        Map<String, Object> latest = null;
        long maxOperateTime = Long.MIN_VALUE;
        for (Object itemObj : (List<?>) logsObj) {
            Map<String, Object> item = asMap(itemObj);
            if (item == null) {
                continue;
            }
            long operateTime = longVal(item.get("operate_time"));
            if (operateTime > maxOperateTime) {
                maxOperateTime = operateTime;
                latest = item;
            }
        }
        return latest;
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toString();
    }

    private LocalDateTime parseDateTime(Object value) {
        String raw = firstNotBlank(value);
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ignore) {
        }
        try {
            long epoch = Long.parseLong(raw);
            if (epoch > 9999999999L) {
                epoch = epoch / 1000;
            }
            return LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.ofHours(8));
        } catch (Exception ignore) {
        }
        return null;
    }

    private Integer intVal(Object value) {
        String raw = firstNotBlank(value);
        if (StrUtil.isBlank(raw)) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (Exception ignore) {
            return 0;
        }
    }

    private long longVal(Object value) {
        String raw = firstNotBlank(value);
        if (StrUtil.isBlank(raw)) {
            return Long.MIN_VALUE;
        }
        try {
            return Long.parseLong(raw);
        } catch (Exception ignore) {
            return Long.MIN_VALUE;
        }
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value);
            if (StrUtil.isNotBlank(text) && !"null".equalsIgnoreCase(text)) {
                return text.trim();
            }
        }
        return null;
    }
}
