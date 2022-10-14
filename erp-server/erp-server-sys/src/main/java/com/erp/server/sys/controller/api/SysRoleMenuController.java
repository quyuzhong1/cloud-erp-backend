package com.erp.server.sys.controller.api;


import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.sys.dto.RoleMenuDTO;
import com.erp.model.sys.dto.SysRoleMenuBatchDTO;
import com.erp.server.sys.service.SysRoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname SysRoleMenuController
 * @Description TODO
 * @Date 2022-07-19 17:18
 * @Created by yl
 */
@RestController
@RequestMapping("sys/roleMenu")
public class SysRoleMenuController extends BaseController {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;


    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody SysRoleMenuBatchDTO batchDTO) {
        boolean flag = sysRoleMenuService.batchSaveRoleMenu(batchDTO);
        return flag == true ? success() : failure();
    }

    @RequestMapping("/tree")
    public ApiResult<RoleMenuDTO> roleMenuTree(String roleId) {
        RoleMenuDTO vo=sysRoleMenuService.findRoleMenuTreeByRoleId(roleId);
        return success(vo);
    }

    @RequestMapping("/save")
    public ApiResult save(String roleId) {
        RoleMenuDTO vo=sysRoleMenuService.findRoleMenuTreeByRoleId(roleId);
        return success(vo);
    }


}
