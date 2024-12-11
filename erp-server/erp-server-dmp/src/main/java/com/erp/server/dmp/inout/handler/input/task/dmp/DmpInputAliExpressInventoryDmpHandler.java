package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAliExpressInventoryDmpHandler extends DmpInputDbConvertDmpHandler {


    public static final String ALIEXPRESS_INVENTORY_ON_WAY_DATA = "aliexpress_inventoryOnway_data";

    public static final String AUTH_ID = "authId";

    @Override
    protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(List<Map<String, Object>> dmpInputMongoEntityList) {
        Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps = new HashMap<>();
        if (CollectionUtils.isEmpty(dmpInputMongoEntityList)){
            return dmpInputDataDmpRelationMaps;
        }

        // 查询子任务其他信息:
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(AUTH_ID, AUTH_ID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
        List<Map<String, Object>> childFindMongoData = mongoService.findMongoData(paramDataList, ALIEXPRESS_INVENTORY_ON_WAY_DATA);

        // 合并结果authId,productSku,platformWarehouseCode
        Map<String, MergeResultDTO> mergeMongoMap =  new HashMap<>();
        for (Map<String, Object> mongoMap : dmpInputMongoEntityList) {
            String authId = mongoMap.getOrDefault("authId", "").toString();
            String storeCode = mongoMap.getOrDefault("store_code", "").toString();
            String scItemId = mongoMap.getOrDefault("sc_item_id", "").toString();
            // 库存类型(1 良品，101 残品)
            String inventoryType = mongoMap.getOrDefault("inventory_type", "").toString();
            if (StringUtils.isBlank(authId) || StringUtils.isBlank(storeCode) || StringUtils.isBlank(scItemId) || StringUtils.isBlank(inventoryType)){
                continue;
            }

            String uniqueId = CharSequenceUtil.format("{}_{}_{}", authId, storeCode, scItemId);
            MergeResultDTO mergeResultDTO =  mergeMongoMap.getOrDefault(uniqueId, new MergeResultDTO());
            TreeMap<String, Object> valueMap = mergeResultDTO.getValueMap();
            if (null == valueMap){
                valueMap = new TreeMap<>();
            }
            valueMap.put("platformWarehouseCode", storeCode);
            valueMap.put("productSku", scItemId);
            valueMap.put("authId", authId);
            String quantity = mongoMap.getOrDefault("quantity", "0").toString();
            String lockQuantity = mongoMap.getOrDefault("lock_quantity", "0").toString();
            // 库存类型(1 良品，101 残品)
            if ("1".equals(inventoryType)){
                int sellable = Integer.parseInt(quantity) - Integer.parseInt(lockQuantity);
                valueMap.put("sellable", sellable);
                mongoMap.put("sellable", sellable);
            } else if ("101".equals(inventoryType)){
                valueMap.put("unsellable", Integer.parseInt(quantity));
                mongoMap.put("unsellable", Integer.parseInt(quantity));
            }
            String storeName = mongoMap.getOrDefault("store_name", "").toString();
            valueMap.put("platformWarehouseName", storeName);
            mergeResultDTO.setUniqueId(uniqueId);
            mergeResultDTO.setMongoMap(mongoMap);
            mergeResultDTO.setValueMap(valueMap);


            mergeMongoMap.put(uniqueId, mergeResultDTO);
        }

        for (Map<String, Object> mongoMap : childFindMongoData) {
            String authId = mongoMap.getOrDefault("authId", "").toString();
            String storeCode = mongoMap.getOrDefault("inbound_store_code", "").toString();
            String scItemId = mongoMap.getOrDefault("sc_item_id", "").toString();
            // 库存类型(1 采购在途，2 调拨在途，3 销售在途，4 销退在途)
            String inventoryType = mongoMap.getOrDefault("inventory_type", "").toString();
            if (StringUtils.isBlank(authId) || StringUtils.isBlank(storeCode) || StringUtils.isBlank(scItemId) || StringUtils.isBlank(inventoryType)){
                continue;
            }

            String uniqueId = CharSequenceUtil.format("{}_{}_{}", authId, storeCode, scItemId);
            MergeResultDTO mergeResultDTO =  mergeMongoMap.getOrDefault(uniqueId, new MergeResultDTO());
            TreeMap<String, Object> valueMap = mergeResultDTO.getValueMap();
            if (null == valueMap){
                valueMap = new TreeMap<>();
            }
            valueMap.put("platformWarehouseCode", storeCode);
            mongoMap.put("storeCode", storeCode);
            valueMap.put("productSku", scItemId);
            valueMap.put("authId", authId);
            String quantity = mongoMap.getOrDefault("quantity", "0").toString();
            //库存类型(1 采购在途，2 调拨在途，3 销售在途，4 销退在途)
            if ("1".equals(inventoryType)){
                valueMap.put("transferOnway", Integer.parseInt(quantity));
                mongoMap.put("transferOnway", Integer.parseInt(quantity));
            } else if ("4".equals(inventoryType)){
                valueMap.put("saleReturnInTransitQty", Integer.parseInt(quantity));
                mongoMap.put("saleReturnInTransitQty", Integer.parseInt(quantity));
            }
            String storeName = mongoMap.getOrDefault("inbound_store_name", "").toString();
            valueMap.put("platformWarehouseName", storeName);
            mongoMap.put("storeName", storeName);
            mergeResultDTO.setUniqueId(uniqueId);
            mergeResultDTO.setMongoMap(mongoMap);
            mergeResultDTO.setValueMap(valueMap);
            mergeMongoMap.put(uniqueId, mergeResultDTO);
        }

        // 组合结果
        for (MergeResultDTO mergeResultDTO : mergeMongoMap.values()) {
            ArrayList<TreeMap<String, Object>> valueList = new ArrayList<>();
            TreeMap<String, Object> dmpInputDmpBaseEntity = mergeResultDTO.getValueMap();
            this.afterDmpInputMongoEntityFixedValue(dmpInputDmpBaseEntity);
            valueList.add(dmpInputDmpBaseEntity);
            ArrayList<Map<String, Object>> keyList = new ArrayList<>();
            keyList.add(mergeResultDTO.getMongoMap());
            dmpInputDataDmpRelationMaps.put(keyList, valueList);
        }
        this.afterConvertData(dmpInputDataDmpRelationMaps);
        return dmpInputDataDmpRelationMaps;
    }


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAliExpressInventoryDmpHandler afterConvertData");
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MergeResultDTO {

        private String uniqueId;

        private Map<String, Object> mongoMap;

        private TreeMap<String, Object> valueMap;

    }
}
