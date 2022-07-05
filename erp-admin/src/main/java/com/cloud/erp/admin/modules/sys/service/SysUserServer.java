package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.dto.SysUserDTO;
import com.cloud.erp.admin.modules.sys.entity.SysUserEntity;

/**
 * @Classname SysUserServer
 * @Description TODO
 * @Date 2022-07-01 17:05
 * @Created by yl
 */
public interface SysUserServer extends IService<SysUserEntity> {

    /**
     * 保存系统用户信息
     * @param sysUserDTO
     * @return
     */
    int insertSysUser(SysUserDTO sysUserDTO);
}
