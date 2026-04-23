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

    private static final String ORDER_TYPE = "orderType";
    private static final String DATE_SHIPPING = "dateShipping";
    private static final String DATE_SHIPPING_STR = "dateShippingStr";
    private static final String PLATFORM_CREATE_TIME = "platformCreateTime";
    private static final String PLATFORM_CREATE_TIME_STR = "platformCreateTimeStr";
    private static final String PLATFORM_UPDATE_TIME = "platformUpdateTime";
    private static final String PLATFORM_UPDATE_TIME_STR = "platformUpdateTimeStr";
    private static final String ORDER_TYPE_B2B = "B2B";

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
            for (TreeMap<String, Object> dmpData : dmpDataList) {
                dmpData.put(ORDER_TYPE, ORDER_TYPE_B2B);
                putDateTimeIfPresent(dmpData, DATE_SHIPPING, sourceData.get(DATE_SHIPPING_STR));
                putDateTimeIfPresent(dmpData, PLATFORM_CREATE_TIME, sourceData.get(PLATFORM_CREATE_TIME_STR));
                putDateTimeIfPresent(dmpData, PLATFORM_UPDATE_TIME, sourceData.get(PLATFORM_UPDATE_TIME_STR));
            }
        }
    }

    private void putDateTimeIfPresent(TreeMap<String, Object> dmpData, String key, Object value) {
        if (Objects.isNull(value)) {
            return;
        }
        LocalDateTime dateTime = parseDateTime(String.valueOf(value));
        if (Objects.nonNull(dateTime)) {
            dmpData.put(key, dateTime);
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
