package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.SupplierInventoryDTO;
import com.erp.server.wms.service.SupplierInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 仓库绑定
 *
 * @author will
 * @since 2025-06-18
 */
@Slf4j
@RestController
@LogSystemModule("供应商关联仓库表")
@RequestMapping("/supplierInventory")
public class SupplierInventoryController extends BaseController {

    @Resource
    private SupplierInventoryService supplierInventoryService;

    /**
     * 分页查询
     * @author will
     * @date 2025/6/18 16:58
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SupplierInventoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SupplierInventoryDTO.PagingParamDTO> dto) {
        return success(supplierInventoryService.paging(dto));
    }


    /**
     * 导出
     * @author will
     * @date 2025/6/18 17:14
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/exportExcel")
    @WebAdvanceQuery
    public ApiResult exportExcel(@RequestBody SupplierInventoryDTO.PagingParamDTO dto) {
        Boolean flag = supplierInventoryService.exportExcel(dto);
        return flag ? success() : failure();
    }
}
