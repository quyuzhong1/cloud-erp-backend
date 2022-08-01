package com.cloud.erp.admin.modules.sys.controller;

import com.cloud.erp.admin.modules.sys.dto.SysRoleMenuBatchDTO;
import com.cloud.erp.admin.modules.sys.service.SysRoleMenuService;
import com.cloud.erp.admin.modules.sys.vo.SysRoleMenuVO;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ApiResult batchSave(@RequestBody SysRoleMenuBatchDTO  batchDTO) {
        boolean flag = sysRoleMenuService.batchSaveRoleMenu(batchDTO);
        return flag == true ? success() : failure();
    }

    @RequestMapping("/tree")
    public ApiResult roleMenuTree(String roleId) {
        List<SysRoleMenuVO> roleMenuTreeList = sysRoleMenuService.findRoleMenuTree(roleId);
        return success(roleMenuTreeList);
    }


}
