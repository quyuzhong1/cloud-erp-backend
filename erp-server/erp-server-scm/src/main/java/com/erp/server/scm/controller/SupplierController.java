package com.erp.server.scm.controller;


import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.scm.service.SupplierService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 供应商管理
 *
 * @author yl
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier")
public class SupplierController extends BaseController {


    @Resource
    private SupplierService supplierService;


    /**
     * 供应商分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SupplierDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        PagingVO<SupplierDTO.PagingViewDTO> pagingVO = supplierService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 添加供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SupplierDTO.AddDTO dto) {
        SupplierEntity supplier = supplierService.addSupplier(dto);
        return supplier != null ? success() : failure();
    }


    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated SupplierDTO.AddDTO dto) {
        Boolean result = supplierService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 修改供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated SupplierDTO.UpdateDTO dto) {
        Boolean result = supplierService.updateSupplier(dto);
        return result == true ? success() : failure();
    }


    /**
     * 供应商详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<SupplierDTO.UpdateDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SupplierDTO.UpdateDTO view = supplierService.view(dto.getId());
        return success(view);
    }


    /**
     * 删除供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 供应商提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierService.submit(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 启用供应商
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = supplierService.updateStatus(dto);
        return result == true ? success() : failure();
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = supplierService.approve(dto);
        return result == true ? success() : failure();
    }

    /**
     * 反审核
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-22 11:56
     */
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = supplierService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }


    /**
     * 供应商导入
     */
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        return success();
    }


}
