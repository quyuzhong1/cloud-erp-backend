package com.common.message.controller;

import com.common.core.controller.vo.ApiResult;
import com.common.message.config.RocketMQConsumerActivationManager;
import com.common.message.config.RocketMQConsumerDrainManager;
import com.common.message.controller.vo.RocketMQLifecycleStatusVO;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.mockito.Mockito;

public class RocketMQConsumerStatusControllerTest {

    @Test
    public void shouldReportDisabledWithoutContainers() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerActivationManager activationManager = Mockito.mock(RocketMQConsumerActivationManager.class);
        RocketMQConsumerDrainManager drainManager = Mockito.mock(RocketMQConsumerDrainManager.class);
        Mockito.when(activationManager.getActivationState()).thenReturn("DEFERRED");
        Mockito.when(drainManager.getDrainState()).thenReturn("RUNNING");
        RocketMQConsumerStatusController controller = new RocketMQConsumerStatusController(
                context, activationManager, drainManager);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> response = controller.status(request);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("DISABLED", response.getBody().getData().getStatus());
        Assert.assertEquals(0, response.getBody().getData().getTotalContainers());
        context.close();
    }

    @Test
    public void shouldRejectNonLoopbackRequest() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerStatusController controller = new RocketMQConsumerStatusController(
                context,
                Mockito.mock(RocketMQConsumerActivationManager.class),
                Mockito.mock(RocketMQConsumerDrainManager.class));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.16.100.10");

        ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> response = controller.status(request);

        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        context.close();
    }

    @Test
    public void shouldStartTerminalDrainForLoopbackRequest() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerActivationManager activationManager = Mockito.mock(RocketMQConsumerActivationManager.class);
        RocketMQConsumerDrainManager drainManager = Mockito.mock(RocketMQConsumerDrainManager.class);
        Mockito.when(drainManager.getDrainState()).thenReturn("DRAINING");
        RocketMQConsumerStatusController controller = new RocketMQConsumerStatusController(
                context, activationManager, drainManager);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> response = controller.drain(request);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("DRAINING", response.getBody().getData().getDrainState());
        Mockito.verify(drainManager).beginDrain();
        context.close();
    }
}
