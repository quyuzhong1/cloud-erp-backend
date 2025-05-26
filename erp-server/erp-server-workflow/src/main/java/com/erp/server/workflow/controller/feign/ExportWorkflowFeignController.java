package com.erp.server.workflow.controller.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.*;
import com.erp.server.workflow.service.*;
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

    @Resource
    private CfgProcessService cfgProcessService;

    @Resource
    private CfgThirdProcessService cfgThirdProcessService;

    @PostMapping("/processDefinition")
    public PagingVO<ProcessDefinitionDTO.ExportDTO> exportProcessDefinition(@RequestBody PagingDTO<ProcessDefinitionDTO.QueryExportDTO> dto){
        return processDefinitionService.exportProcessDefinition(dto);
    }
    @PostMapping("/processManagement")
    public PagingVO<ProcessManagementDTO.PagingResultDTO> exportProcessManagement(@RequestBody PagingDTO<ProcessManagementDTO.SearchDTO> dto){
        return processManagementService.paging(dto);
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

    @PostMapping("/exportCfgApproveSync")
    public PagingVO<CfgApproveSyncDTO.ListDTO> exportCfgApproveSync(@RequestBody PagingDTO<CfgApproveSyncDTO.PagingParamDTO> dto){
        return cfgApproveSyncService.paging(dto);
    }

    @PostMapping("/exportCfgProcess")
    public PagingVO<CfgProcessDTO.ProcessViewDTO> exportCfgProcess(@RequestBody PagingDTO<CfgProcessDTO.SearchParamDTO> dto){
        return cfgProcessService.paging(dto);
    }
    @PostMapping("/exportCfgThirdProcess")
    public PagingVO<CfgThirdProcessDTO.ListDTO> exportCfgThirdProcess(@RequestBody PagingDTO<CfgThirdProcessDTO.PagingParamDTO> dto){
        return cfgThirdProcessService.paging(dto);
    }
}
