package com.erp.server.tms.service.asynctask;

import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * B2C 报关对账异步任务批次执行委托。
 */
public interface TmsB2cDeclareReconciliationAsyncTaskDelegate {

    List<TmsAsyncTaskDetailEntity> prepareDeclareReconciliationBatchDetails(String taskId,
                                                                            String businessType,
                                                                            String retryMode,
                                                                            String retrySourceTaskId,
                                                                            LocalDate startDate,
                                                                            LocalDate endDate,
                                                                            String cursor,
                                                                            int batchSize);

    TmsAsyncTaskRecordDTO.BatchProcessResult executeDeclareReconciliationBatch(String taskId,
                                                                               List<TmsAsyncTaskDetailEntity> taskDetailList,
                                                                               LocalDate startDate,
                                                                               LocalDate endDate,
                                                                               int timeoutSeconds,
                                                                               int staleDetailSeconds);
}
