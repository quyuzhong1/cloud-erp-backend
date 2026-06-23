package com.erp.server.tms.service.asynctask;

import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;

import java.util.List;

/**
 * 小包费用分摊异步任务批次执行委托。
 */
public interface SmallBagCostAllocationAsyncTaskDelegate {

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
}
