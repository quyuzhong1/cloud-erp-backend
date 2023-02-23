package com.erp.server.plm.controller;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.enums.ApprovalStatusEnum;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProjectInfoService;
import com.erp.server.plm.service.SysCodeService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Autowired
    private SysCodeService sysCodeService;

    /**
     * 产品列表-普通分页
     *
     * @param dto
     * @return com.common.core.vo.ApiResult<com.erp.common.vo.PagingVO < com.erp.model.plm.dto.ProductShowDTO>>
     * @author yl
     * @date 2022-10-09 10:17
     */
    @PostMapping("/paging")
   // @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:product:paging", tableAlias = "pt")
    public ApiResult<PagingVO<ProductShowDTO>> paging(@RequestBody @Validated PagingDTO<ProductSearchDTO> dto) {
        PagingVO<ProductShowDTO> pagingVO = productInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 产品列表-无分页
     *
     * @param dto
     * @return ApiResult<List < BasicDTO>>
     * @author Will
     * @date: 2023/2/10 14:47
     */
    @PostMapping("/list")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:product:paging", tableAlias = "pt")
    public ApiResult<List<BasicDTO>> listProductInfo(@RequestBody @Validated ProductSearchDTO dto) {
        List<BasicDTO> list = productInfoService.listProductInfo(dto);
        return success(list);
    }


    /**
     * 产品列表-编辑时候详情
     */
    @PostMapping("/productInfo")
    public ApiResult<ProductDTO> info(@RequestBody @Validated BaseIdDTO dto) {
        ProductDTO product = productInfoService.info(dto.getId());
        return success(product);
    }

    /**
     * 产品列表-更改对应数据
     */
    @PostMapping("/updateProduct")
    //@RequestPermissions("plm:product:updateProduct")
    public ApiResult update(@RequestBody @Validated UpdateProductDTO dto) {
        productInfoService.updateProduct(dto);
        return success();
    }


    /**
     * 产品列表-新建产品
     */
    @PostMapping("/saveOrUpdate")
    //@RequestPermissions("plm:product:saveOrUpdate")

    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:product:updateOrUpdate",
            serviceClass = ProductInfoService.class,
            keyIdName = "id"
    )
    public ApiResult<String> saveOrUpdate(@RequestBody @Validated ProductDTO dto) {
        String productId = productInfoService.saveOrUpdateProduct(dto);
        return success(productId);
    }

    /**
     * 产品列表-移动分类
     */
    @PostMapping("/updateCategory")
    //@RequestPermissions("plm:product:updateCategory")
    public ApiResult updateCategory(@RequestBody @Validated MoveCategoryDTO dto) {
        Boolean flag = productInfoService.updateCategory(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 产品列表-删除产品
     */
    @PostMapping("/remove")
    //@RequestPermissions("plm:product:remove")
    /*@DataPermission(operationType = DataAttributeEnum.CHECK_BY_PARAM,
            tableField = "charge_id",
            menuCode = "plm:product:remove",
            serviceClass = ProductInfoService.class,
            keyIdName = "productId")*/
    public ApiResult removeProduct(@RequestBody @Validated RemoveProductDTO dto) {
        Boolean flag = productInfoService.removeProduct(dto);
        return flag == true ? success() : failure();
    }

    @GetMapping("/exportTemplate")
    //@RequestPermissions("plm:product:exportTemplate")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        productInfoService.exportTemplate(request, response);
    }

    /**
     * 概述
     */
    @PostMapping("/info")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:task:paging",
            tableAlias = "project_task"
    )
    public ApiResult<ProjectInfoDTO> projectInfo(@RequestBody ProductTaskCountShowDTO productTaskCountShowDTO) {
        ProjectInfoDTO info = projectInfoService.projectInfo(productTaskCountShowDTO);
        return success(info);
    }


    /**
     * 保存模板
     */
    @PostMapping("/saveTemplate")
    //@RequestPermissions("plm:product:saveTemplate")
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
    //@RequestPermissions("plm:product:exportProductData")
    public void exportProductData(@RequestBody @Validated ExportProductDataDTO dto) {
        productInfoService.exportProductData(dto);
    }

    /**
     * 产品列表-立项状态下拉框
     *
     * @return ApiResult
     * @author Will
     * @date: 2022/11/24 10:24
     */
    @GetMapping("/getApprovalStatusSelect")
    public ApiResult<List<SelectShowDTO>> getApprovalStatusSelect() {
        List<SelectShowDTO> list = new ArrayList<>();
        Arrays.stream(ApprovalStatusEnum.values()).forEach(obj -> {
            SelectShowDTO dto = new SelectShowDTO();
            dto.setValue(obj.getCode());
            dto.setLabel(obj.getName());
            list.add(dto);
        });
        return success(list);
    }

    /**
     * 产品列表-获取SPU编号
     *
     * @param categoryId
     * @return ApiResult
     * @author Will
     * @date: 2023/1/7 16:09
     */
    @GetMapping("/getSpuNo")
    public ApiResult getSpuNo(@Param("categoryId") String categoryId) {
        String spuNo = sysCodeService.getSpuNo(categoryId);
        return success(spuNo);
    }




}

