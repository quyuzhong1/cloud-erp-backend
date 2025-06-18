package com.erp.server.plm.controller.feign;

import com.common.business.dto.ApproveDTO;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.server.plm.service.WorkflowProcessService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @description: 工作流feign
 * @author Will
 * @date: 2023/8/2 16:32
 */
@RestController
@RequestMapping("feign/plmWorkflow")
public class PlmWorkflowFeignController {

    @Resource
    private WorkflowProcessService workflowProcessService;

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/3 15:42
     * @param dto
     * @return Boolean
     */
    @PostMapping("/approveEnd")
    public Boolean approveEnd(@RequestBody EndProcessDTO dto) {
      return workflowProcessService.approveEnd(dto);
    }

    /**
     * @description: 获取流程的单据头和明细的数据
     * @author jack
     * @date: 2025-05-22
     * @return Map<String, Object>
     */
    @PostMapping("/getVariablesMap")
    public Map<String, Object> getVariablesMap(@RequestBody EndProcessDTO dto) {
        return workflowProcessService.getVariablesMap(dto);
    }

    /**
     * 反审核
     * @author will
     * @date 2025/6/18 09:26
     * @param dto
     * @return void
     */
    @PostMapping("/disApprove")
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
    public void cancelProcess(@RequestBody ApproveDTO.CancelProcessDTO dto) {
        workflowProcessService.cancelProcess(dto);
    }
}
