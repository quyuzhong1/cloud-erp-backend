package com.cloud.erp.admin.modules.sys.controller;

import com.cloud.erp.admin.modules.sys.dto.SysUserDTO;
import com.cloud.erp.admin.modules.sys.service.SysUserServer;
import com.cloud.erp.common.common.ApiRest;
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
@RequestMapping("sys/api/user")
public class SysUserController extends BaseController {


    @Autowired
    private SysUserServer sysUserServer;

    @RequestMapping(value = "/save-user", method = {RequestMethod.POST})
    public ApiRest addSysUser(@RequestBody @Validated SysUserDTO sysUserDTO) {
        int insertFlag = sysUserServer.insertSysUser(sysUserDTO);
        if (insertFlag == 1) {
            return success();
        }
        return failure("保存失败");

    }
}
