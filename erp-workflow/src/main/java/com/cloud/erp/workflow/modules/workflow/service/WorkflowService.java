package com.cloud.erp.workflow.modules.workflow.service;

import com.cloud.erp.workflow.modules.workflow.dto.*;
import com.cloud.erp.workflow.modules.workflow.vo.ApproveRecordVO;

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
   void startProcess(StartProcessDTO dto);


   //发布流程
   void deployDefinitionByResource(DeployProcessDTO dto);

   List<ApproveRecordVO> queryApproveRecord(ProcessBaseDTO dto);





}
