package com.common.message.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.message.config.RocketMQConsumerActivationManager;
import com.common.message.config.RocketMQConsumerDrainManager;
import com.common.message.config.RocketMQConsumerLifecycleCoordinator;
import com.common.message.controller.vo.RocketMQContainerStatusVO;
import com.common.message.controller.vo.RocketMQLifecycleStatusVO;
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
import java.util.Map;

/**
 * Loopback-only RocketMQ consumer lifecycle endpoints used by the release pipeline.
 */
@RestController
@RequestMapping("/internal/rocketmq")
@ConditionalOnWebApplication
public class RocketMQConsumerStatusController extends BaseController {

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
    public ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> status(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            return forbidden("rocketmq status only accepts loopback requests");
        }

        return lifecycleResponse(HttpStatus.OK, 200, "请求成功！", currentStatus());
    }

    /**
     * Starts the irreversible RocketMQ terminal drain.
     *
     * @param request current HTTP request
     * @return typed lifecycle state
     */
    @PostMapping("/drain")
    public ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> drain(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            return forbidden("rocketmq drain only accepts loopback requests");
        }
        try {
            drainManager.beginDrain();
            return lifecycleResponse(HttpStatus.OK, 200, "请求成功！", currentStatus());
        } catch (RuntimeException ex) {
            RocketMQLifecycleStatusVO failed = currentStatus();
            failed.setStatus("DRAIN_FAILED");
            failed.setMessage(ex.getMessage());
            return lifecycleResponse(HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(), ex.getMessage(), failed);
        }
    }

    /**
     * Activates deferred RocketMQ listeners when this Pod owns the MQ active color.
     *
     * @param request current HTTP request
     * @return typed lifecycle state
     */
    @PostMapping("/activate")
    public ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> activate(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            return forbidden("rocketmq activation only accepts loopback requests");
        }
        try {
            activationManager.activate();
            return lifecycleResponse(HttpStatus.OK, 200, "请求成功！", currentStatus());
        } catch (RocketMQConsumerLifecycleCoordinator.TerminalDrainStartedException ex) {
            RocketMQLifecycleStatusVO conflict = currentStatus();
            conflict.setStatus("TERMINAL_DRAIN_STARTED");
            conflict.setMessage(ex.getMessage());
            return lifecycleResponse(HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(), ex.getMessage(), conflict);
        } catch (RocketMQConsumerActivationManager.ActivationNotEligibleException ex) {
            RocketMQLifecycleStatusVO conflict = currentStatus();
            conflict.setStatus("NOT_ELIGIBLE");
            conflict.setMessage(ex.getMessage());
            return lifecycleResponse(HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(), ex.getMessage(), conflict);
        } catch (RuntimeException ex) {
            RocketMQLifecycleStatusVO failed = currentStatus();
            failed.setStatus("ACTIVATION_FAILED");
            failed.setMessage(ex.getMessage());
            return lifecycleResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    ex.getMessage(),
                    failed);
        }
    }

    /**
     * Builds the current lifecycle state without changing listener state.
     *
     * @return current RocketMQ lifecycle status
     */
    private RocketMQLifecycleStatusVO currentStatus() {
        boolean enabled = activationManager.isEffectivelyEnabled();
        Map<String, DefaultRocketMQListenerContainer> containers = applicationContext.getBeansOfType(
                DefaultRocketMQListenerContainer.class, false, false);

        int running = 0;
        RocketMQLifecycleStatusVO status = new RocketMQLifecycleStatusVO();
        for (Map.Entry<String, DefaultRocketMQListenerContainer> entry : containers.entrySet()) {
            DefaultRocketMQListenerContainer container = entry.getValue();
            if (container.isRunning()) {
                running++;
            }
            RocketMQContainerStatusVO detail = new RocketMQContainerStatusVO();
            detail.setBeanName(entry.getKey());
            detail.setConsumerGroup(container.getConsumerGroup());
            detail.setTopic(container.getTopic());
            detail.setRunning(container.isRunning());
            status.getContainers().add(detail);
        }

        status.setStatus(resolveStatus(enabled, containers.size(), running));
        status.setEnabled(enabled);
        status.setStartupEnabled(activationManager.isStartupEnabled());
        status.setActivationState(activationManager.getActivationState());
        status.setMqActiveColor(activationManager.getMqActiveColor());
        status.setLocalColor(activationManager.getLocalColor());
        status.setColorEligible(activationManager.isColorEligible());
        status.setDrainState(drainManager.getDrainState());
        status.setDrainTotalContainers(drainManager.getTotalContainers());
        status.setDrainedContainers(drainManager.getDrainedContainers());
        status.setDrainFailure(drainManager.getFailureMessage());
        status.setTotalContainers(containers.size());
        status.setRunningContainers(running);
        return status;
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

    /**
     * Builds a loopback rejection while preserving the lifecycle response schema.
     *
     * @param message rejection detail
     * @return HTTP 403 response
     */
    private ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> forbidden(String message) {
        RocketMQLifecycleStatusVO forbidden = new RocketMQLifecycleStatusVO();
        forbidden.setStatus("FORBIDDEN");
        forbidden.setMessage(message);
        return lifecycleResponse(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.value(), message, forbidden);
    }

    /**
     * Wraps lifecycle data in the project response protocol while retaining transport status codes.
     *
     * @param httpStatus transport status
     * @param code response code
     * @param responseMessage response message
     * @param data lifecycle state
     * @return wrapped response entity
     */
    private ResponseEntity<ApiResult<RocketMQLifecycleStatusVO>> lifecycleResponse(
            HttpStatus httpStatus,
            int code,
            String responseMessage,
            RocketMQLifecycleStatusVO data) {
        return ResponseEntity.status(httpStatus).body(message(code, responseMessage, data));
    }

    private boolean isLoopbackRequest(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddr)
                || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                || "::1".equals(remoteAddr);
    }
}
