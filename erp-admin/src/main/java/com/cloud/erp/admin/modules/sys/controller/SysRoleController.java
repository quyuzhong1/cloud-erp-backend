package com.cloud.erp.admin.modules.sys.controller;

import com.cloud.erp.admin.modules.sys.entity.SysRoleEntity;
import com.cloud.erp.admin.modules.sys.service.SysRoleService;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.ApiResult;
import com.erp.common.enums.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 角色表
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@RestController
@RequestMapping("sys/role")
public class SysRoleController extends BaseController {
    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public ApiResult list(){
        List<SysRoleEntity> list = sysRoleService.list();
        return success(list);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")

    public ApiResult info(@PathVariable("id") Long id){
		SysRoleEntity sysRole = sysRoleService.getById(id);
        return  success(sysRole);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public ApiResult save(@RequestBody SysRoleEntity sysRole){
        boolean flag=sysRoleService.save(sysRole);
        if(flag){
            return success();
        }else{
            return  failure(ApiError.ERROR_1002);
        }

    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public ApiResult update(@RequestBody SysRoleEntity sysRole){
		sysRoleService.updateById(sysRole);
        return success();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public ApiResult delete(@RequestBody List<String> ids){
        sysRoleService.removeRoleById(ids);
        return success();
    }

    @PostMapping("/copy")
    public ApiResult copy(String roleId){
        sysRoleService.copyRole(roleId);
        return success();
    }

}
