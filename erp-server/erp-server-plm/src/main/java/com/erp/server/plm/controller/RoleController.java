package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.entity.RoleEntity;
import com.erp.server.plm.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.erp.common.controller.BaseController;

import java.util.List;

/**
 *产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("plm/role")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;




    /**
     * 成员分类
     * @author yl
     * @date 2022-10-09 19:33
     * @param
     * @return com.erp.common.dto.base.ApiResult
     */
    @GetMapping("/sort/list")
    public ApiResult roleSortList(){
        return success();
    }

    /**
     * 角色列表
     * @author yl
     * @date 2022-10-09 19:33
     * @param
     * @return com.erp.common.dto.base.ApiResult
     */
    @GetMapping("/list")
    public ApiResult<List<RoleEntity>> list(){
        return success(roleService.list());
    }

}

