package com.erp.rpc.workflow;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-workflow", contextId = "exportWorkflowFeign", configuration = ExportFeignConfig.class)
public interface ExportWorkflowFeign {

    @PostMapping("/feign/export/processDefinition")
    PagingVO<ProcessDefinitionDTO.ExportDTO> exportProcessDefinition(@RequestBody PagingDTO<ProcessDefinitionDTO.QueryExportDTO> dto);
    @PostMapping("/feign/export/processManagement")
    PagingVO<ProcessManagementDTO.PagingResultDTO> exportProcessManagement(@RequestBody PagingDTO<ProcessManagementDTO.SearchDTO> dto);
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

    @PostMapping("/feign/export/exportCfgProcess")
    PagingVO<CfgProcessDTO.ProcessViewDTO> exportCfgProcess(PagingDTO<CfgProcessDTO.SearchParamDTO> dto);

    @PostMapping("/feign/export/exportCfgThirdProcess")
    PagingVO<CfgThirdProcessDTO.ListDTO> exportCfgThirdProcess(PagingDTO<CfgThirdProcessDTO.PagingParamDTO> dto);
    /**
     * 第三方查询导出
     * @author will
     * @date 2025/5/27 11:25
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/feign/export/exportApproveTaskInfo")
    PagingVO<ApproveTaskInfoDTO.ListDTO> exportApproveTaskInfo(PagingDTO<ApproveTaskInfoDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportApproveSyncRecord")
    PagingVO<ApproveSyncRecordDTO.ListDTO> exportApproveSyncRecord(PagingDTO<ApproveSyncRecordDTO.PagingParamDTO> dto);
    /**
     * 审批定义导出
     * @author will
     * @date 2025/7/1 17:26
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/feign/export/exportThirdProcessDefinition")
    PagingVO<ThirdProcessDefinitionDTO.ListDTO> exportThirdProcessDefinition(PagingDTO<ThirdProcessDefinitionDTO.PagingParamDTO> dto);
}
