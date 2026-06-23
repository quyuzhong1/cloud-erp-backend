package com.erp.server.tms.service.asynctask;

import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 头程费用分摊异步任务批次执行委托。
 */
public interface FirstMileCostAllocationAsyncTaskDelegate {

    LocalDate parseReportPeriodMonth(String reportPeriodStr);

    List<String> pageIdsForReAllocation(LocalDate reportPeriodMonth, String reportStatus, String lastId, int batchSize);

    List<String> pageIdsByReportPeriodMonth(LocalDate reportPeriodMonth, String reportStatus, String lastId, int batchSize);

    TmsAsyncTaskRecordDTO.BatchProcessResult processPushAllocationBatch(String taskId,
                                                                        List<String> batchDeliveryIds,
                                                                        String reportDate,
                                                                        int timeoutSeconds,
                                                                        int staleDetailSeconds,
                                                                        LoginUser operatorUser);

    TmsAsyncTaskRecordDTO.BatchProcessResult processUpdateStatusBatch(String taskId,
                                                                      List<String> batchIds,
                                                                      String reportStatus,
                                                                      String reportDate,
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
}
