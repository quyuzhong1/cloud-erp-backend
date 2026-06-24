package com.erp.server.oms.orchestration;

import lombok.Getter;

/**
 * 单步跨服务调用结果
 */
@Getter
public class StepInvokeResult {

    public enum Outcome {
        SUCCESS,
        FAILED,
        WAITING,
        NOT_CLAIMED,
        SKIPPED
    }

    private final Outcome outcome;
    private final String errorMsg;
    private final String outputDataJson;
    private final String responseStatus;
    private final long feignDurationMs;
    private final String errorSource;

    private StepInvokeResult(Outcome outcome, String errorMsg, String outputDataJson,
                             String responseStatus, long feignDurationMs, String errorSource) {
        this.outcome = outcome;
        this.errorMsg = errorMsg;
        this.outputDataJson = outputDataJson;
        this.responseStatus = responseStatus;
        this.feignDurationMs = feignDurationMs;
        this.errorSource = errorSource;
    }

    public static StepInvokeResult notClaimed() {
        return new StepInvokeResult(Outcome.NOT_CLAIMED, null, null, null, 0L, null);
    }

    public static StepInvokeResult skipped() {
        return new StepInvokeResult(Outcome.SKIPPED, null, null, null, 0L, null);
    }

    public static StepInvokeResult success(String outputDataJson) {
        return new StepInvokeResult(Outcome.SUCCESS, null, outputDataJson, null, 0L, null);
    }

    public static StepInvokeResult waiting(String errorMsg) {
        return new StepInvokeResult(Outcome.WAITING, errorMsg, null, null, 0L, "remote");
    }

    public static StepInvokeResult failed(String errorMsg, String errorSource, long feignDurationMs) {
        return new StepInvokeResult(Outcome.FAILED, errorMsg, null, null, feignDurationMs, errorSource);
    }

    public static StepInvokeResult remoteResult(String responseStatus, String errorMsg, String outputDataJson, long feignDurationMs) {
        return new StepInvokeResult(Outcome.SUCCESS, errorMsg, outputDataJson, responseStatus, feignDurationMs, "remote");
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS;
    }

    public boolean isWaiting() {
        return outcome == Outcome.WAITING
                || (responseStatus != null && "waiting".equalsIgnoreCase(responseStatus));
    }

    public boolean isFailed() {
        return outcome == Outcome.FAILED
                || (responseStatus != null && "failed".equalsIgnoreCase(responseStatus));
    }
}
