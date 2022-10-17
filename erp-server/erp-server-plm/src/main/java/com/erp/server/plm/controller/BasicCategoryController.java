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
 * 公共接口
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/category")
public class BasicCategoryController extends BaseController {

    @Autowired
    private BasicCategoryService categoryService;

    /**
     * 产品分类-新增分类
     * @Date 2022/10/17 15:31
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult
     **/
    @PostMapping("/save")
    public ApiResult addCategory(@RequestBody @Validated SaveBasicCategoryDTO dto) {
        categoryService.addCategory(dto);
        return success();
    }

    /**
     * 产品分类-修改分类
     * @Date 2022/10/17 15:32
     * @param dto dto
     * @return com.erp.common.dto.base.ApiResult
     **/
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated UpdateBasicNameDTO dto) {
        categoryService.updateCategory(dto);
        return success();
    }

    /**
     * 产品分类-获取产品分类树结构
     * @Date 2022/10/17 15:32
     * @return com.erp.common.dto.base.ApiResult
     **/
    @GetMapping("/tree")
    public ApiResult<List<BasicCategoryDTO>> tree() {
        List<BasicCategoryDTO> treeList = categoryService.getTree();
        return success(treeList);
    }

    /**
     * 产品分类-删除分类
     * @Author Luo_WG
     * @Date 2022/10/17 15:33
     * @param id id
     * @return com.erp.common.dto.base.ApiResult
     **/
    @RequestMapping(value = "/remove", method = {RequestMethod.POST})
    public ApiResult remove(String id) {
        Boolean flag = categoryService.deleteById(id);
        return flag == true ? success() : failure();
    }

}

