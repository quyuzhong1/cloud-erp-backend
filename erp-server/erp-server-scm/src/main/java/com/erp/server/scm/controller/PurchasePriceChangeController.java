package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePricePagingParamDTO;
import com.erp.model.scm.dto.PurchasePricePagingViewDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购价目管理
 *
 * @author Lambda
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/purchase/price/change")
public class PurchasePriceChangeController extends BaseController {




    /**
     * 采购价目变更分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PurchasePricePagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchasePricePagingParamDTO> dto) {
        return success();
    }

    /**
     * 保存或者修改采购变更
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated PurchasePriceChangeDTO dto) {
        return success();
    }


    /**
     * 采购变更详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<PurchasePriceChangeDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        return success();
    }


    /**
     * 采购价目变更审核
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        return success();
    }
}
