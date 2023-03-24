package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.model.scm.entity.SupplierPhaseEntity;
import com.erp.server.scm.service.SupplierPhaseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 供应商阶段管理
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier/phase")
public class SupplierPhaseController extends BaseController {


    @Resource
    private SupplierPhaseService supplierPhaseService;


    /**
     * 供应商阶段分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SupplierPhaseDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        PagingVO<SupplierPhaseDTO.PagingViewDTO> pagingVO = supplierPhaseService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加供应商阶段
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SupplierPhaseDTO.AddDTO dto) {
        SupplierPhaseEntity entity = supplierPhaseService.add(dto);
        return entity != null ? success() : failure();
    }

    /**
     * 修改供应商阶段
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated SupplierPhaseDTO.UpdateDTO dto) {
        Boolean result = supplierPhaseService.updateSupplierPhase(dto);
        return result == true ? success() : failure();
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated SupplierPhaseDTO.AddDTO dto) {
        Boolean result = supplierPhaseService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 供应商阶段提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierPhaseService.submit(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 供应商阶段 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<SupplierPhaseDTO.UpdateDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SupplierPhaseDTO.UpdateDTO supplierPhase = supplierPhaseService.view(dto.getId());
        return success(supplierPhase);
    }


    /**
     * 供应商阶段审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult view(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = supplierPhaseService.approve(dto);
        return result == true ? success() : failure();
    }


    /**
     * 取消流程
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-23 17:57
     */
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierPhaseService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 删除供应商阶段
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierPhaseService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }

}
