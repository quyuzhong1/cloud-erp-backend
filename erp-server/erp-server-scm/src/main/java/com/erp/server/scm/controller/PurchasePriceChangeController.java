package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.server.scm.service.PurchasePriceChangeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 采购价目变更管理
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
    public ApiResult<PagingVO<PurchasePriceChangeDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchasePriceChangeDTO.PagingParamDTO> dto) {
        PagingVO<PurchasePriceChangeDTO.PagingViewDTO> pagingVO = purchasePriceChangeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加采购变更
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult saveOrUpdate(@RequestBody @Validated PurchasePriceChangeDTO.AddDTO dto) {
        String id = purchasePriceChangeService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
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
    public ApiResult<PurchasePriceChangeDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        PurchasePriceChangeDTO.ViewDTO view = purchasePriceChangeService.view(dto.getId());
        return success(view);
    }

    /**
     * 修改采购价目变更
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchasePriceChangeDTO.UpdateDTO dto) {
        String id = purchasePriceChangeService.updatePurchasePriceChange(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchasePriceChangeDTO.UpdateDTO dto) {
        Boolean result = purchasePriceChangeService.updateAndSubmit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 删除采购价目
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceChangeService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 采购价目变更提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceChangeService.submitApprove(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 采购价目变更审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = purchasePriceChangeService.approve(dto);
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
        Boolean result = purchasePriceChangeService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }
}
