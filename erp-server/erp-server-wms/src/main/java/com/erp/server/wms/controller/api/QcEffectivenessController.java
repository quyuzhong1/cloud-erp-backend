package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.server.wms.service.QcEffectivenessService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 品质时效表
 * @author Will
 * @version 1.0
 * @date 2023/4/12 11:16
 */
@RestController
@LogSystemModule("品质时效报表")
@RequestMapping("/qcEffectiveness")
public class QcEffectivenessController extends BaseController {

    @Resource
    private QcEffectivenessService qcEffectivenessService;

    /**
     * 质检总览查询
     * @author Will
     * @date: 2023/4/12 11:26
     * @param dto
     * @return ApiResult<ViewQcOverviewDTO>
     */
    @PostMapping("/viewQcOverview")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            menuCode = "wms:qcEffectiveness:viewQcOverview",
            tableAlias = "qb"
    )
    public ApiResult<QcEffectivenessDTO.ViewQcOverviewDTO> viewQcOverview(@RequestBody @Validated QcEffectivenessDTO.CommonSearchParamDTO dto) {
        QcEffectivenessDTO.ViewQcOverviewDTO result = qcEffectivenessService.viewQcOverview(dto);
        return success(result);
    }

    /**
     * 质检趋势查询
     * @author Will
     * @date: 2023/4/12 11:26
     * @param dto
     * @return ApiResult<List<ViewQcTrendDTO>>
     */
    @PostMapping("/viewQcTrend")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            menuCode = "wms:qcEffectiveness:viewQcOverview",
            tableAlias = "qb"
    )
    public ApiResult<QcEffectivenessDTO.ViewQcTrendDTO> viewQcTrend(@RequestBody @Validated QcEffectivenessDTO.ViewQcTrendSearchParamDTO dto) {
        QcEffectivenessDTO.ViewQcTrendDTO result = qcEffectivenessService.viewQcTrend(dto);
        return success(result);
    }

    /**
     * 按人员查询
     * @author Will
     * @date: 2023/4/12 11:32
     * @param dto
     * @return ApiResult<List<ViewQcTrendDTO>>
     */
    @PostMapping("/viewQcForPersonnel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            menuCode = "wms:qcEffectiveness:viewQcOverview",
            tableAlias = "qb"
    )
    public ApiResult<PagingVO<QcEffectivenessDTO.ViewQcForPersonnelDTO>> viewQcForPersonnel(@RequestBody @Validated PagingDTO<QcEffectivenessDTO.CommonSearchParamDTO> dto) {
        PagingVO<QcEffectivenessDTO.ViewQcForPersonnelDTO> list = qcEffectivenessService.viewQcForPersonnel(dto);
        return success(list);
    }

    /**
     * 按单据查询
     * @author Will
     * @date: 2023/4/12 11:32
     * @param dto
     * @return ApiResult<List<ViewQcTrendDTO>>
     */
    @PostMapping("/viewQcForDocument")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            menuCode = "wms:qcEffectiveness:viewQcOverview",
            tableAlias = "qb"
    )
    public ApiResult<PagingVO<QcEffectivenessDTO.ViewQcForDocumentDTO>> viewQcForDocument(@RequestBody @Validated PagingDTO<QcEffectivenessDTO.ViewQcForDocumentSearchParamDTO> dto) {
        PagingVO<QcEffectivenessDTO.ViewQcForDocumentDTO> list = qcEffectivenessService.viewQcForDocument(dto);
        return success(list);
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/4/12 12:25
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出品质时")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody QcEffectivenessDTO.ExportExcelSearchParamDTO dto) {
        Boolean flag = qcEffectivenessService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

}
