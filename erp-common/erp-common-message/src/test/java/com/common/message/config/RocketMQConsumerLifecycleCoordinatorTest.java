package com.common.message.config;

import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.mock.env.MockEnvironment;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RocketMQConsumerLifecycleCoordinatorTest {

    @Test
    public void drainWaitsForActivationAndCapturesRegisteredContainer() throws Exception {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerLifecycleCoordinator coordinator = new RocketMQConsumerLifecycleCoordinator();
        CountDownLatch registrationStarted = new CountDownLatch(1);
        CountDownLatch allowRegistration = new CountDownLatch(1);
        AtomicBoolean containerRunning = new AtomicBoolean(true);
        DefaultRocketMQListenerContainer container = Mockito.mock(DefaultRocketMQListenerContainer.class);
        Mockito.when(container.isRunning()).thenAnswer(invocation -> containerRunning.get());
        Mockito.doAnswer(invocation -> {
            containerRunning.set(false);
            return null;
        }).when(container).stop();

        RocketMQConsumerActivationManager activationManager = new RocketMQConsumerActivationManager(
                context,
                deferredEnvironment(),
                () -> {
                    registrationStarted.countDown();
                    await(allowRegistration);
                    context.getBeanFactory().registerSingleton("testRocketMQContainer", container);
                },
                coordinator);
        AsyncTaskExecutor directExecutor = new TaskExecutorAdapter(Runnable::run);
        RocketMQConsumerDrainManager drainManager = new RocketMQConsumerDrainManager(
                context, coordinator, directExecutor, directExecutor);
        AtomicReference<Throwable> activationFailure = new AtomicReference<>();
        AtomicReference<Throwable> drainFailure = new AtomicReference<>();

        Thread activationThread = new Thread(() -> runSafely(activationManager::activate, activationFailure));
        Thread drainThread = new Thread(() -> runSafely(drainManager::beginDrain, drainFailure));
        activationThread.start();
        Assert.assertTrue(registrationStarted.await(2, TimeUnit.SECONDS));
        drainThread.start();
        Thread.sleep(50);
        Assert.assertTrue("drain must wait while listener registration holds the lifecycle lock", drainThread.isAlive());

        allowRegistration.countDown();
        activationThread.join(2000);
        drainThread.join(2000);

        Assert.assertNull(activationFailure.get());
        Assert.assertNull(drainFailure.get());
        Assert.assertEquals("DRAINED", drainManager.getDrainState());
        Assert.assertEquals(1, drainManager.getTotalContainers());
        Mockito.verify(container).stop();
        context.close();
    }

    @Test
    public void applicationReadyActivationIsSkippedAfterTerminalDrainStarts() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerLifecycleCoordinator coordinator = new RocketMQConsumerLifecycleCoordinator();
        AsyncTaskExecutor directExecutor = new TaskExecutorAdapter(Runnable::run);
        RocketMQConsumerDrainManager drainManager = new RocketMQConsumerDrainManager(
                context, coordinator, directExecutor, directExecutor);
        RocketMQConsumerActivationManager activationManager = new RocketMQConsumerActivationManager(
                context,
                deferredEnvironment(),
                () -> Assert.fail("listener registration must remain blocked"),
                coordinator);

        drainManager.beginDrain();
        activationManager.activateOnApplicationReadyWhenColorIsActive();

        Assert.assertEquals("DRAINED", drainManager.getDrainState());
        Assert.assertEquals("DEFERRED", activationManager.getActivationState());
        context.close();
    }

    @Test
    public void directActivationIsRejectedAfterTerminalDrainStarts() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        RocketMQConsumerLifecycleCoordinator coordinator = new RocketMQConsumerLifecycleCoordinator();
        AsyncTaskExecutor directExecutor = new TaskExecutorAdapter(Runnable::run);
        RocketMQConsumerDrainManager drainManager = new RocketMQConsumerDrainManager(
                context, coordinator, directExecutor, directExecutor);
        RocketMQConsumerActivationManager activationManager = new RocketMQConsumerActivationManager(
                context,
                deferredEnvironment(),
                () -> Assert.fail("listener registration must remain blocked"),
                coordinator);

        drainManager.beginDrain();

        try {
            activationManager.activate();
            Assert.fail("direct activation must be rejected after terminal drain");
        } catch (RocketMQConsumerLifecycleCoordinator.TerminalDrainStartedException expected) {
            Assert.assertEquals("DRAINED", drainManager.getDrainState());
        } finally {
            context.close();
        }
    }

    private MockEnvironment deferredEnvironment() {
        return new MockEnvironment()
                .withProperty(RocketMQConsumerBootstrapPostProcessor.CONSUMER_ENABLED_PROPERTY, "false")
                .withProperty(RocketMQConsumerActivationManager.MQ_ACTIVE_COLOR_PROPERTY, "green")
                .withProperty(RocketMQConsumerActivationManager.LOCAL_COLOR_PROPERTY, "green");
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new AssertionError("timed out waiting for test latch");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        }
    }

    private void runSafely(Runnable action, AtomicReference<Throwable> failure) {
        try {
            action.run();
        } catch (Throwable ex) {
            failure.set(ex);
        }
    }
}
