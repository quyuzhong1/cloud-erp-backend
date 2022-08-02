package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.dto.SysAdminUserDTO;
import com.cloud.erp.admin.modules.sys.entity.SysAdminUserEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysAdminUserMapper;
import com.cloud.erp.admin.modules.sys.service.SysAdminUserServer;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.password.PassEntity;
import com.common.core.utils.password.PassHandler;
import org.springframework.stereotype.Service;

/**
 * @Classname SysUserServerImpl
 * @Description TODO
 * @Date 2022-07-01 17:09
 * @Created by yl
 */
@Service
public class SysAdminUserServerImpl extends ServiceImpl<SysAdminUserMapper, SysAdminUserEntity> implements SysAdminUserServer {


    @Override
    public int insertSysUser(SysAdminUserDTO sysAdminUserDTO) {
        SysAdminUserEntity entity = new SysAdminUserEntity();
        //复制属性
        BeanMapperUtils.copy(sysAdminUserDTO,entity);
        PassEntity passInfo = PassHandler.buildPassword("");
        entity.setSalt(passInfo.getSalt());
        entity.setPassword(passInfo.getPassword());
        Boolean flag = this.save(entity);
        if (flag) {
            return 1;
        }
        return 0;
    }
}
