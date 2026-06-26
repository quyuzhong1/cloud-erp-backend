package com.erp.server.oms.service;
import com.erp.model.oms.entity.WorkflowTaskRecordEntity;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务节点记录表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-09-16
 */
public interface WorkflowTaskRecordService extends SuperService<WorkflowTaskRecordEntity> {

    /** PROCESSING 超时分钟数，超时后允许 Job/人工重新抢占 */
    int TASK_PROCESSING_TIMEOUT_MINUTES = 3;

    /** 自动补偿（Job）最大重试次数，超过后仅允许人工 forceRetry */
    int AUTO_RETRY_MAX_COUNT = 3;

    /** 节点终态失败判定阈值（retryCount 达到后不再自动补偿） */
    int TASK_TERMINAL_RETRY_COUNT = AUTO_RETRY_MAX_COUNT;

    List<WorkflowTaskRecordEntity> addTask(WorkflowTaskRecordDTO.AddTaskDTO dto);

    /**
     * 补齐缺失节点（KOL 拆分等场景）。
     * <p>对比字典配置与已存在节点，仅插入 index 缺失的记录；上一节点 output 自动作为新节点 input。</p>
     *
     * @param dto        业务来源与字典类型
     * @param existTasks 调用方已知的已有节点（可与库内数据合并去重）
     * @return 新插入的节点列表；无缺失时返回空列表
     */
    List<WorkflowTaskRecordEntity> addMissingTask(WorkflowTaskRecordDTO.AddTaskDTO dto, List<WorkflowTaskRecordEntity> existTasks);

    List<WorkflowTaskRecordEntity> listErrorTaskByInstance(String id);

    List<WorkflowTaskRecordEntity> listErrorTaskByRecord(String id);

    void workflowTaskRecordRetryJob(String id, String type);

    List<WorkflowTaskRecordDTO.TaskErrorReportDTO> getTaskErrorReport();

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
     * 查询未绑定 instance_id 的历史节点（兼容改造前数据）。
     */
    List<WorkflowTaskRecordEntity> listLegacyBySourceId(String sourceId, String sourceType);

    /**
     * 根据业务键与节点序号查询未删除的最新任务节点。
     *
     * @param sourceId   业务主键
     * @param sourceType 业务类型编码
     * @param index      节点序号
     */
    WorkflowTaskRecordEntity getActiveTask(String sourceId, String sourceType, Integer index);

    /**
     * 条件抢占待执行任务节点（CAS：fromStatus → PROCESSING），避免重复 MQ 并发执行同一节点。
     *
     * @param id         节点 ID
     * @param fromStatus 期望的当前状态（通常为 pending）
     * @return 抢占成功返回 true
     */
    Boolean claimTask(String id, String fromStatus);

    /**
     * 将超时仍处于 PROCESSING 的节点重置为 PENDING。
     * <p>超时阈值见 {@link #TASK_PROCESSING_TIMEOUT_MINUTES}，便于 Job 或人工再次抢占。</p>
     *
     * @param id 节点 ID
     * @return 发生重置时返回 true
     */
    Boolean resetStaleProcessingTask(String id);

    /**
     * 启动或恢复编排（带分布式锁）。
     * <ul>
     *   <li>无实例/无节点：addTask 后调度 index=0</li>
     *   <li>实例终态（success/cancelled）：新建实例并调度首节点</li>
     *   <li>实例运行中：当前节点 PROCESSING 且未超时则幂等跳过，否则调度当前 index</li>
     * </ul>
     */
    void startOrResume(WorkflowTaskRecordDTO.AddTaskDTO dto);

    /**
     * 创建任务并调度首节点（推荐业务入口，替代 addTask + 手动发 MQ）。
     * <p>事务提交后发送 targetIndex=0 的单步 MQ。</p>
     */
    List<WorkflowTaskRecordEntity> addTaskAndStart(WorkflowTaskRecordDTO.AddTaskDTO dto);

    /**
     * 人工强制重试失败、等待中或超时 PROCESSING 的节点。
     * <p>重置节点为 PENDING 后发送单步 MQ；需具备 forceRetry 权限或为超级管理员。</p>
     */
    WorkflowTaskRecordDTO.ForceRetryResultDTO forceRetry(WorkflowTaskRecordDTO.ForceRetryDTO dto);


    /**
     * 判断节点是否允许强制重试（含 PROCESSING 超时判定）。
     */
    boolean isStepForceRetryAllowed(WorkflowTaskRecordEntity entity);

    /**
     * 拼接人工重试备注。
     */
    String formatForceRetryRemark(String oldRemark, String remark);

    /**
     * 获取上一成功节点的 output，作为当前节点重试 input。
     */
    String getPreviousSuccessOutputData(WorkflowTaskRecordEntity entity,
                                        Map<Integer, WorkflowTaskRecordEntity> indexTaskMap);
}
