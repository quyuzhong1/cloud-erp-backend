package com.erp.server.workflow.service.impl;


import com.erp.model.workflow.dto.*;
import com.erp.server.workflow.service.ActHistoryActivityService;
import com.erp.server.workflow.service.ProcessTaskService;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname 任务服务
 * @Description TODO
 * @Date 2022-08-10 16:04
 * @Created by yl
 */
@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {


    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ActHistoryActivityService actHistoryActivityService;


    @Autowired
    private RuntimeService runtimeService;

    /**
     * 查询我的任务待办
     *
     * @param userId
     * @return 任务列表
     * @author yl
     * @date 2022-08-10 16:06
     */
    @Override
    public List<TaskShowDTO> queryMyToDo(String userId) {
        List<TaskShowDTO> resultList = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {
            List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).list();
            for (Task task : tasks) {
                TaskShowDTO vo = new TaskShowDTO();
                vo.setAssignee(task.getAssignee());
                vo.setProcessInstanceId(task.getProcessInstanceId());
                vo.setTaskId(task.getId());
                vo.setNodeId(task.getTaskDefinitionKey());
                resultList.add(vo);
            }
        }
        return resultList;
    }

    /**
     * 审批通过任务
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-08-10 16:57
     */
    @Override
    public ProcessNodeDTO taskPass(ApproveProcessDTO dto) {
        String processInstanceId = dto.getProcessInstanceId();
        Map<String, Object> map = dto.getParameterMap();
        String taskId = dto.getTaskId();
        Task task = taskService.createTaskQuery().
                taskId(taskId).singleResult();
        if (Objects.isNull(task)) {
            return null;
        }
        String nowActivityId = task.getTaskDefinitionKey();
        //添加审批意见
        taskService.createComment(taskId, dto.getProcessInstanceId(), dto.getComment());
        if (map != null && !map.isEmpty()) {
            taskService.complete(taskId, map);
        } else {
            taskService.complete(taskId);
        }

        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setNowActivityId(nowActivityId);
        activityDTO.setProcessInstanceId(processInstanceId);
        //审批通过后 需要保存流程节点信息
        actHistoryActivityService.saveActivity(activityDTO);
        return new ProcessNodeDTO();
    }


    /**
     * 我的已办  任务历史
     *
     * @param dto userId
     * @return java.util.List<org.camunda.bpm.engine.history.HistoricTaskInstance>
     * @author yl
     * @date 2022-08-10 17:26
     */
    @Override
    public List<HistoricTaskInstance> historicTaskInstances(QueryProcessDTO dto) {
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().
                taskAssignee(dto.getUserId()).finished().list();
        return historicTaskInstances;
    }


    /**
     * 删除任务
     *
     * @param taskId
     * @return void
     * @author yl
     * @date 2022-08-11 9:58
     */
    @Override
    public void removeTask(String taskId) {
        taskService.deleteTask(taskId, true);
    }


    /**
     * 单个任务
     *
     * @param dto taskId
     * @return com.cloud.erp.workflow.vo.TaskVO
     * @author yl
     * @date 2022-08-11 12:17
     */
    @Override
    public TaskShowDTO queryTaskInfo(ApproveProcessDTO dto) {
        Task task = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
        TaskShowDTO vo = new TaskShowDTO();
        if (!Objects.isNull(task)) {
            vo.setAssignee(task.getAssignee());
            vo.setProcessInstanceId(task.getProcessInstanceId());
            vo.setTaskId(task.getId());
            vo.setNodeId(task.getTaskDefinitionKey());
            List<Comment> taskComments = taskService.getProcessInstanceComments(dto.getProcessInstanceId());
            List<String> list = new ArrayList<>(taskComments.size());
            for (Comment item : taskComments) {
                list.add(item.getFullMessage());
            }
            vo.setComments(list);

        }

        return vo;
    }


}
