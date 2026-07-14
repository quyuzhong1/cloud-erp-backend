package com.erp.server.dmp.handler;

import com.common.business.dto.WebhookResult;
import com.erp.server.dmp.service.DmpLogisticsTrackWebhookRecordService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class Track123WebhookHandlerTest {

    @Test
    public void process_validRawData_savesTrack123RawRecord() throws Exception {
        Track123WebhookHandler handler = new Track123WebhookHandler();
        DmpLogisticsTrackWebhookRecordService service = mock(DmpLogisticsTrackWebhookRecordService.class);
        setField(handler, "webhookRecordService", service);
        String rawData = "{\"data\":{\"trackNo\":\"T123\"}}";

        WebhookResult result = handler.process(rawData, Collections.emptyMap(), "track123");

        verify(service).saveTrack123RawRecord(rawData);
        assertEquals("success", result.getFlag());
        assertEquals(Integer.valueOf(200), result.getCode());
    }

    @Test
    public void verify_anyPayload_doesNotThrow() {
        Track123WebhookHandler handler = new Track123WebhookHandler();

        handler.verify("{\"data\":[]}", Collections.emptyMap(), "track123");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
