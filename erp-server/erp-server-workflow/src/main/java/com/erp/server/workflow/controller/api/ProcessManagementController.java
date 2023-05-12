package com.erp.server.workflow.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
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
import javax.validation.constraints.NotNull;
import java.util.List;

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
     * 流程审核通过
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult approveProcess(@RequestBody @Valid ProcessManagementDTO.ApproveDTO dto) {
        processManagementService.approveProcess(dto);
        return success();
    }

    /**
     * 驳回流程
     * @param dto
     */
    @PostMapping("/back")
    public ApiResult backProcess(@RequestBody @Valid ProcessManagementDTO.BackDTO dto) {
        processManagementService.back(dto);
        return success();
    }

    /**
     * 撤回流程
     *
     */
    @PostMapping("/revoke")
    public ApiResult revokeProcess(@RequestBody @Valid ProcessManagementDTO.RevokeDTO dto) {
        processManagementService.revoke(dto);
        return success();
    }


    /**
     * 转发任务
     */
    @PostMapping("/transfer")
    public ApiResult transferProcess(@RequestBody @Valid @NotNull List<ProcessManagementDTO.TransferDTO> dto) {
        processManagementService.transfer(dto);
        return success();
    }

    /**
     * 历史流程节点 - 用于指定人驳回
     */
    @PostMapping("/history/activity")
    public ApiResult<List<ProcessManagementDTO.HistoryActivityResultDTO>> historyActivity(@RequestBody @Valid ProcessManagementDTO.HistoryActivityDTO dto) {
        List<ProcessManagementDTO.HistoryActivityResultDTO> resultList = processManagementService.historyActivity(dto);
        return success(resultList);
    }
    /**
     * 流程管理分页列表
     */
    @PostMapping("paging")
    public ApiResult<PagingVO<ProcessManagementDTO.PagingResultDTO>> paging(@RequestBody @Valid PagingDTO<ProcessManagementDTO.SearchDTO> dto) {
        PagingVO<ProcessManagementDTO.PagingResultDTO> resultList = processManagementService.paging(dto);
        return success(resultList);
    }



}
