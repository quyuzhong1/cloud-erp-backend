package com.common.message.controller;

import com.common.core.controller.vo.ApiResult;
import com.common.message.config.RocketMQConsumerActivationManager;
import com.common.message.config.RocketMQConsumerDrainManager;
import com.common.message.controller.vo.RocketMQLifecycleStatusVO;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
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
        Mockito.when(activationManager.getActivationStateValue())
                .thenReturn(RocketMQConsumerActivationManager.ActivationState.DEFERRED);
        Mockito.when(drainManager.getDrainStateValue())
                .thenReturn(RocketMQConsumerDrainManager.DrainState.RUNNING);
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
        Mockito.when(activationManager.getActivationStateValue())
                .thenReturn(RocketMQConsumerActivationManager.ActivationState.DEFERRED);
        Mockito.when(drainManager.getDrainStateValue())
                .thenReturn(RocketMQConsumerDrainManager.DrainState.DRAINING);
        RocketMQConsumerStatusController controller = new RocketMQConsumerStatusController(
                context, activationManager, drainManager);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> response = controller.drain(request);

        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertEquals("DRAINING", response.getBody().getData().getDrainState());
        Mockito.verify(drainManager).beginDrain();
        context.close();
    }

    @Test
    public void shouldNotExposeDrainExceptionMessage() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        DefaultRocketMQListenerContainer unreadableContainer =
                Mockito.mock(DefaultRocketMQListenerContainer.class);
        Mockito.when(unreadableContainer.isRunning())
                .thenThrow(new IllegalStateException("container status unavailable"));
        context.getBeanFactory().registerSingleton("unreadableRocketMQContainer", unreadableContainer);
        RocketMQConsumerActivationManager activationManager = Mockito.mock(RocketMQConsumerActivationManager.class);
        RocketMQConsumerDrainManager drainManager = Mockito.mock(RocketMQConsumerDrainManager.class);
        Mockito.doThrow(new IllegalStateException("internal consumer shutdown detail"))
                .when(drainManager).beginDrain();
        Mockito.when(activationManager.getActivationStateValue())
                .thenReturn(RocketMQConsumerActivationManager.ActivationState.DEFERRED);
        Mockito.when(drainManager.getDrainStateValue())
                .thenReturn(RocketMQConsumerDrainManager.DrainState.FAILED);
        Mockito.when(drainManager.getFailureMessage()).thenReturn("internal consumer shutdown detail");
        RocketMQConsumerStatusController controller = new RocketMQConsumerStatusController(
                context, activationManager, drainManager);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> response = controller.drain(request);

        Assert.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assert.assertEquals("RocketMQ terminal drain failed", response.getBody().getMsg());
        Assert.assertEquals("DRAIN_FAILED", response.getBody().getData().getStatus());
        Assert.assertEquals("RocketMQ terminal drain failed", response.getBody().getData().getMessage());
        Assert.assertEquals("RocketMQ terminal drain failed", response.getBody().getData().getDrainFailure());
        Assert.assertFalse(response.getBody().getMsg().contains("internal consumer shutdown detail"));
        context.close();
    }

    @Test
    public void shouldReportDrainedAsDisabledWithNoRunningContainers() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        DefaultRocketMQListenerContainer stoppedContainer =
                Mockito.mock(DefaultRocketMQListenerContainer.class);
        Mockito.when(stoppedContainer.isRunning()).thenReturn(false);
        context.getBeanFactory().registerSingleton("stoppedRocketMQContainer", stoppedContainer);
        RocketMQConsumerActivationManager activationManager = Mockito.mock(RocketMQConsumerActivationManager.class);
        RocketMQConsumerDrainManager drainManager = Mockito.mock(RocketMQConsumerDrainManager.class);
        Mockito.when(activationManager.getActivationStateValue())
                .thenReturn(RocketMQConsumerActivationManager.ActivationState.ACTIVE);
        Mockito.when(activationManager.isEffectivelyEnabled()).thenReturn(false);
        Mockito.when(drainManager.getDrainStateValue())
                .thenReturn(RocketMQConsumerDrainManager.DrainState.DRAINED);
        Mockito.when(drainManager.getTotalContainers()).thenReturn(1);
        Mockito.when(drainManager.getDrainedContainers()).thenReturn(1);
        RocketMQConsumerStatusController controller = new RocketMQConsumerStatusController(
                context, activationManager, drainManager);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> response = controller.status(request);

        RocketMQLifecycleStatusVO status = response.getBody().getData();
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("DRAINED", status.getStatus());
        Assert.assertFalse(status.isEnabled());
        Assert.assertEquals(0, status.getRunningContainers());
        Assert.assertEquals("DRAINED", status.getDrainState());
        context.close();
    }

    @Test
    public void shouldPreserveActivationFailureInReadOnlyStatus() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerActivationManager activationManager = Mockito.mock(RocketMQConsumerActivationManager.class);
        RocketMQConsumerDrainManager drainManager = Mockito.mock(RocketMQConsumerDrainManager.class);
        Mockito.when(activationManager.getActivationStateValue())
                .thenReturn(RocketMQConsumerActivationManager.ActivationState.FAILED);
        Mockito.when(activationManager.getFailureMessage()).thenReturn("internal activation detail");
        Mockito.when(drainManager.getDrainStateValue())
                .thenReturn(RocketMQConsumerDrainManager.DrainState.RUNNING);
        RocketMQConsumerStatusController controller = new RocketMQConsumerStatusController(
                context, activationManager, drainManager);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> response = controller.status(request);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals("ACTIVATION_FAILED", response.getBody().getData().getStatus());
        Assert.assertEquals("FAILED", response.getBody().getData().getActivationState());
        Assert.assertEquals("RocketMQ activation failed", response.getBody().getData().getActivationFailure());
        context.close();
    }

    @Test
    public void shouldRejectActivationForInvalidConsumerSwitch() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerActivationManager activationManager = Mockito.mock(RocketMQConsumerActivationManager.class);
        RocketMQConsumerDrainManager drainManager = Mockito.mock(RocketMQConsumerDrainManager.class);
        Mockito.doThrow(new RocketMQConsumerActivationManager.InvalidConsumerSwitchException(
                "internal invalid switch detail")).when(activationManager).activate();
        Mockito.when(activationManager.getActivationStateValue())
                .thenReturn(RocketMQConsumerActivationManager.ActivationState.INVALID);
        Mockito.when(drainManager.getDrainStateValue())
                .thenReturn(RocketMQConsumerDrainManager.DrainState.RUNNING);
        RocketMQConsumerStatusController controller = new RocketMQConsumerStatusController(
                context, activationManager, drainManager);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> response = controller.activate(request);

        Assert.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assert.assertEquals("INVALID_CONFIGURATION", response.getBody().getData().getStatus());
        Assert.assertEquals("RocketMQ consumer configuration is invalid", response.getBody().getMsg());
        context.close();
    }
}
