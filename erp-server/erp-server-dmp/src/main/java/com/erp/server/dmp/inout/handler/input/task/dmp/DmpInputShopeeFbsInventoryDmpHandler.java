package com.erp.server.dmp.inout.handler.input.task.dmp;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Shopee FBS 库存 DMP 转换
 */
@Service
@Scope("prototype")
public class DmpInputShopeeFbsInventoryDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<Map<String, Object>> mongoDataMaps = entry.getKey();
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            if (mongoDataMaps.isEmpty()) {
                continue;
            }
            Map<String, Object> mongoData = mongoDataMaps.get(0);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                putString(dmpDataMap, "rowKey", mongoData.get("rowKey"));
                putString(dmpDataMap, "whsRegion", mongoData.get("whsRegion"));
                putString(dmpDataMap, "warehouseItemId", mongoData.get("warehouseItemId"));
                putString(dmpDataMap, "shopSkuId", mongoData.get("shopSkuId"));
                putString(dmpDataMap, "itemId", mongoData.get("itemId"));
                putString(dmpDataMap, "modelId", mongoData.get("modelId"));
                putString(dmpDataMap, "fbsSku", mongoData.get("fbsSku"));
                putString(dmpDataMap, "platformSku", mongoData.get("platformSku"));
                putString(dmpDataMap, "platformProductName", mongoData.get("platformProductName"));
                putString(dmpDataMap, "specName", mongoData.get("specName"));
                putString(dmpDataMap, "warehouseId", mongoData.get("warehouseId"));
                putString(dmpDataMap, "warehouseName", mongoData.get("warehouseName"));
                putString(dmpDataMap, "purchaseMode", mongoData.get("purchaseMode"));
                putInteger(dmpDataMap, "recommendedReplenishmentQty", mongoData.get("recommendedReplenishmentQty"));
                putInteger(dmpDataMap, "totalStockQty", mongoData.get("totalStockQty"));
                putInteger(dmpDataMap, "stockedInboundQty", mongoData.get("stockedInboundQty"));
                putInteger(dmpDataMap, "transferAsnInboundQty", mongoData.get("transferAsnInboundQty"));
                putInteger(dmpDataMap, "reservedQty", mongoData.get("reservedQty"));
                putInteger(dmpDataMap, "unsellableQty", mongoData.get("unsellableQty"));
                putInteger(dmpDataMap, "inTransitQty", mongoData.get("inTransitQty"));
                putInteger(dmpDataMap, "turnoverDays", mongoData.get("turnoverDays"));
                putInteger(dmpDataMap, "warehouseInventoryCoverageDays", mongoData.get("warehouseInventoryCoverageDays"));
                putDecimal(dmpDataMap, "dailyAvgSalesQty", mongoData.get("dailyAvgSalesQty"));
                putInteger(dmpDataMap, "last7DaysSalesQty", mongoData.get("last7DaysSalesQty"));
                putInteger(dmpDataMap, "last15DaysSalesQty", mongoData.get("last15DaysSalesQty"));
                putInteger(dmpDataMap, "last30DaysSalesQty", mongoData.get("last30DaysSalesQty"));
                putInteger(dmpDataMap, "last60DaysSalesQty", mongoData.get("last60DaysSalesQty"));
                putInteger(dmpDataMap, "last90DaysSalesQty", mongoData.get("last90DaysSalesQty"));
                putInteger(dmpDataMap, "stockAge030Qty", mongoData.get("stockAge030Qty"));
                putInteger(dmpDataMap, "stockAge3160Qty", mongoData.get("stockAge3160Qty"));
                putInteger(dmpDataMap, "stockAge6190Qty", mongoData.get("stockAge6190Qty"));
                putInteger(dmpDataMap, "stockAge91120Qty", mongoData.get("stockAge91120Qty"));
                putInteger(dmpDataMap, "stockAge121180Qty", mongoData.get("stockAge121180Qty"));
                putInteger(dmpDataMap, "stockAgeOver180Qty", mongoData.get("stockAgeOver180Qty"));
            }
        }
    }

    private void putString(TreeMap<String, Object> dmpDataMap, String key, Object value) {
        dmpDataMap.put(key, value == null ? "" : String.valueOf(value));
    }

    private void putInteger(TreeMap<String, Object> dmpDataMap, String key, Object value) {
        if (value == null) {
            dmpDataMap.put(key, 0);
            return;
        }
        if (value instanceof Number) {
            dmpDataMap.put(key, ((Number) value).intValue());
            return;
        }
        dmpDataMap.put(key, StringUtils.isBlank(String.valueOf(value)) ? 0 : Integer.parseInt(String.valueOf(value)));
    }

    private void putDecimal(TreeMap<String, Object> dmpDataMap, String key, Object value) {
        if (value == null) {
            dmpDataMap.put(key, BigDecimal.ZERO);
            return;
        }
        if (value instanceof BigDecimal) {
            dmpDataMap.put(key, value);
            return;
        }
        if (value instanceof Number) {
            dmpDataMap.put(key, BigDecimal.valueOf(((Number) value).doubleValue()));
            return;
        }
        dmpDataMap.put(key, new BigDecimal(String.valueOf(value)));
    }
}
