package com.erp.rpc.workflow.handle;

import com.erp.model.workflow.dto.EndProcessDTO;

/**
 * 工作流监听基类
 * 所有需要监听流程的方法都需要继承此接口，同时实现流程结束approveEnd方法
 *
 * @Author Cloud
 * @Date 2023/6/27 18:27
 **/
public interface BaseWorkflowService {

    /**
     * 流程结束监听
     * @param dto
     * @return
     */
    Boolean approveEnd(EndProcessDTO dto);
}
