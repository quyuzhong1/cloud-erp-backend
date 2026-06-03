package com.cloud.erp.gateway.controller;

import com.cloud.erp.gateway.component.GatewayReadinessState;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gateway 本地探针接口，仅供容器内或 K8s 探测使用。
 */
@RestController
@RequestMapping("/internal")
public class GatewayInternalHealthController {

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final String REASON_READY = "READY";
    private static final String REASON_APPLICATION_NOT_READY = "APPLICATION_NOT_READY";
    private static final String RELEASE_ACTIVE_COLOR = "release.active-color";
    private static final String RELEASE_ACTIVE_VERSION = "release.active-version";
    private static final String RELEASE_COLOR = "release.color";
    private static final String RELEASE_VERSION = "release.version";
    private static final String RELEASE_MQ_CONSUMER_ENABLED = "release.mq.consumer.enabled";
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
        if (readinessState.isReady()) {
            return Mono.just(ResponseEntity.ok(body(STATUS_UP, REASON_READY)));
        }
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(body(STATUS_DOWN, REASON_APPLICATION_NOT_READY)));
    }

    @GetMapping("/release-state")
    public Mono<ResponseEntity<Map<String, Object>>> releaseState() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("activeColor", environment.getProperty(RELEASE_ACTIVE_COLOR));
        body.put("activeVersion", environment.getProperty(RELEASE_ACTIVE_VERSION));
        body.put("releaseColor", environment.getProperty(RELEASE_COLOR));
        body.put("releaseVersion", environment.getProperty(RELEASE_VERSION));
        body.put("mqConsumerEnabled", environment.getProperty(RELEASE_MQ_CONSUMER_ENABLED, Boolean.class, true));
        body.put("xxlJobEnabled", environment.getProperty(RELEASE_XXL_JOB_ENABLED, Boolean.class, true));
        body.put("currentReleaseActive", isCurrentReleaseActive());
        return Mono.just(ResponseEntity.ok(body));
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
