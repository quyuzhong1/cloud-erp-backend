package com.erp.server.scm.controller;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.model.scm.dto.SupplierPhasePagingParamDTO;
import com.erp.model.scm.dto.SupplierPhasePagingViewDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商管理
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/supplier/visit")
public class SupplierVisitController extends BaseController {




    /**
     * 供应商 拜访分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SupplierPhasePagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierPhasePagingParamDTO> dto) {
        return success();
    }

    /**
     * 保存或者修改供应商拜访
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated SupplierPhaseDTO dto) {
        return success();
    }

}
