package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TikTok销售仓库DMP转换
 */
@Service
@Scope("prototype")
public class TikTokWarehouseDmpHandler extends DmpInputDbConvertDmpHandler {

    private static final String ENABLED = "ENABLED";

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> relationEntry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = relationEntry.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                normalizeWarehouseData(dmpDataMap);
            }
        }
    }

    private void normalizeWarehouseData(TreeMap<String, Object> dmpDataMap) {
        String warehouseCode = firstNotBlank(dmpDataMap.get("warehouseCode"), dmpDataMap.get("id"));
        String warehouseName = firstNotBlank(dmpDataMap.get("warehouseName"), dmpDataMap.get("name"));
        String shopId = firstNotBlank(dmpDataMap.get("shopId"), dmpDataMap.get("authId"), dmpDataMap.get("nextLevelId"));
        JSONObject addressJson = parseAddress(dmpDataMap.get("address"));

        dmpDataMap.put("warehouseCode", warehouseCode);
        dmpDataMap.put("warehouseName", warehouseName);
        dmpDataMap.put("authId", shopId);
        dmpDataMap.put("countryCode", addressJson.getString("region_code"));
        dmpDataMap.put("address", addressJson.getString("full_address"));
        dmpDataMap.put("platformWarehouseStatus", ENABLED.equalsIgnoreCase(firstNotBlank(dmpDataMap.get("effectStatus"))) ? "1" : "0");
        if (StringUtils.isBlank(firstNotBlank(dmpDataMap.get("sourcePlatform")))) {
            dmpDataMap.put("sourcePlatform", "TikTok");
        }
        if (StringUtils.isBlank(firstNotBlank(dmpDataMap.get("warehousePlatformType")))) {
            dmpDataMap.put("warehousePlatformType", "overseasWarehouse");
        }

        dmpDataMap.remove("id");
        dmpDataMap.remove("name");
        dmpDataMap.remove("shopId");
        dmpDataMap.remove("effectStatus");
        dmpDataMap.remove("entityId");
        dmpDataMap.remove("type");
        dmpDataMap.remove("subType");
        dmpDataMap.remove("isDefault");
    }

    private JSONObject parseAddress(Object addressObj) {
        if (addressObj == null) {
            return new JSONObject();
        }
        if (addressObj instanceof JSONObject) {
            return (JSONObject) addressObj;
        }
        String jsonText = JSON.toJSONString(addressObj);
        if (StringUtils.isBlank(jsonText)) {
            return new JSONObject();
        }
        return JSON.parseObject(jsonText);
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value);
            if (StringUtils.isNotBlank(text) && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return "";
    }
}
