package com.erp.server.tms.service;

import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;

import java.util.List;

/**
 * TMS 异步任务分批 push 业务策略（Policy）。
 *
 * @param <P> 业务载荷类型
 */
public interface TmsAsyncTaskBatchPushHandler<P> {

    String taskDisplayName();

    Class<P> payloadClass();

    String payloadParseErrorMessage();

    String validatePayload(P payload);

    default boolean refreshRecordBeforeClaim() {
        return true;
    }

    /**
     * 首批查询无 businessId 时的任务级提示。
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

    List<String> pageBatchIds(String taskId,
                              TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                              P payload,
                              String lastId,
                              int batchSize,
                              TmsAsyncTaskRecordEntity taskRecord);

    TmsAsyncTaskRecordDTO.BatchProcessResult processBatch(String taskId,
                                                          TmsAsyncTaskRecordDTO.TaskEnvelopeDTO envelope,
                                                          P payload,
                                                          LoginUser operatorUser,
                                                          List<String> batchIds,
                                                          int batchNumber,
                                                          CfgSettingValueDTO.BillBatchParamsDTO billBatchParams);
}
