package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.dto.SysUserDTO;
import com.cloud.erp.admin.modules.sys.entity.SysUserEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysUserMapper;
import com.cloud.erp.admin.modules.sys.service.SysUserServer;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Classname SysUserServerImpl
 * @Description TODO
 * @Date 2022-07-01 17:09
 * @Created by yl
 */
@Service
public class SysUserServerImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements SysUserServer {


    @Override
    public int insertSysUser(SysUserDTO sysUserDTO) {
        SysUserEntity entity = new SysUserEntity();
        entity.setLoginAccount(sysUserDTO.getAdminAccount());
        entity.setPassword(sysUserDTO.getAdminAccountPassword());
        entity.setSalt(sysUserDTO.getAdminSalt());
        entity.setCreateTime(new Date());
        Boolean flag = this.save(entity);
        if (flag) {
            return 1;
        }
        return 0;
    }
}
