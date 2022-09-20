package com.erp.server.plm.controller;


import com.alibaba.fastjson2.JSONObject;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.SaveBasicCategoryDTO;
import com.erp.model.plm.dto.UpdateBasicNameDTO;
import com.erp.server.plm.service.BasicCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.List;

/**
 * <p>
 * 产品分类表 前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/category")
public class BasicCategoryController extends BaseController {

    @Autowired
    private BasicCategoryService categoryService;


    @PostMapping("/save")
    public ApiResult addCategory(@RequestBody @Validated SaveBasicCategoryDTO dto) {
        categoryService.addCategory(dto);
        return success();
    }

    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated UpdateBasicNameDTO dto) {
        categoryService.updateCategory(dto);
        return success();
    }

    @GetMapping("/tree")
    public ApiResult tree() {
        List<BasicCategoryDTO> treeList = categoryService.getTree();
        return success(treeList);
    }

    @RequestMapping(value = "/remove", method = {RequestMethod.POST})
    public ApiResult remove(String id) {
        Boolean flag = categoryService.deleteById(id);
        return flag == true ? success() : failure();
    }

}

