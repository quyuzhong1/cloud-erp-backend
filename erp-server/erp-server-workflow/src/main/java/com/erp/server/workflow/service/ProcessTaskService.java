package com.erp.server.workflow.service;


import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.model.workflow.vo.MyToDoTaskVO;
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
    /**
     * @description: 根据审核任务id查看任务
     * @author Will
     * @date: 2022/11/28 15:20
     * @param taskId
     * @return List<TaskShowDTO>
     */
    List<TaskShowDTO> queryMyToDoByTaskId(String taskId);

   /**
    * @description: 查询流程下所有审核记录
    * @author Will
    * @date: 2023/1/30 11:36
    * @param processId
    * @return List<ApproveRecordShowDTO>
    */
    List<AuditorHandleDTO> getHistoryTaskByProcessId(String processId);

    
    /**
     * 根据用户id
     * @author yl
     * @date 2023-01-31 11:03
     * @param userId
     * @return java.util.List<com.erp.model.workflow.vo.MyToDoTaskVO>
     */
    List<MyToDoTaskVO> getMyToDoTasks(String userId);

    
    /**
     * 审核不通过
     * @author yl
     * @date 2023-02-11 16:13
     * @param dto
     * @return com.erp.model.workflow.dto.ProcessNodeDTO
     */
    ProcessNodeDTO taskNoPass(ApproveProcessDTO dto);

    /**
     * 根据业务表id 获取审核人 操作记录
     * @author yl
     * @date 2023-02-13 17:00
     * @param businessTableId
     * @return java.util.List<com.erp.model.workflow.vo.ApproveNodeRecordVO>
     */
    List<ApproveNodeRecordVO> getHistoryTaskByBusinessTableId(String businessTableId);
}
