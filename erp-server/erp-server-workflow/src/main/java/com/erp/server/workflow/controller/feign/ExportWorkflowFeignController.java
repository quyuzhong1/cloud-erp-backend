package com.erp.server.workflow.controller.feign;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.*;
import com.erp.server.workflow.query.ApproveTaskInfoQueryHandler;
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

    @Resource
    private ApproveTaskInfoService approveTaskInfoService;
    @Resource
    private ApproveSyncRecordService approveSyncRecordService;

    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;


    @PostMapping("/processDefinition")
    public PagingVO<ProcessDefinitionDTO.ExportDTO> exportProcessDefinition(@RequestBody PagingDTO<ProcessDefinitionDTO.QueryExportDTO> dto){
        return processDefinitionService.exportProcessDefinition(dto);
    }
/**
     * 流程管理导出
     * @author will
     * @date 2025/5/13 12:01
     * @param dto
     * @return PagingVO<ListDTO>
     */
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
    /**
     * 第三方查询导出
     * @author will
     * @date 2025/5/27 11:27
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/exportApproveTaskInfo")
    @WebAdvanceQuery(handler = ApproveTaskInfoQueryHandler.class)
    public PagingVO<ApproveTaskInfoDTO.ListDTO> exportApproveTaskInfo(@RequestBody PagingDTO<ApproveTaskInfoDTO.PagingParamDTO> dto){
        return approveTaskInfoService.paging(dto);
    }

    @PostMapping("/exportApproveSyncRecord")
    public PagingVO<ApproveSyncRecordDTO.ListDTO> exportApproveSyncRecord(@RequestBody PagingDTO<ApproveSyncRecordDTO.PagingParamDTO> dto){
        return approveSyncRecordService.paging(dto);
    }

    /**
     * 审批定义导出
     * @author will
     * @date 2025/7/1 17:27
     * @param dto
     * @return PagingVO<ListDTO>
     */
    @PostMapping("/exportThirdProcessDefinition")
    @WebAdvanceQuery
    public PagingVO<ThirdProcessDefinitionDTO.ListDTO> exportThirdProcessDefinition(@RequestBody PagingDTO<ThirdProcessDefinitionDTO.PagingParamDTO> dto){
        return thirdProcessDefinitionService.paging(dto);
    }
}
