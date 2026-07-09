package com.erp.server.dmp.controller.api;

import com.common.business.dto.WebhookResult;
import com.erp.model.dmp.dto.Kuaidi100WebhookResponseDTO;
import com.erp.server.dmp.factory.WebhookHandlerFactory;
import com.erp.server.dmp.handler.WebhookHandler;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebhookControllerTest {

    @Test
    public void receiveWebhook_kuaidi100Success_returnsKuaidi100SuccessBody() {
        WebhookHandlerFactory factory = mock(WebhookHandlerFactory.class);
        WebhookHandler handler = mock(WebhookHandler.class);
        when(factory.getHandler("kuaidi100")).thenReturn(handler);
        WebhookResult<Kuaidi100WebhookResponseDTO> webhookResult = new WebhookResult<>();
        webhookResult.setData(Kuaidi100WebhookResponseDTO.success());
        when(handler.process("param=data", Collections.emptyMap(), "kuaidi100")).thenReturn(webhookResult);
        WebhookController controller = new WebhookController(factory);

        ResponseEntity<?> response = controller.receiveWebhook("kuaidi100", "param=data", Collections.emptyMap());

        assertEquals(200, response.getStatusCodeValue());
        Kuaidi100WebhookResponseDTO actualBody = (Kuaidi100WebhookResponseDTO) response.getBody();
        assertTrue(actualBody.getResult());
        assertEquals("200", actualBody.getReturnCode());
        verify(handler).verify("param=data", Collections.emptyMap(), "kuaidi100");
    }

    @Test
    public void receiveWebhook_kuaidi100HandlerThrows_returnsKuaidi100FailureBody() {
        WebhookHandlerFactory factory = mock(WebhookHandlerFactory.class);
        WebhookHandler handler = mock(WebhookHandler.class);
        when(factory.getHandler("kuaidi100")).thenReturn(handler);
        doThrow(new RuntimeException("bad sign")).when(handler).verify("param=data", Collections.emptyMap(), "kuaidi100");
        WebhookController controller = new WebhookController(factory);

        ResponseEntity<?> response = controller.receiveWebhook("kuaidi100", "param=data", Collections.emptyMap());

        assertEquals(200, response.getStatusCodeValue());
        Kuaidi100WebhookResponseDTO actualBody = (Kuaidi100WebhookResponseDTO) response.getBody();
        assertFalse(actualBody.getResult());
        assertEquals("500", actualBody.getReturnCode());
        assertEquals("bad sign", actualBody.getMessage());
    }

    @Test
    public void receiveKuaidi100Push_success_returnsOfficialResponseBody() {
        WebhookHandlerFactory factory = mock(WebhookHandlerFactory.class);
        WebhookHandler handler = mock(WebhookHandler.class);
        when(factory.getHandler("kuaidi100")).thenReturn(handler);
        WebhookResult<Kuaidi100WebhookResponseDTO> webhookResult = new WebhookResult<>();
        webhookResult.setData(Kuaidi100WebhookResponseDTO.success());
        when(handler.process(startsWith("param=%7B%22status%22"), org.mockito.Mockito.eq(Collections.emptyMap()), org.mockito.Mockito.eq("kuaidi100")))
                .thenReturn(webhookResult);
        WebhookController controller = new WebhookController(factory);

        Kuaidi100WebhookResponseDTO response = controller.receiveKuaidi100Push("{\"status\":\"polling\"}", "ABC123");

        assertTrue(response.getResult());
        assertEquals("200", response.getReturnCode());
        assertEquals("成功", response.getMessage());
        verify(handler).verify(startsWith("param=%7B%22status%22"), org.mockito.Mockito.eq(Collections.emptyMap()), org.mockito.Mockito.eq("kuaidi100"));
    }

    @Test
    public void receiveKuaidi100Push_handlerThrows_returnsOfficialFailureBody() {
        WebhookHandlerFactory factory = mock(WebhookHandlerFactory.class);
        WebhookHandler handler = mock(WebhookHandler.class);
        when(factory.getHandler("kuaidi100")).thenReturn(handler);
        doThrow(new RuntimeException("bad sign"))
                .when(handler).verify(startsWith("param=%7B%22status%22"), org.mockito.Mockito.eq(Collections.emptyMap()), org.mockito.Mockito.eq("kuaidi100"));
        WebhookController controller = new WebhookController(factory);

        Kuaidi100WebhookResponseDTO response = controller.receiveKuaidi100Push("{\"status\":\"polling\"}", "BAD");

        assertFalse(response.getResult());
        assertEquals("500", response.getReturnCode());
        assertEquals("bad sign", response.getMessage());
    }
}
