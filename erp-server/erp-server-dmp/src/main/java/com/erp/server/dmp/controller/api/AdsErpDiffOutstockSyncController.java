package com.erp.server.dmp.controller.api;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ClientTypeEnum;
import com.erp.model.wms.entity.SampleBorrowInfoEntity;
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
import com.erp.server.dmp.enums.InventoryMonthCheckEnum;
import com.erp.server.dmp.query.AdsErpDiffOutstockSyncQueryHandler;
import com.erp.server.dmp.service.AdsErpDiffOutstockSyncService;
import com.erp.server.dmp.service.DmpCfgInputDetailService;

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
    
    @Resource
    private DmpCfgInputDetailService dmpCfgInputDetailService;

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
    	dmpCfgInputDetailService.reCreateInventoryMonthCheck(InventoryMonthCheckEnum.ADS_ERP_DIFF_OUTSTOCK_SYNC, dto.getCheckMonth(), dto.getSourceSystem());
        return success(true);
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

    /**
     * 查询朔源信息分页-平台出库单
     * @author jack
     * @date 2026-02-09
     * @param dto
     * @return ApiResult<PagingVO<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO>>
     */
    @PostMapping("/sourcePlatformPaging")
    public ApiResult<PagingVO<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO>> sourcePlatformPaging(@RequestBody @Validated PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto) {
        dto.getParams().setType("clean_diff_outstock_sync_source_platform");
        return success(adsErpDiffOutstockSyncService.sourcePlatformPaging(dto));
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
    public ApiResult<Object>exportSourcePlatform(@RequestBody @Validated PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto) {
        dto.getParams().setType("clean_diff_outstock_sync_source_platform");
        Boolean flag = adsErpDiffOutstockSyncService.exportSourcePlatform(dto.getParams());
        return flag == true ? success() : failure();
    }

    /**
     * 查询朔源信息分页-ERP出库单
     * @author jack
     * @date 2026-02-09
     * @param dto
     * @return ApiResult<PagingVO<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO>>
     */
    @PostMapping("/sourceSelfPaging")
    public ApiResult<PagingVO<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO>> sourceSelfPaging(@RequestBody @Validated PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto) {
        dto.getParams().setType("clean_diff_outstock_sync_source_self");
        return success(adsErpDiffOutstockSyncService.sourceSelfPaging(dto));
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
    public ApiResult<Object>exportSourceSelf(@RequestBody @Validated PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto) {
        dto.getParams().setType("clean_diff_outstock_sync_source_self");
        Boolean flag = adsErpDiffOutstockSyncService.exportSourceSelf(dto.getParams());
        return flag == true ? success() : failure();
    }

    /**
     * 批量修改--查询
     * @author jack
     * @date 2026-02-11
     * @param dto
     */
    @PostMapping("/listPlateformOutstockNotExistRelation")
    public ApiResult<List<AdsErpDiffOutstockSyncDTO.PlateformOutstockNotExistRelationDTO>> listPlateformOutstockNotExistRelation(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(adsErpDiffOutstockSyncService.listPlateformOutstockNotExistRelation(dto));
    }


    /**
     * 批量修改--查询erp出库单
     * @author jack
     * @date 2026-02-11
     * @param dto
     */
    @PostMapping("/listErpOutstockByParams")
    public ApiResult<List<AdsErpDiffOutstockSyncDTO.ErpOutstockResultDTO>> listErpoutstockByParams(@RequestBody @Validated AdsErpDiffOutstockSyncDTO.ErpOutstockParamsDTO dto) {
        return success(adsErpDiffOutstockSyncService.listErpOutstockByParams(dto));
    }

    /**
     * 批量修改
     * @author jack
     * @date 2026-02-11
     * @param dto
     */
    @PostMapping("/batchUpdateOutstockRelation")
    public ApiResult<List<BatchResultDTO>> batchUpdateOutstockRelation(@RequestBody @Validated AdsErpDiffOutstockSyncDTO.BatchUpdateParamsDTO dto) {
        List<AdsErpDiffOutstockSyncDTO.PlateformOutstockNotExistRelationDTO> list = dto.getList();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());
        for (AdsErpDiffOutstockSyncDTO.PlateformOutstockNotExistRelationDTO relationDTO : list) {
            BatchResultDTO result;
            try {
                result = adsErpDiffOutstockSyncService.batchUpdateOutstockRelation(relationDTO);
            }catch (Exception e){
                result = BatchResultDTO.fail(relationDTO.getId(), relationDTO.getPlatformOutstockCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
