package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * FBT货件主表DMP处理器
 */
@Service
@Scope("prototype")
public class FbtShipmentDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> relationEntry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = relationEntry.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Object carriers = dmpDataMap.get("carriers");
                Object receivedBatches = dmpDataMap.get("receivedBatches");
                dmpDataMap.put("carrierListJson", carriers == null ? "[]" : JSON.toJSONString(carriers));
                dmpDataMap.put("receivedBatchesJson", receivedBatches == null ? "[]" : JSON.toJSONString(receivedBatches));
                dmpDataMap.put("platformUpdateTime", parseDateTime(dmpDataMap.get("platformUpdateTimeRaw")));
                if (StringUtils.isBlank(stringVal(dmpDataMap.get("nextLevelId")))) {
                    dmpDataMap.put("nextLevelId", stringVal(dmpDataMap.get("authId")));
                }
            }
        }
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            return null;
        }
        String raw = String.valueOf(value);
        if (StringUtils.isBlank(raw) || "null".equalsIgnoreCase(raw)) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ignore) {
            return null;
        }
    }

    private String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
