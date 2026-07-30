package com.common.message.config;

import com.xxl.job.core.executor.XxlJobExecutor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReleaseControlledXxlJobSpringExecutorTest {

    @Test
    public void shouldDrainInOrderAndBecomeDrained() throws Exception {
        TestExecutor executor = new TestExecutor(directExecutor());
        markStarted(executor);

        executor.beginDrain();

        Assert.assertEquals(Arrays.asList("stop", "wait", "destroy"), executor.events);
        Assert.assertEquals("DRAINED", executor.getDrainState());
        Assert.assertFalse(executor.isAcceptingTriggers());
    }

    @Test
    public void shouldNotDestroyWhileAcceptedJobIsStillBusy() throws Exception {
        ThreadPoolTaskExecutor taskExecutor = asyncExecutor();
        TestExecutor executor = new TestExecutor(taskExecutor);
        executor.blockWait = true;
        markStarted(executor);

        executor.beginDrain();
        Assert.assertTrue(executor.waitStarted.await(2, TimeUnit.SECONDS));
        Assert.assertFalse(executor.events.contains("destroy"));

        executor.allowIdle.countDown();
        waitForState(executor, "DRAINED");

        Assert.assertEquals(Arrays.asList("stop", "wait", "destroy"), executor.events);
        taskExecutor.shutdown();
    }

    @Test
    public void shouldBecomeFailedWhenDrainStepFails() throws Exception {
        TestExecutor executor = new TestExecutor(directExecutor());
        executor.stopFailure = new IllegalStateException("registry stop failed");
        markStarted(executor);

        executor.beginDrain();

        Assert.assertEquals("FAILED", executor.getDrainState());
        Assert.assertTrue(executor.getDrainFailure().contains("registry stop failed"));
        Assert.assertFalse(executor.events.contains("destroy"));
    }

    @Test
    public void destroyShouldPropagateDrainFailure() throws Exception {
        TestExecutor executor = new TestExecutor(directExecutor());
        executor.stopFailure = new IllegalStateException("registry stop failed");
        markStarted(executor);

        try {
            executor.destroy();
            Assert.fail("Spring destroy must observe terminal drain failure");
        } catch (IllegalStateException expected) {
            Assert.assertEquals("XXL-JOB terminal drain failed", expected.getMessage());
            Assert.assertTrue(expected.getCause().getMessage().contains("registry stop failed"));
        }

        Assert.assertEquals("FAILED", executor.getDrainState());
    }

    @Test
    public void repeatedDrainIsIdempotent() throws Exception {
        TestExecutor executor = new TestExecutor(directExecutor());
        markStarted(executor);

        executor.beginDrain();
        executor.beginDrain();

        Assert.assertEquals(1, count(executor.events, "stop"));
        Assert.assertEquals(1, count(executor.events, "destroy"));
    }

    @Test
    public void destroyWaitsForDrainCoordinator() throws Exception {
        ThreadPoolTaskExecutor taskExecutor = asyncExecutor();
        TestExecutor executor = new TestExecutor(taskExecutor);
        executor.blockWait = true;
        markStarted(executor);
        Thread destroyThread = new Thread(executor::destroy);

        destroyThread.start();
        Assert.assertTrue(executor.waitStarted.await(2, TimeUnit.SECONDS));
        Assert.assertTrue("destroy must wait for accepted jobs", destroyThread.isAlive());

        executor.allowIdle.countDown();
        destroyThread.join(2000);

        Assert.assertFalse(destroyThread.isAlive());
        Assert.assertEquals("DRAINED", executor.getDrainState());
        taskExecutor.shutdown();
    }

    @Test
    public void destroyShouldStopWaitingWhenDrainDoesNotComplete() throws Exception {
        ThreadPoolTaskExecutor taskExecutor = asyncExecutor();
        TestExecutor executor = new TestExecutor(taskExecutor);
        executor.blockWait = true;
        executor.shutdownDrainWaitMillis = 50L;
        markStarted(executor);

        try {
            executor.destroy();
            Assert.fail("Spring destroy must have a bounded drain wait");
        } catch (IllegalStateException expected) {
            Assert.assertTrue(expected.getMessage().contains("timed out during Spring shutdown"));
        }

        Assert.assertEquals("FAILED", executor.getDrainState());
        Assert.assertFalse(executor.events.contains("destroy"));
        taskExecutor.shutdown();
    }

    @Test
    public void shouldMatchXxlJob230PrivateDrainContract() throws Exception {
        Field embedServer = XxlJobExecutor.class.getDeclaredField("embedServer");
        Field jobThreadRepository = XxlJobExecutor.class.getDeclaredField("jobThreadRepository");
        Class<?> embedServerClass = Class.forName("com.xxl.job.core.server.EmbedServer");

        Assert.assertNotNull(embedServer);
        Assert.assertNotNull(jobThreadRepository);
        Assert.assertNotNull(embedServerClass.getDeclaredField("thread"));
        Assert.assertNotNull(embedServerClass.getMethod("stop"));
    }

    private AsyncTaskExecutor directExecutor() {
        return new TaskExecutorAdapter(Runnable::run);
    }

    private ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.initialize();
        return executor;
    }

    private void markStarted(ReleaseControlledXxlJobSpringExecutor executor) throws Exception {
        Field startedField = ReleaseControlledXxlJobSpringExecutor.class.getDeclaredField("executorStarted");
        startedField.setAccessible(true);
        ((AtomicBoolean) startedField.get(executor)).set(true);
    }

    private int count(List<String> events, String expected) {
        int count = 0;
        for (String event : events) {
            if (expected.equals(event)) {
                count++;
            }
        }
        return count;
    }

    private void waitForState(ReleaseControlledXxlJobSpringExecutor executor, String expected)
            throws InterruptedException {
        for (int attempt = 0; attempt < 200; attempt++) {
            if (expected.equals(executor.getDrainState())) {
                return;
            }
            Thread.sleep(10);
        }
        Assert.fail("Timed out waiting for XXL-JOB state " + expected + ", actual=" + executor.getDrainState());
    }

    private static class TestExecutor extends ReleaseControlledXxlJobSpringExecutor {
        private final List<String> events = new ArrayList<>();
        private final CountDownLatch waitStarted = new CountDownLatch(1);
        private final CountDownLatch allowIdle = new CountDownLatch(1);
        private volatile boolean blockWait;
        private volatile RuntimeException stopFailure;
        private volatile long shutdownDrainWaitMillis = 60000L;

        private TestExecutor(AsyncTaskExecutor lifecycleExecutor) {
            super(lifecycleExecutor);
        }

        @Override
        protected void stopAcceptingTriggersAndUnregister() {
            events.add("stop");
            if (stopFailure != null) {
                throw stopFailure;
            }
        }

        @Override
        protected void waitForAcceptedJobs() throws Exception {
            events.add("wait");
            waitStarted.countDown();
            if (blockWait && !allowIdle.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("test job did not become idle");
            }
        }

        @Override
        protected void destroyExecutorAfterDrain() {
            events.add("destroy");
        }

        @Override
        protected long getShutdownDrainWaitMillis() {
            return shutdownDrainWaitMillis;
        }
    }
}
