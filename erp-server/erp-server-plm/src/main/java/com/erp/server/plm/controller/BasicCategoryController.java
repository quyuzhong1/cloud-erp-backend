package com.erp.server.plm.controller;


import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.SaveBasicCategoryDTO;
import com.erp.model.plm.dto.UpdateBasicNameDTO;
import com.erp.server.plm.service.BasicCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

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
     * @return com.common.core.vo.ApiResult
     **/
    @PostMapping("/save")
    //@RequestPermissions("plm:category:save")
    public ApiResult addCategory(@RequestBody @Validated SaveBasicCategoryDTO dto) {
        categoryService.addCategory(dto);
        return success();
    }

    /**
     * 产品分类-修改分类
     * @Date 2022/10/17 15:32
     * @param dto dto
     * @return com.common.core.vo.ApiResult
     **/
    @PostMapping("/update")
    //@RequestPermissions("plm:category:update")
    public ApiResult update(@RequestBody @Validated UpdateBasicNameDTO dto) {
        categoryService.updateCategory(dto);
        return success();
    }

    /**
     * 产品分类-获取产品分类树结构
     * @Date 2022/10/17 15:32
     * @return com.common.core.vo.ApiResult
     **/
    @GetMapping("/tree")
    public ApiResult<List<BasicCategoryDTO>> tree() {
        List<BasicCategoryDTO> treeList = categoryService.getTree();
        return success(treeList);
    }


    /**
     * 产品分类-获取分页列表的 分类树结构
     * @Date 2022/10/17 15:32
     * @return com.common.core.vo.ApiResult
     **/
    @GetMapping("/listTree")
    public ApiResult<List<BasicCategoryDTO>> listTree(String type) {
        List<BasicCategoryDTO> treeList = categoryService.getListTree(type);
        return success(treeList);
    }

    /**
     * 产品分类-删除分类
     * @Author Luo_WG
     * @Date 2022/10/17 15:33
     * @param id id
     * @return com.common.core.vo.ApiResult
     **/
    @RequestMapping(value = "/remove", method = {RequestMethod.POST})
    //@RequestPermissions("plm:category:remove")
    public ApiResult remove(String id) {
        Boolean flag = categoryService.deleteById(id);
        return flag == true ? success() : failure();
    }

}

