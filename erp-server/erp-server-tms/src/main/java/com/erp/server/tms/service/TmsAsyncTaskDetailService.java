package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.common.business.service.SuperService;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 异步任务记录明细 服务类
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
public interface TmsAsyncTaskDetailService extends SuperService<TmsAsyncTaskDetailEntity> {

    void updateDetail(String taskDetailId, String status, String msg);

    /**
     * PENDING 认领为 ING，防止重复执行
     */
    boolean tryClaimDetailForExecution(String taskDetailId);

    List<TmsAsyncTaskDetailEntity> listErrorDetail(String mainId);

    /**
     * 错误重试按失败明细游标分页，避免一次性把大量 businessId 写入任务参数。
     */
    List<String> listFailedBusinessIdsByCursor(String mainId, String lastBusinessId, int batchSize);

    /**
     * 分批保存任务明细，避免单次 saveBatch 数据量过大。
     */
    void saveBatchInChunks(List<TmsAsyncTaskDetailEntity> details, int batchSize);

    /**
     * 查询当前任务下已存在的业务 ID，用于批内幂等落明细。
     */
    List<String> listExistingBusinessIds(String mainId, Collection<String> businessIds);

    /**
     * 错误重试按失败明细游标分页，保留 businessCode 供目标任务明细展示。
     */
    List<TmsAsyncTaskDetailEntity> listFailedDetailsByCursor(String mainId, String lastBusinessId, int batchSize);

    /**
     * 批次并发等待超时或中断时，将本批未完成的明细（含 ING/PENDING）标记为失败。
     *
     * @return 实际标记为失败的明细数量
     */
    int markUnfinishedBatchDetailsFailed(List<TmsAsyncTaskDetailEntity> taskDetailList, String errorMsg);
}
