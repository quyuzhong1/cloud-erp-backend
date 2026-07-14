package com.erp.server.tms.service.asynctask;

import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 头程对账异步任务批次执行委托。
 */
public interface TmsFirstMileReconciliationAsyncTaskDelegate {

    List<TmsAsyncTaskDetailEntity> prepareFirstMileReconciliationBatchDetails(String taskId,
                                                                              String businessType,
                                                                              String retryMode,
                                                                              String retrySourceTaskId,
                                                                              LocalDate startDate,
                                                                              LocalDate endDate,
                                                                              String cursor,
                                                                              int batchSize);

    TmsAsyncTaskRecordDTO.BatchProcessResult executeFirstMileReconciliationBatch(String taskId,
                                                                                 List<TmsAsyncTaskDetailEntity> taskDetailList,
                                                                                 LocalDate startDate,
                                                                                 LocalDate endDate,
                                                                                 int timeoutSeconds,
                                                                                 int staleDetailSeconds);
}
