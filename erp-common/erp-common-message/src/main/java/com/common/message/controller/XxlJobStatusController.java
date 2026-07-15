package com.common.message.controller;

import com.common.message.config.ReleaseControlledXxlJobSpringExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loopback-only XXL-JOB terminal drain endpoints used by the release pipeline. */
@RestController
@RequestMapping("/internal/xxljob")
@ConditionalOnWebApplication
public class XxlJobStatusController {

    private final ObjectProvider<XxlJobSpringExecutor> executorProvider;

    public XxlJobStatusController(ObjectProvider<XxlJobSpringExecutor> executorProvider) {
        this.executorProvider = executorProvider;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            return forbidden();
        }

        XxlJobSpringExecutor executor = executorProvider.getIfAvailable();
        Map<String, Object> body = new LinkedHashMap<>();
        if (executor == null) {
            body.put("status", "NOT_CONFIGURED");
            body.put("configured", false);
            return ResponseEntity.ok(body);
        }
        if (!(executor instanceof ReleaseControlledXxlJobSpringExecutor)) {
            body.put("status", "UNSUPPORTED_EXECUTOR");
            body.put("configured", true);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        ReleaseControlledXxlJobSpringExecutor controlled = (ReleaseControlledXxlJobSpringExecutor) executor;
        body.put("status", controlled.getDrainState());
        body.put("configured", true);
        body.put("acceptingTriggers", controlled.isAcceptingTriggers());
        body.put("registryRemovalRequested", controlled.isRegistryRemovalRequested());
        body.put("busyJobThreads", controlled.getBusyJobThreadCount());
        body.put("failure", controlled.getDrainFailure());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/drain")
    public ResponseEntity<Map<String, Object>> drain(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            return forbidden();
        }

        XxlJobSpringExecutor executor = executorProvider.getIfAvailable();
        if (executor == null) {
            return status(request);
        }
        if (!(executor instanceof ReleaseControlledXxlJobSpringExecutor)) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "UNSUPPORTED_EXECUTOR");
            body.put("configured", true);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        try {
            ((ReleaseControlledXxlJobSpringExecutor) executor).beginDrain();
            return status(request);
        } catch (RuntimeException ex) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "FAILED");
            body.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
    }

    private ResponseEntity<Map<String, Object>> forbidden() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "FORBIDDEN");
        body.put("message", "xxl-job lifecycle endpoints only accept loopback requests");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    private boolean isLoopbackRequest(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddr)
                || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                || "::1".equals(remoteAddr);
    }
}
