package com.erp.server.dmp.controller.api;


import java.util.List;

import javax.annotation.Resource;

import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO;
import com.erp.server.dmp.query.AdsErpDiffOutstockSyncQueryHandler;
import com.erp.server.dmp.query.AdsErpDiffReturnInstockSyncQueryHandler;
import com.erp.server.dmp.service.AdsErpDiffReturnInstockSyncService;

import lombok.extern.slf4j.Slf4j;

/**
 * ERP退货入库单差异表
 *
 * @author shukai
 * @since 2025-11-19
 */
@Slf4j
@RestController
@LogSystemModule("ERP退货入库单差异表")
@RequestMapping("/adsErpDiffReturnInstockSync")
public class AdsErpDiffReturnInstockSyncController extends BaseController {

    @Resource
    private AdsErpDiffReturnInstockSyncService adsErpDiffReturnInstockSyncService;

    /**
    * 列表查询 菜单code = dmp:adsErpDiffReturnInstockSync:paging
    * @author shukai
    * @date: 2025-11-19
    * @param dto
    * @return ApiResult<PagingVO<AdsErpDiffReturnInstockSyncDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = AdsErpDiffReturnInstockSyncQueryHandler.class)
    public ApiResult<PagingVO<AdsErpDiffReturnInstockSyncDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto) {
        return success(adsErpDiffReturnInstockSyncService.paging(dto));
    }
    
    /**
     *  获取 tab列表
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/tabList")
    public ApiResult<List<AdsErpDiffReturnInstockSyncDTO.TabListDTO>> tabList(@RequestBody @Validated PermissionsDTO dto) {
        return success(adsErpDiffReturnInstockSyncService.tabList(dto));
    }

    /**
     *  统计
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/total")
    @WebAdvanceQuery(handler = AdsErpDiffReturnInstockSyncQueryHandler.class)
    public ApiResult<AdsErpDiffReturnInstockSyncDTO.TotalDTO> total(@RequestBody @Validated PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto) {
        return success(adsErpDiffReturnInstockSyncService.total(dto));
    }
    
    /**
     * 重新生成
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "ERP退货入库单差异表重新生成")
    @PostMapping(value = "/reCreate")
    public ApiResult<Boolean> reCreate(@RequestBody @Validated AdsErpDiffReturnInstockSyncDTO.ReCreateDTO dto) {
        return success(adsErpDiffReturnInstockSyncService.reCreate(dto));
    }
    
    /**
     * ERP数据更新
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "ERP退货入库单差异表ERP数据更新")
    @PostMapping(value = "/updateErp")
    @WebAdvanceQuery(handler = AdsErpDiffReturnInstockSyncQueryHandler.class)
    public ApiResult<Boolean> updateErp(@RequestBody @Validated AdsErpDiffReturnInstockSyncDTO.UpdateErpDTO dto) {
    	return success(adsErpDiffReturnInstockSyncService.updateErp(dto));
    }
    
    /**
     * 修改备注
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "ERP退货入库单差异表修改备注")
    @PostMapping(value = "/updateRemark")
    public ApiResult<Boolean> updateRemark(@RequestBody @Validated AdsErpDiffReturnInstockSyncDTO.UpdateRemarkDTO dto) {
    	return success(adsErpDiffReturnInstockSyncService.updateRemark(dto));
    }

    /**
     *  导出Excel
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "ERP退货入库单差异表导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated AdsErpDiffReturnInstockSyncDTO.ExpotParamDTO dto) {
        return success(adsErpDiffReturnInstockSyncService.exportExcel(dto));
    }

    /**
     * 查询朔源信息分页-平台出库单
     * @author jack
     * @date 2026-02-09
     * @param dto
     * @return ApiResult<PagingVO<adsErpDiffReturnInstockSyncService.SourcePlatformDTO>>
     */
    @PostMapping("/sourcePlatformPaging")
    public ApiResult<PagingVO<AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO>> sourcePlatformPaging(@RequestBody @Validated PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto) {
        return success(adsErpDiffReturnInstockSyncService.sourcePlatformPaging(dto));
    }

    /**
     * 导出朔源信息-平台出库单
     * @author jack
     * @date 2026-02-09
     * @param dto
     * @return ApiResult<Object>
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出朔源信息")
    @PostMapping(value = "/exportSourcePlatform")
    public ApiResult<Object>exportSourcePlatform(@RequestBody AdsErpDiffReturnInstockSyncDTO.PagingParamDTO dto) {
        Boolean flag = adsErpDiffReturnInstockSyncService.exportSourcePlatform(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询朔源信息分页-ERP出库单
     * @author jack
     * @date 2026-02-09
     * @param dto
     * @return ApiResult<PagingVO<adsErpDiffReturnInstockSyncService.SourcePlatformDTO>>
     */
    @PostMapping("/sourceSelfPaging")
    public ApiResult<PagingVO<AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO>> sourceSelfPaging(@RequestBody @Validated PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto) {
        return success(adsErpDiffReturnInstockSyncService.sourcePlatformPaging(dto));
    }

    /**
     * 导出朔源信息-ERP出库单
     * @author jack
     * @date 2026-02-09
     * @param dto
     * @return ApiResult<Object>
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出朔源信息")
    @PostMapping(value = "/exportSourceSelf")
    public ApiResult<Object>exportSourceSelf(@RequestBody AdsErpDiffReturnInstockSyncDTO.PagingParamDTO dto) {
        Boolean flag = adsErpDiffReturnInstockSyncService.exportSourceSelf(dto);
        return flag == true ? success() : failure();
    }
}
