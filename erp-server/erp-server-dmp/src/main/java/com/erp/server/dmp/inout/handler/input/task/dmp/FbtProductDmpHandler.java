package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            List<Map<String, Object>> mongoDataMaps = entry.getKey();
            
            if (mongoDataMaps.isEmpty()) {
                continue;
            }
            
            Map<String, Object> mongoDataMap = mongoDataMaps.get(0);
            
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                // 设置关联信息
                dmpDataMap.put("nextLevelId", nextLevelId);
                
                // 从MongoDB数据中获取authId和shopId
                Object authId = mongoDataMap.get("authId");
                if (ObjectUtil.isNotEmpty(authId)) {
                    dmpDataMap.put("authId", authId);
                }
                
                Object shopId = mongoDataMap.get("shopId");
                if (ObjectUtil.isNotEmpty(shopId)) {
                    dmpDataMap.put("shopId", shopId);
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
            }
        }
    }
}
