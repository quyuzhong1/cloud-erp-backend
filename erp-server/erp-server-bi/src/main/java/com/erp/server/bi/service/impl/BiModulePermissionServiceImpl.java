package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.entity.BiModulePermissionEntity;
import com.erp.server.bi.mapper.BiModulePermissionMapper;
import com.erp.server.bi.service.BiModulePermissionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块权限表(BiModulePermission)表服务实现类
 *
 * @author yl
 * @since 2022-12-12 10:29:35
 */
@Service
public class BiModulePermissionServiceImpl extends ServiceImpl<BiModulePermissionMapper, BiModulePermissionEntity> implements BiModulePermissionService {


    @Override
    public void addModulePermission(String moduleId, List<String> permissionUserIdList) {
        //先删除
        deleteByModuleId(moduleId);
        int size = CollectionUtils.isNotEmpty(permissionUserIdList) ? permissionUserIdList.size() : 10;
        List<BiModulePermissionEntity> addList = new ArrayList<>(size);
        for (String userId : permissionUserIdList) {
            BiModulePermissionEntity entity = new BiModulePermissionEntity();
            entity.setModuleId(moduleId);
            entity.setUserId(userId);
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
        queryWrapper.select(BiModulePermissionEntity::getUserId);
        queryWrapper.eq(BiModulePermissionEntity::getModuleId, moduleId);
        return this.listObjs(queryWrapper,Object::toString);
    }
}
