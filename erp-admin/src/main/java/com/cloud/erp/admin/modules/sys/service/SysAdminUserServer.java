package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.dto.SysAdminUserDTO;
import com.cloud.erp.admin.modules.sys.entity.SysAdminUserEntity;

/**
 * @Classname SysUserServer
 * @Description TODO
 * @Date 2022-07-01 17:05
 * @Created by yl
 */
public interface SysAdminUserServer extends IService<SysAdminUserEntity> {

    /**
     * 保存系统用户信息
     * @param sysAdminUserDTO
     * @return
     */
    int insertSysUser(SysAdminUserDTO sysAdminUserDTO);
}
