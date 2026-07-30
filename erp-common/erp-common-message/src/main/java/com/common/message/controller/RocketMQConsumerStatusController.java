package com.common.message.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.message.config.RocketMQConsumerActivationManager;
import com.common.message.config.RocketMQConsumerDrainManager;
import com.common.message.config.RocketMQConsumerLifecycleCoordinator;
import com.common.message.controller.vo.RocketMQContainerStatusVO;
import com.common.message.controller.vo.RocketMQLifecycleStatusVO;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 *
 * <p>This intentionally follows the existing InternalHealthController trust boundary. Jenkins
 * enters the single application container with kubectl exec and calls 127.0.0.1; no lifecycle
 * request is accepted from the Pod or cluster network. A static token in the same container would
 * add SSRF defense in depth but would not protect against a compromised same-container process,
 * while coupling every environment to another distributed secret. Reassess this accepted boundary
 * if a sidecar, local proxy, or SSRF-capable feature is introduced.</p>
 */
@RestController
@RequestMapping("/internal/rocketmq")
@ConditionalOnWebApplication
public class RocketMQConsumerStatusController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerStatusController.class);
    private static final String DRAIN_FAILED_MESSAGE = "RocketMQ terminal drain failed";
    private static final String ACTIVATION_FAILED_MESSAGE = "RocketMQ activation failed";

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
     * Starts the irreversible RocketMQ terminal drain. HTTP 202 means accepted, not completed.
     * The release pipeline polls for DRAINED, fails on FAILED, and owns any bounded-timeout policy.
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
            RocketMQLifecycleStatusVO status = currentStatus();
            RocketMQConsumerDrainManager.DrainState drainState = drainManager.getDrainStateValue();
            if (drainState == RocketMQConsumerDrainManager.DrainState.DRAINING) {
                return lifecycleResponse(
                        HttpStatus.ACCEPTED,
                        HttpStatus.ACCEPTED.value(),
                        "RocketMQ terminal drain accepted",
                        status);
            }
            if (drainState == RocketMQConsumerDrainManager.DrainState.FAILED) {
                status.setMessage(DRAIN_FAILED_MESSAGE);
                return lifecycleResponse(
                        HttpStatus.CONFLICT,
                        HttpStatus.CONFLICT.value(),
                        DRAIN_FAILED_MESSAGE,
                        status);
            }
            return lifecycleResponse(HttpStatus.OK, HttpStatus.OK.value(), "请求成功！", status);
        } catch (RuntimeException ex) {
            LOGGER.error("RocketMQ terminal drain request failed", ex);
            return lifecycleResponse(
                    HttpStatus.CONFLICT,
                    HttpStatus.CONFLICT.value(),
                    DRAIN_FAILED_MESSAGE,
                    drainFailedStatus());
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
        } catch (RocketMQConsumerActivationManager.InvalidConsumerSwitchException ex) {
            LOGGER.error("RocketMQ activation rejected because the consumer switch is invalid", ex);
            RocketMQLifecycleStatusVO conflict = currentStatus();
            conflict.setStatus("INVALID_CONFIGURATION");
            conflict.setMessage("RocketMQ consumer configuration is invalid");
            return lifecycleResponse(
                    HttpStatus.CONFLICT,
                    HttpStatus.CONFLICT.value(),
                    "RocketMQ consumer configuration is invalid",
                    conflict);
        } catch (RocketMQConsumerLifecycleCoordinator.TerminalDrainStartedException ex) {
            LOGGER.warn("RocketMQ activation rejected because terminal drain has started", ex);
            RocketMQLifecycleStatusVO conflict = currentStatus();
            conflict.setStatus("TERMINAL_DRAIN_STARTED");
            conflict.setMessage("RocketMQ terminal drain has already started");
            return lifecycleResponse(
                    HttpStatus.CONFLICT,
                    HttpStatus.CONFLICT.value(),
                    "RocketMQ terminal drain has already started",
                    conflict);
        } catch (RocketMQConsumerActivationManager.ActivationNotEligibleException ex) {
            LOGGER.warn("RocketMQ activation rejected because this Pod is not eligible", ex);
            RocketMQLifecycleStatusVO conflict = currentStatus();
            conflict.setStatus("NOT_ELIGIBLE");
            conflict.setMessage("RocketMQ activation is not eligible for this Pod");
            return lifecycleResponse(
                    HttpStatus.CONFLICT,
                    HttpStatus.CONFLICT.value(),
                    "RocketMQ activation is not eligible for this Pod",
                    conflict);
        } catch (RuntimeException ex) {
            LOGGER.error("RocketMQ activation request failed", ex);
            RocketMQLifecycleStatusVO failed = currentStatus();
            failed.setStatus("ACTIVATION_FAILED");
            failed.setMessage(ACTIVATION_FAILED_MESSAGE);
            return lifecycleResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    ACTIVATION_FAILED_MESSAGE,
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

        RocketMQConsumerActivationManager.ActivationState activationState =
                activationManager.getActivationStateValue();
        RocketMQConsumerDrainManager.DrainState drainState = drainManager.getDrainStateValue();
        status.setStatus(resolveStatus(enabled, containers.size(), running, activationState, drainState));
        status.setEnabled(enabled);
        status.setStartupEnabled(activationManager.isStartupEnabled());
        status.setActivationState(activationState.name());
        status.setActivationFailure(hasText(activationManager.getFailureMessage())
                ? ACTIVATION_FAILED_MESSAGE : null);
        status.setMqActiveColor(activationManager.getMqActiveColor());
        status.setLocalColor(activationManager.getLocalColor());
        status.setColorEligible(activationManager.isColorEligible());
        status.setDrainState(drainState.name());
        status.setDrainTotalContainers(drainManager.getTotalContainers());
        status.setDrainedContainers(drainManager.getDrainedContainers());
        status.setDrainFailure(hasText(drainManager.getFailureMessage())
                ? DRAIN_FAILED_MESSAGE : null);
        status.setTotalContainers(containers.size());
        status.setRunningContainers(running);
        return status;
    }

    /**
     * Checks whether an internal failure detail is present without exposing it.
     *
     * @param value internal failure detail
     * @return true when a failure was recorded
     */
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Builds a minimal failure response without querying listener Beans again from an exception handler.
     *
     * @return stable terminal drain failure status
     */
    private RocketMQLifecycleStatusVO drainFailedStatus() {
        RocketMQLifecycleStatusVO failed = new RocketMQLifecycleStatusVO();
        failed.setStatus("DRAIN_FAILED");
        failed.setMessage(DRAIN_FAILED_MESSAGE);
        failed.setDrainState(RocketMQConsumerDrainManager.DrainState.FAILED.name());
        failed.setDrainFailure(DRAIN_FAILED_MESSAGE);
        failed.setDrainTotalContainers(drainManager.getTotalContainers());
        failed.setDrainedContainers(drainManager.getDrainedContainers());
        return failed;
    }

    /**
     * Resolves the primary release state, giving an irreversible drain precedence over listener counts.
     *
     * @param enabled effective consumer state
     * @param total listener container count
     * @param running running listener container count
     * @param activationState typed listener activation state
     * @param drainState typed terminal drain state
     * @return primary lifecycle status
     */
    private String resolveStatus(boolean enabled,
                                 int total,
                                 int running,
                                 RocketMQConsumerActivationManager.ActivationState activationState,
                                 RocketMQConsumerDrainManager.DrainState drainState) {
        if (drainState == RocketMQConsumerDrainManager.DrainState.DRAINING
                || drainState == RocketMQConsumerDrainManager.DrainState.DRAINED
                || drainState == RocketMQConsumerDrainManager.DrainState.FAILED) {
            return drainState.name();
        }
        if (activationState == RocketMQConsumerActivationManager.ActivationState.FAILED) {
            return "ACTIVATION_FAILED";
        }
        if (activationState == RocketMQConsumerActivationManager.ActivationState.INVALID) {
            return "INVALID_CONFIGURATION";
        }
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

    /**
     * Enforces the current deployment boundary: Jenkins enters the application container with
     * kubectl exec and calls 127.0.0.1. Forwarded client headers are intentionally ignored, so an
     * external caller cannot claim loopback through X-Forwarded-For. This is also enforced before
     * the controller by InternalHealthAccessFilter in services using erp-common-business. This
     * boundary must be revisited if a same-Pod sidecar or local reverse proxy is introduced.
     *
     * @param request current HTTP request
     * @return true only for a connection originating from the Pod network namespace loopback
     */
    private boolean isLoopbackRequest(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddr)
                || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                || "::1".equals(remoteAddr);
    }
}
