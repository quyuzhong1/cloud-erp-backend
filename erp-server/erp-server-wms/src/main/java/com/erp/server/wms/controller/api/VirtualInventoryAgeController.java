package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.server.wms.query.VirtualInventoryAgeQueryHandler;
import com.erp.server.wms.service.VirtualInventoryAgeService;
import com.erp.server.wms.service.VirtualInventoryDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 库龄分析
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@RestController
@LogSystemModule("库龄分析")
@RequestMapping("/virtualInventoryAge")
public class VirtualInventoryAgeController extends BaseController {

    @Resource
    private VirtualInventoryDetailService virtualInventoryDetailService;

    @Resource
    private VirtualInventoryAgeService virtualInventoryAgeService;

    /**
     * 获取配置表头
     * @author will
     * @date 2024/12/9 9:19
     * @return ApiResult<List<String>>
     */
    @GetMapping("/getCfgHead")
    public ApiResult<List<String>> getCfgHead() {
        return success(virtualInventoryAgeService.getCfgHead());
    }

    /**
     * 分页列表
     * @author will
     * @date 2024/12/3 17:33
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "vidh.warehouse_id",
            menuCode = "wms:virtualInventoryAge:paging"
    )
    @WebAdvanceQuery(handler = VirtualInventoryAgeQueryHandler.class)
    public ApiResult<PagingVO<VirtualInventoryAgeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto) {
        return success(virtualInventoryAgeService.paging(dto));
    }

    /**
     * 查看详情
     * @author will
     * @date 2024/12/4 16:18
     * @param dto
     * @return ApiResult<ViewDTO>
     */
    @LogViewService
    @PostMapping("/view")
    public ApiResult<VirtualInventoryAgeDTO.ViewDTO> view(@RequestBody @Validated VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        VirtualInventoryAgeDTO.ViewDTO view = virtualInventoryAgeService.view(dto);
        return success(view);
    }


    /**
     * 分页列表导出excel
     * @author will
     * @date 2024/12/3 18:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "库龄分析导出excel")
    @WebAdvanceQuery(handler = VirtualInventoryAgeQueryHandler.class)
    public ApiResult exportExcel(@RequestBody VirtualInventoryAgeDTO.SearchParamDTO dto) {
        Boolean flag = virtualInventoryAgeService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 库龄分析差异导出excel
     * @author will
     * @date 2024/12/3 18:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/diffExportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "库龄分析差异导出excel")
    @WebAdvanceQuery(handler = VirtualInventoryAgeQueryHandler.class)
    public ApiResult diffExportExcel(@RequestBody VirtualInventoryAgeDTO.SearchParamDTO dto) {
        Boolean flag = virtualInventoryAgeService.diffExportExcel(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 列表弹框分页
     * @author will
     * @date 2024/12/5 9:49
     * @param dto
     * @return ApiResult<PagingVO<HisInventoryAgeDetailDTO>>
     */
    @PostMapping("/framePaging")
    public ApiResult<PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO>> framePaging(@RequestBody @Validated PagingDTO<VirtualInventoryAgeDTO.FrameParamDTO> dto) {
        return success(virtualInventoryAgeService.framePaging(dto));
    }

    /**
     * 列表弹框分页导出excel
     * @author will
     * @date 2024/12/3 18:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/frameExportExcel")
    public ApiResult frameExportExcel(@RequestBody @Validated VirtualInventoryAgeDTO.FrameParamDTO dto) {
        Boolean flag = virtualInventoryAgeService.frameExportExcel(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 详情库龄图
     * @author will
     * @date 2024/12/9 9:47
     * @param dto
     * @return ApiResult<HisInventoryAgeChartDTO>
     */
    @PostMapping("/getHisInventoryAgeChart")
    public ApiResult<VirtualInventoryAgeDTO.HisInventoryAgeChartDTO> getHisInventoryAgeChart(@RequestBody VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        VirtualInventoryAgeDTO.HisInventoryAgeChartDTO chartDTO = virtualInventoryAgeService.getHisInventoryAgeChart(dto);
        return success(chartDTO);
    }

    /**
     * 详情历史库龄图导出excel
     * @author will
     * @date 2024/12/3 18:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/exportHisInventoryAge")
    @LogAction(value = LogActionEnum.EXPORT, desc = "详情历史库龄图导出excel")
    public ApiResult exportHisInventoryAge(@RequestBody @Validated VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        Boolean flag = virtualInventoryAgeService.exportHisInventoryAge(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 详情库龄明细分页查询
     * @author will
     * @date 2024/12/5 9:49
     * @param dto
     * @return ApiResult<PagingVO<HisInventoryAgeDetailDTO>>
     */
    @PostMapping("/hisInventoryAgeDetailPaging")
    public ApiResult<PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO>> hisInventoryAgeDetailPaging(@RequestBody @Validated PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO> dto) {
        return success(virtualInventoryAgeService.hisInventoryAgeDetailPaging(dto));
    }

    /**
     * 详情历史库龄明细
     * @author will
     * @date 2024/12/10 17:28
     * @param dto
     * @return ApiResult<viewHisInventoryAgeDetailDTO>
     */
    @PostMapping("/viewHisInventoryAgeDetail")
    public ApiResult<VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO> viewHisInventoryAgeDetail(@RequestBody @Validated VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto) {
        return success(virtualInventoryAgeService.viewHisInventoryAgeDetail(dto));
    }

    /**
     * 详情历史库龄明细导出excel
     * @author will
     * @date 2024/12/3 18:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/exportHisInventoryAgeDetail")
    @LogAction(value = LogActionEnum.EXPORT, desc = "详情历史库龄明细导出excel")
    public ApiResult exportHisInventoryAgeDetail(@RequestBody @Validated VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto) {
        Boolean flag = virtualInventoryAgeService.exportHisInventoryAgeDetail(dto);
        return flag == true ? success() : failure();
    }


}
