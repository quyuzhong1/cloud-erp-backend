package com.erp.server.dmp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Kuaidi100WebhookPayloadParser {

    public static final String PARAM = "param";
    public static final String SIGN = "sign";
    public static final String WEBHOOK_RECORD_ID = "webhookRecordId";
    public static final String LAST_RESULT = "lastResult";
    public static final String NU = "nu";

    private Kuaidi100WebhookPayloadParser() {
    }

    public static Map<String, String> parseFormBody(String data) {
        Map<String, String> formMap = new HashMap<>();
        if (isBlank(data)) {
            return formMap;
        }
        String[] pairs = data.split("&", -1);
        for (String pair : pairs) {
            int index = pair.indexOf("=");
            String key = index < 0 ? pair : pair.substring(0, index);
            String value = index < 0 ? "" : pair.substring(index + 1);
            formMap.put(urlDecode(key), urlDecode(value));
        }
        return formMap;
    }

    public static JSONObject buildRawData(String param, String sign) {
        JSONObject rawData = new JSONObject(true);
        rawData.put(PARAM, param);
        rawData.put(SIGN, sign);
        return rawData;
    }

    public static String extractTrackNo(String param) {
        return getRequiredTrackNo(parseLastResult(param));
    }

    public static Map<String, Object> buildMongoData(String webhookRecordId, String rawData) {
        JSONObject rawDataJson = parseJsonObject(rawData, ApiError.DMP_KUAIDI100_WEBHOOK_RAW_DATA_REQUIRED, ApiError.DMP_KUAIDI100_WEBHOOK_RAW_DATA_JSON_INVALID);
        String param = rawDataJson.getString(PARAM);
        if (isBlank(param)) {
            throw new ServiceException(ApiError.DMP_KUAIDI100_WEBHOOK_RAW_DATA_PARAM_REQUIRED);
        }
        JSONObject lastResult = parseLastResult(param);
        getRequiredTrackNo(lastResult);
        Map<String, Object> mongoData = new HashMap<>(lastResult);
        mongoData.put(WEBHOOK_RECORD_ID, webhookRecordId);
        return mongoData;
    }

    private static JSONObject parseLastResult(String param) {
        JSONObject paramJson = parseJsonObject(param, ApiError.DMP_KUAIDI100_WEBHOOK_PARAM_REQUIRED, ApiError.DMP_KUAIDI100_WEBHOOK_PARAM_JSON_INVALID);
        Object lastResult = paramJson.get(LAST_RESULT);
        if (!(lastResult instanceof JSONObject)) {
            throw new ServiceException(ApiError.DMP_KUAIDI100_WEBHOOK_LAST_RESULT_REQUIRED);
        }
        return (JSONObject) lastResult;
    }

    private static String getRequiredTrackNo(JSONObject lastResult) {
        String trackNo = lastResult.getString(NU);
        if (isBlank(trackNo)) {
            throw new ServiceException(ApiError.DMP_KUAIDI100_WEBHOOK_TRACK_NO_REQUIRED);
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

    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            throw new ServiceException(e, ApiError.DMP_KUAIDI100_WEBHOOK_FORM_URL_DECODE_FAILED);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
