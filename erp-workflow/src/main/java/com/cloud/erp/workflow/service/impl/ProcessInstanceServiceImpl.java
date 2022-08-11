package com.cloud.erp.workflow.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.cloud.erp.workflow.dto.ApproveProcessRejectDTO;
import com.cloud.erp.workflow.dto.StartProcessDTO;
import com.cloud.erp.workflow.service.ProcessInstanceService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Classname ProcessInstanceServiceImpl
 * @Description TODO
 * @Date 2022-08-10 15:34
 * @Created by yl
 */
@Service
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    @Autowired
    private IdentityService identityService;


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    /**
     * 开启一个流程
     *
     * @param dto processDefinitionKey 程序的id对应的是xml 里面的值
     * @param dto businessKey          业务的key
     * @param dto initiator            发起人
     * @return void
     * @author yl
     * @date 2022-08-10 15:35
     */
    @Override
    public void startProcessInstanceByKey(StartProcessDTO dto) {
        //对应的参数值
        Map<String, Object> map = dto.getMap();
        //流程发起人
        identityService.setAuthenticatedUserId(dto.getInitiator());
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(dto.getProcessDefinitionKey(), dto.getBusinessKey(), map);
        if (!Objects.isNull(processInstance)) {
            processInstance.getProcessInstanceId();
        }
    }

    /**
     * 驳回流程 也就是让流程从某个节点重新开始
     *
     * @param dto processInstanceId 进程实例id
     * @param dto nodeId
     * @return void
     * @author yl
     * @date 2022-08-11 10:25
     */

    @Override
    public void reject(ApproveProcessRejectDTO dto) {
        String processInstanceId = dto.getProcessInstanceId();
        //添加意见
        //   taskService.createComment(dto.getTaskId(), processInstanceId, dto.getComment());
        //获取当前环节实例
        ActivityInstance activity = runtimeService.getActivityInstance(processInstanceId);
        List<HistoricActivityInstance> historyList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityType("userTask")
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .desc()
                .list();
        System.out.println(JSONObject.toJSONString(historyList));
        HistoricActivityInstance last = historyList.get(0);
        System.out.println(last);
        String activityId = last.getActivityId();
//        runtimeService.createProcessInstanceModification(processInstanceId)
//                .cancelActivityInstance(activity.getId())
//                .setAnnotation("驳回")
//                .startBeforeActivity(activityId)
//                .execute();
    }


    @Override
    public void rejectBack(ApproveProcessRejectDTO dto) {
        // 新建一个有序不重复集合
        LinkedHashSet linkedHashSet = new LinkedHashSet();

        String processInstanceId = dto.getProcessInstanceId();

        List<HistoricTaskInstance> historyList = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
        historyList.stream().forEach(h -> {
            linkedHashSet.add(h.getTaskDefinitionKey());
        });
        // 遍历到当前的任务节点后,取上一次遍历的值
        Iterator<String> iterator = linkedHashSet.iterator();
        while (iterator.hasNext()) {
            String nowTask = iterator.next();

            System.out.println(nowTask);
        }

    }
}
