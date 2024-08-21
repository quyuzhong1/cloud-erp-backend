package com.erp.server.workflow.controller.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.server.workflow.service.ProcessDefinitionService;
import com.erp.server.workflow.service.ProcessManagementService;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/processDefinition")
    PagingVO<ProcessDefinitionDTO.ExportDTO> exportProcessDefinition(PagingDTO<ProcessDefinitionDTO.QueryExportDTO> dto){
        return processDefinitionService.exportProcessDefinition(dto);
    }
    @PostMapping("/feign/export/processManagement")
    PagingVO<ProcessManagementDTO.PagingResultDTO> exportProcessManagement(PagingDTO<ProcessManagementDTO.ExportDTO> dto){
        return processManagementService.exportProcessManagement(dto);
    }
}
