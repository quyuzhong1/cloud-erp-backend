package com.erp.server.wms.controller;

import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.annotation.DataPermission;
import com.erp.model.wms.dto.QcStandardDTO;
import com.erp.model.wms.entity.QcStandardEntity;
import com.erp.server.wms.query.QcStandardQueryHandler;
import com.erp.server.wms.service.QcStandardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.common.core.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

/**
 * 质检标准 Controller
 *
 * @author jack
 * @since 2026-03-22
 */
@Slf4j
@RestController
@RequestMapping("/qcStandard")
public class QcStandardController extends BaseController {

    @Autowired
    private QcStandardService qcStandardService;

    @LogAction(value = LogActionEnum.INSERT, desc = "新增质检标准")
    @PostMapping("/add")
    public ApiResult<Void> add(@Validated @RequestBody QcStandardDTO.AddDTO addDTO) {
        qcStandardService.add(addDTO);
        return success();
    }

    @LogAction(value = LogActionEnum.UPDATE, desc = "修改质检标准")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcStandard:update",
            serviceClass = QcStandardService.class,
            keyIdName = "id")
    public ApiResult<Void> update(@Validated @RequestBody QcStandardDTO.UpdateDTO updateDTO) {
        qcStandardService.update(updateDTO);
        return success();
    }

    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcStandard:paging",
            tableAlias = "qs"
    )
    public ApiResult<List<QcStandardDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO permissionsDTO) {
        return success(qcStandardService.tabList(permissionsDTO));
    }

    @WebAdvanceQuery(handler = QcStandardQueryHandler.class)
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcStandard:paging",
            tableAlias = "qs"
    )
    public ApiResult<PagingVO<QcStandardDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<QcStandardDTO.PagingParamDTO> pagingParamDTO) {
        return success(qcStandardService.paging(pagingParamDTO));
    }

    /**
     * 导出
     *
     * @param dto 导出参数
     * @return 业务结果
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcStandard:export",
            tableAlias = "qs"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "质检标准导出Excel数据")
    @WebAdvanceQuery(handler = QcStandardQueryHandler.class)
    public ApiResult<Boolean> exportList(@RequestBody @Validated PagingDTO<QcStandardDTO.PagingParamDTO> dto, HttpServletResponse response) {
        qcStandardService.export(dto);
        return success(true);
    }

    @LogAction(value = LogActionEnum.DELETE, desc = "删除质检标准")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcStandard:delete",
            serviceClass = QcStandardService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcStandardEntity> list = qcStandardService.lambdaQuery().in(QcStandardEntity::getId, ids).list();
        Map<String, QcStandardEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcStandardEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = qcStandardService.delete(id);
            } catch (Exception e) {
                log.error("质检标准删除失败", e);
                QcStandardEntity entity = idEntityMap.get(id);
                if (entity == null) {
                    deleteResult = BatchResultDTO.fail(id, id, "数据不存在, 删除失败");
                } else {
                    deleteResult = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
                }
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    @LogAction(value = LogActionEnum.UPDATE, desc = "启禁用质检标准")
    @PostMapping("/updateStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcStandard:updateStatus",
            serviceClass = QcStandardService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> batchUpdateStatus(@RequestBody @Validated QcStandardDTO.UpdateStatusBatchDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<QcStandardEntity> list = qcStandardService.lambdaQuery().in(QcStandardEntity::getId, ids).list();
        Map<String, QcStandardEntity> idEntityMap = list.stream().collect(Collectors.toMap(QcStandardEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO statusResult;
            try {
                QcStandardDTO.UpdateStatusDTO updateDto = new QcStandardDTO.UpdateStatusDTO();
                updateDto.setId(id);
                updateDto.setDisabled(dto.getDisabled());
                statusResult = qcStandardService.updateStatus(updateDto);
            } catch (Exception e) {
                log.error("质检标准状态更新失败", e);
                QcStandardEntity entity = idEntityMap.get(id);
                if (entity == null) {
                    statusResult = BatchResultDTO.fail(id, id, "数据不存在, 状态更新失败");
                } else {
                    statusResult = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
                }
            }
            resultDTOS.add(statusResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    @GetMapping("/copyBySku")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:qcStandard:copyBySku",
            tableAlias = "qs")
    public ApiResult<QcStandardDTO.ViewDTO> copyBySku(@RequestParam("skuNo") String skuNo) {
        QcStandardDTO.ViewDTO viewDTO = qcStandardService.copyBySku(skuNo);
        if(Objects.isNull(viewDTO)){
            return failure(ApiError.QC_STANDARD_NOT_FOUND.getMsg(),null);
        }
        return success(viewDTO);
    }

    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:qcStandard:view",
            serviceClass = QcStandardService.class,
            keyIdName = "id")
    public ApiResult<QcStandardDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(qcStandardService.view(id));
    }
}
