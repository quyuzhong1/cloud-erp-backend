package com.erp.server.oms.controller.feign;

import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.oms.service.WorkOptionService;
import com.erp.server.oms.service.WorkflowProcessService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * oms工作流feign
 *
 * @Author Cloud
 * @Date 2023/6/28 9:32
 **/
@RestController
@RequestMapping("feign/omsWorkflow")
public class OmsWorkflowFeignController {

    @Resource
    private WorkflowProcessService workflowProcessService;

    @Resource
    private WorkOptionService workOptionService;

    /**
     * 根据入参查询单据数量
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("/getTableNum")
    List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(@RequestBody List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList) {
        return workOptionService.getTableNum(myWorkOptionDTOList);
    }

    /**
     * @description: 结束审核回调
     * @author Will
     * @date: 2023/7/4 10:20
     * @param dto
     */
    @PostMapping("/approveEnd")
    public void approveEnd(@RequestBody EndProcessDTO dto) {
        workflowProcessService.approveEnd(dto);
    }

}
