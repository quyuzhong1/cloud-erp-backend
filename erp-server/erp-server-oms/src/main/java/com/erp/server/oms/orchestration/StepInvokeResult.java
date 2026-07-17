package com.erp.server.oms.orchestration;

import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.erp.model.oms.enums.WorkflowTaskRecordStatusEnum;
import lombok.Getter;

/**
 * 单步跨服务调用结果。
 * <p>
 * {@code pendingNode} 携带字段已在内存中更新（状态/错误/耗时等）但尚未落库的节点实体；
 * 调用方应在短事务中统一持久化节点与实例状态，保证两表写操作的原子性。
 * </p>
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
    /** 内存中已准备好最终状态但尚未落库的节点实体；NOT_CLAIMED/SKIPPED 时为 null */
    private final WorkflowTaskRecordEntity pendingNode;

    private StepInvokeResult(Outcome outcome, String errorMsg, String outputDataJson,
                             String responseStatus, long feignDurationMs, String errorSource,
                             WorkflowTaskRecordEntity pendingNode) {
        this.outcome = outcome;
        this.errorMsg = errorMsg;
        this.outputDataJson = outputDataJson;
        this.responseStatus = responseStatus;
        this.feignDurationMs = feignDurationMs;
        this.errorSource = errorSource;
        this.pendingNode = pendingNode;
    }

    public static StepInvokeResult notClaimed() {
        return new StepInvokeResult(Outcome.NOT_CLAIMED, null, null, null, 0L, null, null);
    }

    public static StepInvokeResult skipped() {
        return new StepInvokeResult(Outcome.SKIPPED, null, null, null, 0L, null, null);
    }

    public static StepInvokeResult success(String outputDataJson) {
        return new StepInvokeResult(Outcome.SUCCESS, null, outputDataJson, null, 0L, null, null);
    }

    public static StepInvokeResult success(String outputDataJson, WorkflowTaskRecordEntity pendingNode) {
        return new StepInvokeResult(Outcome.SUCCESS, null, outputDataJson, null, 0L, null, pendingNode);
    }

    public static StepInvokeResult waiting(String errorMsg) {
        return new StepInvokeResult(Outcome.WAITING, errorMsg, null, null, 0L, "remote", null);
    }

    public static StepInvokeResult waiting(String errorMsg, WorkflowTaskRecordEntity pendingNode) {
        return new StepInvokeResult(Outcome.WAITING, errorMsg, null, null, 0L, "remote", pendingNode);
    }

    public static StepInvokeResult failed(String errorMsg, String errorSource, long feignDurationMs) {
        return new StepInvokeResult(Outcome.FAILED, errorMsg, null, null, feignDurationMs, errorSource, null);
    }

    public static StepInvokeResult failed(String errorMsg, String errorSource, long feignDurationMs,
                                          WorkflowTaskRecordEntity pendingNode) {
        return new StepInvokeResult(Outcome.FAILED, errorMsg, null, null, feignDurationMs, errorSource, pendingNode);
    }

    public static StepInvokeResult remoteResult(String responseStatus, String errorMsg, String outputDataJson, long feignDurationMs) {
        return new StepInvokeResult(Outcome.SUCCESS, errorMsg, outputDataJson, responseStatus, feignDurationMs, "remote", null);
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS;
    }

    public boolean isWaiting() {
        return outcome == Outcome.WAITING
                || (responseStatus != null && WorkflowTaskRecordStatusEnum.WAITING.getCode().equalsIgnoreCase(responseStatus));
    }

    public boolean isFailed() {
        return outcome == Outcome.FAILED
                || (responseStatus != null && WorkflowTaskRecordStatusEnum.FAILED.getCode().equalsIgnoreCase(responseStatus));
    }
}
