package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 产品信息表 前端控制器
 * </p>
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


    @PostMapping("/paging")
    public ApiResult paging(@RequestBody @Validated PagingDTO<ProductSearchDTO> dto) {
        PagingVO pagingVO = productInfoService.paging(dto);
        return success(pagingVO);
    }


    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated ProductDTO dto) {
        Boolean flag = productInfoService.saveOrUpdateProduct(dto);
        return flag == true ? success() : failure();
    }

    @PostMapping("/updateCategory")
    public ApiResult updateCategory(@RequestBody @Validated MoveCategoryDTO dto) {
        Boolean flag = productInfoService.updateCategory(dto);
        return flag == true ? success() : failure();
    }

    @PostMapping("/remove")
    public ApiResult removeProduct(@RequestBody @Validated RemoveProductDTO dto) {
        Boolean flag = productInfoService.removeProduct(dto);
        return flag == true ? success() : failure();
    }

    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        productInfoService.exportTemplate(request, response);
    }

    @GetMapping("/info")
    public ApiResult projectInfo(String productId) {
        ProjectInfoDTO info = projectInfoService.projectInfo(productId);
        return success(info);
    }

    @GetMapping("/saveTemplate")
    public ApiResult projectInfo(@RequestBody @Validated SaveProductTemplateDTO dto) {
        Boolean flag = productInfoService.saveTemplate(dto);
        return flag == true ? success() : failure();
    }


}

