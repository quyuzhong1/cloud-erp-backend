package com.common.message.config;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.thread.JobThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 蓝绿发布时通过 Nacos 动态控制 XXL-JOB 执行器注册，避免新旧版本同时执行定时任务。
 * 发布流程按「新色首次启用、旧色停止后退出」使用；回滚应重建 Pod，不依赖同一 JVM 内反复停启。
 */
public class ReleaseControlledXxlJobSpringExecutor extends XxlJobSpringExecutor
        implements EnvironmentAware, ApplicationListener<ApplicationEvent> {

    private static final Logger log = LoggerFactory.getLogger(ReleaseControlledXxlJobSpringExecutor.class);
    private static final String XXL_JOB_ENABLED_KEY = "release.xxl.job.enabled";
    private static final String ACTIVE_COLOR_KEY = "release.active-color";
    private static final String LOCAL_COLOR_KEY = "release.color";
    private static final String ACTIVE_VERSION_KEY = "release.active-version";
    private static final String LOCAL_VERSION_KEY = "release.version";
    private static final String SHUTDOWN_DRAIN_WAIT_MILLIS_KEY = "erp.xxl-job.shutdown-drain-wait-ms";
    private static final long DEFAULT_SHUTDOWN_DRAIN_WAIT_MILLIS = 60000L;
    // common-message 不直接绑定 spring-cloud-context；用类名识别 Nacos 刷新事件，避免公共消息模块新增传递依赖。
    private static final String ENVIRONMENT_CHANGE_EVENT = "org.springframework.cloud.context.environment.EnvironmentChangeEvent";

    private final AtomicBoolean executorStarted = new AtomicBoolean(false);
    private final AtomicBoolean executorDestroyed = new AtomicBoolean(false);
    private final AsyncTaskExecutor lifecycleExecutor;
    private volatile DrainState drainState = DrainState.DISABLED;
    private volatile String drainFailure;
    private volatile boolean registryRemovalRequested;
    private volatile Future<?> drainFuture;
    private Environment environment;

    /**
     * Creates an executor whose terminal drain runs on a Spring-managed lifecycle executor.
     *
     * @param lifecycleExecutor bounded lifecycle executor
     */
    public ReleaseControlledXxlJobSpringExecutor(AsyncTaskExecutor lifecycleExecutor) {
        this.lifecycleExecutor = lifecycleExecutor;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (isXxlJobEnabled()) {
            startExecutor();
            return;
        }
        log.warn(">>>>>>>>>>> xxl-job executor disabled by {}=false.", XXL_JOB_ENABLED_KEY);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (ENVIRONMENT_CHANGE_EVENT.equals(event.getClass().getName())) {
            refreshExecutorState();
        }
    }

    @Override
    public void destroy() {
        /*
         * Propagating a drain failure makes Spring shutdown diagnostics accurate, but it cannot cancel
         * Kubernetes Pod termination by itself. The release pipeline is the actual safety gate: it calls
         * /internal/xxljob/drain, blocks on FAILED, and applies its explicit bounded-timeout policy while
         * polling for DRAINED before scaling or deleting the old Pod.
         */
        beginDrain();
        awaitDrainCompletion();
    }

    private boolean isXxlJobEnabled() {
        if (environment == null) {
            return true;
        }
        return environment.getProperty(XXL_JOB_ENABLED_KEY, Boolean.class, true)
                && isCurrentReleaseActive();
    }

    private boolean isCurrentReleaseActive() {
        String activeColor = environment.getProperty(ACTIVE_COLOR_KEY);
        String localColor = environment.getProperty(LOCAL_COLOR_KEY);
        if (hasText(activeColor) && (!hasText(localColor) || !activeColor.equalsIgnoreCase(localColor))) {
            return false;
        }

        String activeVersion = environment.getProperty(ACTIVE_VERSION_KEY);
        String localVersion = environment.getProperty(LOCAL_VERSION_KEY);
        if (hasText(activeVersion) && (!hasText(localVersion) || !activeVersion.equalsIgnoreCase(localVersion))) {
            return false;
        }

        return true;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private synchronized void refreshExecutorState() {
        if (isXxlJobEnabled()) {
            startExecutor();
        } else {
            beginDrain();
        }
    }

    private void startExecutor() {
        if (executorDestroyed.get()) {
            log.warn(">>>>>>>>>>> xxl-job executor has been destroyed, skip restart in same JVM. Please rebuild pod.");
            return;
        }
        if (!executorStarted.compareAndSet(false, true)) {
            return;
        }
        try {
            super.afterSingletonsInstantiated();
            drainState = DrainState.RUNNING;
            log.info(">>>>>>>>>>> xxl-job executor started by {}=true.", XXL_JOB_ENABLED_KEY);
        } catch (RuntimeException e) {
            executorStarted.set(false);
            throw e;
        }
    }

    /**
     * Terminal drain for an old blue-green Pod. XXL-JOB 2.3.0 destroy() interrupts active JobThreads,
     * so stop the RPC/registry entry first and destroy only after all accepted jobs have completed.
     */
    public synchronized void beginDrain() {
        if (drainState == DrainState.DRAINING || drainState == DrainState.DRAINED) {
            return;
        }
        if (drainState == DrainState.FAILED) {
            throw new IllegalStateException("XXL-JOB terminal drain has failed: " + drainFailure);
        }
        if (!executorStarted.compareAndSet(true, false)) {
            return;
        }

        // Drain is terminal in XXL-JOB 2.3.0; rollback must rebuild this Pod instead of restarting the singleton threads.
        executorDestroyed.set(true);
        drainState = DrainState.DRAINING;
        drainFailure = null;
        try {
            drainFuture = lifecycleExecutor.submit(this::drainExecutor);
        } catch (RuntimeException ex) {
            drainFailure = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            drainState = DrainState.FAILED;
            throw ex;
        }
    }

    /**
     * Runs the terminal drain and preserves failure in both the lifecycle state and submitted Future.
     * The external release pipeline, rather than this Spring destroy callback, controls Pod removal.
     */
    private void drainExecutor() {
        try {
            stopAcceptingTriggersAndUnregister();
            waitForAcceptedJobs();
            // At this point the official destroy path only interrupts idle JobThreads.
            destroyExecutorAfterDrain();
            drainState = DrainState.DRAINED;
            log.info(">>>>>>>>>>> xxl-job executor unregistered and drained without interrupting an active job.");
        } catch (Throwable ex) {
            drainFailure = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            drainState = DrainState.FAILED;
            log.error(">>>>>>>>>>> xxl-job terminal drain failed; the external release gate must observe FAILED.",
                    ex);
            /*
             * Do not call super.destroy() from a failure/finally path. XXL-JOB 2.3.0 interrupts active
             * JobThreads during destroy; a failed drain must remain visible to the release gate, which
             * applies the bounded timeout and Pod replacement policy.
             */
            if (ex instanceof Error) {
                throw (Error) ex;
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException("XXL-JOB terminal drain failed", ex);
        }
    }

    /**
     * Stops the embedded trigger server and waits for its registry thread to exit.
     *
     * @throws Exception when the XXL-JOB 2.3.0 private contract cannot be accessed or stopped
     */
    protected void stopAcceptingTriggersAndUnregister() throws Exception {
        Field embedServerField = XxlJobExecutor.class.getDeclaredField("embedServer");
        embedServerField.setAccessible(true);
        Object embedServer = embedServerField.get(this);
        if (embedServer == null) {
            throw new IllegalStateException("XXL-JOB embed server is unavailable");
        }

        Method stopMethod = embedServer.getClass().getMethod("stop");
        try {
            stopMethod.invoke(embedServer);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw ex;
        }
        registryRemovalRequested = true;

        Field serverThreadField = embedServer.getClass().getDeclaredField("thread");
        serverThreadField.setAccessible(true);
        Thread serverThread = (Thread) serverThreadField.get(embedServer);
        if (serverThread != null) {
            serverThread.join(10000L);
            if (serverThread.isAlive()) {
                throw new IllegalStateException("XXL-JOB embed server did not stop within 10 seconds");
            }
        }
    }

    /**
     * Waits until accepted jobs remain idle for five consecutive checks.
     *
     * @throws Exception when job thread state cannot be inspected
     */
    protected void waitForAcceptedJobs() throws Exception {
        int consecutiveIdleChecks = 0;
        /*
         * Intentionally no business-task deadline here: forcing the official destroy path on timeout
         * interrupts active JobThreads. Jenkins owns the five-minute release deadline; destroy() itself
         * has a bounded local wait so Spring shutdown cannot block forever.
         */
        while (consecutiveIdleChecks < 5) {
            if (getBusyJobThreadCount() == 0) {
                consecutiveIdleChecks++;
            } else {
                consecutiveIdleChecks = 0;
            }
            if (consecutiveIdleChecks < 5) {
                Thread.sleep(1000L);
            }
        }
    }

    /**
     * Invokes the official executor destroy path only after accepted jobs are idle.
     */
    protected void destroyExecutorAfterDrain() {
        super.destroy();
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, JobThread> getJobThreadRepository() throws Exception {
        Field repositoryField = XxlJobExecutor.class.getDeclaredField("jobThreadRepository");
        repositoryField.setAccessible(true);
        return (Map<Integer, JobThread>) repositoryField.get(null);
    }

    public int getBusyJobThreadCount() {
        try {
            int busy = 0;
            for (JobThread jobThread : getJobThreadRepository().values()) {
                if (jobThread != null && jobThread.isRunningOrHasQueue()) {
                    busy++;
                }
            }
            return busy;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot inspect XXL-JOB 2.3.0 JobThread repository", ex);
        }
    }

    public String getDrainState() {
        return drainState.name();
    }

    /**
     * Returns the typed drain state for internal lifecycle decisions.
     *
     * @return current terminal drain state
     */
    public DrainState getDrainStateValue() {
        return drainState;
    }

    public String getDrainFailure() {
        return drainFailure;
    }

    public boolean isAcceptingTriggers() {
        return drainState == DrainState.RUNNING;
    }

    public boolean isRegistryRemovalRequested() {
        return registryRemovalRequested;
    }

    /**
     * Waits for the local drain task and propagates any failure to Spring shutdown diagnostics.
     */
    private void awaitDrainCompletion() {
        Future<?> future = drainFuture;
        if (future == null) {
            return;
        }
        long shutdownWaitMillis = getShutdownDrainWaitMillis();
        try {
            future.get(shutdownWaitMillis, TimeUnit.MILLISECONDS);
            if (drainState != DrainState.DRAINED) {
                throw new IllegalStateException("XXL-JOB terminal drain did not complete: " + drainFailure);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for XXL-JOB terminal drain", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("XXL-JOB terminal drain failed", ex.getCause());
        } catch (CancellationException ex) {
            markDrainFailed("XXL-JOB terminal drain was cancelled");
            throw new IllegalStateException("XXL-JOB terminal drain was cancelled", ex);
        } catch (TimeoutException ex) {
            if (!future.cancel(true) && drainState == DrainState.DRAINED) {
                return;
            }
            markDrainFailed("XXL-JOB terminal drain timed out during Spring shutdown");
            throw new IllegalStateException(
                    "XXL-JOB terminal drain timed out during Spring shutdown after "
                            + shutdownWaitMillis + "ms",
                    ex);
        }
    }

    /**
     * Resolves the maximum time Spring shutdown waits for an already-running drain.
     *
     * @return positive shutdown wait in milliseconds
     */
    protected long getShutdownDrainWaitMillis() {
        if (environment == null) {
            return DEFAULT_SHUTDOWN_DRAIN_WAIT_MILLIS;
        }
        String configured = environment.getProperty(SHUTDOWN_DRAIN_WAIT_MILLIS_KEY);
        if (!hasText(configured)) {
            return DEFAULT_SHUTDOWN_DRAIN_WAIT_MILLIS;
        }
        try {
            long timeout = Long.parseLong(configured.trim());
            if (timeout > 0L) {
                return timeout;
            }
        } catch (NumberFormatException ex) {
            log.warn("Invalid {} value '{}'; using default {}ms",
                    SHUTDOWN_DRAIN_WAIT_MILLIS_KEY, configured, DEFAULT_SHUTDOWN_DRAIN_WAIT_MILLIS);
            return DEFAULT_SHUTDOWN_DRAIN_WAIT_MILLIS;
        }
        log.warn("Non-positive {} value '{}'; using default {}ms",
                SHUTDOWN_DRAIN_WAIT_MILLIS_KEY, configured, DEFAULT_SHUTDOWN_DRAIN_WAIT_MILLIS);
        return DEFAULT_SHUTDOWN_DRAIN_WAIT_MILLIS;
    }

    /**
     * Records a controlled terminal drain failure without exposing it through public HTTP responses.
     *
     * @param message internal failure detail
     */
    private void markDrainFailed(String message) {
        drainFailure = message;
        drainState = DrainState.FAILED;
        log.error(">>>>>>>>>>> {}", message);
    }

    /** Terminal XXL-JOB drain states exposed to the release status endpoint by name. */
    public enum DrainState {
        DISABLED,
        RUNNING,
        DRAINING,
        DRAINED,
        FAILED
    }
}
