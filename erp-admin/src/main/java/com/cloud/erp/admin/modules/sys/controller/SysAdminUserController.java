package com.cloud.erp.admin.modules.sys.controller;

import com.cloud.erp.admin.modules.sys.dto.SysAdminUserDTO;
import com.cloud.erp.admin.modules.sys.service.SysAdminUserServer;
import com.cloud.erp.common.common.ApiResult;
import com.cloud.erp.common.common.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname SysUserController
 * @Description TODO
 * @Date 2022-07-01 15:59
 * @Created by yl
 */
@RestController
@RequestMapping("sys/admin/user")
public class SysAdminUserController extends BaseController {


    @Autowired
    private SysAdminUserServer  sysAdminUserServer;

    @RequestMapping(value = "/save-user", method = {RequestMethod.POST})
    public ApiResult addSysUser(@RequestBody @Validated SysAdminUserDTO sysAdminUserDTO) {
        int insertFlag = sysAdminUserServer.insertSysUser(sysAdminUserDTO);
        if (insertFlag == 1) {
            return success();
        }
        return failure("保存失败");

    }
}
