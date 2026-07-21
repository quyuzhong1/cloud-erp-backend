package com.erp.server.auth.controller.openapi;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.rpc.workflow.ProcessTaskManagementFeign;
import com.erp.server.auth.config.OpenApi;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 工作流 OpenAPI
 */
@OpenApi
public class WorkflowOpenApi {

    @Resource
    private ProcessTaskManagementFeign processTaskManagementFeign;

    /**
     * 批量获取已完结任务的备注
     */
    @OpenApi("workflowProcessTaskManagementFinishedRemark")
    public ApiResult<List<ProcessTaskManagementDTO.FinishedRemarkDTO>> workflowProcessTaskManagementFinishedRemark(@Valid BaseIdsDTO.IdsDTO dto) {
        return ApiResult.success(processTaskManagementFeign.finishedRemark(dto));
    }
}
