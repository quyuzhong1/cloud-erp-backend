package com.erp.server.workflow.service;


import com.erp.model.workflow.dto.ApproveProcessDTO;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.QueryProcessDTO;
import com.erp.model.workflow.dto.TaskShowDTO;
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
    List<TaskShowDTO>  queryMyToDo(String userId);

    ProcessNodeDTO taskPass(ApproveProcessDTO dto);


    //我的已办
    List<HistoricTaskInstance> historicTaskInstances(QueryProcessDTO dto);

    void removeTask(String taskId);

    TaskShowDTO queryTaskInfo(ApproveProcessDTO dto);

}
