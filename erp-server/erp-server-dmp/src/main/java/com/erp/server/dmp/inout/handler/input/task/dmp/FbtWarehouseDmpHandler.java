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
 * FBT仓库数据处理Handler
 * 将FBT仓库数据转换并存储到overseas_provider_warehouse表
 *
 * @author System
 * @date 2026-02-10
 */
@Service
@Scope("prototype")
public class FbtWarehouseDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();

            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                // 转换FBT仓库数据字段
                // FBT API返回的字段：fbt_warehouse_id, name, address等

                // 设置平台仓库编码（使用fbt_warehouse_id）
                Object fbtWarehouseId = dmpDataMap.get("fbt_warehouse_id");
                if (fbtWarehouseId != null) {
                    dmpDataMap.put("platformWarehouseCode", fbtWarehouseId.toString());
                }

                // 设置平台仓库名称（使用name）
                Object name = dmpDataMap.get("name");
                if (name != null) {
                    dmpDataMap.put("platformWarehouseName", name.toString());
                }

                // 处理地址信息
                String addressJson = dmpDataMap.getOrDefault("address", "").toString();
                dmpDataMap.put("address", "");
                if (StringUtils.isNotBlank(addressJson)) {
                    try {
                        JSONObject addressObj = JSON.parseObject(addressJson);

                        // 组装完整地址
                        StringBuilder fullAddress = new StringBuilder();
                        if (addressObj.containsKey("full_address")) {
                            fullAddress.append(addressObj.getString("full_address"));
                        } else {
                            // 如果没有full_address，则拼接各个字段
                            String country = addressObj.getString("country");
                            String state = addressObj.getString("state");
                            String city = addressObj.getString("city");
                            String district = addressObj.getString("district");
                            String detailAddress = addressObj.getString("detail_address");

                            if (StringUtils.isNotBlank(country)) fullAddress.append(country).append(" ");
                            if (StringUtils.isNotBlank(state)) fullAddress.append(state).append(" ");
                            if (StringUtils.isNotBlank(city)) fullAddress.append(city).append(" ");
                            if (StringUtils.isNotBlank(district)) fullAddress.append(district).append(" ");
                            if (StringUtils.isNotBlank(detailAddress)) fullAddress.append(detailAddress);
                        }

                        dmpDataMap.put("address", fullAddress.toString().trim());

                        // 提取国家信息
                        if (addressObj.containsKey("country_code")) {
                            dmpDataMap.put("country", addressObj.getString("country_code"));
                        }
                    } catch (Exception e) {
                        // JSON解析失败，保持空地址
                        dmpDataMap.put("address", addressJson);
                    }
                }

                // 设置仓库类型（FBT仓默认为标准仓）
                dmpDataMap.put("platformWarehouseType", "0"); // 0:标准仓

                // 设置仓库状态（默认可用）
                dmpDataMap.put("platformWarehouseStatus", "1"); // 1:可用

                // 设置是否禁用（默认启用）
                dmpDataMap.put("disabled", false);

                // 关联店铺ID（由InitHandler添加）
                Object shopId = dmpDataMap.get("shopId");
                if (shopId != null) {
                    dmpDataMap.put("shopId", shopId.toString());
                }

                // 关联授权ID（由InitHandler添加）
                Object authId = dmpDataMap.get("authId");
                if (authId != null) {
                    dmpDataMap.put("mainId", authId.toString());
                }

                // 移除原始字段
                dmpDataMap.remove("fbt_warehouse_id");
                dmpDataMap.remove("name");
            }
        }
    }
}
