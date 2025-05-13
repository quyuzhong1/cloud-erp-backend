package com.erp.server.workflow.controller.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.dto.ProcessDelegateDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.server.workflow.service.CfgApproveSyncService;
import com.erp.server.workflow.service.ProcessDefinitionService;
import com.erp.server.workflow.service.ProcessDelegateService;
import com.erp.server.workflow.service.ProcessManagementService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/export")
public class ExportWorkflowFeignController {

    @Resource
    private ProcessDefinitionService processDefinitionService;

    @Resource
    private ProcessManagementService processManagementService;

    @Resource
    private ProcessDelegateService processDelegateService;

    @Resource
    private CfgApproveSyncService cfgApproveSyncService;


    @PostMapping("/processDefinition")
    public PagingVO<ProcessDefinitionDTO.ExportDTO> exportProcessDefinition(@RequestBody PagingDTO<ProcessDefinitionDTO.QueryExportDTO> dto){
        return processDefinitionService.exportProcessDefinition(dto);
    }
    @PostMapping("/processManagement")
    public PagingVO<ProcessManagementDTO.PagingResultDTO> exportProcessManagement(@RequestBody PagingDTO<ProcessManagementDTO.ExportDTO> dto){
        return processManagementService.exportProcessManagement(dto);
    }
    /**
     * 委托审批导出
     * @author will
     * @date 2025/5/13 12:01
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/processDelegate")
    public PagingVO<ProcessDelegateDTO.ListDTO> exportProcessDelegate(@RequestBody PagingDTO<ProcessDelegateDTO.PagingParamDTO> dto){
        return processDelegateService.paging(dto);
    }

    @PostMapping("/exportCfgApproveSynce")
    public PagingVO<CfgApproveSyncDTO.ListDTO> exportCfgApproveSync(PagingDTO<CfgApproveSyncDTO.PagingParamDTO> dto){
        return cfgApproveSyncService.paging(dto);
    }
}
