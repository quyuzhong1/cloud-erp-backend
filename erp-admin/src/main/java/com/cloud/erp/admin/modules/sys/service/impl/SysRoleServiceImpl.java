package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.entity.SysRoleEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysRoleMapper;
import com.cloud.erp.admin.modules.sys.service.SysRoleMenuService;
import com.cloud.erp.admin.modules.sys.service.SysRoleService;
import com.cloud.erp.admin.modules.sys.service.SysRoleUserService;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

    @Autowired
    private SysRoleUserService sysRoleUserService;


    @Autowired

    private SysRoleMenuService sysRoleMenuService;


    /**
     * 根据角色id 删除 角色
     *
     * @param ids
     */
    @Override
    @Transactional
    public void removeRoleById(List<String> ids) {
        boolean flag = this.removeByIds(ids);
        //表示删除成功 则清掉用户
        if (flag) {
            //删除角色与用户的关系
            sysRoleUserService.removeRefByRoleId(ids);
            //删除角色与菜单的关系
            sysRoleMenuService.removeRefByRoleIds(ids);

        }
    }

    /**
     * 根据角色id 复制角该角色
     * 包括 角色下的用户以及权限
     *
     * @param copyRoleId
     * @return void
     * @author yl
     * @date 2022-07-29 14:28
     */
    @Override
    @Transactional
    public void copyRole(String copyRoleId) {
        SysRoleEntity roleEntity = this.getById(copyRoleId);
        if (Objects.isNull(roleEntity)) {
            throw new ServiceException(ApiError.ERROR_9021);
        }
        SysRoleEntity addEntity = new SysRoleEntity();
        addEntity.setRoleName(roleEntity.getRoleName());
        addEntity.setRoleRemark(roleEntity.getRoleRemark());
        String newRoleId = IdWorker.getIdStr(addEntity);
        addEntity.setId(newRoleId);
        Boolean saveResult = this.save(addEntity);
        if (saveResult) {
            //复制角色下的用户
            sysRoleUserService.copyRoleUser(copyRoleId, newRoleId);

            //复制角色下的权限
            sysRoleMenuService.copyRoleMenu(copyRoleId,newRoleId);
        }



    }


}