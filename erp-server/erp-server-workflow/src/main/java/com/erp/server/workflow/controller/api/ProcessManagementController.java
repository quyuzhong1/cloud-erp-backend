package com.erp.server.workflow.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.server.workflow.service.ProcessManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 流程管理
 *
 * @author Cloud
 * @since 2023-04-21
 */

@Slf4j
@RestController
@LogSystemModule("流程管理")
@RequestMapping("/process/management")
public class ProcessManagementController extends BaseController {

    @Resource
    private ProcessManagementService processManagementService;

    /**
     * 启动流程
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启动流程,业务名称={businessName},业务表id={businessId}")
    @PostMapping("/start")
    public ApiResult<ProcessManagementDTO.StartResultDTO> startProcess(@RequestBody @Valid ProcessManagementDTO.StartDTO dto) {
        ProcessManagementDTO.StartResultDTO result =  processManagementService.startProcess(dto);
        return success(result);
    }

    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启动流程:业务类型={businessKey},业务表id={businessId}")
    @PostMapping("/batchStart")
    public ApiResult<List<ProcessManagementDTO.StartResultDTO>> batchStartProcess(@RequestBody @Valid ValidList<ProcessManagementDTO.StartDTO> dto) {
        List<ProcessManagementDTO.StartResultDTO> result =  processManagementService.batchStartProcess(dto);
        return success(result);
    }

    /**
     * 流程审核
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "流程审核")
    @PostMapping("/approve")
    public ApiResult<ProcessManagementDTO.ApproveResultDTO> approveProcess(@RequestBody @Valid ProcessManagementDTO.ApproveDTO dto) {
        ProcessManagementDTO.ApproveResultDTO resultDTO = processManagementService.approveProcess(dto,Boolean.TRUE);
        return success(resultDTO);
    }

    /**
     * 批量审批
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "批量审批流程")
    @PostMapping("/batchApprove")
    public ApiResult<List<ProcessManagementDTO.ApproveResultDTO>> batchApproveProcess(@RequestBody @Valid ValidList<ProcessManagementDTO.ApproveDTO> dto) {
        List<ProcessManagementDTO.ApproveResultDTO> resultDTO = processManagementService.batchApproveProcess(dto);
        return success(resultDTO);
    }


    /**
     * 流程驳回到指定节点
     * @param dto
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "流程驳回到指定节点：驳回的目标节点ID={activityId}")
    @PostMapping("/back")
    public ApiResult<ProcessManagementDTO.BackResultDTO> backProcess(@RequestBody @Valid ProcessManagementDTO.BackDTO dto) {
        ProcessManagementDTO.BackResultDTO resultDTO = processManagementService.back(dto);
        return success(resultDTO);
    }

    /**
     * 撤回流程
     *
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤回流程")
    @PostMapping("/revoke")
    public ApiResult<ProcessManagementDTO.RevokeResultDTO> revokeProcess(@RequestBody @Valid ProcessManagementDTO.RevokeDTO dto) {
        ProcessManagementDTO.RevokeResultDTO revokeResult = processManagementService.revoke(dto);
        return success(revokeResult);
    }


    /**
     * 批量-转办任务-流程管理
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "批量-转办任务:ids={ids},转办目标人={targetUserId}")
    @PostMapping("/transfer/batch")
    public ApiResult<Boolean> transferBatchProcess(@RequestBody @Valid ProcessManagementDTO.TransferBatchDTO dto) {
        return success(processManagementService.transferBatch(dto));
    }

    /**
     * 转办-业务流程
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "转办任务{businessId}")
    @PostMapping("/transfer")
    public ApiResult<Boolean> transferProcess(@RequestBody @Valid ProcessManagementDTO.TransferDTO dto) {
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
    @LogAction(value = LogActionEnum.EXPORT, desc = "流程管理导出")
    @PostMapping("/export")
    public ApiResult<Boolean> export(@RequestBody @Valid ProcessManagementDTO.ExportDTO dto) {
        processManagementService.export(dto);
        return ApiResult.success(true);
    }
    /**
     * 查看流程进度
     */
    @PostMapping("/progress")
    public ApiResult<ProcessManagementDTO.ProcessResultDTO> progress(@RequestBody @Valid ProcessManagementDTO.ProgressDTO dto) {
        return success(processManagementService.progress(dto));
    }

    /**
     * 批量查询流程当前审批人
     */
    @PostMapping("/batchCurApprover")
    public ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> batchCurApprover(@RequestBody @Valid ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList) {
        List<ProcessManagementDTO.CurApproveInfoDTO> resultList = processManagementService.batchCurApprover(dtoList);
        return success(resultList);
    }

    /**
     * 批量查询当前待审核业务单据
     */
    @PostMapping("/batchCurApproverByApprove")
    public ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> batchCurApproverByApprove(@RequestBody @Valid ValidList<ProcessManagementDTO.ApproveActivityDTO> dtoList) {
        List<ProcessManagementDTO.CurApproveInfoDTO> resultList = processManagementService.batchCurApproverByApprove(dtoList);
        return success(resultList);
    }

}
