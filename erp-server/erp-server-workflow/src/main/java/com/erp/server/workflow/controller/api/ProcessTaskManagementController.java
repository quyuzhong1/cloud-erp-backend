package com.erp.server.workflow.controller.api;


import com.common.business.dto.base.BaseIdsDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.server.workflow.service.ProcessTaskManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 流程任务管理
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Slf4j
@RestController
@RequestMapping("/process/task/management")
public class ProcessTaskManagementController extends BaseController {

    @Resource
    private ProcessTaskManagementService processTaskManagementService;

    /**
     * 批量获取已完结任务的备注
     */
    @PostMapping("/finishedRemark")
    public ApiResult<List<ProcessTaskManagementDTO.FinishedRemarkDTO>> finishedRemark(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(processTaskManagementService.listFinishedTaskRemarksBatch(dto.getIds()));
    }
}
