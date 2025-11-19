package com.erp.server.dmp.controller.api;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO;
import com.erp.server.dmp.query.AdsErpDiffOutstockSyncQueryHandler;
import com.erp.server.dmp.service.AdsErpDiffOutstockSyncService;

import lombok.extern.slf4j.Slf4j;

/**
 * ERP出库单差异表
 *
 * @author shukai
 * @since 2025-11-18
 */
@Slf4j
@RestController
@LogSystemModule("ERP出库单差异表")
@RequestMapping("/adsErpDiffOutstockSync")
public class AdsErpDiffOutstockSyncController extends BaseController {

    @Resource
    private AdsErpDiffOutstockSyncService adsErpDiffOutstockSyncService;

    /**
    * 列表查询 菜单code = dmp:adsErpDiffOutstockSync:paging
    * @author shukai
    * @date: 2025-11-18
    * @param dto
    * @return ApiResult<PagingVO<AdsErpDiffOutstockSyncDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = AdsErpDiffOutstockSyncQueryHandler.class)
    public ApiResult<PagingVO<AdsErpDiffOutstockSyncDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto) {
        return success(adsErpDiffOutstockSyncService.paging(dto));
    }
    
    /**
     *  获取 tab列表
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/tabList")
    public ApiResult<List<AdsErpDiffOutstockSyncDTO.TabListDTO>> tabList(@RequestBody @Validated PermissionsDTO dto) {
        return success(adsErpDiffOutstockSyncService.tabList(dto));
    }

    /**
     *  统计
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/total")
    @WebAdvanceQuery(handler = AdsErpDiffOutstockSyncQueryHandler.class)
    public ApiResult<AdsErpDiffOutstockSyncDTO.TotalDTO> total(@RequestBody @Validated PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto) {
        return success(adsErpDiffOutstockSyncService.total(dto));
    }
    
    /**
     * 重新生成
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "ERP出库单差异表重新生成")
    @PostMapping(value = "/reCreate")
    public ApiResult<Boolean> reCreate(@RequestBody @Validated AdsErpDiffOutstockSyncDTO.ReCreateDTO dto) {
        return success(adsErpDiffOutstockSyncService.reCreate(dto));
    }
    
    /**
     * ERP数据更新
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "ERP出库单差异表ERP数据更新")
    @PostMapping(value = "/updateErp")
    public ApiResult<Boolean> updateErp(@RequestBody @Validated AdsErpDiffOutstockSyncDTO.UpdateErpDTO dto) {
    	return success(adsErpDiffOutstockSyncService.updateErp(dto));
    }
    
    /**
     * 修改备注
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "ERP出库单差异表修改备注")
    @PostMapping(value = "/updateRemark")
    public ApiResult<Boolean> updateRemark(@RequestBody @Validated AdsErpDiffOutstockSyncDTO.UpdateRemarkDTO dto) {
    	return success(adsErpDiffOutstockSyncService.updateRemark(dto));
    }

    /**
     *  导出Excel
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "ERP出库单差异表导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated AdsErpDiffOutstockSyncDTO.ExpotParamDTO dto) {
        return success(adsErpDiffOutstockSyncService.exportExcel(dto));
    }
}
