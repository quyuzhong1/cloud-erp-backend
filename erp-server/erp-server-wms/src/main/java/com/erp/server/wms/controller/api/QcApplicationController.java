package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.QcApplicationDTO;
import com.erp.model.wms.entity.QcApplicationEntity;
import com.erp.server.wms.query.QcApplicationQueryHandler;
import com.erp.server.wms.service.QcApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 质检申请单主表
 *
 * @author will
 * @since 2026-03-20
 */
@Slf4j
@RestController
@LogSystemModule("质检申请单主表")
@RequestMapping("/qcApplication")
public class QcApplicationController extends BaseController {

    @Resource
    private QcApplicationService qcApplicationService;

    /**
    * 新增
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "质检申请单主表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated QcApplicationDTO.AddDTO dto) {
        return success(qcApplicationService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "质检申请单主表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:qcApplication:update",
        serviceClass = QcApplicationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated QcApplicationDTO.UpdateDTO dto) {
        qcApplicationService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:paging",
            tableAlias = "qa"
    )
    public ApiResult<List<QcApplicationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(qcApplicationService.tabList(dto));
    }

    /**
    * 列表查询
    * @author will
    * @date: 2026-03-20
    * @param dto
    * @return ApiResult<PagingVO<QcApplicationDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:paging",
            tableAlias = "qa"
    )
    @WebAdvanceQuery(handler = QcApplicationQueryHandler.class)
    public ApiResult<PagingVO<QcApplicationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<QcApplicationDTO.PagingParamDTO> dto) {
        return success(qcApplicationService.paging(dto));
    }

    /**
    * 提交审核
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:submit",
            serviceClass = QcApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "质检申请单主表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
		Map<String, QcApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = qcApplicationService.submit(id);
            }catch (Exception e){
                log.error("质检申请单主单 提交审核失败",e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 提交失败");
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
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:approve",
            serviceClass = QcApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "质检申请单主表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
		Map<String, QcApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = qcApplicationService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("质检申请单主单审核失败",e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 审核失败");
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
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:disApprove",
            serviceClass = QcApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "质检申请单主表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
		Map<String, QcApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = qcApplicationService.disApprove(id);
            }catch (Exception e){
                log.error("质检申请单主单反审核失败",e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 反审核失败");
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
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:delete",
            serviceClass = QcApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "质检申请单主表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
		Map<String, QcApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = qcApplicationService.delete(id);
            }catch (Exception e){
                log.error("质检申请单主单删除失败",e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 删除失败");
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
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:cancelProcess",
            serviceClass = QcApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "质检申请单主表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
        Map<String, QcApplicationEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = qcApplicationService.cancelProcess(new ApproveDTO.CancelProcessDTO(id));
            }catch (Exception e){
                log.error("质检申请单主单撤回流程失败",e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 撤回流程失败");
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
    * @author will
    * @date:  2026-03-20
    * @param id
    * @return ApiResult<QcApplicationDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:view",
            serviceClass = QcApplicationService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<QcApplicationDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(qcApplicationService.view(id));
    }

    /**
    * 导出Excel数据
    * @author will
    * @date:  2026-03-20
    * @param dto
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:export",
            tableAlias = "qa"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "质检申请单主表导出Excel数据")
    @WebAdvanceQuery(handler = QcApplicationQueryHandler.class)
    public ApiResult<Object>exportExcel(@RequestBody @Validated QcApplicationDTO.PagingParamDTO dto) {
        Boolean flag = qcApplicationService.exportList(dto);
        return flag ? success() : failure();
    }


    /**
     * 下推质检通知数据回显
     * @author will
     * @date 2026/3/23 12:24
     * @param dto
     * @return QcApplicationDTO.ListPushQcNoticeDTO
     */
    @PostMapping("/listPushQcNotice")
    public ApiResult<List<QcApplicationDTO.ListPushQcNoticeDTO>> listPushQcNotice(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(qcApplicationService.listPushQcNotice(dto.getIds()));
    }

    /**
     * 下推质检通知数据保存
     * @author will
     * @date 2026/3/23 12:24
     * @param list
     * @return QcApplicationDTO.ListPushQcNoticeDTO
     */
    @PostMapping("/generateQcNotice")
    public ApiResult<List<BatchResultDTO>> generateQcNotice(@RequestBody @Validated ValidList<QcApplicationDTO.GenerateQcNoticeDTO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<String> ids = list.stream().map(QcApplicationDTO.GenerateQcNoticeDTO::getId).collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcApplicationEntity> qcList = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
        Map<String, QcApplicationEntity> idEntityMap = qcList.stream().collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (QcApplicationDTO.GenerateQcNoticeDTO generateQcNoticeDTO : list.getList()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = qcApplicationService.generateQcNotice(generateQcNoticeDTO);
            }catch (Exception e){
                log.error("质检申请单主单撤回流程失败",e);
                QcApplicationEntity entity = idEntityMap.get(generateQcNoticeDTO.getId());
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(generateQcNoticeDTO.getId(), generateQcNoticeDTO.getId(), "质检申请单主单不存在, 撤回流程失败");
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
     * 采购订单下推质检申请数据保存
     * @author will
     * @date 2026/3/23 12:24
     * @param list
     * @return QcApplicationDTO.ListPushQcNoticeDTO
     */
    @PostMapping("/generatePoRefQcApplication")
    public ApiResult<Object> generatePoRefQcApplication(@RequestBody @Validated ValidList<QcApplicationDTO.GeneratePoRefQcApplicationDTO> list) {
        return success(qcApplicationService.generatePoRefQcApplication(list));
    }

}
