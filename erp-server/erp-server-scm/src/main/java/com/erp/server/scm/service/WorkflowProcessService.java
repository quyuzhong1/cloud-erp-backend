package com.erp.server.scm.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.workflow.dto.EndProcessDTO;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: 工作流业务接口
 * @date 2023/7/3 15:38
 */
@Service
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
     * @date: 2023/7/3 15:42
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
