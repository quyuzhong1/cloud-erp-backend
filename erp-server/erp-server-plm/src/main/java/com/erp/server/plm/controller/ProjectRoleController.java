package com.erp.server.plm.controller;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.plm.dto.ProductRoleDTO;
import com.erp.model.plm.dto.ProjectRoleDTO;
import com.erp.model.plm.entity.ProjectRoleEntity;
import com.erp.server.plm.service.ProjectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("plm/role")
public class ProjectRoleController extends BaseController {

    @Autowired
    private ProjectRoleService projectRoleService;


    /**
     * 产品列表-成员分类
     *
     * @param
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-10-09 19:33
     */
    @GetMapping("/sort/list")
    // @RequestPermissions("plm:role:sort:list")
    public ApiResult<List<ProductRoleDTO>> roleSortList() {
        return success(projectRoleService.roleSortList());
    }


    /**
     * 设置-项目成员-新增角色
     *
     * @param
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-10-09 19:33
     */
    @PostMapping("/save")
    //  @RequestPermissions("plm:role:save")
    public ApiResult save(@RequestBody @Validated ProjectRoleDTO dto) {
        boolean flag = projectRoleService.saveRole(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 设置-项目成员-获取角色列表
     *
     * @param
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-10-09 19:33
     */
    @GetMapping("/list")
    //   @RequestPermissions("plm:role:list")
    public ApiResult<List<ProjectRoleEntity>> list(String productId) {
        return success(projectRoleService.listByProductId(productId));
    }

}

