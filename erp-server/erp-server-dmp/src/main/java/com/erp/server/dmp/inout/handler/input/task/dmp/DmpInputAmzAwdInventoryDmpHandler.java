package com.erp.server.dmp.inout.handler.input.task.dmp;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * @Author: wtr
 * @Date: 2025/12/25 8:40
 * @Param:
 * @Return:
 * @Description:
 **/
@Service
@Scope("prototype")
public class DmpInputAmzAwdInventoryDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
            Map<String, Object> mongoData = mongoDataMaps.get(0);
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("msku",Objects.nonNull(dmpDataMap.get("sku")) && StringUtils.isNotBlank(dmpDataMap.get("sku").toString()) ? dmpDataMap.get("sku") : "");
                dmpDataMap.put("totalInboundQty",mongoData.get("totalInboundQuantity"));
                dmpDataMap.put("totalOnhandQty",mongoData.get("totalOnhandQuantity"));
                dmpDataMap.put("availableDistributableQty",mongoData.get("availableDistributableQuantity"));
                dmpDataMap.put("replenishmentQty",mongoData.get("replenishmentQuantity"));
                dmpDataMap.put("reservedDistributableQty",mongoData.get("reservedDistributableQuantity"));
            }
        }
    }

}
