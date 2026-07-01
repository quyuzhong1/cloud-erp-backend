package com.cloud.erp.gateway.controller;

import com.cloud.erp.gateway.component.GatewayReadinessState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gateway 本地探针接口，仅供容器内或 K8s 探测使用。
 */
@RestController
@RequestMapping("/internal")
@ConditionalOnProperty(prefix = "erp.internal-health", name = "enabled", havingValue = "true")
public class GatewayInternalHealthController {

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final String STATUS_PRE_STOPPING = "PRE_STOPPING";
    private static final String REASON_READY = "READY";
    private static final String REASON_APPLICATION_NOT_READY = "APPLICATION_NOT_READY";
    private static final String REASON_PRE_STOPPING = "PRE_STOPPING";
    private static final String REASON_PRE_STOP_FORBIDDEN = "PRE_STOP_FORBIDDEN";
    private static final String REASON_RELEASE_STATE_FORBIDDEN = "RELEASE_STATE_FORBIDDEN";
    private static final String RELEASE_ACTIVE_COLOR = "release.active-color";
    private static final String RELEASE_ACTIVE_VERSION = "release.active-version";
    private static final String RELEASE_COLOR = "release.color";
    private static final String RELEASE_VERSION = "release.version";
    private static final String RELEASE_XXL_JOB_ENABLED = "release.xxl.job.enabled";

    @Resource
    private GatewayReadinessState readinessState;

    @Resource
    private Environment environment;

    @GetMapping("/live")
    public Mono<ResponseEntity<Map<String, Object>>> live() {
        return Mono.just(ResponseEntity.ok(body(STATUS_UP, null)));
    }

    @GetMapping("/ready")
    public Mono<ResponseEntity<Map<String, Object>>> ready() {
        if (!readinessState.isApplicationReady()) {
            return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(body(STATUS_DOWN, REASON_APPLICATION_NOT_READY)));
        }
        if (readinessState.isPreStopping()) {
            return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(body(STATUS_DOWN, REASON_PRE_STOPPING)));
        }
        return Mono.just(ResponseEntity.ok(body(STATUS_UP, REASON_READY)));
    }

    @GetMapping("/release-state")
    public Mono<ResponseEntity<Map<String, Object>>> releaseState(ServerHttpRequest request) {
        if (!isLoopbackRequest(request)) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(body(STATUS_DOWN, REASON_RELEASE_STATE_FORBIDDEN)));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("activeColor", environment.getProperty(RELEASE_ACTIVE_COLOR));
        body.put("activeVersion", environment.getProperty(RELEASE_ACTIVE_VERSION));
        body.put("releaseColor", environment.getProperty(RELEASE_COLOR));
        body.put("releaseVersion", environment.getProperty(RELEASE_VERSION));
        body.put("xxlJobEnabled", environment.getProperty(RELEASE_XXL_JOB_ENABLED, Boolean.class, true));
        body.put("currentReleaseActive", isCurrentReleaseActive());
        return Mono.just(ResponseEntity.ok(body));
    }

    @PostMapping("/pre-stop")
    public Mono<ResponseEntity<Map<String, Object>>> preStop(ServerHttpRequest request) {
        if (!isLoopbackRequest(request)) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(body(STATUS_DOWN, REASON_PRE_STOP_FORBIDDEN)));
        }
        readinessState.markPreStopping();
        return Mono.just(ResponseEntity.ok(body(STATUS_PRE_STOPPING, REASON_PRE_STOPPING)));
    }

    private boolean isCurrentReleaseActive() {
        String activeColor = environment.getProperty(RELEASE_ACTIVE_COLOR);
        String localColor = environment.getProperty(RELEASE_COLOR);
        if (hasText(activeColor) && hasText(localColor)) {
            return activeColor.equalsIgnoreCase(localColor);
        }

        String activeVersion = environment.getProperty(RELEASE_ACTIVE_VERSION);
        String localVersion = environment.getProperty(RELEASE_VERSION);
        if (hasText(activeVersion) && hasText(localVersion)) {
            return activeVersion.equalsIgnoreCase(localVersion);
        }

        return true;
    }

    private boolean isLoopbackRequest(ServerHttpRequest request) {
        String remoteAddress = getRemoteAddress(request);
        return "127.0.0.1".equals(remoteAddress)
                || "0:0:0:0:0:0:0:1".equals(remoteAddress)
                || "::1".equals(remoteAddress);
    }

    private String getRemoteAddress(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return null;
        }
        return remoteAddress.getAddress().getHostAddress();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Map<String, Object> body(String status, String reason) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        if (reason != null) {
            body.put("reason", reason);
        }
        return body;
    }
}
