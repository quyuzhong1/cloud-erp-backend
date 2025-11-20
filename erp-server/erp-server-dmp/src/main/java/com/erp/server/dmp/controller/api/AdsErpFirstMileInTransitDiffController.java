package com.erp.server.dmp.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
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
import com.common.core.utils.ExcelUtil;
import com.erp.model.dmp.dto.AdsErpFirstMileInTransitDiffDTO;
import com.erp.model.dmp.entity.doris.AdsErpFirstMileInTransitDiffEntity;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffEntity;
import com.erp.server.dmp.service.AdsErpFirstMileInTransitDiffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台在途报告
 *
 * @author Jim
 * @since 2025-11-13
 */
@Slf4j
@RestController
@LogSystemModule("平台在途报告")
@RequestMapping("/adsErpFirstMileInTransitDiff")
public class AdsErpFirstMileInTransitDiffController extends BaseController {

    @Resource
    private AdsErpFirstMileInTransitDiffService adsErpFirstMileInTransitDiffService;

    /**
     * 列表查询
     *
     * @param dto
     * @return ApiResult<PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO>>
     * @author Jim
     * @date: 2025-11-13
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpFirstMileInTransitDiff:paging",
            tableAlias = "aefmid"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpFirstMileInTransitDiffDTO.PagingParamDTO> dto) {
        return success(adsErpFirstMileInTransitDiffService.paging(dto));
    }

    /**
     * 详情
     *
     * @param id
     * @return ApiResult<AdsErpFirstMileInTransitDiffDTO.ViewDTO>>
     * @author Jim
     * @date: 2025-11-13
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpFirstMileInTransitDiff:view",
            serviceClass = AdsErpFirstMileInTransitDiffService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<AdsErpFirstMileInTransitDiffDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(adsErpFirstMileInTransitDiffService.view(id));
    }

    /**
     * 下载平台期初在途导入模板
     *
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载平台期初在途导入模板")
    @GetMapping("/downloadInitTemplate")
    public ApiResult<Object> downloadInitTemplate(HttpServletResponse response) {
        String path = "excel/adsErpFirstMileInTransitInit.xlsx";
        String excelName = "头程平台期初在途导入模板.xlsx";
        ExcelUtil.downloadTemplate(path, excelName, response);
        return success();
    }

    /**
     * 下载平台在途调整导入模板
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载平台在途调整导入模板")
    @GetMapping("/downloadAdjustTemplate")
    public ApiResult<Object> downloadAdjustTemplate(HttpServletResponse response) {
        String path = "classpath:excel/adsErpFirstMileInTransitAdjust.xlsx";
        String excelName = "头程平台调整在途导入模板.xlsx";
        ExcelUtil.downloadTemplate(path, excelName, response);
        return success();
    }

    /**
     * 期初在途导入Excel
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "期初在途导入Excel")
    @PostMapping("/importInitFile")
    public ApiResult<?> importInitFile(@ModelAttribute @Validated AdsErpFirstMileInTransitDiffDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        Boolean result = adsErpFirstMileInTransitDiffService.importInitFile(excelImportDTO.getExcelFile(), response);
        return result ? success() : failure();
    }


    /**
     * 在途调整导入Excel
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入Excel")
    @PostMapping("/importAdjustFile")
    public ApiResult<?> importAdjustFile(@ModelAttribute @Validated AdsErpFirstMileInTransitDiffDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        Boolean result = adsErpFirstMileInTransitDiffService.importAdjustFile(excelImportDTO.getExcelFile(), response);
        return result ? success() : failure();
    }

    /**
     * 导出Excel数据
     *
     * @param dto
     * @param response
     * @return
     * @author Jim
     * @date: 2025-11-13
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpFirstMileInTransitDiff:export",
            tableAlias = "aefmid"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "平台在途报告导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated AdsErpFirstMileInTransitDiffDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = adsErpFirstMileInTransitDiffService.exportList(dto, response);
        return result ? ApiResult.success(result) : failure(result);
    }


    /**
     * 期末在途调整
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "期末在途调整")
    @PostMapping(value = "/adjustTransitQty")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:adsErpFirstMileInTransitDiff:adjustTransitQty",
            serviceClass = AdsErpFirstMileInTransitDiffService.class,
            keyIdName = "id")
    public ApiResult<Boolean> adjustTransitQty(@RequestBody AdsErpFirstMileInTransitDiffDTO.AdjustDTO adjustDTO) {
        Boolean flag = adsErpFirstMileInTransitDiffService.adjustTransitQty(adjustDTO);
        return flag ? success() : failure();
    }

    /**
     * 编辑备注
     * @author Jim
     * @date: 2025-11-13
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "编辑备注")
    public ApiResult<List<BatchResultDTO>> updateRemark(@RequestBody @Validated BaseIdsDTO.BlankRemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = adsErpFirstMileInTransitDiffService.updateRemark(id,dto.getRemark());
            }catch (Exception e){
                log.error("平台在途报告编辑备注",e);
                AdsErpFirstMileInTransitDiffEntity entity = adsErpFirstMileInTransitDiffService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "平台在途报告不存在,平台在途报告备注失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
