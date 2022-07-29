package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.entity.SysRoleEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysRoleMapper;
import com.cloud.erp.admin.modules.sys.service.SysRoleMenuService;
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


    @Autowired

    private SysRoleMenuService  sysRoleMenuService;








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
            //删除角色与用户的关系
            sysRoleUserService.removeRefByRoleId(ids);
            //删除角色与菜单的关系
            sysRoleMenuService.removeRefByRoleIds(ids);

        }
    }



}