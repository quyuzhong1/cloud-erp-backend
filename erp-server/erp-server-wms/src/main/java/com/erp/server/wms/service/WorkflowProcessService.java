package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.workflow.dto.EndProcessDTO;

/**
 * @author Will
 * @version 1.0
 * @description: 流程业务层
 * @date 2023/8/2 16:04
 */
public interface WorkflowProcessService {

    /**
     * 审核
     * @author jack
     * @date 2025-11-19
     */
    BatchResultDTO approve(ApproveDTO.ApproveOneDTO dto) ;

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/8/2 16:13
     * @param dto
     * @return Boolean
     */
    Boolean approveEnd(EndProcessDTO dto);


    /**
     * 反审核
     * @author will
     * @date 2025/6/18 09:32
     * @param dto
     * @return Boolean
     */
    Boolean disApprove(ApproveDTO.DisApproveDTO dto);
    /**
     * 撤销流程
     * @author will
     * @date 2025/6/18 10:41
     * @param dto
     * @return Boolean
     */
    Boolean cancelProcess(ApproveDTO.CancelProcessDTO dto);

    /**
     * 添加评论
     * @author will
     * @date 2025/11/27 15:04
     * @param dto
     * @return Boolean
     */
    Boolean addComment(ApproveDTO.AddCommentDTO dto);
}
