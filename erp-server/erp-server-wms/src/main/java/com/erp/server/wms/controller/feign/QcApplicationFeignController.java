package com.erp.server.wms.controller.feign;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 质检申请单 app端 / 飞书小程序端 Feign 控制器
 * <p>
 * 列表、详情、编辑、审核、下推能力与 PC 端
 * {@link com.erp.server.wms.controller.api.QcApplicationController} 共享 Service，
 * 保证业务逻辑、数据权限、高级查询条件保持一致。
 *
 * @Author zdy
 * @since 2026-07-23
 */
@Slf4j
@RestController
@LogSystemModule("质检申请单app端")
@RequestMapping("/feign/qcApplication")
public class QcApplicationFeignController extends BaseController {

    @Resource
    private QcApplicationService qcApplicationService;

    /**
     * 状态统计
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:paging",
            tableAlias = "qa"
    )
    public ApiResult<List<QcApplicationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(qcApplicationService.pdaTabList(dto));
    }

    /**
     * 分页列表查询（支持高级搜索）
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
     * 详情
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
     * 修改
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "质检申请单app端修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:update",
            serviceClass = QcApplicationService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated QcApplicationDTO.UpdateDTO dto) {
        Boolean isSrm = qcApplicationService.isSrmSourceData(dto.getId());
        if (Boolean.TRUE.equals(isSrm)) {
            throw new ServiceException(ApiError.QC_APPLICATION_SOURCE_WAIT_DELIVERY_NOT_OPTION);
        }
        qcApplicationService.update(dto);
        return success();
    }

    /**
     * 提交审核（支持批量）
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:submit",
            serviceClass = QcApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "质检申请单app端提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
        Map<String, QcApplicationEntity> idEntityMap = list.stream()
                .collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO submit;
            try {
                if (Boolean.TRUE.equals(qcApplicationService.isSrmSourceData(id))) {
                    throw new ServiceException(ApiError.QC_APPLICATION_SOURCE_WAIT_DELIVERY_NOT_OPTION);
                }
                submit = qcApplicationService.submit(id);
            } catch (Exception e) {
                log.error("质检申请单app端提交审核失败", e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e);
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 审核（支持批量）
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:approve",
            serviceClass = QcApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "质检申请单app端审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
        Map<String, QcApplicationEntity> idEntityMap = list.stream()
                .collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = qcApplicationService.approve(new ApproveOneDTO(id, dto.getType(), dto.getComment()));
            } catch (Exception e) {
                log.error("质检申请单app端审核失败", e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e);
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 删除（支持批量）
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcApplication:delete",
            serviceClass = QcApplicationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "质检申请单app端删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcApplicationEntity> list = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
        Map<String, QcApplicationEntity> idEntityMap = list.stream()
                .collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO deleteResult;
            try {
                if (Boolean.TRUE.equals(qcApplicationService.isSrmSourceData(id))) {
                    throw new ServiceException(ApiError.QC_APPLICATION_SOURCE_WAIT_DELIVERY_NOT_OPTION);
                }
                deleteResult = qcApplicationService.delete(id);
            } catch (Exception e) {
                log.error("质检申请单app端删除失败", e);
                QcApplicationEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "质检申请单主单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e);
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下推质检通知数据回显
     */
    @PostMapping("/listPushQcNotice")
    public ApiResult<List<QcApplicationDTO.ListPushQcNoticeDTO>> listPushQcNotice(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(qcApplicationService.listPushQcNotice(dto.getIds()));
    }

    /**
     * 下推质检通知数据保存
     */
    @PostMapping("/generateQcNotice")
    @LogAction(value = LogActionEnum.INSERT, desc = "质检申请单app端下推质检通知")
    public ApiResult<List<BatchResultDTO>> generateQcNotice(
            @RequestBody @Validated ValidList<QcApplicationDTO.GenerateQcNoticeDTO> list) {
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        List<String> ids = list.stream().map(QcApplicationDTO.GenerateQcNoticeDTO::getId).collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcApplicationEntity> qcList = qcApplicationService.lambdaQuery().in(QcApplicationEntity::getId, ids).list();
        Map<String, QcApplicationEntity> idEntityMap = qcList.stream()
                .collect(Collectors.toMap(QcApplicationEntity::getId, w -> w));
        for (QcApplicationDTO.GenerateQcNoticeDTO generateQcNoticeDTO : list.getList()) {
            BatchResultDTO result;
            try {
                result = qcApplicationService.generateQcNotice(generateQcNoticeDTO);
            } catch (Exception e) {
                log.error("质检申请单app端下推质检通知失败", e);
                QcApplicationEntity entity = idEntityMap.get(generateQcNoticeDTO.getId());
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(generateQcNoticeDTO.getId(), generateQcNoticeDTO.getId(),
                            "质检申请单主单不存在, 下推失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e);
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
