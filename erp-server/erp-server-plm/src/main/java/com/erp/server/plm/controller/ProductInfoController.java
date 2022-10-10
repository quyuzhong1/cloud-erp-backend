package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProjectInfoService;
import com.erp.server.plm.service.UserAddProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("plm/product")
public class ProductInfoController extends BaseController {

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private ProjectInfoService projectInfoService;





    /**
     * 产品列表-普通分页
     *
     * @param dto
     * @return com.erp.common.dto.base.ApiResult<com.erp.common.vo.PagingVO < com.erp.model.plm.dto.ProductShowDTO>>
     * @author yl
     * @date 2022-10-09 10:17
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<ProductShowDTO>> paging(@RequestBody @Validated PagingDTO<ProductSearchDTO> dto) {
        PagingVO<ProductShowDTO> pagingVO = productInfoService.paging(dto);
        return success(pagingVO);
    }




    /**
     * 产品列表-编辑时候详情
     *
     */
    @PostMapping("/productInfo")
    public ApiResult<ProductDTO> info(@RequestBody @Validated BaseIdDTO dto) {
        ProductDTO product = productInfoService.info(dto.getId());
        return success(product);
    }

    /**
     * 产品列表-更改对应数据
     *
     */
    @PostMapping("/updateProduct")
    public ApiResult update(@RequestBody @Validated UpdateProductDTO dto) {
        productInfoService.updateProduct(dto);
        return success();
    }


    /**
     * 产品列表-新建产品
     *
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated ProductDTO dto) {
        Boolean flag = productInfoService.saveOrUpdateProduct(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 产品列表-移动分类
     *
     */
    @PostMapping("/updateCategory")
    public ApiResult updateCategory(@RequestBody @Validated MoveCategoryDTO dto) {
        Boolean flag = productInfoService.updateCategory(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 产品列表-删除产品
     *
     */
    @PostMapping("/remove")
    public ApiResult removeProduct(@RequestBody @Validated RemoveProductDTO dto) {
        Boolean flag = productInfoService.removeProduct(dto);
        return flag == true ? success() : failure();
    }

    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        productInfoService.exportTemplate(request, response);
    }

    /**
     * 概述
     */
    @GetMapping("/info")
    public ApiResult<ProjectInfoDTO> projectInfo(String productId) {
        ProjectInfoDTO info = projectInfoService.projectInfo(productId);
        return success(info);
    }


    /**
     * 保存模板
     */
    @PostMapping("/saveTemplate")
    public ApiResult projectInfo(@RequestBody @Validated SaveProductTemplateDTO dto) {
        Boolean flag = productInfoService.saveTemplate(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新建产品-获取关联产品
     */
    @GetMapping("/list")
    public ApiResult list() {
        List<Map<String, Object>> list = productInfoService.getListObjs();
        return success(list);
    }


    /**
     * 数据导出
     */
    @PostMapping(value = "/exportProductData", produces = "application/octet-stream")
    public void exportProductData(@RequestBody @Validated ExportProductDataDTO dto) {
        productInfoService.exportProductData(dto);
    }

    /**
     * 产品列表-项目归档
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-10-09 14:38
     */
    @PostMapping("/archive")
    public ApiResult archive(@RequestParam(value = "productId") String productId) {
        boolean flag = productInfoService.archive(productId);
        return flag==true?success():failure();
    }





}

