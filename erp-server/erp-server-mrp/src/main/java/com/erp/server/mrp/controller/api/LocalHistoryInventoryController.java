package com.erp.server.mrp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.LocalHistoryInventoryDTO;
import com.erp.server.mrp.service.LocalHistoryInventoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;

/**
 * <p>
 * 本地仓库存 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2024-11-08
 */
@RestController
@RequestMapping("/local-history-inventory")
public class LocalHistoryInventoryController extends BaseController {

    @Resource
    private LocalHistoryInventoryService localHistoryInventoryService;

    /**
     * 即时库存分页列表
     * @param dto 参数
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<LocalHistoryInventoryDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<LocalHistoryInventoryDTO.SearchParamDTO> dto) {
        return success(localHistoryInventoryService.paging(dto));
    }


    /**
     * 即时库存导出
     * @param dto 参数
     * @return
     */
    @PostMapping(value = "/exportInventoryExcel")
    public ApiResult<Boolean> exportInventoryExcel(@RequestBody LocalHistoryInventoryDTO.ExportDTO dto) {
        localHistoryInventoryService.exportExcel(dto);
        return success(true);
    }
}
