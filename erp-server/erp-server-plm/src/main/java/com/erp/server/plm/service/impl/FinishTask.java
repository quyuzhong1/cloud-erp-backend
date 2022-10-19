package com.erp.server.plm.service.impl;

import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.entity.BusinessProcessEntity;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.enums.TaskTypeEnum;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 完成任务
 *
 * @Classname FinishTask
 * @Description TODO
 * @Date 2022-10-18 18:43
 * @Created by yl
 */
public class FinishTask implements TaskOperateStrategy {

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private PreTaskService preTaskService;

    @Autowired
    private BusinessProcessService businessProcessService;

    @Autowired
    private ProjectMembersService projectMembersService;

    @Autowired
    private WorkflowFeign workflowFeign;


    @Override
    @Transactional
    public Boolean updateTaskState(List<String> taskIds, Integer state, String userId) {
        //获取所有的任务列表
        List<ProjectTaskEntity> list = projectTaskService.getByTaskIds(taskIds);
        //评审任务code
        Integer reviewTaskCode = TaskTypeEnum.REVIEW_TASK.getCode();
        /**
         * 评审任务
         */
        List<ProjectTaskEntity> reviewList = list.stream().filter(t -> reviewTaskCode.equals(t.getType())).collect(Collectors.toList());
        //表示有 评审任务 则要 踢出去
        if (CollectionUtils.isNotEmpty(reviewList) && reviewList.size() > 0) {
            throw new ServiceException(ApiError.ERROR_95033);
        }

        List<String> allTaskIds = list.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        //检查前置任务是否完成
        preTaskService.checkPreTaskFinish(allTaskIds);
        //检查子任务是否有完成
        projectTaskService.checkSonTaskFinish(allTaskIds);

        //一般任务code
        Integer generalTaskCode = TaskTypeEnum.GENERAL_TASK.getCode();


        //一般任务 列表  都是将任务状态改为进行中
        List<ProjectTaskEntity> generalTasks = list.stream().filter(t -> generalTaskCode.equals(t.getType())).collect(Collectors.toList());

        //一般任务 没有流程
        List<ProjectTaskEntity> noProcessList = generalTasks.stream().filter(p -> StringUtils.isBlank(p.getProcessId())).collect(Collectors.toList());

        //一般任务 有流程
        List<ProjectTaskEntity> processList = generalTasks.stream().filter(p -> StringUtils.isBlank(p.getBusinessProcessId())).collect(Collectors.toList());

        //进行中
        Integer ingCode = TaskStateEnum.ING.getCode();


        //找出 没有流程中 不是进行中的任务 如果有表示 不能完成任务
        long noProcess = list.stream().filter(t -> t.getStatus() != ingCode).count();
        if (noProcess > 0) {
            throw new ServiceException(ApiError.ERROR_95034);
        }

        //完成待审核
        Integer finishWaitConfirmCode = TaskStateEnum.FINISH_WAIT_CONFIRM.getCode();
        /**
         *
         *   到了这一步 那么可以完成任务了
         *   没有流程审核的任务  更改状态为已完成
         *
         */
        List<String> noProcessTaskIds = noProcessList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        Date nowDate = new Date();
        projectTaskService.updateTaskState(noProcessTaskIds, TaskStateEnum.FINISH.getCode(), nowDate, null);
        //获取到所有流程的信息
        List<BusinessProcessEntity> businessProcessList = businessProcessService.list();
        //获取到所有到负责人的成员信息
        List<ProjectMembersEntity> projectMembersList = projectMembersService.getChargeList();
        //有审核流程的 要启动流程了
        for (ProjectTaskEntity processTask : processList) {
            String businessProcessId = processTask.getBusinessProcessId();
            if (StringUtils.isNotBlank(businessProcessId)) {
                BusinessProcessEntity processEntity = businessProcessList.stream().filter(b -> businessProcessId.equals(b.getId())).findFirst().orElse(null);
                if (!Objects.isNull(processEntity)) {
                    List<String> membersIds = projectMembersList.stream().filter(p -> p.getProductId().equals(processTask.getProductId())).map(ProjectMembersEntity::getMemberId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(membersIds)) {
                        StartProcessDTO startProcess = new StartProcessDTO();
                        startProcess.setBusinessKey(processEntity.getBusinessKey());
                        startProcess.setProcessDefinitionKey(processEntity.getProcessDefinitionKey());
                        startProcess.setUserId(userId);
                        Map<String, Object> parameterMap = new HashMap<>();

                        parameterMap.put("memberChargeList", membersIds);
                        startProcess.setParameterMap(parameterMap);
                        //启动流程
                        ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
                        String processId = processResult.getProcessId();
                        //当流程id不为空的时候
                        if (StringUtils.isNotBlank(processId)) {
                            processTask.setProcessId(processId);
                            processTask.setStatus(finishWaitConfirmCode);
                            processTask.setRealityStartTime(nowDate);
                            projectTaskService.updateById(processTask);
                        }
                    }
                }
            }
        }
        return true;
    }
}
