package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.BaseStatusEnum;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.HandleTaskScheduleDTO;
import com.erp.model.plm.entity.ProjectPlanEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.vo.ScheduleTaskVO;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.mapper.ProjectPlanMapper;
import com.erp.server.plm.service.ProjectPlanService;
import com.erp.server.plm.service.ProjectPlanTaskService;
import com.erp.server.plm.service.ProjectTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

        if(saveResult){
            projectPlanTaskService.savePlanTask(id,dto.getProductId(),taskList);
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
        List<ScheduleTaskVO> taskList = projectPlanTaskService.getScheduleTaskList(productId, BaseStatusEnum.WAIT_SUBMIT.getStatus());
        List<String> scheduleTaskId = taskList.stream().map(ScheduleTaskVO::getTaskId).collect(Collectors.toList());

        long count = taskIds.stream().filter(t -> scheduleTaskId.contains(t)).count();
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
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 17:35
     */
    @Override
    public Boolean cancelSchedule(HandleTaskScheduleDTO dto) {
        return null;
    }

    /**
     * 重启提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-02-03 17:35
     */
    @Override
    public Boolean restartSchedule(HandleTaskScheduleDTO dto) {
        return null;
    }


    /**
     * 变更排期
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean changeSchedule(HandleTaskScheduleDTO dto) {
        return null;
    }


}
