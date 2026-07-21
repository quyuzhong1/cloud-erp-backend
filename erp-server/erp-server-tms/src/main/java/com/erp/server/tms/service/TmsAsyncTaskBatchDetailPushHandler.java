package com.erp.server.tms.service;

import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;

import java.util.List;

/**
 * TMS 异步任务「明细先行」分批 push 业务策略（Policy）。
 * <p>
 * 适用于 prepare 阶段即创建/幂等写入 {@link TmsAsyncTaskDetailEntity} 的下推类任务。
 *
 * @param <P> 业务载荷类型
 */
public interface TmsAsyncTaskBatchDetailPushHandler<P> {

    String taskDisplayName();

    Class<P> payloadClass();

    String payloadParseErrorMessage();

    String validatePayload(P payload);

    default boolean refreshRecordBeforeClaim() {
        return true;
    }

    /**
     * 首批无明细时的任务级提示。
     * <p>
     * 返回非空时主任务直接 FINISH 并写入 {@code errorData}，且不再调用 {@code updateTaskFinally}。
     */
    default String emptyFirstBatchMessage(TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope, P payload) {
        if (envelope != null
            && TmsAsyncTaskRecordDTO.RETRY_MODE_FAILED_ONLY.equals(envelope.getRetryMode())) {
            return "无失败明细可重试";
        }
        return null;
    }

    String resolveBusinessType(TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                               TmsAsyncTaskRecordEntity taskRecord,
                               P payload);

    List<TmsAsyncTaskDetailEntity> prepareBatchDetails(String taskId,
                                                       String businessType,
                                                       TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                       P payload,
                                                       String cursor,
                                                       int batchSize);

    TmsAsyncTaskRecordDTO.BatchProcessResult processPreparedBatch(String taskId,
                                                                  TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                                  P payload,
                                                                  List<TmsAsyncTaskDetailEntity> batchDetails,
                                                                  int batchNumber,
                                                                  CfgSettingValueDTO.BillBatchParamsDTO billBatchParams);
}
