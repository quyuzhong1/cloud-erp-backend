package com.erp.server.workflow.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessManagementEntity;
import com.erp.server.workflow.query.ProcessManagementQueryHandler;
import com.erp.server.workflow.service.ProcessManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

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
     * 获取状态统计
     * @author will
     * @date 2025/5/12 16:10
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tabList")
    public ApiResult<List<ProcessManagementDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(processManagementService.tabList(dto));
    }

    /**
     * 启动流程
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启动流程,业务名称={businessName},业务表id={businessId}")
    @PostMapping("/start")
    public ApiResult<ProcessManagementDTO.StartResultDTO> startProcess(@RequestBody @Valid ProcessManagementDTO.StartDTO dto) {
        ProcessManagementDTO.StartResultDTO result =  processManagementService.startProcessManagement(dto);
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
    public ApiResult<?> transferBatchProcess(@RequestBody @Valid ProcessManagementDTO.TransferBatchDTO dto) {
        List<BatchResultDTO> resultDTOS = new LinkedList<>();
        // 查询当前执行任务
        Map<String, ProcessManagementDTO.ManagementTaskDTO> entityMap = processManagementService.listTaskById(dto.getIds())
                .stream()
                .collect(Collectors.toMap(ProcessManagementDTO.ManagementTaskDTO::getTaskManagementId, e -> e));
        for (String id : dto.getIds()) {
            ProcessManagementDTO.ManagementTaskDTO entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id, ApiError.TASK_NOT_EXIST.msg));
                continue;
            }
            try {
                boolean flag = processManagementService.transferBatch(new ProcessManagementDTO.TransferBatchDTO(
                        Collections.singletonList(id),
                        dto.getTargetUserId(),
                        dto.getRemark()));
                if (flag){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getBusinessCode(),"转办任务成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getBusinessCode(),"转办任务失败"));
                }
            }catch (Exception e){
                log.error("转办任务失败",e);
                resultDTOS.add(BatchResultDTO.fail(id, entity.getBusinessCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
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
    @WebAdvanceQuery(handler = ProcessManagementQueryHandler.class)
    public ApiResult<PagingVO<ProcessManagementDTO.PagingResultDTO>> paging(@RequestBody @Valid PagingDTO<ProcessManagementDTO.SearchDTO> dto) {
        PagingVO<ProcessManagementDTO.PagingResultDTO> resultList = processManagementService.paging(dto);
        return success(resultList);
    }

    /**
     * 流程管理分页列表
     * @author will
     * @date 2025/6/4 14:15
     * @param dto
     * @return ApiResult<PagingVO<MainPagingResultDTO>>
     */
    @PostMapping("/mainPaging")
    @WebAdvanceQuery(handler = ProcessManagementQueryHandler.class)
    public ApiResult<PagingVO<ProcessManagementDTO.MainPagingResultDTO>> mainPaging(@RequestBody @Valid PagingDTO<ProcessManagementDTO.SearchDTO> dto) {
        PagingVO<ProcessManagementDTO.MainPagingResultDTO> resultList = processManagementService.mainPaging(dto);
        return success(resultList);
    }

    /**
     * 明细数据
     * @author will
     * @date 2025/6/4 10:48
     * @param dto
     * @return ApiResult<PagingVO<DetailPagingResultDTO>>
     */
    @PostMapping("/listDetail")
    public ApiResult<List<ProcessManagementDTO.DetailPagingResultDTO>> listDetail(@RequestBody @Valid ProcessManagementDTO.DetailSearchDTO dto) {
        List<ProcessManagementDTO.DetailPagingResultDTO> resultList = processManagementService.listDetail(dto);
        return success(resultList);
    }

    /**
     * 流程管理导出
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "流程管理导出")
    @PostMapping("/export")
    @WebAdvanceQuery(handler = ProcessManagementQueryHandler.class)
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
     * 批量查询流程当前审批人
     */
    @PostMapping("/getCurApprover")
    public ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> getCurApprover(@RequestBody ProcessManagementDTO.HistoryActivityDTO dto) {
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        dtoList.add(dto);
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

    /**
     * 强制通过
     * @author will
     * @date 2025/5/15 17:22
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/processPass")
    public ApiResult<List<BatchResultDTO>> processPass(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = processManagementService.processPass(id);
            }catch (Exception e){
                log.error("流程管理强制通过失败",e);
                ProcessManagementEntity entity = processManagementService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "流程管理数据不存在, 强制通过失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getProcessName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 强制驳回
     * @author will
     * @date 2025/5/15 17:22
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/processReject")
    public ApiResult<List<BatchResultDTO>> processReject(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = processManagementService.processReject(id);
            }catch (Exception e){
                log.error("流程管理强制驳回失败",e);
                ProcessManagementEntity entity = processManagementService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "流程管理数据不存在, 强制驳回失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getProcessName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 恢复
     * @author will
     * @date 2025/5/15 17:22
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/processRestore")
    public ApiResult<List<BatchResultDTO>> processRestore(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = processManagementService.processRestore(id);
            }catch (Exception e){
                log.error("流程管理恢复失败",e);
                ProcessManagementEntity entity = processManagementService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "流程管理数据不存在, 恢复失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getProcessName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 暂停
     * @author will
     * @date 2025/5/15 17:22
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/processSuspend")
    public ApiResult<List<BatchResultDTO>> processSuspend(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = processManagementService.processSuspend(id);
            }catch (Exception e){
                log.error("流程管理暂停失败",e);
                ProcessManagementEntity entity = processManagementService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "流程管理数据不存在, 暂停失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getProcessName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
