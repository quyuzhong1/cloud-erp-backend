package com.erp.server.oms.service;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.enums.DictBasicTypeEnum;

import java.util.List;

/**
 * <p>
 * 任务节点记录表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-09-16
 */
public interface WorkflowTaskRecordService extends SuperService<WorkflowTaskRecordEntity> {

    int TASK_PROCESSING_TIMEOUT_MINUTES = 3;

    List<WorkflowTaskRecordEntity> addTask(WorkflowTaskRecordDTO.AddTaskDTO dto);

    List<WorkflowTaskRecordEntity> listErrorTask();

    void WorkflowTaskRecordRetryJob(String id);

    List<WorkflowTaskRecordDTO.TaskErrorReportDTO> getTaskErrorReport();

    /**
     * 人工强制重试终态失败或等待中的任务节点。
     */
    WorkflowTaskRecordDTO.ForceRetryResultDTO forceRetry(WorkflowTaskRecordDTO.ForceRetryDTO dto);

    /**
     * 根据sourceId和sourceType删除任务记录
     * @param sourceId
     * @param sourceType
     */
    void removeBySourceIdAndSourceType(String sourceId, String sourceType);

    /**
     * 根据sourceId查询任务记录
     * @param soId
     * @return
     */
    List<WorkflowTaskRecordEntity> listBySourceId(String soId, String sourceType);

    /**
     * 根据业务键查询未删除任务节点。
     */
    WorkflowTaskRecordEntity getActiveTask(String sourceId, String sourceType, Integer index);

    /**
     * 条件抢占待执行任务节点，避免重复消息并发执行同一节点。
     */
    Boolean claimTask(String id, String fromStatus);

    /**
     * 将超时仍处于 PROCESSING 的节点重置为 PENDING，便于 MQ 补偿重新抢占。
     */
    Boolean resetStaleProcessingTask(String id);
}
