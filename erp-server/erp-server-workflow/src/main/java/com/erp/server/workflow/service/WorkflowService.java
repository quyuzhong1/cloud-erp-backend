package com.erp.server.workflow.service;


import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;

import java.util.List;

/**
 * @Classname WorkflowService
 * @Description TODO
 * @Date 2022-08-16 17:24
 * @Created by yl
 */
public interface WorkflowService {

    //撤销流程
    void withDrawProcess(ProcessBaseDTO dto);

    void fetchBackProcess(ApproveProcessDTO dto);

    //驳回到起点
    void rejectOriginProcess(ApproveProcessDTO dto);

    //驳回到上一级
    void rejectGoBackProcess(ApproveProcessDTO dto);

    //驳回到某一个节点
    //  void rejectNodeProcess();


    //启动一个流程
    ProcessNodeDTO startProcess(StartProcessDTO dto);

    List<AuditorHandleDTO> queryApproveRecord(ProcessBaseDTO dto);


    void terminateProcess(String  processInstanceId);

    List<ApproveNodeRecordVO> queryApproveRecordById(String id);

    
    /**
     * 根据业务表 撤销流程
     * @author yl
     * @date 2023-02-20 18:54
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean withDrawProcessByBusinessTable(WithDrawProcessBusinessDTO dto);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/3/20 10:00
     * @param ids

     */
    void cancelProcess(List<String> ids);
}
