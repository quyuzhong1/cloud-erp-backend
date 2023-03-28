package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.server.scm.service.PurchasePriceChangeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 采购价目管理
 *
 * @author Lambda
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/purchase/price/change")
public class PurchasePriceChangeController extends BaseController {


    @Resource
    private PurchasePriceChangeService purchasePriceChangeService;


    /**
     * 采购价目变更分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PurchasePriceDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchasePriceDTO.PagingParamDTO> dto) {
        return success();
    }

    /**
     * 添加采购变更
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult saveOrUpdate(@RequestBody @Validated PurchasePriceChangeDTO.AddDTO dto) {
        PurchasePriceChangeEntity priceChange = purchasePriceChangeService.add(dto);
        return priceChange != null ? success() : failure();
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchasePriceChangeDTO.AddDTO dto) {
        Boolean result = purchasePriceChangeService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 采购价目变更详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<PurchasePriceChangeDTO.UpdateDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        PurchasePriceChangeDTO.UpdateDTO view = purchasePriceChangeService.view(dto.getId());
        return success(view);
    }





    /**
     * 采购价目变更审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        return success();
    }
}
