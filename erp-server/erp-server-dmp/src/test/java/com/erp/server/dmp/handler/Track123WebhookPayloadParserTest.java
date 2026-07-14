package com.erp.server.dmp.handler;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Track123WebhookPayloadParserTest {

    @Test
    public void extractTrackNo_dataNodeWithTrackNo_returnsTrackNo() {
        String rawData = "{\"data\":{\"trackNo\":\"T123\",\"transitStatus\":\"DELIVERED\"}}";

        String trackNo = Track123WebhookPayloadParser.extractTrackNo(rawData);

        assertEquals("T123", trackNo);
    }

    @Test
    public void buildMongoData_rootPayload_addsWebhookRecordId() {
        String rawData = "{\"trackNo\":\"T123\",\"localLogisticsInfo\":{\"trackingDetails\":[]}}";

        Map<String, Object> mongoData = Track123WebhookPayloadParser.buildMongoData("record-1", rawData);

        assertEquals("T123", mongoData.get("trackNo"));
        assertEquals("record-1", mongoData.get("webhookRecordId"));
    }

    @Test
    public void extractTrackNo_blankRawData_throwsApiError() {
        try {
            Track123WebhookPayloadParser.extractTrackNo("");
            fail("Expected ServiceException");
        } catch (ServiceException e) {
            assertEquals(ApiError.DMP_TRACK123_WEBHOOK_RAW_DATA_REQUIRED.getCode(), e.getCode());
        }
    }

    @Test
    public void extractTrackNo_invalidJson_throwsApiError() {
        try {
            Track123WebhookPayloadParser.extractTrackNo("{");
            fail("Expected ServiceException");
        } catch (ServiceException e) {
            assertEquals(ApiError.DMP_TRACK123_WEBHOOK_RAW_DATA_JSON_INVALID.getCode(), e.getCode());
        }
    }

    @Test
    public void extractTrackNo_dataNodeIsArray_throwsApiError() {
        try {
            Track123WebhookPayloadParser.extractTrackNo("{\"data\":[]}");
            fail("Expected ServiceException");
        } catch (ServiceException e) {
            assertEquals(ApiError.DMP_TRACK123_WEBHOOK_DATA_NODE_INVALID.getCode(), e.getCode());
        }
    }

    @Test
    public void extractTrackNo_missingTrackNo_throwsApiError() {
        try {
            Track123WebhookPayloadParser.extractTrackNo("{\"data\":{\"transitStatus\":\"DELIVERED\"}}");
            fail("Expected ServiceException");
        } catch (ServiceException e) {
            assertEquals(ApiError.DMP_TRACK123_WEBHOOK_TRACK_NO_REQUIRED.getCode(), e.getCode());
        }
    }
}
