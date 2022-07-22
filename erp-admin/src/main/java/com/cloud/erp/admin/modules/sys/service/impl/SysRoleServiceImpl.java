package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.entity.SysRoleEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysRoleMapper;
import com.cloud.erp.admin.modules.sys.service.SysRoleService;
import com.cloud.erp.admin.modules.sys.service.SysRoleUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

    @Autowired
    private SysRoleUserService  sysRoleUserService;








    /**
     * 根据角色id 删除 角色
     * @param ids
     */
    @Override
    @Transactional
    public void removeRoleById(List<String> ids) {
       boolean flag= this.removeByIds(ids);
        //表示删除成功 则清掉用户
        if(flag){
            sysRoleUserService.removeRefByRoleId(ids);
        }
    }



}