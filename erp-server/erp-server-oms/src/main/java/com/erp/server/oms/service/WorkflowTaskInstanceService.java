package com.erp.server.oms.service;



import com.common.business.dto.base.PagingDTO;

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

     * 人工重试：委托节点级 forceRetry，可指定 stepId 或 instanceId。

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
     * 校验当前用户对编排实例的数据权限（创建人/部门范围）。
     *
     * @param instanceId 实例 ID
     */
    void assertInstanceDataPermission(String instanceId);

}

