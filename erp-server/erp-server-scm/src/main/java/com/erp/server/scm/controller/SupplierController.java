package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.SupplierPagingParamDTO;
import com.erp.model.scm.dto.SupplierPagingViewDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 供应商管理
 *
 * @author yl
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier")
public class SupplierController extends BaseController {


    /**
     * 供应商分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SupplierPagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierPagingParamDTO> dto) {
        return success();
    }


    /**
     * 添加供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult saveOrUpdate(@RequestBody @Validated SupplierDTO dto) {
        return success();
    }



    /**
     * 修改供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated SupplierDTO dto) {
        return success();
    }


    /**
     * 供应商详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult view(@RequestBody @Validated BaseIdDTO dto) {
        SupplierDTO supplier = new SupplierDTO();
        return success(supplier);
    }


    /**
     * 删除供应商
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdDTO dto) {
        return success();
    }


    /**
     * 启用供应商
     * @param dto
     * @return
     */
    @PostMapping("/updateStatus")
    public ApiResult start(@RequestBody @Validated UpdateStateDTO dto) {
        return success();
    }

    /**
     * 审核
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        return success();
    }


    /**
     * 供应商导入
     */
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
          return success();
    }

    /**
     * 供应商导出模板
     * @return
     */
    @PostMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        return success();
    }







}
