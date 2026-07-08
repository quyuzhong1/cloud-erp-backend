package com.erp.server.dmp.handler;

import com.common.business.dto.WebhookResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.Kuaidi100WebhookResponseDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.rpc.tms.feign.LogisticsFeign;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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
        String formBody = formBody(param, DigestUtils.md5Hex(param.getBytes(StandardCharsets.UTF_8)).toUpperCase());

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
    public void processParsesFormBodyAndForwardsDto() throws Exception {
        Kuaidi100WebhookHandler handler = new Kuaidi100WebhookHandler();
        LogisticsFeign logisticsFeign = mock(LogisticsFeign.class);
        setField(handler, "logisticsFeign", logisticsFeign);
        String param = callbackParam();
        String sign = DigestUtils.md5Hex(param.getBytes(StandardCharsets.UTF_8)).toUpperCase();

        WebhookResult<Kuaidi100WebhookResponseDTO> result = handler.process(formBody(param, sign), Collections.emptyMap(), "kuaidi100");

        ArgumentCaptor<LogisticsTrackDTO.Kuaidi100WebHookDTO> captor = ArgumentCaptor.forClass(LogisticsTrackDTO.Kuaidi100WebHookDTO.class);
        verify(logisticsFeign).webhookByKuaidi100(captor.capture());
        LogisticsTrackDTO.Kuaidi100WebHookDTO dto = captor.getValue();
        assertEquals(sign, dto.getSign());
        assertEquals("polling", dto.getStatus());
        assertEquals("YT123", dto.getLastResult().getNu());
        assertEquals("3", dto.getLastResult().getState());
        assertEquals(1, dto.getLastResult().getData().size());
        assertEquals("signed", dto.getLastResult().getData().get(0).getContext());
        assertTrue(result.getData().getResult());
        assertEquals("200", result.getData().getReturnCode());
    }

    @Test(expected = ServiceException.class)
    public void process_missingLastResult_throwsServiceException() throws Exception {
        Kuaidi100WebhookHandler handler = new Kuaidi100WebhookHandler();
        String param = "{\"status\":\"polling\",\"message\":\"ok\"}";
        String sign = DigestUtils.md5Hex(param.getBytes(StandardCharsets.UTF_8)).toUpperCase();

        handler.process(formBody(param, sign), Collections.emptyMap(), "kuaidi100");
    }

    private String callbackParam() {
        return "{\"status\":\"polling\",\"billstatus\":\"check\",\"message\":\"ok\",\"lastResult\":{\"message\":\"ok\",\"nu\":\"YT123\",\"ischeck\":\"1\",\"com\":\"yuantong\",\"status\":\"200\",\"state\":\"3\",\"data\":[{\"time\":\"2026-07-07 10:00:00\",\"context\":\"signed\",\"areaName\":\"Shanghai\"}]}}";
    }

    private String formBody(String param, String sign) throws Exception {
        return "param=" + URLEncoder.encode(param, StandardCharsets.UTF_8.name()) + "&sign=" + sign;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
