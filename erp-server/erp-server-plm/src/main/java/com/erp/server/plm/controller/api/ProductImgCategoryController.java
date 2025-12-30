package com.erp.server.plm.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.plm.service.ProductImgCategoryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.ProductImgCategoryDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.plm.entity.ProductImgCategoryEntity;

/**
 * 图片分类表
 *
 * @author wuhaotian
 * @since 2025-12-29
 */
@Slf4j
@RestController
@LogSystemModule("图片分类表")
@RequestMapping("/productImgCategory")
public class ProductImgCategoryController extends BaseController {

    @Resource
    private ProductImgCategoryService productImgCategoryService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-12-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "图片分类表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ProductImgCategoryDTO.AddDTO dto) {
        return success(productImgCategoryService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-12-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "图片分类表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:productImgCategory:update",
        serviceClass = ProductImgCategoryService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ProductImgCategoryDTO.UpdateDTO dto) {
        productImgCategoryService.update(dto);
        return success();
    }


    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:productImgCategory:paging",
            tableAlias = ""
    )
    public ApiResult<List<ProductImgCategoryDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(productImgCategoryService.tabList(dto));
    }

    /**
    * 列表查询
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return ApiResult<PagingVO<ProductImgCategoryDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:productImgCategory:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<ProductImgCategoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ProductImgCategoryDTO.PagingParamDTO> dto) {
        return success(productImgCategoryService.paging(dto));
    }


    /**
    * 详情
    * @author wuhaotian
    * @date:  2025-12-29
    * @param id
    * @return ApiResult<ProductImgCategoryDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productImgCategory:view",
            serviceClass = ProductImgCategoryService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<ProductImgCategoryDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(productImgCategoryService.view(id));
    }

    /**
    * 导出Excel数据
    * @author wuhaotian
    * @date:  2025-12-29
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:productImgCategory:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "图片分类表导出Excel数据")
    public void exportList(@RequestBody @Validated ProductImgCategoryDTO.ExportDTO dto, HttpServletResponse response) {
        productImgCategoryService.exportList(dto, response);
    }


}
