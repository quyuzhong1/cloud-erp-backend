package com.erp.server.plm.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.server.plm.service.BasicCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description
 * @Author yl
 * @Date 2023-09-26 16:43
 */
@RestController
@RequestMapping("feign/category")
public class BasicCategoryFeignController extends BaseController {

    @Autowired
    private BasicCategoryService categoryService;

    /**
     * 获取分类数结果
     */
    @GetMapping("/tree")
    public List<BasicCategoryDTO> tree() {
        List<BasicCategoryDTO> treeList = categoryService.getTree();
        return treeList;
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/getCategoryList")
    public List<BasicCategoryEntity> getCategoryList() {
        return categoryService.getCategoryList();
    }

    /**
     * 根据父id查询子id
     */
    @GetMapping("/getCategoryByPid")
    public List<BasicCategoryDTO> getCategoryByPid(@RequestParam String pid){
        return categoryService.getCategoryByPid(pid);
    }

}
