package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.server.scm.service.PurchasePriceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 采购价目管理
 *
 * @author Lambda
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/purchase/price")
public class PurchasePriceController extends BaseController {

    @Resource
    private PurchasePriceService purchasePriceService;


    /**
     * 采购价目分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PurchasePriceDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchasePriceDTO.PagingParamDTO> dto) {
        PagingVO<PurchasePriceDTO.PagingViewDTO> pagingVO = purchasePriceService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加采购价目表
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchasePriceDTO.AddDTO dto) {
        PurchasePriceEntity purchasePrice = purchasePriceService.add(dto);
        return purchasePrice != null ? success() : failure();
    }


    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchasePriceDTO.AddDTO dto) {
        Boolean result = purchasePriceService.addAndSubmit(dto);
        return result == true ? success() : failure();
    }

    /**
     * 采购价目详情
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<PurchasePriceDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        PurchasePriceDTO.ViewDTO view = purchasePriceService.view(dto.getId());
        return  success(view);
    }

    /**
     * 修改采购价目
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated PurchasePriceDTO.ViewDTO dto) {
        PurchasePriceEntity view = purchasePriceService.updatePurchasePrice(dto);
        return  view==null?success():failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated PurchasePriceDTO.ViewDTO dto) {
        Boolean result = purchasePriceService.updateAndSubmit(dto);
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
        Boolean result = purchasePriceService.deleteByIds(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 采购价目提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = purchasePriceService.submitApprove(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     *审核
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = purchasePriceService.approve(dto);
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
        Boolean result = purchasePriceService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     *
     * 采购价目数据导出
     */
    @PostMapping("/exportPurchasePrice")
    public ApiResult exportPurchasePrice(@RequestBody @Valid PurchasePriceDTO.PagingParamDTO dto, HttpServletResponse response) {
        purchasePriceService.exportPurchasePrice(dto, response);
        return success();
    }



}
