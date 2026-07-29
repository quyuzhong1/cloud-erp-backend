package com.common.message.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.message.config.ReleaseControlledXxlJobSpringExecutor;
import com.common.message.controller.vo.XxlJobLifecycleStatusVO;
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
/** Loopback-only XXL-JOB terminal drain endpoints used by the release pipeline. */
@RestController
@RequestMapping("/internal/xxljob")
@ConditionalOnWebApplication
public class XxlJobStatusController extends BaseController {

    private final ObjectProvider<XxlJobSpringExecutor> executorProvider;

    public XxlJobStatusController(ObjectProvider<XxlJobSpringExecutor> executorProvider) {
        this.executorProvider = executorProvider;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResult<XxlJobLifecycleStatusVO>> status(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            return forbidden("xxl-job lifecycle endpoints only accept loopback requests");
        }

        XxlJobSpringExecutor executor = executorProvider.getIfAvailable();
        if (executor == null) {
            XxlJobLifecycleStatusVO status = new XxlJobLifecycleStatusVO();
            status.setStatus("NOT_CONFIGURED");
            return lifecycleResponse(HttpStatus.OK, 200, "请求成功！", status);
        }
        if (!(executor instanceof ReleaseControlledXxlJobSpringExecutor)) {
            XxlJobLifecycleStatusVO status = new XxlJobLifecycleStatusVO();
            status.setStatus("UNSUPPORTED_EXECUTOR");
            status.setConfigured(true);
            return lifecycleResponse(
                    HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(), "unsupported xxl-job executor", status);
        }

        return lifecycleResponse(
                HttpStatus.OK,
                200,
                "请求成功！",
                controlledStatus((ReleaseControlledXxlJobSpringExecutor) executor));
    }

    /**
     * Starts the irreversible XXL-JOB terminal drain.
     *
     * @param request current HTTP request
     * @return typed lifecycle state
     */
    @PostMapping("/drain")
    public ResponseEntity<ApiResult<XxlJobLifecycleStatusVO>> drain(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            return forbidden("xxl-job lifecycle endpoints only accept loopback requests");
        }

        XxlJobSpringExecutor executor = executorProvider.getIfAvailable();
        if (executor == null) {
            return status(request);
        }
        if (!(executor instanceof ReleaseControlledXxlJobSpringExecutor)) {
            XxlJobLifecycleStatusVO status = new XxlJobLifecycleStatusVO();
            status.setStatus("UNSUPPORTED_EXECUTOR");
            status.setConfigured(true);
            return lifecycleResponse(
                    HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(), "unsupported xxl-job executor", status);
        }

        ReleaseControlledXxlJobSpringExecutor controlled = (ReleaseControlledXxlJobSpringExecutor) executor;
        try {
            controlled.beginDrain();
            return lifecycleResponse(HttpStatus.OK, 200, "请求成功！", controlledStatus(controlled));
        } catch (RuntimeException ex) {
            XxlJobLifecycleStatusVO failed = controlledStatus(controlled);
            failed.setStatus("FAILED");
            failed.setMessage(ex.getMessage());
            return lifecycleResponse(HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(), ex.getMessage(), failed);
        }
    }

    /**
     * Builds the current controlled executor state.
     *
     * @param controlled release-controlled executor
     * @return current lifecycle state
     */
    private XxlJobLifecycleStatusVO controlledStatus(ReleaseControlledXxlJobSpringExecutor controlled) {
        XxlJobLifecycleStatusVO status = new XxlJobLifecycleStatusVO();
        status.setStatus(controlled.getDrainState());
        status.setConfigured(true);
        status.setAcceptingTriggers(controlled.isAcceptingTriggers());
        status.setRegistryRemovalRequested(controlled.isRegistryRemovalRequested());
        status.setBusyJobThreads(controlled.getBusyJobThreadCount());
        status.setFailure(controlled.getDrainFailure());
        return status;
    }

    /**
     * Builds a loopback rejection while preserving the lifecycle response schema.
     *
     * @param message rejection detail
     * @return HTTP 403 response
     */
    private ResponseEntity<ApiResult<XxlJobLifecycleStatusVO>> forbidden(String message) {
        XxlJobLifecycleStatusVO forbidden = new XxlJobLifecycleStatusVO();
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
    private ResponseEntity<ApiResult<XxlJobLifecycleStatusVO>> lifecycleResponse(
            HttpStatus httpStatus,
            int code,
            String responseMessage,
            XxlJobLifecycleStatusVO data) {
        return ResponseEntity.status(httpStatus).body(message(code, responseMessage, data));
    }

    private boolean isLoopbackRequest(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddr)
                || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                || "::1".equals(remoteAddr);
    }
}
