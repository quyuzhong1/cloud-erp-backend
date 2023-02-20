package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.SysRoleDTO;
import com.erp.model.sys.entity.SysRoleEntity;
import com.erp.model.sys.entity.SysRoleUserEntity;
import com.erp.server.sys.mapper.SysRoleMapper;
import com.erp.server.sys.service.SysRoleMenuService;
import com.erp.server.sys.service.SysRoleService;
import com.erp.server.sys.service.SysRoleUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


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
            sysRoleMenuService.copyRoleMenu(copyRoleId, newRoleId);
        }


    }

    @Override
    public boolean saveRoleEntity(SysRoleEntity sysRole) {
        checkRoleName(sysRole.getRoleName());
        return this.save(sysRole);
    }

    @Override
    public List<String> listRoleByIds(List<String> roleIds) {
        LambdaQueryWrapper<SysRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRoleEntity::getId,roleIds);
        queryWrapper.select(SysRoleEntity::getRoleName);
        return this.listObjs(queryWrapper,Object::toString);
    }

    @Override
    public List<SysRoleDTO> listRoleByUserIds(List<String> userIds) {
        List<SysRoleUserEntity> list = sysRoleUserService.findRoleIdsByUidList(userIds);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> roleIds = list.stream().map(SysRoleUserEntity::getRoleId).distinct().collect(Collectors.toList());
        List<SysRoleEntity> sysRoleList = this.listByIds(roleIds);
        if (CollectionUtils.isEmpty(sysRoleList)) {
            return new ArrayList<>();
        }
        return BeanMapperUtils.copyList(SysRoleDTO.class,sysRoleList);
    }

    @Override
    public List<SysRoleDTO> getByRoleName(String roleName) {
        LambdaQueryWrapper<SysRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleEntity::getRoleName,roleName);
        List<SysRoleEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return BeanMapperUtils.copyList(SysRoleDTO.class,list);
    }

    /**
     * 检查角色名是否存在
     *
     * @param roleName
     * @return void
     * @author yl
     * @date 2022-11-15 18:39
     */
    private void checkRoleName(String roleName) {
        LambdaQueryWrapper<SysRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleEntity::getRoleName, roleName);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_9025);
        }
    }


}