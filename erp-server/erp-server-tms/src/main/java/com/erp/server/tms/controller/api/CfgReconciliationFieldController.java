package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.tms.entity.CfgReconciliationFieldEntity;
import com.erp.server.tms.query.CfgReconciliationFieldQueryHandler;
import com.erp.server.tms.service.CfgReconciliationFieldService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.TmsFirstMileReconciliationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 对账字段配置
 *
 * @author Jim
 * @since 2024-03-25
 */
@Slf4j
@RestController
@LogSystemModule("对账字段配置")
@RequestMapping("/cfgReconciliationField")
public class CfgReconciliationFieldController extends BaseController {

    @Resource
    private CfgReconciliationFieldService cfgReconciliationFieldService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    /**
     * 修改
     *
     * @param dto DTO
     * @return ApiResult
     * @author Jim
     * {@code @date:}  2024-03-25
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "对账字段配置表修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgReconciliationField:update",
            serviceClass = CfgReconciliationFieldService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgReconciliationFieldDTO.UpdateDTO dto) {
        cfgReconciliationFieldService.update(dto);
        return success();
    }

    /**
     * 分页
     *
     * @param dto DTO
     * @return ApiResult
     * @author Jim
     * {@code @date:}  2024-03-25
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = CfgReconciliationFieldQueryHandler.class)
    public ApiResult<PagingVO<CfgReconciliationFieldDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<CfgReconciliationFieldDTO.PagingParamDTO> dto) {
        PagingVO<CfgReconciliationFieldDTO.PagingVO> pagingVO = cfgReconciliationFieldService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 删除
     *
     * @param dto DTO
     * @return ApiResult<List < BatchResultDTO>>
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:cfgReconciliationField:delete",
            serviceClass = TmsFirstMileReconciliationService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "对账字段配置删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = cfgReconciliationFieldService.delete(id);
            } catch (Exception e) {
                log.error("对账字段配置删除失败", e);
                CfgReconciliationFieldEntity entity = cfgReconciliationFieldService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "对账字段配置不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 查看详情
     *
     * @param id DTO
     * @return ApiResult<List < BatchResultDTO>>
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @LogViewService
    @GetMapping("/view")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "warehouse_keeper_id",
//            menuCode = "wms:cfgReconciliationField:view",
//            serviceClass = TmsFirstMileReconciliationService.class,
//            keyIdName = "id")
    public ApiResult<CfgReconciliationFieldDTO.ViewDTO> view(@RequestParam("id") String id) {
        CfgReconciliationFieldDTO.ViewDTO dto = cfgReconciliationFieldService.view(id);
        return success(dto);
    }

    /**
     * 导出
     *
     * @param dto DTO
     * @return ApiResult<?>
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出对账字段配置")
    @PostMapping(value = "/exportExcel")
    public ApiResult<?> exportExcel(@RequestBody @Validated CfgReconciliationFieldDTO.PagingParamDTO dto) {
        Boolean flag = cfgReconciliationFieldService.exportExcel(dto);
        return flag ? success() : failure();
    }


    /**
     * 下载导入模板
     *
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @GetMapping("/downloadTemplate")
    public ApiResult<?> downloadTemplate(HttpServletResponse response) {
        cfgReconciliationFieldService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入
     *
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入对账字段配置")
    @PostMapping("/import")
    public ApiResult<?> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = cfgReconciliationFieldService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 数大臣字段列表
     * reconciliationType=核对类型(空=所有)
     * 来源/tms/common/enumDropDown?type=CfgReconciliationType
     *
     * @author Jim
     * {@code @date:} 2024-03-25
     */
    @PostMapping("/erpFieldList")
    public ApiResult<?> erpFieldList(@RequestBody List<String> reconciliationTypeList) {
        List<CfgReconciliationFieldDTO.ErpFieldDropDownDTO> list = cfgReconciliationFieldService.erpFieldList(reconciliationTypeList);
        return success(list);
    }

    /**
     * 物流商列表supperId
     */
    @GetMapping("/logisticsSupplierList")
    public ApiResult<List<BaseDropDownDTO.SupplierDisabledDTO>> listLogisticsSupplier(@RequestParam(name = "reconciliationTypeList", required = false) List<String> reconciliationTypeList) {
        List<BaseDropDownDTO.SupplierDisabledDTO> list = cfgReconciliationFieldService.logisticsSupplierList(reconciliationTypeList);
        return success(list);
    }

}
