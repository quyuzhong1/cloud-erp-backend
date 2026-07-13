package com.erp.server.fms.controller.feign;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.enums.LogActionEnum;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.fms.service.FmsWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FMS工作流回调控制器
 * @author System
 * @date 2025/11/10
 */
@Slf4j
@RestController
@RequestMapping("/feign/fmsWorkflow")
public class FmsWorkflowFeignController {

    @Autowired
    private FmsWorkflowService fmsWorkflowService;
    /**
     * @description: 审核
     * @author jack
     * @date: 2025-11-21
     * @param dto
     * @return Boolean
     */
    @PostMapping("/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "FMS工作流审核")
    public BatchResultDTO approve(@RequestBody ApproveDTO.ApproveOneDTO dto) {
        return fmsWorkflowService.approve(dto);
    }

    /**
     * 审核结束回调
     * @param dto 审核结束参数
     * @return 处理结果
     */
    @PostMapping("/approveEnd")
    @LogAction(value = LogActionEnum.APPROVE, desc = "FMS工作流审核结束")
    public Boolean approveEnd(@RequestBody EndProcessDTO dto) {
        log.info("FMS工作流审核结束回调，businessId: {}, businessKey: {}", dto.getBusinessId(), dto.getBusinessKey());
        return fmsWorkflowService.approveEnd(dto);
    }

    /**
     * 反审核回调
     * @param dto 反审核参数
     * @return 处理结果
     */
    @PostMapping("/disApprove")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "FMS工作流反审核")
    public Boolean disApprove(@RequestBody ApproveDTO.DisApproveDTO dto) {
        log.info("FMS工作流反审核回调， businessKey: {}", dto.getBusinessKey());
        return fmsWorkflowService.disApprove(dto);
    }

    /**
     * 撤销流程回调
     * @param dto 撤销流程参数
     * @return 处理结果
     */
    @PostMapping("/cancelProcess")
    @LogAction(value = LogActionEnum.CANCEL, desc = "FMS工作流撤销")
    public Boolean cancelProcess(@RequestBody ApproveDTO.CancelProcessDTO dto) {
        log.info("FMS工作流撤销流程回调， businessKey: {}", dto.getBusinessKey());
        return fmsWorkflowService.cancelProcess(dto);
    }

    /**
     * 撤销流程
     * @author will
     * @date 2025/6/18 10:41
     * @param dto
     * @return void
     */
    @PostMapping("/addComment")
    @LogAction(value = LogActionEnum.UPDATE, desc = "FMS工作流添加评论")
    public void addComment(@RequestBody ApproveDTO.AddCommentDTO dto) {
        fmsWorkflowService.addComment(dto);
    }
}
