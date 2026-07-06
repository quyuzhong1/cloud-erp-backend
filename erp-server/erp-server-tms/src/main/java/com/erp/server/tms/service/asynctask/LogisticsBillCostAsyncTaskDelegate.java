package com.erp.server.tms.service.asynctask;

import com.common.business.vo.LoginUser;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流费用异步任务批次执行委托。
 */
public interface LogisticsBillCostAsyncTaskDelegate {

    TmsAsyncTaskRecordDTO.BatchProcessResult processPushAllocationBatch(String taskId,
                                                                        String businessType,
                                                                        List<String> batchIds,
                                                                        String reportDate,
                                                                        int timeoutSeconds,
                                                                        int staleDetailSeconds,
                                                                        LogisticsBillCostDTO.SmallBagPushAllocationContext pushContext);

    LogisticsBillCostDTO.SmallBagPushAllocationContext buildSmallBagPushAllocationContext();

    LogisticsBillCostDTO.UpdateReconciliationStatusPageQueryDTO buildUpdateReconciliationStatusPageQuery(
        TmsAsyncTaskRecordDTO.UpdateReconciliationStatusPayloadDTO payload, String costType);

    String resolveCostTypeFromUpdateReconciliationMethodType(String methodType);

    TmsAsyncTaskRecordDTO.BatchProcessResult processUpdateReconciliationStatusBatch(String taskId,
                                                                                    List<String> batchIds,
                                                                                    String reconciliationStatus,
                                                                                    LocalDateTime confirmTime,
                                                                                    String costType,
                                                                                    LoginUser operatorUser,
                                                                                    int timeoutSeconds,
                                                                                    int staleDetailSeconds);
}
