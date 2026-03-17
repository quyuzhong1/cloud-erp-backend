package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.server.dmp.service.DmpProductInfoService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * FBT产品DMP数据转换Handler
 * 将FBT商品数据转换为DMP格式
 *
 * @author System
 * @since 2026-02-10
 */
@Service
@Scope("prototype")
public class FbtProductDmpHandler extends DmpInputDbConvertDmpHandler {

    @Resource
    private DmpProductInfoService dmpProductInfoService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        Map<String, String> spuIdMainIdMap = this.buildSpuMainIdMap(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            List<Map<String, Object>> mongoDataMaps = entry.getKey();
            
            if (mongoDataMaps.isEmpty()) {
                continue;
            }
            
            Map<String, Object> mongoDataMap = mongoDataMaps.get(0);
            String productId = firstNotBlank(
                    mongoDataMap.get("id"),
                    mongoDataMap.get("goods_id"),
                    getMapValue(asMap(mongoDataMap.get("goods")), "id"));
            String referenceCode = firstNotBlank(
                    mongoDataMap.get("reference_code"),
                    getMapValue(asMap(mongoDataMap.get("goods")), "reference_code"),
                    getMapValue(firstMap(asList(mongoDataMap.get("skus"))), "reference_code"));
            String productName = firstNotBlank(
                    mongoDataMap.get("name"),
                    mongoDataMap.get("goods_name"),
                    getMapValue(asMap(mongoDataMap.get("goods")), "name"),
                    getMapValue(asMap(getMapValue(firstMap(asList(mongoDataMap.get("skus"))), "product")), "name"));
            String barcode = firstNotBlank(
                    mongoDataMap.get("code"),
                    getMapValue(firstMap(asList(mongoDataMap.get("barcodes"))), "code"),
                    getMapValue(asMap(mongoDataMap.get("goods")), "code"));
            String imageUrl = firstNotBlank(
                    mongoDataMap.get("image_url"),
                    getMapValue(asMap(getMapValue(firstMap(asList(mongoDataMap.get("skus"))), "product")), "image_url"));
            
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                normalizeProductFields(dmpDataMap, productId, referenceCode, productName, barcode, imageUrl);
                // 设置关联信息
                dmpDataMap.put("nextLevelId", nextLevelId);
                
                // 从MongoDB数据中获取authId和shopId
                Object authId = mongoDataMap.get("authId");
                if (ObjectUtil.isNotEmpty(authId)) {
                    dmpDataMap.put("authId", authId);
                }
                
                Object shopId = mongoDataMap.get("shopId");
                if (ObjectUtil.isNotEmpty(shopId)) {
                    // dmp_product_info 没有 shopId 字段，复用 nextLevelId 保存店铺ID给 MQ 输出使用
                    dmpDataMap.put("nextLevelId", shopId);
                }
                
                // 仓库信息
                Object warehouseName = mongoDataMap.get("warehouseName");
                if (ObjectUtil.isNotEmpty(warehouseName)) {
                    dmpDataMap.put("warehouseName", warehouseName);
                }
                
                Object warehouseShortName = mongoDataMap.get("warehouseShortName");
                if (ObjectUtil.isNotEmpty(warehouseShortName)) {
                    dmpDataMap.put("warehouseShortName", warehouseShortName);
                }
                
                Object serviceProvider = mongoDataMap.get("serviceProvider");
                if (ObjectUtil.isNotEmpty(serviceProvider)) {
                    dmpDataMap.put("serviceProvider", serviceProvider);
                }

                // dmp_sku_info 需要主表ID关联，供 MQ 输出阶段按主子关系组装数据
                if ("dmp_sku_info".equals(dmpCfgInputConvertEntity.getStorageName())) {
                    String spuId = this.getStringValue(dmpDataMap.get("spuId"));
                    String mainId = spuIdMainIdMap.get(spuId);
                    if (ObjectUtil.isNotEmpty(mainId)) {
                        dmpDataMap.put("mainId", mainId);
                    }
                }
            }
        }
    }

    private void normalizeProductFields(TreeMap<String, Object> dmpDataMap,
                                        String productId,
                                        String referenceCode,
                                        String productName,
                                        String barcode,
                                        String imageUrl) {
        String storageName = dmpCfgInputConvertEntity.getStorageName();
        if ("dmp_product_info".equals(storageName)) {
            putIfNotBlank(dmpDataMap, "spuId", productId);
            putIfNotBlank(dmpDataMap, "spuNo", referenceCode);
            putIfNotBlank(dmpDataMap, "spuName", productName);
            putIfNotBlank(dmpDataMap, "sourceId", productId);
            return;
        }
        if ("dmp_sku_info".equals(storageName)) {
            putIfNotBlank(dmpDataMap, "spuId", productId);
            putIfNotBlank(dmpDataMap, "skuId", productId);
            putIfNotBlank(dmpDataMap, "skuNo", referenceCode);
            putIfNotBlank(dmpDataMap, "name", productName);
            putIfNotBlank(dmpDataMap, "thirdId", barcode);
            putIfNotBlank(dmpDataMap, "imageUrls", imageUrl);
        }
    }

    private Map<String, String> buildSpuMainIdMap(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        Map<String, String> spuIdMainIdMap = new HashMap<>();
        if (!"dmp_sku_info".equals(dmpCfgInputConvertEntity.getStorageName())) {
            return spuIdMainIdMap;
        }
        Set<String> spuIdSet = new HashSet<>();
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                String spuId = this.getStringValue(dmpDataMap.get("spuId"));
                if (ObjectUtil.isNotEmpty(spuId)) {
                    spuIdSet.add(spuId);
                }
            }
        }
        if (spuIdSet.isEmpty()) {
            return spuIdMainIdMap;
        }
        List<DmpProductInfoEntity> productInfoList = dmpProductInfoService.lambdaQuery()
                .eq(DmpProductInfoEntity::getInputTaskId, inputTaskId)
                .in(DmpProductInfoEntity::getSpuId, spuIdSet)
                .list();
        for (DmpProductInfoEntity productInfo : productInfoList) {
            if (ObjectUtil.isNotEmpty(productInfo.getSpuId()) && ObjectUtil.isNotEmpty(productInfo.getId())) {
                spuIdMainIdMap.put(productInfo.getSpuId(), productInfo.getId());
            }
        }
        return spuIdMainIdMap;
    }

    private String getStringValue(Object value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object value) {
        if (!(value instanceof List)) {
            return null;
        }
        return (List<Map<String, Object>>) value;
    }

    private Map<String, Object> firstMap(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private Object getMapValue(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            if (Objects.isNull(value)) {
                continue;
            }
            String text = String.valueOf(value);
            if (StrUtil.isNotBlank(text) && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return null;
    }

    private void putIfNotBlank(TreeMap<String, Object> map, String key, String value) {
        if (StrUtil.isNotBlank(value)) {
            map.put(key, value);
        }
    }
}
