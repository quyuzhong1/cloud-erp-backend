package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.common.business.service.SuperService;

import java.util.Collection;
import java.time.LocalDateTime;
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
     * 将待执行明细从 PENDING 认领为 ING。
     * <p>
     * 认领成功时会刷新 startTime，使其表示真实执行开始时间，供僵死 ING 判定使用。
     *
     * @param taskDetailId 任务明细 ID
     * @return true 表示认领成功；false 表示明细已被其他线程处理或状态已变化
     */
    boolean tryClaimDetailForExecution(String taskDetailId);

    /**
     * 将失败/僵死执行中的明细重新认领为 ING（错误重试、watchdog 恢复）。
     * <p>
     * 仅当库中状态仍为 FAILED 或 ING 时 CAS 成功，避免并发覆盖终态。
     *
     * @param taskDetailId 任务明细 ID
     * @return true 表示认领成功；false 表示状态已变化
     */
    boolean tryClaimDetailForRetry(String taskDetailId);

    /**
     * 将指定明细中未进入终态的数据标记为失败。
     *
     * @param detailIds 任务明细 ID 集合
     * @param errorMsg 失败原因，写入 errorData 前会截断
     * @return 实际标记为失败的明细数量
     */
    int markDetailsFailed(Collection<String> detailIds, String errorMsg);

    /**
     * 将超过执行窗口的 ING 明细标记为失败，不重置为 PENDING，后续走错误重试。
     *
     * @param mainId 主任务 ID
     * @param businessIds 业务 ID 集合
     * @param staleBefore 僵死阈值时间，早于或等于该时间的 ING 明细会被标记失败
     * @param errorMsg 失败原因，写入 errorData 前会截断
     * @return 实际标记为失败的明细数量
     */
    int markStaleIngDetailsFailed(String mainId, Collection<String> businessIds, java.time.LocalDateTime staleBefore, String errorMsg);

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
     * 查询本批待执行的 PENDING 明细；必要时追加游标占位行，保证批次消费框架能正确推进游标。
     *
     * @param mainId 主任务 ID
     * @param businessIds 本批业务 ID
     * @param cursorBusinessId 本批 source 末条 businessId，用于游标推进
     */
    List<TmsAsyncTaskDetailEntity> listPendingDetailsWithCursorAnchor(String mainId, Collection<String> businessIds,
                                                                      String cursorBusinessId);

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

    /**
     * 限量查询已完成主任务下仍为 ING 的明细 ID。
     */
    List<String> listOrphanIngDetailIds(int limit);

    /**
     * 限量查询指定任务类型下已超过执行窗口的 ING 明细 ID。
     */
    List<String> listStaleIngDetailIdsByTaskType(String businessType, String methodType, LocalDateTime staleBefore, int limit);
}
