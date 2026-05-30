package com.cloud.erp.gateway.controller;

import com.cloud.erp.gateway.component.GatewayReadinessState;
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

    @Resource
    private GatewayReadinessState readinessState;

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

    private Map<String, Object> body(String status, String reason) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        if (reason != null) {
            body.put("reason", reason);
        }
        return body;
    }
}
