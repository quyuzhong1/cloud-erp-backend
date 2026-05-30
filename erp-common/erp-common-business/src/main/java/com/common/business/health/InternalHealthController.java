package com.common.business.health;

import com.common.core.controller.BaseController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * K8s 内部探针接口：liveness 只看进程，readiness 才检查 Nacos 注册状态。
 */
@RestController
@RequestMapping("/internal")
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "erp.internal-health", name = "enabled", havingValue = "true")
public class InternalHealthController extends BaseController {

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final String STATUS_PRE_STOPPING = "PRE_STOPPING";
    private static final String REASON_APPLICATION_NOT_READY = "APPLICATION_NOT_READY";
    private static final String REASON_PRE_STOPPING = "PRE_STOPPING";
    private static final String REASON_PRE_STOP_FORBIDDEN = "PRE_STOP_FORBIDDEN";

    @Resource
    private ReadinessState readinessState;

    @Resource
    private NacosSelfRegistrationChecker nacosSelfRegistrationChecker;

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> live() {
        return ResponseEntity.ok(body(STATUS_UP, null, null));
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        if (!readinessState.isApplicationReady()) {
            return response(HttpStatus.SERVICE_UNAVAILABLE, STATUS_DOWN, REASON_APPLICATION_NOT_READY,
                    "application ready event has not been published");
        }
        if (readinessState.isPreStopping()) {
            return response(HttpStatus.SERVICE_UNAVAILABLE, STATUS_DOWN, REASON_PRE_STOPPING,
                    "application is pre-stopping");
        }

        NacosSelfRegistrationChecker.CheckResult checkResult = nacosSelfRegistrationChecker.checkSelfRegistration();
        if (checkResult.isReady()) {
            return response(HttpStatus.OK, STATUS_UP, checkResult.getReason(), checkResult.getMessage());
        }
        return response(HttpStatus.SERVICE_UNAVAILABLE, STATUS_DOWN, checkResult.getReason(),
                checkResult.getMessage());
    }

    @PostMapping("/pre-stop")
    public ResponseEntity<Map<String, Object>> preStop(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            return response(HttpStatus.FORBIDDEN, STATUS_DOWN, REASON_PRE_STOP_FORBIDDEN,
                    "pre-stop only accepts loopback requests");
        }
        readinessState.markPreStopping();
        boolean disabled = nacosSelfRegistrationChecker.setSelfEnabled(false);
        boolean deregistered = nacosSelfRegistrationChecker.deregisterSelf();
        Map<String, Object> body = body(STATUS_PRE_STOPPING, REASON_PRE_STOPPING, "application is pre-stopping");
        body.put("nacosDisabled", disabled);
        body.put("nacosDeregistered", deregistered);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String healthStatus, String reason,
                                                         String message) {
        return ResponseEntity.status(status).body(body(healthStatus, reason, message));
    }

    private boolean isLoopbackRequest(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr);
    }

    private Map<String, Object> body(String status, String reason, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        if (reason != null) {
            body.put("reason", reason);
        }
        if (message != null) {
            body.put("message", message);
        }
        return body;
    }
}
