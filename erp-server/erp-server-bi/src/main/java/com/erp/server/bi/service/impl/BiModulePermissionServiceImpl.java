package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.entity.BiModulePermissionEntity;
import com.erp.model.bi.enums.BiShareIdentityTypeEnum;
import com.erp.server.bi.enums.DashboardEnum;
import com.erp.server.bi.mapper.BiModulePermissionMapper;
import com.erp.server.bi.service.BiModulePermissionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模块权限表(BiModulePermission)表服务实现类
 *
 * @author yl
 * @since 2022-12-12 10:29:35
 */
@Service
public class BiModulePermissionServiceImpl extends ServiceImpl<BiModulePermissionMapper, BiModulePermissionEntity> implements BiModulePermissionService {


    @Override
    public void addModulePermission(String moduleId, List<String> permissionUserIdList, BiShareIdentityTypeEnum typeEnum) {
        //先删除
        deleteByModuleId(moduleId);
        int size = CollectionUtils.isNotEmpty(permissionUserIdList) ? permissionUserIdList.size() : 10;
        List<BiModulePermissionEntity> addList = new ArrayList<>(size);
        for (String userId : permissionUserIdList) {
            BiModulePermissionEntity entity = new BiModulePermissionEntity();
            entity.setModuleId(moduleId);
            entity.setIdentityId(userId);
            entity.setIdentityType(typeEnum.getCode());
            addList.add(entity);
        }
        this.saveBatch(addList);
    }


    /**
     * 根据模块id 删除对应的权限
     *
     * @param moduleId
     * @return void
     * @author yl
     * @date 2022-12-12 11:12
     */
    @Override
    public void deleteByModuleId(String moduleId) {
        LambdaQueryWrapper<BiModulePermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiModulePermissionEntity::getModuleId, moduleId);
        this.remove(queryWrapper);
    }

    /**
     * 根据模块id 获取用户
     *
     * @param moduleId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-12-16 16:17
     */
    @Override
    public List<String> getByModuleId(String moduleId) {
        LambdaQueryWrapper<BiModulePermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiModulePermissionEntity::getIdentityId);
        queryWrapper.eq(BiModulePermissionEntity::getModuleId, moduleId);
        return this.listObjs(queryWrapper,Object::toString);
    }

    /**
     * 根据用户id 查询可以看见的模块id
     * @author yl
     * @date 2023-01-05 17:28
     * @param userId
     * @return java.util.List<java.lang.String>
     */
    @Override
    public List<String> getModuleIdsByUserId(String userId) {
        LambdaQueryWrapper<BiModulePermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiModulePermissionEntity::getModuleId);
        queryWrapper.eq(BiModulePermissionEntity::getIdentityId, userId);
        return this.listObjs(queryWrapper,Object::toString);
    }

    /**
     * 检查和添加
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndAddModulePermission(List<String> shareFlagIdList, String mainId, String shareFlag) {
        // 私人
        if (DashboardEnum.PERSONAL.getFlag().equalsIgnoreCase(shareFlag)){
            // 移除其他
            deleteByModuleId(mainId);
            return;
        }

        // 类
        BiShareIdentityTypeEnum identityTypeEnum = BiShareIdentityTypeEnum.isRoleCheck(shareFlag);
        // 添加
        addModulePermission(mainId, shareFlagIdList, identityTypeEnum);
    }

    /**
     * 通过ModuleId查询
     */
    @Override
    public List<BiModulePermissionEntity> findByModuleId(String moduleId) {
        return lambdaQuery()
                .eq(BiModulePermissionEntity::getModuleId, moduleId)
                .list();
    }

    @Override
    public Map<String, List<BiModulePermissionEntity>> mapByModuleIds(List<String> moduleIds) {
        if (CollectionUtils.isEmpty(moduleIds)){
            return Collections.emptyMap();
        }
        return lambdaQuery()
                .in(BiModulePermissionEntity::getModuleId, moduleIds)
                .list()
                .stream()
                .collect(Collectors.groupingBy(BiModulePermissionEntity::getModuleId))
                ;
    }

    @Override
    public List<String> findModuleId(String userId, List<String> roleIdList) {
        LambdaQueryChainWrapper<BiModulePermissionEntity> lambdaWrapper = lambdaQuery()
                .eq(BiModulePermissionEntity::getIdentityType, BiShareIdentityTypeEnum.USER.getCode())
                .eq(BiModulePermissionEntity::getIdentityId, userId);

        if (CollectionUtils.isNotEmpty(roleIdList)){
            lambdaWrapper = lambdaWrapper.or(w->
                    w.eq(BiModulePermissionEntity::getIdentityType, BiShareIdentityTypeEnum.ROLE.getCode())
                            .in(BiModulePermissionEntity::getIdentityId, roleIdList)
            );
        }
        return lambdaWrapper.list().stream()
                .map(BiModulePermissionEntity::getModuleId)
                .distinct()
                .collect(Collectors.toList());
    }
}
