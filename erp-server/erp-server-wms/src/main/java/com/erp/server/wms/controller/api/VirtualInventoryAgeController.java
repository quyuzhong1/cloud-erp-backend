package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.server.wms.service.VirtualInventoryDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
     * 导出excel
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



}
