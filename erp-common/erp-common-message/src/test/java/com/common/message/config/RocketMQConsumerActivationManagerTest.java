package com.common.message.config;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RocketMQConsumerActivationManagerTest {

    @Test
    public void shouldActivateOnlyOnceWhenMqColorMatches() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        MockEnvironment environment = deferredEnvironment("green", "green");
        AtomicInteger registrations = new AtomicInteger();
        DefaultRocketMQListenerContainer container = Mockito.mock(DefaultRocketMQListenerContainer.class);
        Mockito.when(container.isRunning()).thenReturn(true);
        RocketMQConsumerActivationManager manager = new RocketMQConsumerActivationManager(
                context, environment, () -> {
                    registrations.incrementAndGet();
                    context.getBeanFactory().registerSingleton("testRocketMQContainer", container);
                });

        manager.activate();
        manager.activate();

        Assert.assertEquals(1, registrations.get());
        Assert.assertTrue(manager.isEffectivelyEnabled());
        Assert.assertEquals("ACTIVE", manager.getActivationState());
        context.close();
    }

    @Test
    public void shouldCompleteActivationWhenServiceHasNoMqListeners() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        AtomicInteger registrations = new AtomicInteger();
        RocketMQConsumerActivationManager manager = new RocketMQConsumerActivationManager(
                context, deferredEnvironment("green", "green"), registrations::incrementAndGet);

        manager.activate();

        Assert.assertEquals(1, registrations.get());
        Assert.assertTrue(manager.isEffectivelyEnabled());
        Assert.assertEquals("ACTIVE", manager.getActivationState());
        Assert.assertEquals(0, manager.containerSummary().getTotal());
        context.close();
    }

    @Test(expected = RocketMQConsumerActivationManager.ActivationNotEligibleException.class)
    public void shouldRejectActivationWhenMqColorDoesNotMatch() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerActivationManager manager = new RocketMQConsumerActivationManager(
                context, deferredEnvironment("blue", "green"), () -> Assert.fail("registrar must not be called"));
        try {
            manager.activate();
        } finally {
            context.close();
        }
    }

    @Test
    public void shouldRejectActivationForMalformedStartupSwitch() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        MockEnvironment environment = deferredEnvironment("green", "green")
                .withProperty(RocketMQConsumerBootstrapPostProcessor.CONSUMER_ENABLED_PROPERTY, "mqEnabled");

        RocketMQConsumerActivationManager manager = new RocketMQConsumerActivationManager(
                context, environment, () -> Assert.fail("registrar must not run for an invalid switch"));

        Assert.assertFalse(manager.isStartupEnabled());
        Assert.assertEquals("INVALID", manager.getActivationState());
        try {
            manager.activate();
            Assert.fail("malformed consumer switch must fail closed");
        } catch (RocketMQConsumerActivationManager.InvalidConsumerSwitchException expected) {
            Assert.assertTrue(expected.getMessage().contains("startup switch is invalid"));
        }
        context.close();
    }

    @Test
    public void shouldStopNewlyStartedContainersWhenActivationIsIncomplete() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        AtomicBoolean firstRunning = new AtomicBoolean(true);
        DefaultMQPushConsumer firstConsumer = Mockito.mock(DefaultMQPushConsumer.class);
        DefaultRocketMQListenerContainer firstContainer = Mockito.mock(DefaultRocketMQListenerContainer.class);
        DefaultRocketMQListenerContainer failedContainer = Mockito.mock(DefaultRocketMQListenerContainer.class);
        Mockito.when(firstContainer.isRunning()).thenAnswer(invocation -> firstRunning.get());
        Mockito.when(firstContainer.getConsumer()).thenReturn(firstConsumer);
        Mockito.doAnswer(invocation -> {
            firstRunning.set(false);
            return null;
        }).when(firstContainer).stop();
        Mockito.when(failedContainer.isRunning()).thenReturn(false);
        RocketMQConsumerActivationManager manager = new RocketMQConsumerActivationManager(
                context,
                deferredEnvironment("green", "green"),
                () -> {
                    context.getBeanFactory().registerSingleton("firstRocketMQContainer", firstContainer);
                    context.getBeanFactory().registerSingleton("failedRocketMQContainer", failedContainer);
                });

        try {
            manager.activate();
            Assert.fail("partial listener activation must fail");
        } catch (IllegalStateException expected) {
            Assert.assertTrue(expected.getMessage().contains("activation incomplete"));
        }

        Mockito.verify(firstConsumer).setAwaitTerminationMillisWhenShutdown(Long.MAX_VALUE);
        Mockito.verify(firstContainer).stop();
        Assert.assertFalse(firstContainer.isRunning());
        Assert.assertFalse(failedContainer.isRunning());
        Assert.assertFalse(manager.isEffectivelyEnabled());
        Assert.assertEquals("FAILED", manager.getActivationState());
        context.close();
    }

    @Test
    public void shouldReportDisabledAfterTerminalDrainStarts() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerLifecycleCoordinator coordinator = new RocketMQConsumerLifecycleCoordinator();
        RocketMQConsumerActivationManager manager = new RocketMQConsumerActivationManager(
                context,
                deferredEnvironment("green", "green"),
                () -> {
                    // This service intentionally has no listeners.
                },
                coordinator);
        manager.activate();
        Assert.assertTrue(manager.isEffectivelyEnabled());

        coordinator.beginTerminalDrain(() -> "captured");

        Assert.assertFalse(manager.isEffectivelyEnabled());
        context.close();
    }

    private MockEnvironment deferredEnvironment(String mqActiveColor, String localColor) {
        return new MockEnvironment()
                .withProperty(RocketMQConsumerBootstrapPostProcessor.CONSUMER_ENABLED_PROPERTY, "false")
                .withProperty(RocketMQConsumerActivationManager.MQ_ACTIVE_COLOR_PROPERTY, mqActiveColor)
                .withProperty(RocketMQConsumerActivationManager.LOCAL_COLOR_PROPERTY, localColor);
    }
}
