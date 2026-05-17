package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.mask.resolver.MaskPermissionEvictPublisher;
import com.common.business.dataperm.DataPermissionContextEvictPublisher;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.dto.BatchSaveRoleUserDTO;
import com.erp.model.sys.entity.SysRoleUserEntity;
import com.erp.server.sys.mapper.SysRoleUserMapper;
import com.erp.server.sys.service.SysRoleUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class SysRoleUserServiceImpl extends ServiceImpl<SysRoleUserMapper, SysRoleUserEntity> implements SysRoleUserService {

    @Resource
    private SysRoleUserMapper sysRoleUserMapper;

    @Autowired(required = false)
    private MaskPermissionEvictPublisher maskPermissionEvictPublisher;

    @Autowired(required = false)
    private DataPermissionContextEvictPublisher dataPermissionContextEvictPublisher;

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
            //排除已存在的关联数据
            List<SysRoleUserEntity> oldRoleIds = lambdaQuery().in(SysRoleUserEntity::getRoleId, roleIds).eq(SysRoleUserEntity::getUserId,uid).list();
            if(CollUtil.isNotEmpty(oldRoleIds)){
                Set<String> existingIds = oldRoleIds.stream()
                        .map(SysRoleUserEntity::getRoleId)
                        .collect(Collectors.toSet());
                //把oldList从roleIds中删除
                roleIds.removeIf(existingIds::contains);
            }
            if(CollectionUtils.isNotEmpty(roleIds)){
                List<SysRoleUserEntity> saveList = new LinkedList<>();
                LocalDateTime now = LocalDateTime.now();
                LoginUser loginUser = UserContext.getNonLoginUser();
                for (String roleId : roleIds) {
                    SysRoleUserEntity entity = new SysRoleUserEntity();
                    entity.setRoleId(roleId);
                    entity.setUserId(uid);
                    // 处理公共字段
                    handleCommonField(entity, now, loginUser);
                    saveList.add(entity);
                }
                this.saveBatch(saveList);
            }
        }
        publishMaskPermEvict(Collections.singleton(uid), "SysRoleUserService.batchInsertRef");
    }

    private static void handleCommonField(SysRoleUserEntity entity, LocalDateTime now, LoginUser loginUser) {
        entity.setUpdateTime(now);
        entity.setUpdateUserId(loginUser.getUid());
        entity.setUpdateUserName(loginUser.getUserName());
        entity.setCreateTime(now);
        entity.setCreateUserId(loginUser.getUid());
        entity.setCreateUserName(loginUser.getUserName());
    }

    /**
     * 根据角色id 删除对应关系
     *
     * @param roleIds
     */
    @Override
    public void removeRefByRoleId(List<String> roleIds) {
        // 先查出受影响的 uid 集合，再删除，便于精确广播失效
        Set<String> affectedUids = Collections.emptySet();
        if (CollectionUtils.isNotEmpty(roleIds)) {
            LambdaQueryWrapper<SysRoleUserEntity> q = new LambdaQueryWrapper<>();
            q.select(SysRoleUserEntity::getUserId);
            q.in(SysRoleUserEntity::getRoleId, roleIds);
            List<SysRoleUserEntity> list = sysRoleUserMapper.selectList(q);
            if (CollectionUtils.isNotEmpty(list)) {
                affectedUids = list.stream().map(SysRoleUserEntity::getUserId)
                        .filter(Objects::nonNull).collect(Collectors.toSet());
            }
        }
        LambdaQueryWrapper<SysRoleUserEntity> wrapper = new LambdaQueryWrapper();
        wrapper.in(SysRoleUserEntity::getRoleId, roleIds);
        sysRoleUserMapper.delete(wrapper);
        publishMaskPermEvict(affectedUids, "SysRoleUserService.removeRefByRoleId");
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
        LocalDateTime now = LocalDateTime.now();
        LoginUser loginUser = UserContext.getNonLoginUser();
        for (String userId : userIds) {
            SysRoleUserEntity entity = new SysRoleUserEntity();
            entity.setUserId(userId);
            entity.setRoleId(roleId);
            // 处理公共字段
            handleCommonField(entity, now, loginUser);
            addList.add(entity);
        }
        try {
            if (CollectionUtils.isNotEmpty(addList)) {
                return this.saveBatch(addList);
            }
            return false;
        } finally {
            publishMaskPermEvict(userIds, "SysRoleUserService.saveBatchRoleUser");
        }
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
            LocalDateTime now = LocalDateTime.now();
            LoginUser loginUser = UserContext.getNonLoginUser();
            for (SysRoleUserEntity entity : roleUserList) {
                SysRoleUserEntity addEntity = new SysRoleUserEntity();
                addEntity.setRoleId(newRoleId);
                addEntity.setUserId(entity.getUserId());
                // 处理公共字段
                handleCommonField(addEntity, now, loginUser);
                addList.add(addEntity);
            }
            this.saveBatch(addList);
            Set<String> affectedUids = roleUserList.stream().map(SysRoleUserEntity::getUserId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            publishMaskPermEvict(affectedUids, "SysRoleUserService.copyRoleUser");
        }

    }

    @Override
    public List<SysRoleUserEntity> roleUserList(String roleId){
        LambdaQueryWrapper<SysRoleUserEntity> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleUserEntity::getRoleId,roleId);
        return baseMapper.selectList(queryWrapper);

    }

    @Override
    public List<SysUserDTO.RoleDTO> listRoleByUserIds(List<String> userIds) {
        if(CollUtil.isEmpty(userIds)){
            return  Collections.emptyList();
        }
        return baseMapper.listRoleByUserIds(userIds);
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

    /**
     * 安全调用脱敏权限失效广播；publisher 未注入或 Redis 异常都不影响主业务事务
     *
     * <p>{@code sys_role_user} 改动同时影响"用户的权限码集合"（mask 关心）和"数据权限上下文里
     * roleIdList / permissionsList / dep+shop+warehouse 也是按 roleId 推导"（dataPerm 关心），
     * 所以 mask 与 dataPerm 两个 publisher 都打 publishUser。两者独立 channel，
     * 任一 publisher 故障互不影响。</p>
     */
    private void publishMaskPermEvict(Collection<String> uids, String source) {
        if (uids == null || uids.isEmpty()) {
            return;
        }
        if (maskPermissionEvictPublisher != null) {
            try {
                maskPermissionEvictPublisher.publishUser(uids, source);
            } catch (Throwable ignore) {
                // publisher 内部已经容错，这里再吞一次保证 service 主流程不受影响
            }
        }
        if (dataPermissionContextEvictPublisher != null) {
            try {
                dataPermissionContextEvictPublisher.publishUser(uids, source);
            } catch (Throwable ignore) {
                // 同上，dataPerm 失效广播失败不阻塞主业务事务
            }
        }
    }
}