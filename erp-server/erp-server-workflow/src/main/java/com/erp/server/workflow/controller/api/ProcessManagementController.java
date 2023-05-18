package com.erp.server.workflow.controller.api;


import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.server.workflow.service.ProcessManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 流程管理
 *
 * @author Cloud
 * @since 2023-04-21
 */

@Slf4j
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
    public ApiResult<ProcessManagementDTO.ApproveResultDTO> approveProcess(@RequestBody @Valid ProcessManagementDTO.ApproveDTO dto) {
        ProcessManagementDTO.ApproveResultDTO resultDTO = processManagementService.approveProcess(dto);
        return success(resultDTO);
    }

    /**
     * 驳回流程
     * @param dto
     */
    @PostMapping("/back")
    public ApiResult<ProcessManagementDTO.BackResultDTO> backProcess(@RequestBody @Valid ProcessManagementDTO.BackDTO dto) {
        ProcessManagementDTO.BackResultDTO resultDTO = processManagementService.back(dto);
        return success(resultDTO);
    }

    /**
     * 撤回流程
     *
     */
    @PostMapping("/revoke")
    public ApiResult<ProcessManagementDTO.RevokeResultDTO> revokeProcess(@RequestBody @Valid ProcessManagementDTO.RevokeDTO dto) {
        ProcessManagementDTO.RevokeResultDTO revokeResult = processManagementService.revoke(dto);
        return success(revokeResult);
    }


    /**
     * 转发任务
     */
    @PostMapping("/transfer")
    public ApiResult<Boolean> transferProcess(@RequestBody @Valid @NotNull List<ProcessManagementDTO.TransferDTO> dto) {
        return success(processManagementService.transfer(dto));
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
    @PostMapping("/paging")
    public ApiResult<PagingVO<ProcessManagementDTO.PagingResultDTO>> paging(@RequestBody @Valid PagingDTO<ProcessManagementDTO.SearchDTO> dto) {
        PagingVO<ProcessManagementDTO.PagingResultDTO> resultList = processManagementService.paging(dto);
        return success(resultList);
    }

    /**
     * 流程管理导出
     */
    @PostMapping("/export")
    public ApiResult<String> export(@RequestBody @Valid ProcessManagementDTO.SearchDTO dto, HttpServletResponse response) {
        try {
            processManagementService.export(dto, response);
        } catch (Exception e) {
            log.error("导出流程管理数据失败 dot = {}", JSONUtil.toJsonStr(dto), e);
            return failure(e.getMessage());
        }
        return success();
    }
    /**
     * 查看流程进度
     */
    @PostMapping("/progress")
    public ApiResult<ProcessManagementDTO.ProcessResultDTO> progress(@RequestBody @Valid ProcessManagementDTO.ProgressDTO dto) {
        return success(processManagementService.progress(dto));
    }



}
