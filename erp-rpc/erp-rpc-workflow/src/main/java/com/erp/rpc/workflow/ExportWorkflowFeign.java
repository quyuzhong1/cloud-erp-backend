package com.erp.rpc.workflow;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-workflow", contextId = "exportWorkflowFeign", configuration = ExportFeignConfig.class)
public interface ExportWorkflowFeign {

    @PostMapping("/feign/export/processDefinition")
    PagingVO<ProcessDefinitionDTO.ExportDTO> exportProcessDefinition(@RequestBody PagingDTO<ProcessDefinitionDTO.QueryExportDTO> dto);
    @PostMapping("/feign/export/processManagement")
    PagingVO<ProcessManagementDTO.PagingResultDTO> exportProcessManagement(@RequestBody PagingDTO<ProcessManagementDTO.ExportDTO> dto);
}
