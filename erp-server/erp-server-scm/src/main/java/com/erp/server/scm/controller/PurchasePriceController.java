package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.PurchasePriceExportResultDTO;
import com.erp.model.scm.dto.PurchasePricePagingParamDTO;
import com.erp.model.scm.dto.PurchasePricePagingViewDTO;
import com.erp.server.scm.service.PurchasePriceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public ApiResult<PagingVO<PurchasePricePagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchasePricePagingParamDTO> dto) {
        return success();
    }

    /**
     * 添加采购价目表
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult saveOrUpdate(@RequestBody @Validated PurchasePriceDTO.AddDTO dto) {
        return success();
    }


    /**
     * 采购价目 导出数据
     *
     * @return
     */
    @PostMapping("/exportPurchasePrice")
    public ApiResult exportPurchasePrice(HttpServletRequest request, HttpServletResponse response, PurchasePricePagingParamDTO dto) {
        return success();
    }

    /**
     * 采购价目 产品价格明细导出模板
     *
     * @return
     */
    @PostMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        return success();
    }


    /**
     * 采购价目 产品价格明细导入
     *
     * @return
     */
    @PostMapping("/import")
    public ApiResult<PurchasePriceExportResultDTO> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return success();
    }


    /**
     * 采购价目详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<PurchasePriceDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        return success();
    }

    /**
     * 采购价目审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        return success();
    }


}
