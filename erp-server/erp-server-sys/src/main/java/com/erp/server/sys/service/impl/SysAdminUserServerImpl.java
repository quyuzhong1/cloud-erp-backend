package com.erp.server.sys.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.entity.password.PassEntity;
import com.erp.model.sys.entity.password.PassHandler;
import com.erp.model.sys.dto.SysAdminUserDTO;
import com.erp.model.sys.entity.SysAdminUserEntity;
import com.erp.server.sys.mapper.SysAdminUserMapper;
import com.erp.server.sys.service.SysAdminUserServer;
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
