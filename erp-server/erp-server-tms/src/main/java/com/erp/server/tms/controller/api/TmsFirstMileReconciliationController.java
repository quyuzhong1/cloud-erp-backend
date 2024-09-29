package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationEntity;
import com.erp.server.tms.query.TmsFirstMileReconciliationQueryHandler;
import com.erp.server.tms.service.TmsFirstMileReconciliationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 头程对账单
 *
 * @author Jim
 * @since 2024-03-25
 */
@Slf4j
@RestController
@LogSystemModule("头程对账单")
@RequestMapping("/tmsFirstMileReconciliation")
public class TmsFirstMileReconciliationController extends BaseController {

    @Resource
    private TmsFirstMileReconciliationService tmsFirstMileReconciliationService;

    /**
     * 修改
     *
     * @param dto DTO
     * @return ApiResult
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程对账单修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:update",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TmsFirstMileReconciliationDTO.UpdateDTO dto) {
        tmsFirstMileReconciliationService.updateReconciliation(dto);
        return success();
    }

    /**
     * 获取状态统计
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:paging",
            tableAlias = "tfmr"
    )
    public ApiResult<List<TmsFirstMileReconciliationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(tmsFirstMileReconciliationService.tabList(dto));
    }

    /**
     * 列表查询
     *
     * @param dto DTO DTO
     * @return ApiResult<PagingVO < TmsFirstMileReconciliationDTO.ListDTO>>
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:paging",
            tableAlias = "tfmr"
    )
    @WebAdvanceQuery(handler = TmsFirstMileReconciliationQueryHandler.class)
    public ApiResult<PagingVO<TmsFirstMileReconciliationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TmsFirstMileReconciliationDTO.PagingParamDTO> dto) {
        return success(tmsFirstMileReconciliationService.paging(dto));
    }

    /**
     * 修改并提交审核
     *
     * @param dto DTO
     * @return ApiResult<Void>
     * @author Jim
     * {@code @date:}2024-03-25
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:updateAndSubmit",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated TmsFirstMileReconciliationDTO.UpdateDTO dto) {
        tmsFirstMileReconciliationService.updateAndSubmit(dto);
        return success();
    }

    /**
     * 提交审核
     *
     * @param dto DTO
     * @return ApiResult<List < BatchResultDTO>>
     * @author Jim
     * {@code @date:}2024-03-25
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:submit",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "头程对账单提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = tmsFirstMileReconciliationService.submit(id);
            } catch (Exception e) {
                log.error("头程对账单 提交审核失败", e);
                TmsFirstMileReconciliationEntity entity = tmsFirstMileReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "头程对账单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 审核
     *
     * @param dto DTO
     * @return ApiResult<List < BatchResultDTO>>
     * @author Jim
     * {@code @date:}2024-03-25
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:approve",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "头程对账单审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = tmsFirstMileReconciliationService.approve(new ApproveOneDTO(id, dto.getType(), dto.getComment()));
            } catch (Exception e) {
                log.error("头程对账单审核失败", e);
                TmsFirstMileReconciliationEntity entity = tmsFirstMileReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "头程对账单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     *
     * @param dto DTO
     * @return ApiResult<List < BatchResultDTO>>
     * @author Jim
     * {@code @date:}2024-03-25
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:disApprove",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "头程对账单反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = tmsFirstMileReconciliationService.disApprove(id);
            } catch (Exception e) {
                log.error("头程对账单反审核失败", e);
                TmsFirstMileReconciliationEntity entity = tmsFirstMileReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "头程对账单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 删除
     *
     * @param dto DTO
     * @return ApiResult<List < BatchResultDTO>>
     * @author Jim
     * {@code @date:}2024-03-25
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:delete",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "头程对账单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = tmsFirstMileReconciliationService.delete(id);
            } catch (Exception e) {
                log.error("头程对账单删除失败", e);
                TmsFirstMileReconciliationEntity entity = tmsFirstMileReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "头程对账单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销
     *
     * @param dto DTO
     * @return ApiResult<List < BatchResultDTO>>
     * @author Jim
     * {@code @date:}2024-03-25
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:cancelProcess",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "头程对账单撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = tmsFirstMileReconciliationService.cancelProcess(id);
            } catch (Exception e) {
                log.error("头程对账单撤回流程失败", e);
                TmsFirstMileReconciliationEntity entity = tmsFirstMileReconciliationService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "头程对账单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 详情
     *
     * @param id ID
     * @return ApiResult<TmsFirstMileReconciliationDTO.ViewDTO>>
     * @author Jim
     * {@code @date:}2024-03-25
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliation:view",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<TmsFirstMileReconciliationDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(tmsFirstMileReconciliationService.view(id));
    }

    /**
     * 导出Excel数据
     *
     * @param dto DTO
     * @author Jim
     * {@code @date:}2024-03-25
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "头程对账单导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated TmsFirstMileReconciliationDTO.ExportDTO dto) {
        tmsFirstMileReconciliationService.exportList(dto);
        return success(true);
    }


}
