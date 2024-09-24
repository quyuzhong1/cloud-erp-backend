package com.erp.server.dmp.inout.handler.input.task.dmp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 亚马逊报告查询字段映射转换
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportQueryHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzReportQueryHandler afterConvertData 处理");
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("created_method", "system");
                dmpDataMap.put("report_schedule_id", "");

                Object marketplaceIdsObj = dmpDataMap.get("marketplaceIds");
                String marketplaceIdsStr = null == marketplaceIdsObj ? "" : marketplaceIdsObj.toString();
                dmpDataMap.put("marketplace_ids", marketplaceIdsStr);
                // 店铺ID
                dmpDataMap.put("shop_id", dmpCfgInputDetailEntity.getNextLevelId());
            }
        }
    }
}
