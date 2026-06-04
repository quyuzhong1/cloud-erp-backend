package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDTO;
import com.erp.server.dmp.enums.InventoryMonthCheckEnum;
import com.erp.server.dmp.query.AdsErpReceiveFlowDiffQueryHandler;
import com.erp.server.dmp.service.AdsErpReceiveFlowDiffService;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 签收流水差异表
 * @author will
 * @date 2026/3/9 11:43
 */
@Slf4j
@RestController
@LogSystemModule("ERP签收流水差异表")
@RequestMapping("/adsErpReceiveFlowDiff")
public class AdsErpReceiveFlowDiffController extends BaseController {

    @Resource
    private AdsErpReceiveFlowDiffService adsErpReceiveFlowDiffService;

    @Resource
    private DmpCfgInputDetailService dmpCfgInputDetailService;

    /**
     *  分页查询
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = AdsErpReceiveFlowDiffQueryHandler.class)
    public ApiResult<PagingVO<AdsErpReceiveFlowDiffDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpReceiveFlowDiffDTO.PagingParamDTO> dto) {
        return success(adsErpReceiveFlowDiffService.paging(dto));
    }
    
    /**
     *  获取 tab列表
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/tabList")
    public ApiResult<List<AdsErpReceiveFlowDiffDTO.TabListDTO>> tabList(@RequestBody @Validated PermissionsDTO dto) {
        return success(adsErpReceiveFlowDiffService.tabList(dto));
    }

    /**
     *  统计
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/total")
    @WebAdvanceQuery(handler = AdsErpReceiveFlowDiffQueryHandler.class)
    public ApiResult<AdsErpReceiveFlowDiffDTO.TotalDTO> total(@RequestBody @Validated PagingDTO<AdsErpReceiveFlowDiffDTO.PagingParamDTO> dto) {
        return success(adsErpReceiveFlowDiffService.total(dto));
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
    public ApiResult<Boolean> reCreate(@RequestBody @Validated AdsErpReceiveFlowDiffDTO.ReCreateDTO dto) {
        dmpCfgInputDetailService.reCreateInventoryMonthCheck(InventoryMonthCheckEnum.ADS_ERP_RECEIVE_FLOW_DIFF, dto.getCheckMonth(), dto.getSourceSystem());
        return success(Boolean.TRUE);
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
    public ApiResult<Boolean> updateRemark(@RequestBody @Validated AdsErpReceiveFlowDiffDTO.UpdateRemarkDTO dto) {
    	return success(adsErpReceiveFlowDiffService.updateRemark(dto));
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
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated AdsErpReceiveFlowDiffDTO.ExportParamDTO dto) {
        return success(adsErpReceiveFlowDiffService.exportExcel(dto));
    }
}
