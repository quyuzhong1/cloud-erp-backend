package com.common.message.config;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.GenericApplicationContext;

public class RocketMQConsumerDrainManagerTest {

    @Test
    public void shouldDrainAllContainersWithUnboundedRocketMqWait() throws Exception {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        DefaultMQPushConsumer consumer = Mockito.mock(DefaultMQPushConsumer.class);
        DefaultRocketMQListenerContainer container = Mockito.mock(DefaultRocketMQListenerContainer.class);
        Mockito.when(container.getConsumer()).thenReturn(consumer);
        context.getBeanFactory().registerSingleton("testRocketMQContainer", container);
        RocketMQConsumerDrainManager manager = new RocketMQConsumerDrainManager(context);

        manager.beginDrain();
        waitForState(manager, "DRAINED");

        Mockito.verify(consumer).setAwaitTerminationMillisWhenShutdown(Long.MAX_VALUE);
        Mockito.verify(container).stop();
        Assert.assertEquals(1, manager.getTotalContainers());
        Assert.assertEquals(1, manager.getDrainedContainers());
        context.close();
    }

    @Test
    public void shouldImmediatelyDrainWhenNoContainersExist() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerDrainManager manager = new RocketMQConsumerDrainManager(context);

        manager.beginDrain();

        Assert.assertEquals("DRAINED", manager.getDrainState());
        Assert.assertEquals(0, manager.getTotalContainers());
        context.close();
    }

    private void waitForState(RocketMQConsumerDrainManager manager, String expected) throws InterruptedException {
        for (int attempt = 0; attempt < 100; attempt++) {
            if (expected.equals(manager.getDrainState())) {
                return;
            }
            Thread.sleep(10);
        }
        Assert.fail("Timed out waiting for drain state " + expected + ", actual=" + manager.getDrainState());
    }
}
