package com.erp.server.oms.service;



import com.common.business.dto.base.PagingDTO;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;

import com.common.business.vo.PagingVO;

import com.erp.model.oms.dto.WorkflowTaskInstanceDTO;

import com.erp.model.oms.dto.WorkflowTaskRecordDTO;

import com.erp.model.oms.entity.WorkflowTaskInstanceEntity;

import com.erp.model.oms.entity.WorkflowTaskRecordEntity;



import java.util.List;



/**

 * 任务编排实例服务：聚合一次业务触发的全链路节点，提供状态流转与运维监控能力。

 */

public interface WorkflowTaskInstanceService extends SuperService<WorkflowTaskInstanceEntity> {



    /**

     * 创建编排实例并置为 running，与 {@link WorkflowTaskRecordService#addTask} 配套使用。

     *

     * @param dto         业务来源信息

     * @param totalSteps  字典配置的节点总数

     * @return 新建的实例实体

     */

    WorkflowTaskInstanceEntity createInstance(WorkflowTaskRecordDTO.AddTaskDTO dto, int totalSteps);



    /**

     * 按业务键查询最新一条未删除的编排实例（按创建时间倒序）。

     *

     * @param sourceId   业务主键

     * @param sourceType 业务类型编码

     * @return 最新实例，不存在时返回 null

     */

    WorkflowTaskInstanceEntity getLatestBySource(String sourceId, String sourceType);



    /**

     * 为历史无 instance_id 的节点补建实例并回填关联。

     * <p>用于 MQ 消费或 startOrResume 时兼容改造前已落库的节点数据。</p>

     *

     * @param dto   业务来源信息

     * @param steps 同一 source 下的已有节点列表

     * @return 关联后的实例；steps 为空时返回 null

     */

    WorkflowTaskInstanceEntity ensureInstanceForLegacy(WorkflowTaskRecordDTO.AddTaskDTO dto, List<WorkflowTaskRecordEntity> steps);



    /**

     * 将实例标记为执行中，并同步当前节点序号与总步数。

     */

    void markRunning(String instanceId, int currentIndex, int totalSteps);

    /**
     * 将实例标记为执行中；若实例不存在或乐观锁冲突则抛出业务异常。
     * <p>用于人工重试等必须确保实例状态切换成功后才能发送 MQ 的场景。</p>
     */
    void markRunningOrThrow(String instanceId, int currentIndex, int totalSteps);



    /**

     * 将实例标记为等待中（节点返回 WAITING，需 Job 或业务事件再次唤醒）。

     */

    void markWaiting(String instanceId, int currentIndex, String lastError);



    /**

     * 将实例标记为失败，并记录当前卡住节点与错误摘要。

     */

    void markFailed(String instanceId, int currentIndex, String lastError);



    /**

     * 将实例标记为成功并写入完成时间。

     */

    void markSuccess(String instanceId, int currentIndex, int totalSteps);



    /**

     * 取消运行中的实例，并将未成功节点置为失败。

     *

     * @param instanceId 实例 ID

     * @param remark       取消原因，写入 lastError

     */

    void markCancelled(String instanceId, String remark);



    /**

     * 实例分页查询，支持高级搜索与数据权限过滤。

     */

    PagingVO<WorkflowTaskInstanceDTO.ListDTO> paging(PagingDTO<WorkflowTaskInstanceDTO.PagingParamDTO> dto);



    /**

     * 实例详情：含节点时间线、进度、是否已达自动重试上限等。

     *

     * @param id 实例 ID

     */

    WorkflowTaskInstanceDTO.ViewDTO view(String id);



    /**

     * 按业务键查询该单据的全部编排实例历史（含多次触发记录）。

     */

    List<WorkflowTaskInstanceDTO.ViewDTO> listBySource(WorkflowTaskInstanceDTO.ListBySourceParamDTO param);



    /**

     * 异常统计：按业务类型 × 节点 × 目标服务聚合失败次数。

     */

    List<WorkflowTaskInstanceDTO.ErrorReportDTO> errorReport(WorkflowTaskInstanceDTO.ErrorReportParamDTO param);



    /**

     * 人工重试：按 instanceId 执行强制重试。

     */

    WorkflowTaskInstanceDTO.RetryResultDTO retry(WorkflowTaskInstanceDTO.RetryDTO dto);



    /**

     * 从指定 index 起重置后续节点为 PENDING，并调度单步 MQ 重新执行。

     */

    WorkflowTaskInstanceDTO.RetryResultDTO retryFromStep(WorkflowTaskInstanceDTO.RetryFromStepDTO dto);



    /**

     * 取消指定实例，终止后续节点调度。

     */

    void cancel(WorkflowTaskInstanceDTO.CancelDTO dto);

    /**
     * tab汇总
     * @param dto
     * @return
     */
    List<WorkflowTaskInstanceDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 原子持久化节点最终状态并同步实例状态（节点 FAILED 或 WAITING 时使用）。
     * <p>在同一事务内完成节点 updateById + 实例 status 更新，避免两步写库之间出现中间态。</p>
     *
     * @param node          内存中已准备好最终状态的节点实体（尚未落库）
     * @param instanceId    对应编排实例 ID
     * @param currentIndex  当前节点序号
     * @param totalSteps    实例节点总数
     * @param outcome       节点结果：FAILED 或 WAITING（SUCCESS 请使用 persistLastNodeSuccess）
     * @param lastError     错误摘要，用于写入实例 last_error
     */
    void persistNodeAndSyncInstance(WorkflowTaskRecordEntity node,
                                    String instanceId,
                                    int currentIndex,
                                    int totalSteps,
                                    com.erp.server.oms.orchestration.StepInvokeResult.Outcome outcome,
                                    String lastError);

    /**
     * 事务提交后 MQ 发送失败时，新开独立事务将目标节点与实例标记为 FAILED，确保 Job 可扫描到并补偿。
     *
     * @param instanceId  编排实例 ID
     * @param targetIndex 目标节点 index
     * @param reason      失败原因，写入 last_error
     */
    void markDispatchMqFailed(String instanceId, Integer targetIndex, String reason);

    /**
     * 末节点成功：原子写节点 SUCCESS + 实例 SUCCESS，避免两步写库之间出现「节点成功/实例仍运行」中间态。
     *
     * @param node       末节点实体（内存中已准备好 SUCCESS 状态，尚未落库）
     * @param instanceId 对应编排实例 ID
     * @param maxIndex   末节点 index（= currentIndex）
     * @param totalSteps 实例节点总数
     */
    void persistLastNodeSuccess(WorkflowTaskRecordEntity node,
                                String instanceId,
                                int maxIndex,
                                int totalSteps);

    /**
     * 中间节点成功：原子写当前节点 SUCCESS + 下一节点 inputData，避免两次独立写库之间断链。
     * <p>MQ 发送应在本方法事务提交后进行（由调用方通过 scheduleNextStep 处理）。</p>
     *
     * @param currentNode   当前节点（内存中已准备好 SUCCESS 状态，尚未落库）
     * @param nextNode      下一节点实体；为 null 或 nextInputData 为空时仅写当前节点
     * @param nextInputData 要回填到下一节点的 inputData；为空时跳过下一节点更新
     */
    void persistMiddleNodeSuccess(WorkflowTaskRecordEntity currentNode,
                                  WorkflowTaskRecordEntity nextNode,
                                  String nextInputData);
}

