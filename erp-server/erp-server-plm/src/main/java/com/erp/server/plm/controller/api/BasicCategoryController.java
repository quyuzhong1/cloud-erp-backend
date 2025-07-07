package com.erp.server.plm.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.*;
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
@LogSystemModule("产品开发管理")
@RequestMapping("category")
public class BasicCategoryController extends BaseController {

    @Autowired
    private BasicCategoryService categoryService;

    /**
     * 产品分类-新增分类
     * @Date 2022/10/17 15:31
     * @param dto dto
     * @return com.common.core.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增产品分类")
    @PostMapping("/save")
    public ApiResult<Object> addCategory(@RequestBody @Validated SaveBasicCategoryDTO dto) {
        categoryService.addCategory(dto);
        return success();
    }

    /**
     * 产品分类-修改分类
     * @Date 2022/10/17 15:32
     * @param dto dto
     * @return com.common.core.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改产品分类:id={id},名称={name}")
    @PostMapping("/update")
    public ApiResult<Object> update(@RequestBody @Validated UpdateBasicNameDTO dto) {
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
    @LogAction(value = LogActionEnum.DELETE, desc = "删除产品分类")
    @RequestMapping(value = "/remove", method = {RequestMethod.POST})
    public ApiResult<Object> remove(String id) {
        Boolean flag = categoryService.deleteById(id);
        return flag == true ? success() : failure();
    }

    /**
     * 品类下拉框
     * @mock 2
     * @param grade 0:全部分类 1:一级分类 2:二级分类 默认二级分类
     */
    @GetMapping("/drop/down")
    public ApiResult<List<CategoryControllerDTO.CategoryDropDownDTO>> listCategoryDropDown(@RequestParam(name = "grade",defaultValue = "2", required = false) Integer grade){
        List<CategoryControllerDTO.CategoryDropDownDTO> result = categoryService.listCategoryDropDown(grade);
        return success(result);
    }

    /**
     * 品类下拉框2
     * @mock 2
     * (获取二级分类，并拼接一级分类名称)
     */
    @GetMapping("/grade/down")
    public ApiResult<List<BasicCategoryTreeDTO>> categoryGradeDown(){
        List<BasicCategoryTreeDTO> result = categoryService.categoryGradeDown();
        return success(result);
    }


    /**
     * 品类下拉框
     */
    @GetMapping("/getCategoryDropdown")
    public ApiResult<List<BasicCategoryDTO.DropdownDTO>> getCategoryDropdown(){
        return success(categoryService.getCategoryDropdown());
    }

}

