package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Shopee 售后退货明细 DMP 转换。
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputShopeeReturnDetailDmpHandler extends DmpInputDoNextDmpHandler {

    private static final int RETURN_SOLUTION_RETURN_AND_REFUND = 0;
    private static final int REASON_MAX_LENGTH = 255;
    private static final String PARENT_STORAGE_NAME = "dmp_so_return_info";
    private static final String SOLUTION_TYPE_RETURN_AND_REFUND = "return_and_refund";

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
        if (!isReturnAndRefund(dmpInputMongoEntity)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> itemList = parseItemList(dmpInputMongoEntity.get("item"));
        if (CollUtil.isEmpty(itemList)) {
            return new ArrayList<>();
        }
        String returnSn = String.valueOf(dmpInputMongoEntity.getOrDefault("return_sn", ""));
        String orderSn = String.valueOf(dmpInputMongoEntity.getOrDefault("order_sn", ""));
        String textReason = String.valueOf(dmpInputMongoEntity.getOrDefault("text_reason", ""));
        List<Map<String, Object>> detailList = new ArrayList<>();
        for (Map<String, Object> item : itemList) {
            Map<String, Object> detail = new HashMap<>(item);
            detail.put("return_sn", returnSn);
            detail.put("order_sn", orderSn);
            detail.put("text_reason", textReason);
            detail.put("return_solution", dmpInputMongoEntity.get("return_solution"));
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
                    log.warn("Shopee退货明细item结构无法转换为Map,item:{}", item, e);
                }
            }
        }
        return resultList;
    }

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        Iterator<Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>>> iterator =
                dmpInputDataDmpRelationMaps.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry = iterator.next();
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            Iterator<TreeMap<String, Object>> dataIterator = dmpDataMaps.iterator();
            while (dataIterator.hasNext()) {
                TreeMap<String, Object> dmpDataMap = dataIterator.next();
                if (!isReturnAndRefund(dmpDataMap)) {
                    dataIterator.remove();
                    continue;
                }
                String platformSku = resolvePlatformSku(dmpDataMap);
                if (StringUtils.isNotBlank(platformSku)) {
                    dmpDataMap.put("skuNo", platformSku);
                }
                // Shopee 退货明细中的 amount 字段按平台语义表示数量，不是金额。
                Object amountObj = dmpDataMap.get("amount");
                if (amountObj != null) {
                    Integer qty = parseInteger(amountObj);
                    if (qty != null) {
                        dmpDataMap.put("qty", qty);
                    }
                }
                Object itemPriceObj = dmpDataMap.get("item_price");
                if (itemPriceObj != null) {
                    dmpDataMap.put("sellPrice", itemPriceObj);
                }
                dmpDataMap.put("solutionType", SOLUTION_TYPE_RETURN_AND_REFUND);
                Object returnSnObj = dmpDataMap.get("return_sn");
                String detailKey = resolveThirdDetailKey(dmpDataMap, platformSku);
                if (returnSnObj != null && StringUtils.isNotBlank(detailKey)) {
                    dmpDataMap.put("thirdDetailId", returnSnObj + "_" + detailKey);
                }
                Object orderSnObj = dmpDataMap.get("order_sn");
                if (orderSnObj != null) {
                    dmpDataMap.put("thirdOrderCode", orderSnObj);
                    dmpDataMap.put("platformOrderCode", orderSnObj);
                }
                Object reasonObj = dmpDataMap.get("text_reason");
                if (reasonObj != null) {
                    dmpDataMap.put("reason", StringUtils.left(String.valueOf(reasonObj), REASON_MAX_LENGTH));
                }
            }
            if (dmpDataMaps.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private boolean isReturnAndRefund(Map<String, Object> dataMap) {
        Object returnSolution = dataMap.get("return_solution");
        Integer returnSolutionValue = parseInteger(returnSolution);
        return returnSolutionValue != null && RETURN_SOLUTION_RETURN_AND_REFUND == returnSolutionValue;
    }

    private String resolvePlatformSku(Map<String, Object> item) {
        String modelSku = readString(item, "model_sku");
        if (StringUtils.isNotBlank(modelSku)) {
            return modelSku;
        }
        String itemSku = readString(item, "item_sku");
        if (StringUtils.isNotBlank(itemSku)) {
            return itemSku;
        }
        return readString(item, "variation_sku");
    }

    private String resolveThirdDetailKey(Map<String, Object> item, String platformSku) {
        if (StringUtils.isNotBlank(platformSku)) {
            return platformSku;
        }
        String itemId = readString(item, "item_id");
        String modelId = readString(item, "model_id");
        if (StringUtils.isNoneBlank(itemId, modelId)) {
            return itemId + "_" + modelId;
        }
        if (StringUtils.isNotBlank(itemId)) {
            return itemId;
        }
        return modelId;
    }

    private String readString(Map<String, Object> item, String field) {
        Object value = item.get(field);
        if (Objects.isNull(value)) {
            return "";
        }
        return StringUtils.trimToEmpty(String.valueOf(value));
    }

    private Integer parseInteger(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            log.warn("退货明细字段解析失败,value:{}", value);
            return null;
        }
    }
}
