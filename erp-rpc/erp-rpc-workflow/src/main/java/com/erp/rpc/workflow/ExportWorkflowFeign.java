package com.erp.rpc.workflow;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.dto.ProcessDelegateDTO;
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
    /**
     * 委托审批导出
     * @author will 
     * @date 2025/5/13 11:55
     * @param dto 
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/feign/export/processDelegate")
    PagingVO<ProcessDelegateDTO.ListDTO> exportProcessDelegate(PagingDTO<ProcessDelegateDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportCfgApproveSync")
    PagingVO<CfgApproveSyncDTO.ListDTO> exportCfgApproveSync(PagingDTO<CfgApproveSyncDTO.PagingParamDTO> dto);
}
