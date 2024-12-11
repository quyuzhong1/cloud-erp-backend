package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
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

    /**
     * 获取配置表头
     * @author will
     * @date 2024/12/9 9:19
     * @return ApiResult<List<String>>
     */
    @GetMapping("/getCfgHead")
    public ApiResult<List<String>> getCfgHead() {
        return success(virtualInventoryDetailService.getCfgHead());
    }

    /**
     * 分页列表
     * @author will
     * @date 2024/12/3 17:33
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<VirtualInventoryAgeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualInventoryAgeDTO.SearchParamDTO> dto) {
        return success(virtualInventoryDetailService.paging(dto));
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
        VirtualInventoryAgeDTO.ViewDTO view = virtualInventoryDetailService.view(dto);
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
    @WebAdvanceQuery
    public ApiResult exportExcel(@RequestBody VirtualInventoryAgeDTO.SearchParamDTO dto) {
        Boolean flag = virtualInventoryDetailService.exportExcel(dto);
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
        return success(virtualInventoryDetailService.framePaging(dto));
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
        Boolean flag = virtualInventoryDetailService.frameExportExcel(dto);
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
        VirtualInventoryAgeDTO.HisInventoryAgeChartDTO chartDTO = virtualInventoryDetailService.getHisInventoryAgeChart(dto);
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
    public ApiResult exportHisInventoryAge(@RequestBody @Validated VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto) {
        Boolean flag = virtualInventoryDetailService.exportHisInventoryAge(dto);
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
        return success(virtualInventoryDetailService.hisInventoryAgeDetailPaging(dto));
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
        return success(virtualInventoryDetailService.viewHisInventoryAgeDetail(dto));
    }

    /**
     * 详情历史库龄明细导出excel
     * @author will
     * @date 2024/12/3 18:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/exportHisInventoryAgeDetail")
    public ApiResult exportHisInventoryAgeDetail(@RequestBody @Validated VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto) {
        Boolean flag = virtualInventoryDetailService.exportHisInventoryAgeDetail(dto);
        return flag == true ? success() : failure();
    }


}
