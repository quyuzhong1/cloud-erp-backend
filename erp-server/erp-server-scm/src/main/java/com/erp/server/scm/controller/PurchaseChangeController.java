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
 * 销售需求明细表 前端控制器
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
    public ApiResult<PagingVO<List<PurchaseChangePagingViewDTO>>> queryByPage(@RequestBody @Validated PagingDTO<PurchaseChangePagingParamDTO> dto) {
        PagingVO<List<PurchaseChangePagingViewDTO>> pagingVO = purchaseChangeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 获取变更单号
     * @author Will
     * @date: 2023/3/16 10:52
     * @return ApiResult
     */
    @GetMapping("/getCode")
    public ApiResult getCode() {
        String code = purchaseChangeService.getCode();
        return success(code);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/3/15 17:34
     * @param purchaseChangeDTO
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseChangeDTO purchaseChangeDTO) {
        Boolean flag = purchaseChangeService.add(purchaseChangeDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param purchaseChangeDTO
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchaseChangeDTO purchaseChangeDTO) {
        Boolean flag = purchaseChangeService.update(purchaseChangeDTO);
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
    public ApiResult<PurchaseChangeDTO> view(@Param("id") String id) {
        PurchaseChangeDTO purchaseChangeDTO = purchaseChangeService.view(id);
        return success(purchaseChangeDTO);
    }

    /**
     * 删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param id
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestParam("id") String id) {
        Boolean flag = purchaseChangeService.delete(id);
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
     * 提交
     * @author Will
     * @date: 2023/3/15 17:47
     * @param id
     * @return ApiResult
     */
    @PostMapping("/commit")
    public ApiResult commit(@RequestParam("id") String id) {
        Boolean flag = purchaseChangeService.commit(id);
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
     * 批量反审核
     * @author Will
     * @date: 2023/3/15 17:50
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/unApprove")
    public ApiResult unAudit(@RequestParam("ids") List<String> ids) {
        Boolean flag = purchaseChangeService.unApprove(ids);
        return flag == true ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/3/15 18:01
     * @param purchaseChangePagingParamDTO
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody PurchaseChangePagingParamDTO purchaseChangePagingParamDTO, HttpServletResponse response) {
        Boolean flag = purchaseChangeService.exportExcel(purchaseChangePagingParamDTO, response);
        return flag == true ? success() : failure();
    }

}
