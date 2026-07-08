package com.sdk.tms.kuaidi100.service;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.sdk.tms.kuaidi100.model.request.Kuaidi100SubscribeParam;
import com.sdk.tms.kuaidi100.model.response.Kuaidi100SubscribeResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Kuaidi100 SDK parameter and response parsing verification.
 */
public class Kuaidi100ServiceTest {

    private final Kuaidi100Service kuaidi100Service = new Kuaidi100Service();

    @Test
    public void buildKuaidi100SubscribeParamWithMobile() {
        Kuaidi100SubscribeParam param = kuaidi100Service.buildKuaidi100SubscribeParam(
                "YuanTong", "YT123", "appKey", "https://erp.test/webhook/kuaidi100/push", true, "13800138000");

        assertEquals("yuantong", param.getCompany());
        assertEquals("YT123", param.getNumber());
        assertEquals("appKey", param.getKey());
        assertEquals("https://erp.test/webhook/kuaidi100/push", param.getParameters().getCallbackurl());
        assertEquals("", param.getParameters().getSalt());
        assertEquals("4", param.getParameters().getResultv2());
        assertEquals("13800138000", param.getParameters().getPhone());
    }

    @Test
    public void buildKuaidi100SubscribeParamWithoutMobile() {
        Kuaidi100SubscribeParam param = kuaidi100Service.buildKuaidi100SubscribeParam(
                "YTO", "YT123", "appKey", "https://erp.test/webhook/kuaidi100/push", false, "13800138000");

        assertEquals("yto", param.getCompany());
        assertNull(param.getParameters().getPhone());
    }

    @Test
    public void parseSubscribeResponseJson() {
        String responseJson = "{\"result\":true,\"returnCode\":\"200\",\"message\":\"submitted\"}";

        Kuaidi100SubscribeResponse response = JSONObject.parseObject(responseJson, Kuaidi100SubscribeResponse.class);

        assertTrue(response.getResult());
        assertEquals("200", response.getReturnCode());
        assertEquals("submitted", response.getMessage());
    }

    @Test
    public void convertTrackStatusCoversKuaidi100States() {
        assertEquals(LogisticTrackStatusEnum.TRACK_ING.getCode(), kuaidi100Service.convertTrackStatus("0"));
        assertEquals(LogisticTrackStatusEnum.WAIT_COLLECT.getCode(), kuaidi100Service.convertTrackStatus("1"));
        assertEquals(LogisticTrackStatusEnum.MAYBE_EXCEPTION.getCode(), kuaidi100Service.convertTrackStatus("2"));
        assertEquals(LogisticTrackStatusEnum.SIGN.getCode(), kuaidi100Service.convertTrackStatus("3"));
        assertEquals(LogisticTrackStatusEnum.RETURNED.getCode(), kuaidi100Service.convertTrackStatus("4"));
        assertEquals(LogisticTrackStatusEnum.DELIVERY_ING.getCode(), kuaidi100Service.convertTrackStatus("5"));
        assertEquals(LogisticTrackStatusEnum.DELIVERY_FAIL.getCode(), kuaidi100Service.convertTrackStatus("14"));
        assertEquals(LogisticTrackStatusEnum.NOT_FIND.getCode(), kuaidi100Service.convertTrackStatus(""));
        assertEquals(LogisticTrackStatusEnum.NOT_FIND.getCode(), kuaidi100Service.convertTrackStatus("999"));
    }
}
