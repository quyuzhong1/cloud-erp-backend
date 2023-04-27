package com.erp.server.workflow.controller.api;


import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.server.workflow.service.ProcessManagementService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@RestController
@RequestMapping("/process/management")
public class ProcessManagementController extends BaseController {

    @Resource
    private ProcessManagementService processManagementService;

    /**
     * 启动流程
     * @param dto
     * @return
     */
    @PostMapping("/start")
    public ApiResult<ProcessManagementDTO.StartResultDTO> startProcess(@RequestBody @Valid ProcessManagementDTO.StartDTO dto) {
        ProcessManagementDTO.StartResultDTO result =  processManagementService.startProcess(dto);
        return success(result);
    }

    /**
     * 流程审核
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult approveProcess(@RequestBody @Valid ProcessManagementDTO.ApproveDTO dto) {
        processManagementService.approveProcess(dto);
        return success();
    }
}
