package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * B2B三方仓出库状态 DMP 转换处理器
 */
@Service
@Scope("prototype")
public class B2bThirdOutboundDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataList = entry.getValue();
            List<Map<String, Object>> sourceDataList = entry.getKey();
            if (sourceDataList.isEmpty()) {
                continue;
            }
            Map<String, Object> sourceData = sourceDataList.get(0);
            if (!sourceData.containsKey("dateShippingStr") || Objects.isNull(sourceData.get("dateShippingStr"))) {
                continue;
            }
            LocalDateTime shippingTime = parseDateTime(String.valueOf(sourceData.get("dateShippingStr")));
            if (Objects.isNull(shippingTime)) {
                continue;
            }
            for (TreeMap<String, Object> dmpData : dmpDataList) {
                dmpData.put("dateShipping", shippingTime);
            }
        }
    }

    private LocalDateTime parseDateTime(String deliveryTimeStr) {
        try {
            return LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(deliveryTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
        }
        return null;
    }
}
