package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.erp.server.dmp.utils.DmpFieldMapUtils;
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
        DmpFieldMapUtils.putStringIfNotBlank(dmpDataMap, "inboundPlanId", mongoData.get("inboundPlanId"));
        DmpFieldMapUtils.putStringIfNotBlank(dmpDataMap, "name", mongoData.get("name"));
        DmpFieldMapUtils.putStringIfNotBlank(dmpDataMap, "status", mongoData.get("status"));
        DmpFieldMapUtils.putStringIfNotBlank(dmpDataMap, "createdAtPlatform", mongoData.get("createdAt"));
        DmpFieldMapUtils.putStringIfNotBlank(dmpDataMap, "lastUpdatedAtPlatform", mongoData.get("lastUpdatedAt"));
        DmpFieldMapUtils.putJsonIfPresent(dmpDataMap, "marketplaceIdsJson", mongoData, "marketplaceIds");
        DmpFieldMapUtils.putJsonIfPresent(dmpDataMap, "sourceAddressJson", mongoData, "sourceAddress");
    }
}
