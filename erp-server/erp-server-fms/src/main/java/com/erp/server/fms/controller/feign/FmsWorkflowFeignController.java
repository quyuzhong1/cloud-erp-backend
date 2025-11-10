package com.erp.server.fms.controller.feign;

import com.common.business.dto.ApproveDTO;
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
     * 审核结束回调
     * @param dto 审核结束参数
     * @return 处理结果
     */
    @PostMapping("/approveEnd")
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
    public Boolean cancelProcess(@RequestBody ApproveDTO.CancelProcessDTO dto) {
        log.info("FMS工作流撤销流程回调， businessKey: {}", dto.getBusinessKey());
        return fmsWorkflowService.cancelProcess(dto);
    }
}
