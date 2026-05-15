package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * InboundPlan summary -> dmp_fba_inbound_plans
 */
@Service
@Scope("prototype")
public class DmpInputAmzFbaInboundPlansFbaShipmentDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        if (CollUtil.isEmpty(dmpInputDataDmpRelationMaps)) {
            return;
        }
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> relationEntry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<Map<String, Object>> mongoDataList = relationEntry.getKey();
            List<TreeMap<String, Object>> dmpDataMaps = relationEntry.getValue();
            if (CollUtil.isEmpty(mongoDataList) || CollUtil.isEmpty(dmpDataMaps)) {
                continue;
            }
            Map<String, Object> mongoData = mongoDataList.get(0);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                fillInboundPlansFields(dmpDataMap, mongoData);
            }
        }
    }

    private void fillInboundPlansFields(TreeMap<String, Object> dmpDataMap, Map<String, Object> mongoData) {
        putStringIfNotBlank(dmpDataMap, "inboundPlanId", mongoData.get("inboundPlanId"));
        putStringIfNotBlank(dmpDataMap, "name", mongoData.get("name"));
        putStringIfNotBlank(dmpDataMap, "status", mongoData.get("status"));
        putStringIfNotBlank(dmpDataMap, "createdAtPlatform", mongoData.get("createdAt"));
        putStringIfNotBlank(dmpDataMap, "lastUpdatedAtPlatform", mongoData.get("lastUpdatedAt"));
        putJsonIfPresent(dmpDataMap, "marketplaceIdsJson", mongoData, "marketplaceIds");
        putJsonIfPresent(dmpDataMap, "sourceAddressJson", mongoData, "sourceAddress");
    }

    private void putStringIfNotBlank(TreeMap<String, Object> dmpDataMap, String key, Object value) {
        if (value == null) {
            return;
        }
        String str = value.toString();
        if (StringUtils.isNotBlank(str)) {
            dmpDataMap.put(key, str);
        }
    }

    private void putJsonIfPresent(TreeMap<String, Object> dmpDataMap, String targetKey, Map<String, Object> source, String sourceKey) {
        if (source == null || !source.containsKey(sourceKey)) {
            return;
        }
        Object value = source.get(sourceKey);
        if (value == null) {
            return;
        }
        if (value instanceof String) {
            if (StringUtils.isNotBlank((String) value)) {
                dmpDataMap.put(targetKey, value);
            }
            return;
        }
        dmpDataMap.put(targetKey, JSON.toJSONString(value));
    }
}
