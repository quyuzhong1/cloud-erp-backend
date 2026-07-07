package com.erp.server.scm.controller.feign;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.enums.LogActionEnum;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.scm.service.WorkflowProcessService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description: 工作流feign
 * @author Will
 * @date: 2023/7/3 15:32
 */
@RestController
@RequestMapping("feign/scmWorkflow")
public class ScmWorkflowFeignController {

    @Resource
    private WorkflowProcessService workflowProcessService;
    /**
     * @description: 审核
     * @author jack
     * @date: 2025-11-21
     * @param dto
     * @return Boolean
     */
    @PostMapping("/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "SCM工作流审核")
    public BatchResultDTO approve(@RequestBody ApproveDTO.ApproveOneDTO dto) {
        return workflowProcessService.approve(dto);
    }

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 15:42
     * @param dto
     * @return Boolean
     */
    @PostMapping("/approveEnd")
    @LogAction(value = LogActionEnum.APPROVE, desc = "SCM工作流审核结束")
    public Boolean approveEnd(@RequestBody EndProcessDTO dto) {
      return workflowProcessService.approveEnd(dto);
    }

    /**
     * 反审核
     * @author will
     * @date 2025/6/18 09:26
     * @param dto
     * @return void
     */
    @PostMapping("/disApprove")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "SCM工作流反审核")
    public void disApprove(@RequestBody ApproveDTO.DisApproveDTO dto) {
        workflowProcessService.disApprove(dto);
    }

    /**
     * 撤销流程
     * @author will
     * @date 2025/6/18 10:41
     * @param dto
     * @return void
     */
    @PostMapping("/cancelProcess")
    @LogAction(value = LogActionEnum.CANCEL, desc = "SCM工作流撤销")
    public void cancelProcess(@RequestBody ApproveDTO.CancelProcessDTO dto) {
        workflowProcessService.cancelProcess(dto);
    }

    /**
     * 撤销流程
     * @author will
     * @date 2025/6/18 10:41
     * @param dto
     * @return void
     */
    @PostMapping("/addComment")
    @LogAction(value = LogActionEnum.UPDATE, desc = "SCM工作流添加评论")
    public void addComment(@RequestBody ApproveDTO.AddCommentDTO dto) {
        workflowProcessService.addComment(dto);
    }
}
