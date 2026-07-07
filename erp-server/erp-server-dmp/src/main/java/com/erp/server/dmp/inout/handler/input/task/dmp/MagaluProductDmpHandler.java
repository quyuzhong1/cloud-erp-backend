package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.server.dmp.inout.dto.request.DmpInputDmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputMongoResponse;
import com.erp.server.dmp.service.DmpProductInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Magalu Listing(SKU) -> dmp_product_info / dmp_sku_info
 * 字段映射见飞书文档「3.3.2平台Listing拉取」
 */
@Service
@Scope("prototype")
public class MagaluProductDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final String MAGALU_PLATFORM = "Magalu";
    /** Magazine Luiza 渠道，仅同步该渠道 Listing */
    private static final String MAGALU_CHANNEL_ID = "9fe0d853-732b-4e4a-a0b0-cff988ed043d";
    private static final String STORAGE_PRODUCT_INFO = "dmp_product_info";
    private static final String STORAGE_SKU_INFO = "dmp_sku_info";

    @Resource
    private DmpProductInfoService dmpProductInfoService;

    @Override
    public List<Map<String, Object>> convertMongoToDmp(DmpInputDmpRequest dmpRequest, DmpInputMongoResponse dmpResponse) {
        List<Map<String, Object>> skuList = super.convertMongoToDmp(dmpRequest, dmpResponse);
        if (CollUtil.isEmpty(skuList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> filteredList = new ArrayList<>();
        for (Map<String, Object> sku : skuList) {
            if (matchMagaluChannel(sku)) {
                filteredList.add(sku);
            }
        }
        if (CollUtil.isEmpty(filteredList)) {
            return Collections.emptyList();
        }
        if (STORAGE_PRODUCT_INFO.equals(storageName)) {
            return buildProductInfoRows(filteredList);
        }
        if (STORAGE_SKU_INFO.equals(storageName)) {
            return buildSkuInfoRows(filteredList);
        }
        return filteredList;
    }

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        if (!STORAGE_SKU_INFO.equals(dmpCfgInputConvertEntity.getStorageName())) {
            return;
        }
        Map<String, String> spuMainIdMap = buildSpuMainIdMap(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            for (TreeMap<String, Object> row : entry.getValue()) {
                row.put("nextLevelId", nextLevelId);
                String spuId = stringValue(row.get("spuId"));
                String mainId = spuMainIdMap.get(spuId);
                if (StringUtils.isNotBlank(mainId)) {
                    row.put("mainId", mainId);
                }
            }
        }
    }

    private List<Map<String, Object>> buildProductInfoRows(List<Map<String, Object>> skuList) {
        Map<String, Map<String, Object>> productMap = new LinkedHashMap<>();
        for (Map<String, Object> sku : skuList) {
            String spuId = resolveSpuId(sku);
            if (StringUtils.isBlank(spuId) || productMap.containsKey(spuId)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("nextLevelId", nextLevelId);
            row.put("sourceSystem", MAGALU_PLATFORM);
            row.put("sourcePlatform", MAGALU_PLATFORM);
            row.put("spuId", spuId);
            row.put("spuNo", spuId);
            row.put("spuName", stringValue(sku.get("title")));
            row.put("sourceId", spuId);
            row.put("platformUpdateTime", parseTime(firstNotBlank(sku.get("updated_at"), sku.get("updater"))));
            productMap.put(spuId, row);
        }
        return new ArrayList<>(productMap.values());
    }

    private List<Map<String, Object>> buildSkuInfoRows(List<Map<String, Object>> skuList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> sku : skuList) {
            String spuId = resolveSpuId(sku);
            String platformSku = stringValue(firstNotBlank(sku.get("sku"), sku.get("SKU")));
            if (StringUtils.isBlank(spuId) || StringUtils.isBlank(platformSku)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("nextLevelId", nextLevelId);
            row.put("spuId", spuId);
            row.put("skuId", platformSku);
            row.put("skuNo", platformSku);
            row.put("name", stringValue(sku.get("title")));
            row.put("status", mapListingStatus(sku.get("status")));
            row.put("platformUpdateTime", parseTime(firstNotBlank(sku.get("updated_at"), sku.get("updater"))));
            row.put("imageUrls", extractImageUrl(sku));
            // categoryName 暂存规格文本，供 MagaluProductRocketMQTaskHandler 写入 productSpec
            row.put("categoryName", buildAttributesSpec(sku));
            fillPackageDimensions(row, sku);
            resultList.add(row);
        }
        return resultList;
    }

    private Map<String, String> buildSpuMainIdMap(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> relationMaps) {
        Map<String, String> spuMainIdMap = new LinkedHashMap<>();
        Set<String> spuIdSet = new LinkedHashSet<>();
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : relationMaps.entrySet()) {
            for (TreeMap<String, Object> row : entry.getValue()) {
                String spuId = stringValue(row.get("spuId"));
                if (StringUtils.isNotBlank(spuId)) {
                    spuIdSet.add(spuId);
                }
            }
        }
        if (spuIdSet.isEmpty()) {
            return spuMainIdMap;
        }
        List<DmpProductInfoEntity> productInfoList = dmpProductInfoService.lambdaQuery()
                .eq(DmpProductInfoEntity::getInputTaskId, inputTaskId)
                .in(DmpProductInfoEntity::getSpuId, spuIdSet)
                .list();
        for (DmpProductInfoEntity productInfo : productInfoList) {
            if (ObjectUtil.isNotEmpty(productInfo.getSpuId()) && ObjectUtil.isNotEmpty(productInfo.getId())) {
                spuMainIdMap.put(productInfo.getSpuId(), productInfo.getId());
            }
        }
        return spuMainIdMap;
    }

    private boolean matchMagaluChannel(Map<String, Object> sku) {
        List<Map<String, Object>> channels = listMap(sku.get("channels"));
        if (CollUtil.isEmpty(channels)) {
            return false;
        }
        for (Map<String, Object> channel : channels) {
            if (MAGALU_CHANNEL_ID.equalsIgnoreCase(stringValue(channel.get("id")))) {
                return true;
            }
        }
        return false;
    }

    private String resolveSpuId(Map<String, Object> sku) {
        Map<String, Object> group = mapValue(sku.get("group"));
        return firstNotBlank(group.get("id"), sku.get("sku"), sku.get("SKU"));
    }

    private String mapListingStatus(Object statusObj) {
        String status = stringValue(statusObj);
        if (StringUtils.isBlank(status)) {
            return "5";
        }
        switch (status.toUpperCase()) {
            case "PUBLISHED":
                return "1";
            case "UNPUBLISHED":
            case "BLOCKED":
                return "3";
            case "UNDER_REVIEW":
            case "DELETING":
                return "5";
            case "DELETED":
                return "7";
            default:
                return "5";
        }
    }

    private String buildAttributesSpec(Map<String, Object> sku) {
        List<Map<String, Object>> attributes = listMap(sku.get("attributes"));
        if (CollUtil.isEmpty(attributes)) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (Map<String, Object> attribute : attributes) {
            String name = stringValue(attribute.get("name"));
            String value = stringValue(firstNotBlank(attribute.get("value"), attribute.get("value_name")));
            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(value)) {
                parts.add(name + ":" + value);
            }
        }
        return String.join(",", parts);
    }

    private void fillPackageDimensions(Map<String, Object> row, Map<String, Object> sku) {
        List<Map<String, Object>> dimensions = listMap(sku.get("dimensions"));
        for (Map<String, Object> dimension : dimensions) {
            if (!"package".equalsIgnoreCase(stringValue(dimension.get("name")))) {
                continue;
            }
            row.put("packageLength", dimensionNumber(dimension.get("length")));
            row.put("packageWidth", dimensionNumber(dimension.get("width")));
            row.put("packageHeight", dimensionNumber(dimension.get("height")));
            String unit = dimensionUnit(dimension.get("length"));
            if (StringUtils.isNotBlank(unit)) {
                row.put("packageUnit", unit);
            }
            break;
        }
    }

    private BigDecimal dimensionNumber(Object dimensionObj) {
        Map<String, Object> dimension = mapValue(dimensionObj);
        Object value = dimension.get("value");
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String dimensionUnit(Object dimensionObj) {
        Map<String, Object> dimension = mapValue(dimensionObj);
        return stringValue(dimension.get("unit"));
    }

    private String extractImageUrl(Map<String, Object> sku) {
        List<Map<String, Object>> images = listMap(sku.get("images"));
        if (CollUtil.isEmpty(images)) {
            return "";
        }
        return firstNotBlank(images.get(0).get("reference"), images.get(0).get("url"));
    }

    private LocalDateTime parseTime(Object value) {
        String text = stringValue(value);
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = value.toString();
            if (StringUtils.isNotBlank(text) && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return "";
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    private List<Map<String, Object>> listMap(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            if (item instanceof Map) {
                result.add((Map<String, Object>) item);
            }
        }
        return result;
    }
}
