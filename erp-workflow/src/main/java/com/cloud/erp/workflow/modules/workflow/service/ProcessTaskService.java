package com.cloud.erp.workflow.modules.workflow.service;

import com.cloud.erp.workflow.modules.workflow.dto.ApproveProcessDTO;
import com.cloud.erp.workflow.modules.workflow.dto.ProcessBaseDTO;
import com.cloud.erp.workflow.modules.workflow.dto.QueryProcessDTO;
import com.cloud.erp.workflow.modules.workflow.vo.TaskVO;

import org.camunda.bpm.engine.history.HistoricTaskInstance;

import java.util.List;

/**
 * @Classname 任务服务
 * @Description TODO
 * @Date 2022-08-10 16:03
 * @Created by yl
 */
public interface ProcessTaskService {

    //我的待办
    List<TaskVO>  queryMyToDo(String userId);

    void taskPass(ApproveProcessDTO dto);


    //我的已办
    List<HistoricTaskInstance> historicTaskInstances(QueryProcessDTO  dto);

    void removeTask(String taskId);

    TaskVO queryTaskInfo(ApproveProcessDTO dto);

}
