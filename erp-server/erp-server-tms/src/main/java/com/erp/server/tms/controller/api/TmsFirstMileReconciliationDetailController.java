package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.erp.server.tms.query.TmsB2cDeclareReconciliationDetailQueryHandler;
import com.erp.server.tms.query.TmsFirstMileReconciliationDetailQueryHandler;
import com.erp.server.tms.query.TmsFirstMileReconciliationQueryHandler;
import com.erp.server.tms.service.TmsFirstMileReconciliationDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 头程对账单明细
 *
 * @author Jim
 * @since 2024-03-25
 */
@Slf4j
@RestController
@LogSystemModule("头程对账单明细")
@RequestMapping("/tmsFirstMileReconciliationDetail")
public class TmsFirstMileReconciliationDetailController extends BaseController {

    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;


    /**
     * 更新对账状态
     *
     * @param dto DTO
     * @return ApiResult<String>
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.INSERT, desc = "更新对账状态")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody @Validated TmsFirstMileReconciliationDetailDTO.UpdateStatusDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = tmsFirstMileReconciliationDetailService.updateStatus(id, dto.getStatus());
            } catch (Exception e) {
                log.error("头程对账单明细 更新对账状态失败", e);
                TmsFirstMileReconciliationDetailEntity entity = tmsFirstMileReconciliationDetailService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "头程对账单明细不存在, 更新对账状态失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下载模板
     *
     * @return ApiResult<String>
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载头程对账单模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<?> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/firstMileReconciliationDetailTemplate.xlsx";
        String excelName = "template.xlsx";
        ExcelUtil.downloadTemplate(path, excelName, response);
        return success();
    }

    /**
     * 导入
     *
     * @return ApiResult<ImportDTO>
     * @author Jim
     * {@code @date:} 2024-03-25
     */
//    @LogAction(value = LogActionEnum.IMPORT, desc = "导入对账单明细")
    @PostMapping("/importFile")
    public ApiResult<TmsFirstMileReconciliationDetailDTO.ImportDTO> importFile(@ModelAttribute @Validated TmsFirstMileReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        TmsFirstMileReconciliationDetailDTO.ImportDTO importDTO = tmsFirstMileReconciliationDetailService.importFile(excelImportDTO, response);
        return success(importDTO);
    }

    /**
     * 待对账分页查询
     *
     * @param dto DTO
     * @return ApiResult<String>
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @PostMapping("/waitPaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliationDetail:paging",
            tableAlias = "tfmrd"
    )
    @WebAdvanceQuery(handler = TmsB2cDeclareReconciliationDetailQueryHandler.class)
    public ApiResult<PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO>> waitPaging(@RequestBody @Validated PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> dto) {
        return success(tmsFirstMileReconciliationDetailService.waitReconciliationPaging(dto));
    }


    /**
     * 确定添加待对账分账列表(提交预估)响应：预估/实际/差异
     *
     * @param dto 来源ID集合
     * @return ApiResult<List<TmsFirstMileReconciliationDetailDTO.ListDTO>>
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @PostMapping("/addWaitList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliationDetail:paging",
            tableAlias = "tfmrd"
    )
    public ApiResult<List<TmsFirstMileReconciliationDetailDTO.ListDTO>> addWaitList(@RequestBody @Validated TmsFirstMileReconciliationDetailDTO.AddWaitListDTO dto) {
        return success(tmsFirstMileReconciliationDetailService.addWaitReconciliation(dto.getSourceIdList()));
    }

    /**
     * 导出Excel数据
     *
     * @param dto DTO
     * @author Jim
     * {@code @date:}2024-03-25
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileReconciliationDetail:export",
            tableAlias = "tfmrd"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "头程对账单导出Excel数据")
    public void exportList(@RequestBody @Validated TmsFirstMileReconciliationDetailDTO.ExportDTO dto, HttpServletResponse response) {
        tmsFirstMileReconciliationDetailService.exportList(dto, response);
    }

}
