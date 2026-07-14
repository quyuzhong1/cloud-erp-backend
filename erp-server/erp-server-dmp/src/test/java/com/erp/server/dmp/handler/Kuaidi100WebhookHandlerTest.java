package com.erp.server.dmp.handler;

import com.common.business.dto.WebhookResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.Kuaidi100WebhookResponseDTO;
import com.erp.server.dmp.service.DmpLogisticsTrackWebhookRecordService;
import com.sdk.tms.kuaidi100.service.Kuaidi100Service;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class Kuaidi100WebhookHandlerTest {

    @Test
    public void verifyValidFormBodyWithMd5UpperSign() throws Exception {
        Kuaidi100WebhookHandler handler = new Kuaidi100WebhookHandler();
        String param = callbackParam();
        String formBody = formBody(param, buildSign(param));

        handler.verify(formBody, Collections.emptyMap(), "kuaidi100");
    }

    @Test(expected = ServiceException.class)
    public void verifyInvalidSignThrowsException() throws Exception {
        Kuaidi100WebhookHandler handler = new Kuaidi100WebhookHandler();

        handler.verify(formBody(callbackParam(), "BAD_SIGN"), Collections.emptyMap(), "kuaidi100");
    }

    @Test(expected = ServiceException.class)
    public void verify_missingParam_throwsServiceException() {
        Kuaidi100WebhookHandler handler = new Kuaidi100WebhookHandler();

        handler.verify("sign=SIGN", Collections.emptyMap(), "kuaidi100");
    }

    @Test(expected = ServiceException.class)
    public void verify_missingSign_throwsServiceException() throws Exception {
        Kuaidi100WebhookHandler handler = new Kuaidi100WebhookHandler();

        handler.verify("param=" + URLEncoder.encode(callbackParam(), StandardCharsets.UTF_8.name()), Collections.emptyMap(), "kuaidi100");
    }

    @Test
    public void processParsesFormBodyAndSavesRawRecord() throws Exception {
        Kuaidi100WebhookHandler handler = new Kuaidi100WebhookHandler();
        DmpLogisticsTrackWebhookRecordService service = mock(DmpLogisticsTrackWebhookRecordService.class);
        setField(handler, "webhookRecordService", service);
        String param = callbackParam();
        String sign = buildSign(param);

        WebhookResult<Kuaidi100WebhookResponseDTO> result = handler.process(formBody(param, sign), Collections.emptyMap(), "kuaidi100");

        verify(service).saveKuaidi100RawRecord(param, sign);
        assertTrue(result.getData().getResult());
        assertEquals("200", result.getData().getReturnCode());
    }

    private String callbackParam() {
        return "{\"status\":\"polling\",\"billstatus\":\"check\",\"message\":\"ok\",\"lastResult\":{\"message\":\"ok\",\"nu\":\"YT123\",\"ischeck\":\"1\",\"com\":\"yuantong\",\"status\":\"200\",\"state\":\"3\",\"data\":[{\"time\":\"2026-07-07 10:00:00\",\"context\":\"signed\",\"areaName\":\"Shanghai\"}]}}";
    }

    private String formBody(String param, String sign) throws Exception {
        return "param=" + URLEncoder.encode(param, StandardCharsets.UTF_8.name()) + "&sign=" + sign;
    }

    private String buildSign(String param) {
        return DigestUtils.md5Hex((param + Kuaidi100Service.SALT).getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
