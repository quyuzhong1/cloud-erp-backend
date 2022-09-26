package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.server.plm.service.ProjectMembersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.erp.common.controller.BaseController;

/**
 * <p>
 * 项目成员表 前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("plm/project/member")
public class ProjectMembersController extends BaseController {

    @Autowired
    private ProjectMembersService  projectMembersService;

    @GetMapping("/list")
    public ApiResult getList(String productId){

        return success(projectMembersService.getListByProductId(productId));
    }
}

