package com.common.message.config;

import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

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

    private MockEnvironment deferredEnvironment(String mqActiveColor, String localColor) {
        return new MockEnvironment()
                .withProperty(RocketMQConsumerBootstrapPostProcessor.CONSUMER_ENABLED_PROPERTY, "false")
                .withProperty(RocketMQConsumerActivationManager.MQ_ACTIVE_COLOR_PROPERTY, mqActiveColor)
                .withProperty(RocketMQConsumerActivationManager.LOCAL_COLOR_PROPERTY, localColor);
    }
}
