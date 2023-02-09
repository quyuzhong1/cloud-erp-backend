package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.BaseStatusEnum;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.ChangeTaskScheduleDTO;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.entity.ProjectPlanEntity;
import com.erp.model.plm.entity.ProjectPlanTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.vo.ScheduleTaskVO;
import com.erp.server.plm.constant.ProjectPlanConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.ProjectPlanMapper;
import com.erp.server.plm.service.ProjectPlanService;
import com.erp.server.plm.service.ProjectPlanTaskService;
import com.erp.server.plm.service.ProjectTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 项目计划表(ProjectPlan)表服务实现类
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
@Service
public class ProjectPlanServiceImpl extends ServiceImpl<ProjectPlanMapper, ProjectPlanEntity> implements ProjectPlanService {

    @Resource
    private ProjectTaskService taskService;

    @Resource
    private ProjectPlanTaskService projectPlanTaskService;


    /**
     * 提交项目计划
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 17:08
     */
    @Override
    public Boolean submitSchedule(HandleTaskScheduleDTO dto) {
        List<String> taskIds = dto.getTaskIdList();
        List<ProjectTaskEntity> taskList = taskService.getByTaskIds(taskIds);
        String productId = dto.getProductId();
        if (CollectionUtils.isNotEmpty(taskIds)) {
            checkTaskTime(taskList);
            checkTaskStatus(taskIds, productId);
        }
        long approvalTaskCount = taskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).count();
        long projectTaskCount = taskList.stream().filter(t -> TaskConstant.PROJECT_TASK.equals(t.getProperty())).count();
        String phase = "all";
        if (approvalTaskCount > 0 && projectTaskCount == 0) {
            phase = "projectApproval";
        }
        if (approvalTaskCount == 0 && projectTaskCount > 0) {
            phase = "project";
        }
        ProjectPlanEntity projectPlan = new ProjectPlanEntity();
        String id = IdWorker.getIdStr();
        projectPlan.setProductId(productId);
        projectPlan.setType("initial");
        projectPlan.setTaskQuantity(dto.getTaskIdList().size());
        projectPlan.setPhase(phase);
        projectPlan.setId(id);
        Boolean saveResult = this.save(projectPlan);
        if (saveResult) {
            projectPlanTaskService.savePlanTask(id, dto.getProductId(), taskList);
            //发起流程啊
        }
        return saveResult;
    }


    /**
     * 检查任务状态
     *
     * @param taskIds
     * @return void
     * @author yl
     * @date 2023-02-08 17:37
     */
    private void checkTaskStatus(List<String> taskIds, String productId) {
        List<ScheduleTaskVO> taskList = projectPlanTaskService.getByTaskIds(productId, taskIds);
        List<String> statusList = new ArrayList<>(2);
        String waitSubmit = BaseStatusEnum.WAIT_SUBMIT.getStatus();
        String cancel = BaseStatusEnum.CANCEL.getStatus();
        statusList.add(waitSubmit);
        statusList.add(cancel);
        long count = taskList.stream().filter(t -> !statusList.contains(t.getScheduleStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95116);
        }
    }


    /**
     * 检查任务计划时间是否设置
     *
     * @param taskList
     * @return void
     * @author yl
     * @date 2023-02-08 17:31
     */
    public void checkTaskTime(List<ProjectTaskEntity> taskList) {
        long count = taskList.stream().
                filter(t -> Objects.isNull(t.getPlanEndTime()) || Objects.isNull(t.getPlanStartTime())).
                count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95010);
        }

    }

    /**
     * 取消排期
     *
     * @param id
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 17:35
     */
    @Override
    public Boolean cancelSchedule(String id) {
        ProjectPlanEntity plan = this.getById(id);
        if (Objects.isNull(plan)) {
            throw new ServiceException(ApiError.ERROR_95122);
        }
        String status = plan.getStatus();
        if (BaseStatusEnum.WAIT_AUDIT.getStatus().equals(status)) {
            throw new ServiceException(ApiError.ERROR_95121);
        }
        String cancelStatus = BaseStatusEnum.CANCEL.getStatus();
        plan.setStatus(cancelStatus);
        Boolean result = this.updateById(plan);
        if (result) {
            String productId = plan.getProductId();
            List<ProjectPlanTaskEntity> taskList = projectPlanTaskService.getByProjectPlanId(id);
            List<String> taskIds = taskList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
            //更改任务状态
            taskService.updateScheduleStatus(productId, taskIds, cancelStatus,plan.getType());
        }
        return result;
    }


    /**
     * 根据表id 获取实体
     *
     * @param projectPlanIds
     * @return java.util.List<com.erp.model.plm.entity.ProjectPlanEntity>
     * @author yl
     * @date 2023-02-09 11:15
     */
    @Override
    public List<ProjectPlanEntity> getByIds(List<String> projectPlanIds) {
        if (CollectionUtils.isNotEmpty(projectPlanIds)) {
            LambdaQueryWrapper<ProjectPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProjectPlanEntity::getId, projectPlanIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();

    }

    /**
     * 重启提交
     * 审核不通过重新提交
     *
     * @param id
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 17:35
     */
    @Override
    public Boolean restartSchedule(String id) {
        ProjectPlanEntity plan = this.getById(id);
        if (Objects.isNull(plan)) {
            throw new ServiceException(ApiError.ERROR_95122);
        }
        String status = plan.getStatus();
        if (BaseStatusEnum.AUDIT_NO_PASS.getStatus().equals(status)) {
            throw new ServiceException(ApiError.ERROR_95119);
        }
        String waitAuditStatus = BaseStatusEnum.WAIT_AUDIT.getStatus();
        plan.setStatus(waitAuditStatus);
        Boolean result = this.updateById(plan);
        if (result) {
            //这里要发起流程

            String productId = plan.getProductId();
            List<ProjectPlanTaskEntity> taskList = projectPlanTaskService.getByProjectPlanId(id);
            List<String> taskIds = taskList.stream().map(ProjectPlanTaskEntity::getTaskId).collect(Collectors.toList());
            //更改任务状态
            taskService.updateScheduleStatus(productId, taskIds, waitAuditStatus,plan.getType());
        }

        return result;

    }


    /**
     * 变更排期
     * 添加一个变更流程 保存数据
     *
     * @param
     * @return
     */
    @Override
    public Boolean changeSchedule(List<ChangeTaskScheduleDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<String> taskIds = list.stream().map(ChangeTaskScheduleDTO::getTaskId).collect(Collectors.toList());
        List<ProjectTaskEntity> taskList = taskService.getByTaskIds(taskIds);
        String productId = list.get(0).getProductId();
        long approvalTaskCount = taskList.stream().filter(t -> TaskConstant.APPROVAL_TASK.equals(t.getProperty())).count();
        long projectTaskCount = taskList.stream().filter(t -> TaskConstant.PROJECT_TASK.equals(t.getProperty())).count();
        String phase = "all";
        if (approvalTaskCount > 0 && projectTaskCount == 0) {
            phase = "projectApproval";
        }
        if (approvalTaskCount == 0 && projectTaskCount > 0) {
            phase = "project";
        }
        ProjectPlanEntity projectPlan = new ProjectPlanEntity();
        String id = IdWorker.getIdStr();
        projectPlan.setProductId(productId);
        String type = ProjectPlanConstant.PROJECT_PLAN_CHANGE;
        projectPlan.setType(type);
        projectPlan.setTaskQuantity(taskIds.size());
        projectPlan.setPhase(phase);
        projectPlan.setId(id);
        Boolean saveResult = this.save(projectPlan);
        if (saveResult) {
            projectPlanTaskService.saveChangePlanTask(id, productId, taskList, list);

            //发起流程
            //更改任务状态
            taskService.updateScheduleStatus(productId, taskIds, BaseStatusEnum.WAIT_AUDIT.getStatus(),type);
        }
        return saveResult;


    }


}
