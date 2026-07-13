package com.erp.server.dmp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;

import java.util.HashMap;
import java.util.Map;

public final class Track123WebhookPayloadParser {

    public static final String WEBHOOK_RECORD_ID = "webhookRecordId";
    public static final String DATA = "data";
    public static final String TRACK_NO = "trackNo";

    private Track123WebhookPayloadParser() {
    }

    public static String extractTrackNo(String rawData) {
        return getRequiredTrackNo(parseTrackData(rawData));
    }

    public static Map<String, Object> buildMongoData(String webhookRecordId, String rawData) {
        JSONObject data = parseTrackData(rawData);
        getRequiredTrackNo(data);
        Map<String, Object> mongoData = new HashMap<>(data);
        mongoData.put(WEBHOOK_RECORD_ID, webhookRecordId);
        return mongoData;
    }

    private static JSONObject parseTrackData(String rawData) {
        JSONObject rawDataJson = parseJsonObject(rawData,
                ApiError.DMP_TRACK123_WEBHOOK_RAW_DATA_REQUIRED,
                ApiError.DMP_TRACK123_WEBHOOK_RAW_DATA_JSON_INVALID);
        Object data = rawDataJson.get(DATA);
        if (data == null) {
            return rawDataJson;
        }
        if (!(data instanceof JSONObject)) {
            throw new ServiceException(ApiError.DMP_TRACK123_WEBHOOK_DATA_NODE_INVALID);
        }
        return (JSONObject) data;
    }

    private static String getRequiredTrackNo(JSONObject data) {
        String trackNo = data.getString(TRACK_NO);
        if (isBlank(trackNo)) {
            throw new ServiceException(ApiError.DMP_TRACK123_WEBHOOK_TRACK_NO_REQUIRED);
        }
        return trackNo;
    }

    private static JSONObject parseJsonObject(String data, ApiError blankError, ApiError invalidError) {
        if (isBlank(data)) {
            throw new ServiceException(blankError);
        }
        try {
            JSONObject jsonObject = JSON.parseObject(data);
            if (jsonObject == null) {
                throw new ServiceException(invalidError);
            }
            return jsonObject;
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException(e, invalidError);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
