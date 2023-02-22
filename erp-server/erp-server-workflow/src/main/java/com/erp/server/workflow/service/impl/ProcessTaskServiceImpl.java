package com.erp.server.workflow.service.impl;


import com.alibaba.excel.util.DateUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.BaseStatusEnum;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.entity.ActHistoryActivityEntity;
import com.erp.model.workflow.entity.WorkflowBusinessProcessEntity;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.workflow.vo.MyToDoTaskVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.service.ActHistoryActivityService;
import com.erp.server.workflow.service.ProcessTaskService;
import com.erp.server.workflow.service.WorkflowBusinessProcessService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private WorkflowBusinessProcessService businessProcessService;

    @Autowired
    private SysUserFeign sysUserFeign;

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
    @Transactional
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
        activityDTO.setAuditStatus(BaseStatusEnum.AUDIT_PASS.getStatus());
        //审批通过后 需要保存流程节点信息
        actHistoryActivityService.saveActivity(activityDTO);
        return new ProcessNodeDTO();

    }

    /**
     * 审批 不通过任务
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-08-10 16:57
     */
    @Override
    @Transactional
    public ProcessNodeDTO taskNoPass(ApproveProcessDTO dto) {
        String processInstanceId = dto.getProcessInstanceId();
        String taskId = dto.getTaskId();
        Task task = taskService.createTaskQuery().
                taskId(taskId).singleResult();
        if (Objects.isNull(task)) {
            return null;
        }
        if (StringUtils.isBlank(dto.getComment())) {
            dto.setComment("审核不通过");
        }
        //添加审批意见
        taskService.createComment(taskId, processInstanceId, dto.getComment());
        Map<String, Object> map = dto.getParameterMap();
        if (map != null && !map.isEmpty()) {
            taskService.complete(taskId, map);
        } else {
            taskService.complete(taskId);
        }

        String nowActivityId = task.getTaskDefinitionKey();
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setNowActivityId(nowActivityId);
        activityDTO.setProcessInstanceId(processInstanceId);
        activityDTO.setAuditStatus(BaseStatusEnum.AUDIT_NO_PASS.getStatus());
        //审批通过后 需要保存流程节点信息
        actHistoryActivityService.saveActivity(activityDTO);
        return new ProcessNodeDTO();

    }


    /**
     * 根据业务表id 获取审核人 操作记录
     *
     * @param businessTableId
     * @return java.util.List<com.erp.model.workflow.vo.ApproveNodeRecordVO>
     * @author yl
     * @date 2023-02-13 17:00
     */
    @Override
    public List<ApproveNodeRecordVO> getHistoryTaskByBusinessTableId(String businessTableId) {
        List<WorkflowBusinessProcessDTO> list = businessProcessService.getProcessByTables(Arrays.asList(businessTableId));
        if (CollectionUtils.isNotEmpty(list)) {
            WorkflowBusinessProcessDTO dto = list.get(0);
            List<AuditorHandleDTO> auditorHandleList = this.getHistoryTaskByProcessId(dto.getProcessId());
            List<FindUserDTO> userList = sysUserFeign.getUserList();
            for (AuditorHandleDTO item : auditorHandleList) {
                FindUserDTO findUser = userList.stream().filter(u -> item.getHandleUserId().equals(u.getUserId())).findFirst().orElse(null);
                if (findUser != null) {
                    item.setHandleUserName(findUser.getUserName());
                } else {
                    item.setHandleUserName("");
                }
            }

            LinkedHashMap<String, List<AuditorHandleDTO>> map = auditorHandleList.stream().
                    collect(Collectors.groupingBy(AuditorHandleDTO::getTaskDefinitionKey, LinkedHashMap::new, Collectors.toList()));

            List<ApproveNodeRecordVO> resultList = new ArrayList<>(map.size());

            for (Map.Entry<String, List<AuditorHandleDTO>> item : map.entrySet()) {
                ApproveNodeRecordVO vo = new ApproveNodeRecordVO();
                vo.setAuditorHandleList(item.getValue());
                resultList.add(vo);
            }
            return resultList;
        }
        return new ArrayList<>();
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

    @Override
    public List<TaskShowDTO> queryMyToDoByTaskId(String taskId) {
        List<TaskShowDTO> resultList = new ArrayList<>();
        if (StringUtils.isNotBlank(taskId)) {
            List<Task> tasks = taskService.createTaskQuery().taskId(taskId).list();
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

    @Override
    public List<AuditorHandleDTO> getHistoryTaskByProcessId(String processId) {
        List<HistoricTaskInstance> list = historyService // 历史相关Service
                .createHistoricTaskInstanceQuery() // 创建历史任务实例查询
                .processInstanceId(processId) // 用流程实例id查询
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
        List<AuditorHandleDTO> resultList = new ArrayList<>();
        AuditorHandleDTO auditorHandleDTO = null;
        String approvalSuggestion = "";
        List<Comment> commentList = null;
        List<ActHistoryActivityEntity> historyActivityList = actHistoryActivityService.getByProcessId(processId);
        for (HistoricTaskInstance item : list) {
            auditorHandleDTO = new AuditorHandleDTO();
            commentList = taskService.getTaskComments(item.getId());
            if (commentList != null && !commentList.isEmpty()) {
                approvalSuggestion = commentList.get(0).getFullMessage();
            } else {
                approvalSuggestion = "";
            }

            //这个是处理时间
            Date endTime = item.getEndTime();
            //表示没有处理
            if (endTime == null) {
                auditorHandleDTO.setHandContent("待审核");
            } else {
                //表示有处理
                //表示有活动节点
                ActHistoryActivityEntity entity = historyActivityList.stream().filter(a -> a.getActivityId().
                        equals(item.getTaskDefinitionKey())).findFirst().orElse(null);
                if (entity != null) {
                    auditorHandleDTO.setHandContent(BaseStatusEnum.getName(entity.getAuditStatus()));
                } else {
                    auditorHandleDTO.setHandContent("completed".equals(item.getDeleteReason()) ? "审核通过" : "待审核");
                }
            }

            auditorHandleDTO.setActivityName(item.getName());
            auditorHandleDTO.setStartTime(DateUtils.format(item.getStartTime(), DateUtils.DATE_FORMAT_19));
            if("待审核".equals(auditorHandleDTO.getHandContent())){
                auditorHandleDTO.setStartTime("");
            }
            auditorHandleDTO.setEndTime(DateUtils.format(item.getEndTime(), DateUtils.DATE_FORMAT_19));
            auditorHandleDTO.setHandleUserId(item.getAssignee());
            auditorHandleDTO.setTaskDefinitionKey(item.getTaskDefinitionKey());
            auditorHandleDTO.setComment(approvalSuggestion);
            resultList.add(auditorHandleDTO);
        }
        return resultList;
    }

    /**
     * 根据用户id 获取用户待办的任务
     *
     * @param userId
     * @return java.util.List<com.erp.model.workflow.vo.MyToDoTaskVO>
     * @author yl
     * @date 2023-01-31 11:06
     */
    @Override
    public List<MyToDoTaskVO> getMyToDoTasks(String userId) {
        List<MyToDoTaskVO> resultList = new ArrayList<>();
        if (StringUtils.isNotBlank(userId)) {
            List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).list();
            for (Task task : tasks) {
                MyToDoTaskVO vo = new MyToDoTaskVO();
                vo.setAssignee(task.getAssignee());
                vo.setProcessInstanceId(task.getProcessInstanceId());
                vo.setTaskId(task.getId());
                vo.setNodeId(task.getTaskDefinitionKey());
                resultList.add(vo);
            }
        }
        List<String> processIds = resultList.stream().map(MyToDoTaskVO::getProcessInstanceId).collect(Collectors.toList());
        List<WorkflowBusinessProcessEntity> businessProcessList = businessProcessService.getByProcessIds(processIds);
        for (MyToDoTaskVO item : resultList) {
            String processId = item.getProcessInstanceId();
            WorkflowBusinessProcessEntity businessProcess = businessProcessList.stream().
                    filter(b -> b.getProcessId().equals(processId)).
                    findFirst().orElse(null);
            if (businessProcess != null) {
                item.setBusinessTableId(businessProcess.getBusinessTableId());
            }

        }
        return resultList;
    }

}
