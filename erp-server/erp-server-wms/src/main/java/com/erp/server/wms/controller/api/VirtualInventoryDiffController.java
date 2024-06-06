package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.server.wms.service.VirtualInventoryDiffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 虚拟库存表
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@RestController
@RequestMapping("/virtualInventoryDiff")
public class VirtualInventoryDiffController extends BaseController {

    @Resource
    private VirtualInventoryDiffService virtualInventoryDiffService;

   /**
    * 库存差异列表
    * @author will
    * @date 2024/6/3 16:27
    * @param dto
    * @return ApiResult<PagingVO<ListDTO>>
    */
    @PostMapping("/diffPaging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<VirtualInventoryDiffDTO.ListDTO>> diffPaging(@RequestBody @Validated PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        return success(virtualInventoryDiffService.diffPaging(dto));
    }

    /**
     * 库存差异明细列表
     * @author will
     * @date 2024/6/3 16:45
     * @param dto
     * @return ApiResult<PagingVO<ListDetailDTO>>
     */
    @PostMapping("/diffDetailPaging")
    public ApiResult<PagingVO<VirtualInventoryDiffDTO.ListDetailDTO>> diffDetailPaging(@RequestBody @Validated PagingDTO<VirtualInventoryDiffDTO.SearchParamDetailDTO> dto) {
        return success(virtualInventoryDiffService.diffDetailPaging(dto));
    }


    /**
     * 库存差异列表导出
     * @author will
     * @date 2024/6/3 17:57
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping("/exportExcel")
    @WebAdvanceQuery
    public ApiResult exportExcel(@RequestBody VirtualInventoryDiffDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = virtualInventoryDiffService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }
}
