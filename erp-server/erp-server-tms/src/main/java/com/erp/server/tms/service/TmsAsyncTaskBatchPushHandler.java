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

    default void afterBatchProcessed(String taskId,
                                     int batchNumber,
                                     List<String> batchIds,
                                     TmsAsyncTaskRecordDTO.BatchProcessResult result) {
    }
}
