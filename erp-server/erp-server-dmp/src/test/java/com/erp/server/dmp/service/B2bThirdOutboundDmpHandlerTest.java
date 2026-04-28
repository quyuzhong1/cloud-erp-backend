package com.erp.server.dmp.service;

import com.erp.server.dmp.inout.handler.input.task.dmp.B2bThirdOutboundDmpHandler;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class B2bThirdOutboundDmpHandlerTest {

    @Test
    public void shouldFillB2bOrderTypeAndPlatformTime() {
        TestB2bThirdOutboundDmpHandler handler = new TestB2bThirdOutboundDmpHandler();
        Map<String, Object> sourceData = new HashMap<>();
        sourceData.put("dateShippingStr", "2026-05-07 15:15:26");
        sourceData.put("platformCreateTimeStr", "2026-05-07 10:00:00");
        sourceData.put("platformUpdateTimeStr", "2026-05-07 15:15:26");

        TreeMap<String, Object> dmpData = new TreeMap<>();

        handler.apply(sourceData, dmpData);

        assertEquals("B2B", dmpData.get("orderType"));
        assertEquals(LocalDateTime.of(2026, 5, 7, 15, 15, 26), dmpData.get("dateShipping"));
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 0, 0), dmpData.get("platformCreateTime"));
        assertEquals(LocalDateTime.of(2026, 5, 7, 15, 15, 26), dmpData.get("platformUpdateTime"));
    }

    private static class TestB2bThirdOutboundDmpHandler extends B2bThirdOutboundDmpHandler {

        private void apply(Map<String, Object> sourceData, TreeMap<String, Object> dmpData) {
            Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> relationMap = new HashMap<>();
            List<Map<String, Object>> sourceList = new ArrayList<>();
            sourceList.add(sourceData);
            List<TreeMap<String, Object>> dmpList = new ArrayList<>();
            dmpList.add(dmpData);
            relationMap.put(sourceList, dmpList);
            afterConvertData(relationMap);
        }
    }
}
