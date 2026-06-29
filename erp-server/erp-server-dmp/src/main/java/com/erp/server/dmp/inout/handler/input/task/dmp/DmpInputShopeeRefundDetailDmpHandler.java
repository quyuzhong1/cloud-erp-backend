package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Shopee 仅退款明细 DMP 转换：return_solution=1 且 status=ACCEPTED。
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputShopeeRefundDetailDmpHandler extends DmpInputDoNextDmpHandler {

    private static final int RETURN_SOLUTION_REFUND_ONLY = 1;
    private static final String STATUS_REFUND_ACCEPTED = "ACCEPTED";
    private static final String PARENT_STORAGE_NAME = "dmp_so_refund_info";

    @Override
    protected DmpCfgInputConvertEntity getMainConvertId() {
        List<DmpCfgInputConvertEntity> list = dmpHandlerCache.getDmpCfgInputConvertEntityList(d ->
                d.getMainId().equals(dmpCfgInputConvertEntity.getMainId())
                        && d.getInputStatus().equals(dmpCfgInputConvertEntity.getInputStatus()));
        return list.stream()
                .filter(item -> PARENT_STORAGE_NAME.equals(item.getStorageName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        if (!isRefundOnlyClosed(dmpInputMongoEntity)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> itemList = parseItemList(dmpInputMongoEntity.get("item"));
        if (CollUtil.isEmpty(itemList)) {
            return new ArrayList<>();
        }
        String returnSn = String.valueOf(dmpInputMongoEntity.getOrDefault("return_sn", ""));
        String orderSn = String.valueOf(dmpInputMongoEntity.getOrDefault("order_sn", ""));
        List<Map<String, Object>> detailList = new ArrayList<>();
        for (Map<String, Object> item : itemList) {
            Map<String, Object> detail = new HashMap<>(item);
            detail.put("return_sn", returnSn);
            detail.put("order_sn", orderSn);
            detail.put("return_solution", dmpInputMongoEntity.get("return_solution"));
            detail.put("status", dmpInputMongoEntity.get("status"));
            detailList.add(detail);
        }
        return detailList;
    }

    private List<Map<String, Object>> parseItemList(Object itemObj) {
        if (itemObj == null) {
            return new ArrayList<>();
        }
        if (itemObj instanceof List) {
            return castMapList((List<?>) itemObj);
        }
        if (itemObj instanceof JSONArray) {
            return castMapList(((JSONArray) itemObj).toJavaList(Map.class));
        }
        return castMapList(JSON.parseArray(JSON.toJSONString(itemObj), Map.class));
    }

    private List<Map<String, Object>> castMapList(List<?> rawList) {
        if (CollUtil.isEmpty(rawList)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof Map) {
                Map<?, ?> rawMap = (Map<?, ?>) item;
                Map<String, Object> itemMap = new HashMap<>();
                rawMap.forEach((key, value) -> itemMap.put(String.valueOf(key), value));
                resultList.add(itemMap);
                continue;
            }
            if (item != null) {
                try {
                    Map<String, Object> itemMap = JSON.parseObject(JSON.toJSONString(item), Map.class);
                    if (itemMap != null) {
                        resultList.add(itemMap);
                    }
                } catch (Exception e) {
                    log.warn("Shopee仅退款明细item结构无法转换为Map,item:{}", item, e);
                }
            }
        }
        return resultList;
    }

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                if (!isRefundOnlyClosed(dmpDataMap)) {
                    continue;
                }
                String platformSku = resolvePlatformSku(dmpDataMap);
                if (StringUtils.isNotBlank(platformSku)) {
                    dmpDataMap.put("skuNo", platformSku);
                }
                // Shopee 仅退款明细中的 amount 字段按平台语义表示数量，不是金额。
                Object amountObj = dmpDataMap.get("amount");
                Integer qty = parseInteger(amountObj);
                if (qty != null) {
                    dmpDataMap.put("qty", qty);
                    Object itemPriceObj = dmpDataMap.get("item_price");
                    BigDecimal itemPrice = parseBigDecimal(itemPriceObj);
                    if (itemPrice != null) {
                        dmpDataMap.put("amount", itemPrice.multiply(BigDecimal.valueOf(qty)));
                    }
                }
                Object returnSnObj = dmpDataMap.get("return_sn");
                if (returnSnObj != null && StringUtils.isNotBlank(platformSku)) {
                    dmpDataMap.put("thirdDetailId", returnSnObj + "_" + platformSku);
                }
                Object orderSnObj = dmpDataMap.get("order_sn");
                if (orderSnObj != null) {
                    dmpDataMap.put("thirdOrderCode", orderSnObj);
                    dmpDataMap.put("platformOrderCode", orderSnObj);
                }
            }
        }
    }

    private boolean isRefundOnlyClosed(Map<String, Object> dataMap) {
        Object returnSolution = dataMap.get("return_solution");
        Integer returnSolutionValue = parseInteger(returnSolution);
        if (returnSolutionValue == null || RETURN_SOLUTION_REFUND_ONLY != returnSolutionValue) {
            return false;
        }
        return STATUS_REFUND_ACCEPTED.equalsIgnoreCase(resolvePlatformStatus(dataMap));
    }

    private String resolvePlatformStatus(Map<String, Object> dataMap) {
        Object platformStatus = dataMap.get("platformOriginalStatus");
        if (platformStatus == null) {
            platformStatus = dataMap.get("platform_original_status");
        }
        if (platformStatus == null) {
            platformStatus = dataMap.get("platformStatus");
        }
        if (platformStatus == null) {
            platformStatus = dataMap.get("status");
        }
        return String.valueOf(platformStatus == null ? "" : platformStatus);
    }

    private String resolvePlatformSku(Map<String, Object> item) {
        String modelSku = String.valueOf(item.getOrDefault("model_sku", ""));
        if (StringUtils.isNotBlank(modelSku)) {
            return modelSku;
        }
        String itemSku = String.valueOf(item.getOrDefault("item_sku", ""));
        if (StringUtils.isNotBlank(itemSku)) {
            return itemSku;
        }
        return String.valueOf(item.getOrDefault("variation_sku", ""));
    }

    private Integer parseInteger(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
