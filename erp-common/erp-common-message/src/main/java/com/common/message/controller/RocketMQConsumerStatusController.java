package com.common.message.controller;

import com.common.message.config.RocketMQConsumerActivationManager;
import com.common.message.config.RocketMQConsumerDrainManager;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loopback-only RocketMQ consumer lifecycle endpoints used by the release pipeline.
 */
@RestController
@RequestMapping("/internal/rocketmq")
@ConditionalOnWebApplication
public class RocketMQConsumerStatusController {

    private final ApplicationContext applicationContext;
    private final RocketMQConsumerActivationManager activationManager;
    private final RocketMQConsumerDrainManager drainManager;

    public RocketMQConsumerStatusController(ApplicationContext applicationContext,
                                            RocketMQConsumerActivationManager activationManager,
                                            RocketMQConsumerDrainManager drainManager) {
        this.applicationContext = applicationContext;
        this.activationManager = activationManager;
        this.drainManager = drainManager;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            Map<String, Object> forbidden = new LinkedHashMap<>();
            forbidden.put("status", "FORBIDDEN");
            forbidden.put("message", "rocketmq status only accepts loopback requests");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(forbidden);
        }

        boolean enabled = activationManager.isEffectivelyEnabled();
        Map<String, DefaultRocketMQListenerContainer> containers = applicationContext.getBeansOfType(
                DefaultRocketMQListenerContainer.class, false, false);

        int running = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        for (Map.Entry<String, DefaultRocketMQListenerContainer> entry : containers.entrySet()) {
            DefaultRocketMQListenerContainer container = entry.getValue();
            if (container.isRunning()) {
                running++;
            }
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("beanName", entry.getKey());
            detail.put("consumerGroup", container.getConsumerGroup());
            detail.put("topic", container.getTopic());
            detail.put("running", container.isRunning());
            details.add(detail);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", resolveStatus(enabled, containers.size(), running));
        body.put("enabled", enabled);
        body.put("startupEnabled", activationManager.isStartupEnabled());
        body.put("activationState", activationManager.getActivationState());
        body.put("mqActiveColor", activationManager.getMqActiveColor());
        body.put("localColor", activationManager.getLocalColor());
        body.put("colorEligible", activationManager.isColorEligible());
        body.put("drainState", drainManager.getDrainState());
        body.put("drainTotalContainers", drainManager.getTotalContainers());
        body.put("drainedContainers", drainManager.getDrainedContainers());
        body.put("drainFailure", drainManager.getFailureMessage());
        body.put("totalContainers", containers.size());
        body.put("runningContainers", running);
        body.put("containers", details);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/drain")
    public ResponseEntity<Map<String, Object>> drain(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            Map<String, Object> forbidden = new LinkedHashMap<>();
            forbidden.put("status", "FORBIDDEN");
            forbidden.put("message", "rocketmq drain only accepts loopback requests");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(forbidden);
        }
        try {
            drainManager.beginDrain();
            return status(request);
        } catch (RuntimeException ex) {
            Map<String, Object> failed = new LinkedHashMap<>();
            failed.put("status", "DRAIN_FAILED");
            failed.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(failed);
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<Map<String, Object>> activate(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            Map<String, Object> forbidden = new LinkedHashMap<>();
            forbidden.put("status", "FORBIDDEN");
            forbidden.put("message", "rocketmq activation only accepts loopback requests");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(forbidden);
        }
        if (!"RUNNING".equals(drainManager.getDrainState())) {
            Map<String, Object> conflict = new LinkedHashMap<>();
            conflict.put("status", "TERMINAL_DRAIN_STARTED");
            conflict.put("message", "rocketmq consumers cannot be activated after terminal drain starts");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflict);
        }
        try {
            activationManager.activate();
            return status(request);
        } catch (RocketMQConsumerActivationManager.ActivationNotEligibleException ex) {
            Map<String, Object> conflict = new LinkedHashMap<>();
            conflict.put("status", "NOT_ELIGIBLE");
            conflict.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflict);
        } catch (RuntimeException ex) {
            Map<String, Object> failed = new LinkedHashMap<>();
            failed.put("status", "ACTIVATION_FAILED");
            failed.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(failed);
        }
    }

    private String resolveStatus(boolean enabled, int total, int running) {
        if (!enabled) {
            return total == 0 ? "DISABLED" : "INVALID_DISABLED_STATE";
        }
        if (total == 0) {
            return "EMPTY";
        }
        return total == running ? "RUNNING" : "DEGRADED";
    }

    private boolean isLoopbackRequest(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddr)
                || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                || "::1".equals(remoteAddr);
    }
}
