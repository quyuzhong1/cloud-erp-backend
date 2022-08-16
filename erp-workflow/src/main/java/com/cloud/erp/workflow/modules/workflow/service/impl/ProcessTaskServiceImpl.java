package com.cloud.erp.workflow.modules.workflow.service.impl;

import com.cloud.erp.workflow.modules.workflow.dto.ActivityDTO;
import com.cloud.erp.workflow.modules.workflow.dto.ApproveProcessPassDTO;
import com.cloud.erp.workflow.modules.workflow.dto.ProcessBaseDTO;
import com.cloud.erp.workflow.modules.workflow.service.ActHistoryActivityService;
import com.cloud.erp.workflow.modules.workflow.service.ProcessTaskService;
import com.cloud.erp.workflow.modules.workflow.vo.TaskVO;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

    /**
     * 查询我的任务待办
     *
     * @param userId
     * @return 任务列表
     * @author yl
     * @date 2022-08-10 16:06
     */
    @Override
    public List<TaskVO> queryMyToDo(String userId) {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).list();
        List<TaskVO> resultList = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            TaskVO vo = new TaskVO();
            vo.setAssignee(task.getAssignee());
            vo.setProcessInstanceId(task.getProcessInstanceId());
            vo.setTaskId(task.getId());
            vo.setNodeId(task.getTaskDefinitionKey());
            resultList.add(vo);
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
    public void taskPass(ApproveProcessPassDTO dto) {
        String processInstanceId=dto.getProcessInstanceId();
        Map<String, Object> map = dto.getParameterMap();
        String taskId = dto.getTaskId();
        Task task = taskService.createTaskQuery().
                taskId(taskId).singleResult();
        String nowActivityId = task.getTaskDefinitionKey();
        //添加审批意见
        taskService.createComment(taskId, dto.getProcessInstanceId(), dto.getComment());
        if (map != null&&!map.isEmpty()) {
            taskService.complete(taskId, map);
        } else {
            taskService.complete(taskId);
        }

        ActivityDTO activityDTO=new ActivityDTO();
        activityDTO.setNowActivityId(nowActivityId);
        activityDTO.setProcessInstanceId(processInstanceId);
        //审批通过后 需要保存流程节点信息
        actHistoryActivityService.saveActivity(activityDTO);

    }


    /**
     * 我的已办  任务历史
     *
     * @param userId
     * @return java.util.List<org.camunda.bpm.engine.history.HistoricTaskInstance>
     * @author yl
     * @date 2022-08-10 17:26
     */
    @Override
    public List<HistoricTaskInstance> historicTaskInstances(String userId) {
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().
                taskAssignee(userId).finished().list();
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
    public TaskVO queryTaskInfo(ProcessBaseDTO dto){
        Task task = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
        TaskVO vo = new TaskVO();
        if (!Objects.isNull(task)) {
            vo.setAssignee(task.getAssignee());
            vo.setProcessInstanceId(task.getProcessInstanceId());
            vo.setTaskId(task.getId());
            vo.setNodeId(task.getTaskDefinitionKey());
            List<Comment> taskComments=taskService.getProcessInstanceComments(dto.getProcessInstanceId());
            List<String> list=new ArrayList<>(taskComments.size());
            for(Comment item:taskComments){
                list.add(item.getFullMessage());
            }
            vo.setComments(list);

        }

        return vo;
    }
}
