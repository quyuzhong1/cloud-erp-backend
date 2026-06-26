package com.erp.server.tms.service.asynctask;

import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 中转费用分摊异步任务批次执行委托。
 */
public interface TransferDeclareCostAllocationAsyncTaskDelegate {

    TmsAsyncTaskRecordDTO.BatchProcessResult processUpdateStatusBatch(String taskId,
                                                                      List<String> batchIds,
                                                                      String reportDate,
                                                                      String reportStatus,
                                                                      int timeoutSeconds,
                                                                      int staleDetailSeconds,
                                                                      LoginUser operatorUser);

    TmsAsyncTaskRecordDTO.BatchProcessResult processReAllocationBatch(String taskId,
                                                                      List<String> batchIds,
                                                                      int timeoutSeconds,
                                                                      int staleDetailSeconds,
                                                                      LoginUser operatorUser);

    TmsAsyncTaskRecordDTO.BatchProcessResult processDeleteBatch(String taskId,
                                                               List<String> batchIds,
                                                               int timeoutSeconds,
                                                               int staleDetailSeconds,
                                                               LoginUser operatorUser);

    List<TmsAsyncTaskDetailEntity> prepareTransferDeclarePushBatchDetails(String taskId,
                                                                          String businessType,
                                                                          String retryMode,
                                                                          String retrySourceTaskId,
                                                                          LocalDate startDate,
                                                                          LocalDate endDate,
                                                                          String cursor,
                                                                          int batchSize);

    TmsAsyncTaskRecordDTO.BatchProcessResult executeTransferDeclarePushBatch(String taskId,
                                                                             List<TmsAsyncTaskDetailEntity> taskDetailList,
                                                                             int timeoutSeconds,
                                                                             int staleDetailSeconds);
}
