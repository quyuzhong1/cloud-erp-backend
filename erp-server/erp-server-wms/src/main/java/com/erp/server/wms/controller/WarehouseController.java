package com.erp.server.wms.controller;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehousePagingParamDTO;
import com.erp.model.wms.dto.WarehousePagingViewDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 仓库管理
 * @author Lambda
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/warehouse")
public class WarehouseController extends BaseController {



    /**
     * 仓库分页列表
     *
     * @param
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<WarehousePagingViewDTO>> paging(@RequestBody @Validated PagingDTO<WarehousePagingParamDTO> dto) {
        return success();
    }

    /**
     * 保存或者修改仓库
     *
     * @param
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated WarehouseDTO dto) {
        return success();
    }



    /**
     * 导入仓库
     */
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        return success();
    }

    /**
     * 导出模板
     * @return
     */
    @PostMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        return success();
    }

    /**
     * 导出仓库数据
     * @return
     */
    @PostMapping("/exportWarehouse")
    public ApiResult exportTemplate(@RequestBody WarehousePagingParamDTO dto) {
        return success();
    }
}
