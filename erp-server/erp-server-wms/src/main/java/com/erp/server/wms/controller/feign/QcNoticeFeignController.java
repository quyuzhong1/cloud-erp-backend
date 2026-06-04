package com.erp.server.wms.controller.feign;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.entity.QcNoticeEntity;
import com.erp.server.wms.query.QcNoticeQueryHandler;
import com.erp.server.wms.service.QcNoticeService;
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
 * 质检通知单 app端 / 飞书小程序端 Feign 控制器
 * <p>
 * 列表、详情、新增、编辑、审核能力，与 PC 端 {@link com.erp.server.wms.controller.api.QcNoticeController} 共享 Service
 * 实现，保证业务逻辑、数据权限、高级查询条件保持一致。
 *
 * @author qcNoticeApp
 * @since 2026-05-29
 */
@Slf4j
@RestController
@LogSystemModule("质检通知单app端")
@RequestMapping("/feign/qcNotice")
public class QcNoticeFeignController extends BaseController {

    @Resource
    private QcNoticeService qcNoticeService;

    /**
     * 状态统计
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:paging",
            tableAlias = "qn"
    )
    public ApiResult<List<QcNoticeDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO param) {
        return success(qcNoticeService.tabList(param));
    }

    /**
     * 分页列表查询
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:paging",
            tableAlias = "qn"
    )
    @WebAdvanceQuery(handler = QcNoticeQueryHandler.class)
    public ApiResult<PagingVO<QcNoticeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<QcNoticeDTO.PagingParamDTO> dto) {
        return success(qcNoticeService.paging(dto));
    }

    /**
     * 详情
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:view",
            serviceClass = QcNoticeService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<QcNoticeDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        return success(qcNoticeService.view(id));
    }

    /**
     * 新增（暂存）
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "质检通知单app端新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated QcNoticeDTO.AddDTO dto) {
        return success(qcNoticeService.add(dto));
    }

    /**
     * 修改（暂存）
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "质检通知单app端修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:update",
            serviceClass = QcNoticeService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated QcNoticeDTO.UpdateDTO dto) {
        qcNoticeService.update(dto);
        return success();
    }

    /**
     * 新增并提交审核
     */
    @PostMapping("/addAndSubmit")
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "质检通知单app端新增并提交审核")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated QcNoticeDTO.AddDTO dto) {
        return success(qcNoticeService.addAndSubmit(dto));
    }

    /**
     * 修改并提交审核
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:updateAndSubmit",
            serviceClass = QcNoticeService.class,
            keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "质检通知单app端修改并提交审核")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated QcNoticeDTO.UpdateDTO dto) {
        qcNoticeService.updateAndSubmit(dto);
        return success();
    }

    /**
     * 提交审核
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:submit",
            serviceClass = QcNoticeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "质检通知单app端提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcNoticeEntity> list = qcNoticeService.lambdaQuery().in(QcNoticeEntity::getId, ids).list();
        Map<String, QcNoticeEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcNoticeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO submitResult;
            try {
                submitResult = qcNoticeService.submit(id);
            } catch (Exception e) {
                log.error("质检通知单app端 提交审核失败", e);
                QcNoticeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submitResult = BatchResultDTO.fail(id, id, "质检通知单不存在, 提交失败");
                    resultDTOS.add(submitResult);
                    continue;
                }
                submitResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submitResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 审核 - 外验质检（outsideQc / b2bOutsideQc）类型审核通过时必须传 planQcDate
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcNotice:approve",
            serviceClass = QcNoticeService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "质检通知单app端审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcNoticeEntity> list = qcNoticeService.lambdaQuery().in(QcNoticeEntity::getId, ids).list();
        Map<String, QcNoticeEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcNoticeEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                ApproveOneDTO approveOneDTO = new ApproveOneDTO();
                approveOneDTO.setId(id);
                approveOneDTO.setType(dto.getType());
                approveOneDTO.setComment(dto.getComment());
                approveOneDTO.setPlanQcDate(dto.getPlanQcDate());
                approveResult = qcNoticeService.approve(approveOneDTO);
            } catch (Exception e) {
                log.error("质检通知单app端审核失败", e);
                QcNoticeEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "质检通知单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
