package com.erp.server.workflow.controller.feign;

import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.server.workflow.service.ProcessTaskManagementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 流程任务管理Feign控制器
 * @date 2024-09-05
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/processTaskManagement")
public class ProcessTaskManagementFeignController {
    @Resource
    private ProcessTaskManagementService processTaskManagementService;

    @GetMapping("/listApproveHistory")
    public List<ProcessTaskManagementDTO.ApproveHistoryDTO> listApproveHistory(@RequestParam String businessId){
        return processTaskManagementService.listApproveHistory(businessId);
    }

    /**
     * 批量获取已完结任务的备注
     */
    @PostMapping("/finishedRemark")
    public List<ProcessTaskManagementDTO.FinishedRemarkDTO> finishedRemark(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return processTaskManagementService.listFinishedTaskRemarksBatch(dto.getIds());
    }
}
