package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.entity.TmsFirstMileReconciliationDetailEntity;
import com.erp.model.tms.enums.CfgReconciliationTypeEnum;
import com.erp.server.tms.query.TmsFirstMileReconciliationDetailQueryHandler;
import com.erp.server.tms.service.CfgReconciliationFieldService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.TmsFirstMileReconciliationDetailService;
import com.erp.server.tms.service.TmsFirstMileReconciliationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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
    @Resource
    private CfgReconciliationFieldService cfgReconciliationFieldService;
    @Resource
    private TmsFirstMileReconciliationService tmsFirstMileReconciliationService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;


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
    public ApiResult<Object> exportTemplate(@ModelAttribute @Validated TmsFirstMileReconciliationDetailDTO.ExcelDownloadTemplateDTO dto, HttpServletRequest request, HttpServletResponse response) {
        switch (dto.getTypeEnum()) {
            case STANDARD:
                String standardPath = "classpath:excel/firstMileReconciliationDetailTemplate.xlsx";
                String standardExcelName = "templateStandard.xlsx";
                ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
                return success();
            case CONFIG:
                String logisticSupplierId =  tmsFirstMileReconciliationService.checkAndGetSupplier(dto.getId());
                // SCM来源物流商ID
                String supplierId = logisticsSupplierService.getByIdOpt(logisticSupplierId).orElseThrow(() -> new ServiceException("物流供应商不存在")).getSupplierId();
                LinkedList<String> headerNameList = cfgReconciliationFieldService.thirdFieldListName(Collections.singletonList(CfgReconciliationTypeEnum.FIRST_MILE.getCode()), supplierId, true);
                headerNameList.addFirst("币种");
                if (!headerNameList.contains("物流运单号")){
                	headerNameList.addFirst("物流运单号");
                }
                // 去重
                String configExcelName = "templateConfig.xlsx";
                ExcelUtil.downloadDynamicTemplate(headerNameList, configExcelName, response);
                return success();
            default:
                throw new ServiceException("输入导入的类型有误");
        }
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
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "logistics_supplier_id",
//            menuCode = "tms:tmsFirstMileReconciliationDetail:waitPaging",
//            tableAlias = "lb"
//    )
    @WebAdvanceQuery(handler = TmsFirstMileReconciliationDetailQueryHandler.class)
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
            menuCode = "tms:tmsFirstMileReconciliationDetail:waitPaging",
            tableAlias = "tfmrd"
    )
    public ApiResult<List<TmsFirstMileReconciliationDetailDTO.ListDTO>> addWaitList(@RequestBody @Validated TmsFirstMileReconciliationDetailDTO.AddWaitListDTO dto) {
        return success(tmsFirstMileReconciliationDetailService.addWaitReconciliation(dto.getSourceIdList(),dto.getSupplierType(),dto.getLogisticsSupplierId()));
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
    public ApiResult<Boolean> exportList(@RequestBody @Validated TmsFirstMileReconciliationDetailDTO.ExportDTO dto) {
        tmsFirstMileReconciliationDetailService.exportList(dto);
        return success(true);
    }

    /**
     * 重置总物流单费用
     * @param codeList
     * @return
     */
    @PostMapping("/initTotalLogisticsCost")
    public ApiResult<Object>initTotalLogisticsCost(@RequestBody List<String> codeList){
        tmsFirstMileReconciliationDetailService.initTotalLogisticsCost(codeList);
        return ApiResult.success();
    }
}
