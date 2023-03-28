package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.*;
import com.erp.server.scm.service.PurchaseChangeService;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 采购变更管理
 *
 * @author will
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/purchaseChange")
public class PurchaseChangeController extends BaseController {

    @Resource
    private PurchaseChangeService purchaseChangeService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/3/15 16:47
     * @param dto
     * @return ApiResult<PagingVO<List<ScmSalesDemandDTO>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PurchaseChangeDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseChangeDTO.SearchParamDTO> dto) {
        PagingVO<PurchaseChangeDTO.ListDTO> pagingVO = purchaseChangeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseChangeDTO.AddDTO dto) {
        Boolean flag = purchaseChangeService.add(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchaseChangeDTO.UpdateDTO dto) {
        Boolean flag = purchaseChangeService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/3/15 17:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseChangeDTO.AddDTO dto) {
        Boolean flag = purchaseChangeService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<ScmPurchaseChangeDTO>
     */
    @GetMapping("/view")
    public ApiResult<PurchaseChangeDTO.ViewDTO> view(@Param("id") String id) {
        PurchaseChangeDTO.ViewDTO dto = purchaseChangeService.view(id);
        return success(dto);
    }

    /**
     * 批量删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestParam("ids") List<String> ids) {
        Boolean flag = purchaseChangeService.delete(ids);
        return flag == true ? success() : failure();
    }


    /**
     * 批量作废
     * @author Will
     * @date: 2023/3/15 17:50
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestParam("ids") List<String> ids) {
        Boolean flag = purchaseChangeService.invalid(ids);
        return flag == true ? success() : failure();
    }


    /**
     * 批量提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestParam("ids") List<String> ids) {
        Boolean flag = purchaseChangeService.submit(ids);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/3/15 17:54
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("/approve")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        purchaseChangeService.approve(baseApproveParamDTO);
        return success();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/3/15 18:01
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseChangeDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = purchaseChangeService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

}
