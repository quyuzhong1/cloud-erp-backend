package com.erp.server.sys.controller.api;


import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.enums.ApiError;
import com.erp.model.sys.dto.CopyRoleDTO;
import com.erp.model.sys.entity.SysRoleEntity;
import com.erp.server.sys.service.SysRoleService;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
        boolean flag=sysRoleService.saveRoleEntity(sysRole);
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
   public ApiResult copy(@RequestBody @Validated CopyRoleDTO dto){
        sysRoleService.copyRole(dto.getRoleId());
        return success();
    }

}
