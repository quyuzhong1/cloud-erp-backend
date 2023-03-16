package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierPagingParamDTO;
import com.erp.model.scm.dto.SupplierPagingViemDTO;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商阶段管理
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier/phase")
public class SupplierPhaseController extends BaseController {




    /**
     * 供应商阶段分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SupplierPagingViemDTO>> paging(@RequestBody @Validated PagingDTO<SupplierPagingParamDTO> dto) {
        return success();
    }

    /**
     * 保存或者修改供应商阶段
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated SupplierPhaseDTO dto) {
        return success();
    }


    /**
     * 供应商阶段 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<SupplierPhaseDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SupplierPhaseDTO supplierPhase = new SupplierPhaseDTO();
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
        return success();
    }


    /**
     * 删除供应商阶段
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdDTO dto) {
        return success();
    }

}
