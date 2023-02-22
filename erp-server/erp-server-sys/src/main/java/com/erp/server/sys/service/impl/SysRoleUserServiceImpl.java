package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.dto.BatchSaveRoleUserDTO;
import com.erp.model.sys.entity.SysRoleUserEntity;
import com.erp.server.sys.mapper.SysRoleUserMapper;
import com.erp.server.sys.service.SysRoleUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


@Service
public class SysRoleUserServiceImpl extends ServiceImpl<SysRoleUserMapper, SysRoleUserEntity> implements SysRoleUserService {

    @Resource
    private SysRoleUserMapper sysRoleUserMapper;

    /**
     * 批量保存 用户 与角色的  关系
     *
     * @param uid     用户id
     * @param roleIds 角色id
     * @param ifAdd
     */
    @Override
    public void batchInsertRef(String uid, List<String> roleIds, boolean ifAdd) {
        //如果是修改 则要先删除数据
        if (!ifAdd) {
            deleteUidRoleRef(uid);
        }
        if (CollectionUtils.isNotEmpty(roleIds)) {
            List<SysRoleUserEntity> saveList = new LinkedList<>();
            for (String roleId : roleIds) {
                SysRoleUserEntity entity = new SysRoleUserEntity();
                entity.setRoleId(roleId);
                entity.setUserId(uid);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }


    }

    /**
     * 根据角色id 删除对应关系
     *
     * @param roleIds
     */
    @Override
    public void removeRefByRoleId(List<String> roleIds) {
        LambdaQueryWrapper<SysRoleUserEntity> wrapper = new LambdaQueryWrapper();
        wrapper.in(SysRoleUserEntity::getRoleId, roleIds);
        sysRoleUserMapper.delete(wrapper);
    }

    @Override
    public List<SysUserDTO> findRoleUser(BaseSearchDTO dto) {
        List<SysUserDTO> list = sysRoleUserMapper.findRoleUser(dto);
        return list;
    }

    /**
     * 根据
     *
     * @param uid
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-07-20 14:07
     */
    @Override
    public List<String> findRoleIdsByUid(String uid) {
        LambdaQueryWrapper<SysRoleUserEntity> wrapper = new LambdaQueryWrapper();
        wrapper.select(SysRoleUserEntity::getRoleId);
        wrapper.eq(SysRoleUserEntity::getUserId, uid);
        return this.listObjs(wrapper,Object::toString);
    }


    /**
     * 根据用户id获取到 用户信息
     *
     * @param userIds
     * @return java.util.List<com.cloud.erp.admin.modules.sys.entity.SysRoleUserEntity>
     * @author yl
     * @date 2022-07-22 16:59
     */
    @Override
    public List<SysRoleUserEntity> findRoleIdsByUidList(List<String> userIds) {
        LambdaQueryWrapper<SysRoleUserEntity> wrapper = new LambdaQueryWrapper();
        if (CollectionUtils.isNotEmpty(userIds)) {
            wrapper.in(SysRoleUserEntity::getUserId, userIds);
            return this.list(wrapper);
        }
        return new ArrayList<>();


    }

    /**
     * 批量保存角色用户
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-29 14:12
     */
    @Override
    public boolean saveBatchRoleUser(BatchSaveRoleUserDTO dto) {
        Set<String> userIds = dto.getUserIds();
        String roleId = dto.getRoleId();
        //先删除对应的关系
        removeRoleUser(roleId, userIds);
        //在添加
        List<SysRoleUserEntity> addList = new LinkedList<>();
        for (String userId : userIds) {
            SysRoleUserEntity entity = new SysRoleUserEntity();
            entity.setUserId(userId);
            entity.setRoleId(roleId);
            addList.add(entity);
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            return this.saveBatch(addList);
        }
        return false;
    }

    /**
     * 复制角色下的用户信息
     *
     * @param copyRoleId
     * @return void
     * @author yl
     * @date 2022-07-29 14:39
     */
    @Override
    public void copyRoleUser(String copyRoleId, String newRoleId) {
        //根据角色id 获取列表
        List<SysRoleUserEntity> roleUserList = roleUserList(copyRoleId);
        if (CollectionUtils.isNotEmpty(roleUserList)) {
            List<SysRoleUserEntity> addList = new LinkedList<>();
            for (SysRoleUserEntity entity : roleUserList) {
                SysRoleUserEntity addEntity = new SysRoleUserEntity();
                addEntity.setRoleId(newRoleId);
                addEntity.setUserId(entity.getUserId());
                addList.add(addEntity);
            }
            this.saveBatch(addList);
        }

    }

    @Override
    public List<SysRoleUserEntity> roleUserList(String roleId){
        LambdaQueryWrapper<SysRoleUserEntity> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleUserEntity::getRoleId,roleId);
        return baseMapper.selectList(queryWrapper);

    }


    public void removeRoleUser(String roleId, Set<String> userIds) {
        if (CollectionUtils.isNotEmpty(userIds)) {
            LambdaQueryWrapper<SysRoleUserEntity> wrapper = new LambdaQueryWrapper();
            wrapper.eq(SysRoleUserEntity::getRoleId, roleId);
            baseMapper.delete(wrapper);
        }
    }


    /**
     * @param uid
     */
    private void deleteUidRoleRef(String uid) {
        LambdaQueryWrapper<SysRoleUserEntity> wrapper = new LambdaQueryWrapper();
        wrapper.eq(SysRoleUserEntity::getUserId, uid);
        sysRoleUserMapper.delete(wrapper);
    }


}