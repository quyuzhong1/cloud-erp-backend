package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectPlanTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.ProjectTaskVO;
import com.erp.model.plm.entity.TaskDocsNameEntity;
import com.erp.model.plm.vo.ScheduleTaskExportExcelVO;
import com.erp.model.plm.vo.ScheduleTaskVO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import org.apache.commons.math3.util.Pair;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品任务表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProjectTaskService extends IService<ProjectTaskEntity> {


    List<ProjectTaskEntity> addSysTask(String id, List<TaskDocsNameEntity> taskDocsNameList, LoginUser loginUser,String  productPropertyId);

    void removeTaskByProductId(String productId);

    List<ProjectTaskEntity> getByProductIds(List<String> productIds);

    List<ProjectTaskEntity> getByProductId(String productId);

    void copyTaskByProject(String saveProductId, String saveProjectId,String  flagProjectId);


    Pair<List<String>, List<ProjectTaskEntity>> copyTaskBySys(String productId, String projectId);


    PagingVO<TaskPagingShowDTO> paging(PagingDTO<TaskPagingDTO> dto);

    Boolean save(ProjectTaskDTO dto);

    List<Map<String, Object>> getTaskListByProductId(String productId );

    Boolean removeTask(String id);



    List<TaskConductDTO> getTaskConductList(String productId);

    TaskConductDTO getTaskConduct(String productId);

    List<TaskExcelDTO> getExportTask(List<String> productIds);


    ProjectTaskDetailsDTO getTaskDetails(String taskId);

    /**
     * 修改任务
     * @author yl
     * @date 2023-03-13 9:55
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateTask(ProjectTaskDTO dto);

    ProductTaskCountDTO getProductTaskCount(ProductTaskCountShowDTO showDTO, Date date);

    Boolean updateBaseTask(UpdateTaskDTO dto);


    ProjectTaskVO taskDetails(String taskId);

    List<ProjectTaskEntity> getByTaskIds(List<String> taskIds);

    boolean updateTaskState(List<String> taskIds, Integer state, LocalDateTime realityStart, LocalDateTime realityEnd);


    int countUndoneByTaskIds(List<Integer> excludeStatusList , List<String> preTaskIds);

    void checkSonTaskFinish(List<String> noProcessTaskIds,String productId);
    void checkSonTaskFinish(List<String> noProcessTaskIds,List<ProjectTaskEntity> taskList);

    Boolean startTask(OperateBaseTaskDTO dto);

    Boolean publishTask(OperateBaseTaskDTO dto);

    Boolean cancelPublishTask(OperateBaseTaskDTO dto);

    Boolean closeTask(OperateBaseTaskDTO dto);

    Boolean finishTask(OperateBaseTaskDTO dto);

    Boolean approvalPass(TaskOperateDTO dto);

    Boolean approvalReject(TaskOperateDTO dto);


    List<TaskProcessNodeDTO>  findTaskProcess(String taskId);

    void approvalTaskPass(String processId);

    Integer getTaskProperty(ProjectTaskEntity taskEntity);



    void checkTaskFinish(List<ProjectTaskEntity> list);

    List<TaskGroupResultDTO> getGroupCondition(TaskGroupParamDTO dto);

    PagingVO<TaskPagingShowDTO> expertPaging(PagingDTO<TaskSearchParamDTO> searchParamDTO);

    List<Map<String, Object>> operateMoreList(String taskId);

    List<ProjectTaskEntity> getExpireTaskList(Date nowDay, int i);

    Boolean restartTask(OperateBaseTaskDTO dto);

    PagingVO<TaskPagingShowDTO> assignToMePaging(PagingDTO<TaskSearchParamDTO> searchParamDTO);

    PagingVO<TaskPagingShowDTO> myCreatePaging(PagingDTO<TaskSearchParamDTO> searchParamDTO);

    void taskFinishSku(TaskFinishSkuDTO dto);

    /**
     * 分配给我
     * @author yl
     * @date 2022-11-29 16:23
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.TaskGroupResultDTO>
     */
    List<TaskGroupResultDTO> getGroupAssignToMeCondition(TaskGroupParamDTO dto);

    /**
     * 我创造的分组条件
     * @author yl
     * @date 2022-11-29 16:37
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.TaskGroupResultDTO>
     */
    List<TaskGroupResultDTO> groupMyCreateConditionList(TaskGroupParamDTO dto);

    
    /**
     * 交付文档 填写 是否更改了sku
     * @author yl
     * @date 2022-11-30 12:31
     * @param dto
     * @return void
     */
    void skuChangeResult(StateDTO dto);

    /**
     * @description: 根据产品id查询任务
     * @author Will
     * @date: 2023/1/10 10:54
     * @param productId
     * @return List<ProjectTaskEntity>
     */
    List<ProjectTaskEntity> listByProductId(String productId);

    List<ProjectTaskEntity> listByProductIds(List<String> productIds);

    /**
     * 分配给我 待审核
     * @author yl
     * @date 2023-01-10 15:33
     * @param searchParamDTO
     * @return com.erp.common.vo.PagingVO<java.util.List<com.erp.model.plm.dto.TaskPagingShowDTO>>
     */
    PagingVO<TaskPagingShowDTO> assignToMeWaitAuditPaging(PagingDTO<TaskSearchParamDTO> searchParamDTO);
    /**
     * @description: 查询各个分类任务的数量
     * @author Will
     * @date: 2023/1/30 16:05
     * @param dto
     * @return List<ProductTaskCategoryCountDTO>
     */
    List<ProductTaskCategoryCountDTO> listProductTaskCategoryCount(TaskPagingDTO dto);

    /**
     * @description: 飞书提醒
     * @author Will
     * @date: 2023/2/1 14:14
     * @param dto
     */
    void flyingBookReminder(FlyingBookReminderDTO dto);

    
    /**
     * 更改任务排期状态
     * @author yl
     * @date 2023-02-09 14:16
     * @param productId
     * @param taskIdList
     * @return void
     */
    void updateScheduleStatus(String productId, List<String> taskIdList,String status,String scheduleType);

    /**
     * 获取产品名称及任务数
     * @author yl
     * @date 2023-02-09 17:14
     * @param productId
     * @return java.util.Map<java.lang.String,java.lang.Integer>
     */
    Map<String, Object> getProductMapByProductId(String productId);

    /**
     * 获取计划的任务 根据任务id
     * @author yl
     * @date 2023-02-09 19:51
     * @param productId
     * @param taskIds
     * @return java.util.List<com.erp.model.plm.vo.ScheduleTaskVO>
     */
    List<ScheduleTaskVO> getScheduleTaskByTaskIds(String productId, List<String> taskIds);

    ScheduleTaskExportExcelVO getExport(String productId, String taskId);

    
    /**
     * 批量更新任务字段
     * @author yl
     * @date 2023-02-10 16:37
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean batchUpdate(BatchScheduleTaskDTO dto);

    
    /**
     * 当变更通过后 更改负责人 和时间
     * @author yl
     * @date 2023-02-11 16:42
     * @param taskList
     * @param status
     * @return void
     */
    void updateScheduleTask(List<ProjectPlanTaskEntity> taskList, String status,LoginUser loginUser,String productId);

    /**
     * 如果是初始排期  任务审核通过后
     * 就要发布
     * @author yl
     * @date 2023-02-17 10:57
     * @param productId
     * @param taskIdList
     * @param scheduleStatus
     * @return void
     */
    void initialScheduleTaskPass(LoginUser loginUser,String productId, List<String> taskIdList, String scheduleStatus);

    /**
     * 根据任务名 和产品id 获取到对应的人
     * @author yl
     * @date 2023-02-17 15:14
     * @param productId
     * @param taskName
     * @return com.erp.model.plm.entity.ProjectTaskEntity
     */
    ProjectTaskEntity getbyName(String productId, String taskName);
    /**
     * @description: 批量更新任务阶段名称
     * @author Will
     * @date: 2023/2/20 20:52
     * @param taskEntity
     */
    void updatePhase(ProjectTaskEntity taskEntity);

    /**
     * 根据任务id查询列表
     * @param preTaskIds
     * @return
     */
    List<ProjectTaskEntity> listByTaskIds(List<String> preTaskIds);

    /**
     * 根据任务名称查询任务
     * @param productId
     * @param name
     * @return void
     * @author yl
     * @date 2022-09-22 16:36
     */
    ProjectTaskEntity getTaskByName(String productId, String name);

    /**
     * 批量删除任务
     * @Author Luo_WG
     * @Date 2023/3/29 18:00
     * @param ids ids
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean removeBatch(List<String> ids);

    List<ProjectTaskEntity> listByTaskNames(String productId, List<String> taskNameList);



    /**
     * 根据任务ids 获取到任务信息
     * @param taskIdList
     * @return
     */
    List<ProductTask.TaskInfoDTO> listTaskInfo(List<String> taskIdList);


    /**
     * 撤销流程
     * @author yl
     * @date 2023-06-25 17:11
     * @param taaskIdList
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> taaskIdList);

    /**
     * 获取任务审核情况
     * @author yl
     * @date 2023-07-03 14:29
     * @param taskId
     * @return java.util.List<com.erp.model.plm.dto.TaskProcessNodeDTO>
     */
    List<ApproveNodeRecordVO> listTaskAudit(String taskId);

    /**
     * @description: 更新负责人名称
     * @author Will
     * @date: 2023/10/19 11:08
     * @param sysUserInfoDTO
     */
    void updateProjectTaskChargeName(SysUserInfoDTO sysUserInfoDTO);

    /**
     * 任务高级查询
     */
    PagingVO<ProjectTaskDTO.SimpleViewDTO> pagingByAdvanceQuery(PagingDTO<ProjectTaskDTO.PagingParamDTO> dto);

    List<ProjectTaskDTO.SimpleViewDTO> listSimpleViewByIds(List<String> taskIds);
}
