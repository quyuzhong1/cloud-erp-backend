package com.common.business.health;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 检查当前进程是否已经出现在 Nacos 实例列表中，避免 K8s readiness 早于注册中心。
 */
@Slf4j
@Component
@ConditionalOnMissingBean(NacosSelfRegistrationChecker.class)
@ConditionalOnProperty(prefix = "erp.internal-health", name = "enabled", havingValue = "true")
public class NacosSelfRegistrationChecker {

    public static final String REASON_READY = "READY";
    public static final String REASON_NACOS_NOT_REGISTERED = "NACOS_NOT_REGISTERED";
    public static final String REASON_NACOS_NOT_HEALTHY = "NACOS_NOT_HEALTHY";

    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";

    private final NacosDiscoveryProperties discoveryProperties;
    private final Environment environment;
    private final ObjectProvider<Registration> registrationProvider;
    private final ThreadPoolExecutor queryExecutor;
    private final AtomicLong lastWarnTime = new AtomicLong(0L);

    @Value("${erp.internal-health.nacos-check-timeout-ms:1000}")
    private long nacosCheckTimeoutMs;

    @Value("${erp.internal-health.warn-interval-ms:30000}")
    private long warnIntervalMs;

    public NacosSelfRegistrationChecker(NacosDiscoveryProperties discoveryProperties, Environment environment,
                                        ObjectProvider<Registration> registrationProvider) {
        this.discoveryProperties = discoveryProperties;
        this.environment = environment;
        this.registrationProvider = registrationProvider;
        this.queryExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1), new DaemonThreadFactory());
    }

    public CheckResult checkSelfRegistration() {
        RegistrationInfo registrationInfo = resolveRegistrationInfo();
        if (!registrationInfo.hasRequiredInfo()) {
            return CheckResult.notReady(REASON_NACOS_NOT_REGISTERED, "local nacos registration info is incomplete");
        }
        return executeWithTimeout(() -> doCheckSelfRegistration(registrationInfo), CheckResult.notReady(
                REASON_NACOS_NOT_REGISTERED, "nacos query timeout or failed"));
    }

    public boolean deregisterSelf() {
        RegistrationInfo registrationInfo = resolveRegistrationInfo();
        if (!registrationInfo.hasRequiredInfo()) {
            log.warn("Skip deregistering instance from Nacos, local registration info is incomplete: {}",
                    registrationInfo);
            return false;
        }
        Boolean success = executeWithTimeout(() -> {
            NamingService namingService = discoveryProperties.namingServiceInstance();
            namingService.deregisterInstance(registrationInfo.getServiceName(), registrationInfo.getGroupName(),
                    registrationInfo.getIp(), registrationInfo.getPort());
            return Boolean.TRUE;
        }, Boolean.FALSE);
        if (!success) {
            log.warn("Failed to deregister instance from Nacos: {}", registrationInfo);
        }
        return success;
    }

    @PreDestroy
    public void destroy() {
        queryExecutor.shutdownNow();
    }

    private CheckResult doCheckSelfRegistration(RegistrationInfo registrationInfo) throws Exception {
        NamingService namingService = discoveryProperties.namingServiceInstance();
        List<Instance> instances = namingService.getAllInstances(registrationInfo.getServiceName(),
                registrationInfo.getGroupName());
        if (instances == null || instances.isEmpty()) {
            return CheckResult.notReady(REASON_NACOS_NOT_REGISTERED, "no instances found in nacos");
        }
        for (Instance instance : instances) {
            if (!isCurrentInstance(instance, registrationInfo)) {
                continue;
            }
            if (!instance.isHealthy()) {
                return CheckResult.notReady(REASON_NACOS_NOT_HEALTHY, "nacos instance healthy=false");
            }
            if (!instance.isEnabled()) {
                return CheckResult.notReady(REASON_NACOS_NOT_HEALTHY, "nacos instance enabled=false");
            }
            return CheckResult.ready();
        }
        return CheckResult.notReady(REASON_NACOS_NOT_REGISTERED, "current ip and port not found in nacos");
    }

    private boolean isCurrentInstance(Instance instance, RegistrationInfo registrationInfo) {
        return StringUtils.equals(instance.getIp(), registrationInfo.getIp())
                && instance.getPort() == registrationInfo.getPort();
    }

    private <T> T executeWithTimeout(Callable<T> callable, T fallback) {
        Future<T> future;
        try {
            future = queryExecutor.submit(callable);
        } catch (RejectedExecutionException ex) {
            warnThrottled("Nacos self registration check is busy", ex);
            return fallback;
        }
        try {
            return future.get(nacosCheckTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            warnThrottled("Nacos self registration check timed out", ex);
            return fallback;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            warnThrottled("Nacos self registration check was interrupted", ex);
            return fallback;
        } catch (ExecutionException ex) {
            warnThrottled("Nacos self registration check failed", ex);
            return fallback;
        }
    }

    private RegistrationInfo resolveRegistrationInfo() {
        Registration registration = registrationProvider.getIfAvailable();
        String serviceName = registration == null ? null : registration.getServiceId();
        serviceName = StringUtils.defaultIfBlank(serviceName, discoveryProperties.getService());
        serviceName = StringUtils.defaultIfBlank(serviceName,
                environment.getProperty("spring.cloud.nacos.discovery.service"));
        serviceName = StringUtils.defaultIfBlank(serviceName, environment.getProperty("spring.application.name"));

        String namespace = StringUtils.defaultIfBlank(discoveryProperties.getNamespace(),
                environment.getProperty("spring.cloud.nacos.discovery.namespace"));
        String groupName = StringUtils.defaultIfBlank(discoveryProperties.getGroup(), DEFAULT_GROUP);
        String ip = registration == null ? null : registration.getHost();
        ip = StringUtils.defaultIfBlank(ip, discoveryProperties.getIp());
        ip = StringUtils.defaultIfBlank(ip,
                environment.getProperty("spring.cloud.nacos.discovery.ip"));
        int port = registration == null ? -1 : registration.getPort();
        if (port <= 0) {
            port = discoveryProperties.getPort();
        }
        if (port <= 0) {
            port = resolvePortFromEnvironment();
        }
        return new RegistrationInfo(serviceName, namespace, groupName, ip, port);
    }

    private int resolvePortFromEnvironment() {
        String discoveryPort = environment.getProperty("spring.cloud.nacos.discovery.port");
        if (StringUtils.isNotBlank(discoveryPort)) {
            return parsePort(discoveryPort);
        }
        return parsePort(environment.getProperty("server.port"));
    }

    private int parsePort(String port) {
        if (StringUtils.isBlank(port)) {
            return -1;
        }
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException ex) {
            warnThrottled("Invalid nacos registration port: " + port, ex);
            return -1;
        }
    }

    private void warnThrottled(String message, Throwable throwable) {
        long now = System.currentTimeMillis();
        long last = lastWarnTime.get();
        if (now - last < warnIntervalMs) {
            return;
        }
        if (lastWarnTime.compareAndSet(last, now)) {
            log.warn(message, throwable);
        }
    }

    public static class CheckResult {

        private final boolean ready;
        private final String reason;
        private final String message;

        private CheckResult(boolean ready, String reason, String message) {
            this.ready = ready;
            this.reason = reason;
            this.message = message;
        }

        public static CheckResult ready() {
            return new CheckResult(true, REASON_READY, "ready");
        }

        public static CheckResult notReady(String reason, String message) {
            return new CheckResult(false, reason, message);
        }

        public boolean isReady() {
            return ready;
        }

        public String getReason() {
            return reason;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class RegistrationInfo {

        private final String serviceName;
        private final String namespace;
        private final String groupName;
        private final String ip;
        private final int port;

        private RegistrationInfo(String serviceName, String namespace, String groupName, String ip, int port) {
            this.serviceName = serviceName;
            this.namespace = namespace;
            this.groupName = groupName;
            this.ip = ip;
            this.port = port;
        }

        private boolean hasRequiredInfo() {
            return StringUtils.isNotBlank(serviceName) && StringUtils.isNotBlank(groupName)
                    && StringUtils.isNotBlank(ip) && port > 0;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return "RegistrationInfo{"
                    + "serviceName='" + serviceName + '\''
                    + ", namespace='" + namespace + '\''
                    + ", groupName='" + groupName + '\''
                    + ", ip='" + ip + '\''
                    + ", port=" + port
                    + '}';
        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "erp-nacos-self-registration-checker");
            thread.setDaemon(true);
            return thread;
        }
    }
}
