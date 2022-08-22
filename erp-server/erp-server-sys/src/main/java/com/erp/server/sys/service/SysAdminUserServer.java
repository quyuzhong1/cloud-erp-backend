package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.sys.dto.SysAdminUserDTO;
import com.erp.model.sys.entity.SysAdminUserEntity;

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
