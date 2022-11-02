package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.SetDocsPowerDTO;
import com.erp.model.plm.entity.DocsPermissionEntity;
import com.erp.server.plm.mapper.DocsPermissionEntityMapper;
import com.erp.server.plm.service.DocsPermissionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class DocsPermissionServiceImpl extends ServiceImpl<DocsPermissionEntityMapper, DocsPermissionEntity>
        implements DocsPermissionService {


    /**
     * 根据用户id 获取到
     *
     * @param uid
     * @return java.util.List<jav ` a.lang.String>
     * @author yl
     * @date 2022-09-23 15:13
     */
    @Override
    public List<String> getDocsIdsByUserId(String uid, String productId) {
        LambdaQueryWrapper<DocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DocsPermissionEntity::getDeliveryDocsId);
        queryWrapper.eq(DocsPermissionEntity::getProductId, productId);
        queryWrapper.eq(DocsPermissionEntity::getQueryRoleId, uid);
        return this.listObjs(queryWrapper, Object::toString);
    }

    @Override
    public void removePermission(String taskId) {
        LambdaQueryWrapper<DocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocsPermissionEntity::getTaskId, taskId);
        this.remove(queryWrapper);
    }

    @Override
    public void removeByDeliveryDocsId(List<String> docsIds) {
        LambdaQueryWrapper<DocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DocsPermissionEntity::getDeliveryDocsId, docsIds);
        this.remove(queryWrapper);
    }


    /**
     * 根据产品id 获取 文档权限
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.entity.DocsPermissionEntity>
     * @author yl
     * @date 2022-10-31 9:53
     */
    @Override
    public List<DocsPermissionEntity> getDocsPermissionByProductId(String productId) {
        LambdaQueryWrapper<DocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocsPermissionEntity::getProductId, productId);
        return this.list(queryWrapper);
    }

    @Override
    public List<String> getDocsIdsByProductId(String productId) {
        LambdaQueryWrapper<DocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DocsPermissionEntity::getDeliveryDocsId);
        queryWrapper.eq(DocsPermissionEntity::getProductId, productId);
        return listObjs(queryWrapper, Object::toString);
    }


    /**
     * 根据用户角色 查询到权限
     *
     * @param userRoleIds
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-11-01 19:07
     */
    @Override
    public List<String> getDocsIdsByRoleIds(List<String> userRoleIds, String productId) {
        LambdaQueryWrapper<DocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DocsPermissionEntity::getDeliveryDocsId);
        queryWrapper.eq(DocsPermissionEntity::getProductId, productId);
        queryWrapper.in(DocsPermissionEntity::getQueryRoleId, userRoleIds);
        return this.listObjs(queryWrapper, Object::toString);
    }


    /**
     * 查询 设置了全部的 文档id
     *
     * @param productId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-11-01 19:13
     */
    @Override
    public List<String> getAllDeliveryDocsIds(String productId) {
        LambdaQueryWrapper<DocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DocsPermissionEntity::getDeliveryDocsId);
        queryWrapper.eq(DocsPermissionEntity::getProductId, productId);
        queryWrapper.eq(DocsPermissionEntity::getQueryRoleId, "");
        return this.listObjs(queryWrapper, Object::toString);
    }

    @Override
    public SetDocsPowerDTO getDocsPower(String deliveryDocsId) {
        if (StringUtils.isBlank(deliveryDocsId)) {
            throw new ServiceException(ApiError.ERROR_95052);
        }
        LambdaQueryWrapper<DocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocsPermissionEntity::getDeliveryDocsId, deliveryDocsId);
        List<DocsPermissionEntity> list = this.list(queryWrapper);
        SetDocsPowerDTO power = new SetDocsPowerDTO();
        power.setId(deliveryDocsId);
        List<String> roleIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            if (StringUtils.isNotBlank(list.get(0).getQueryRoleId())) {
                roleIdList = list.stream().map(DocsPermissionEntity::getQueryRoleId).collect(Collectors.toList());
            }
        }
        power.setRoleIdList(roleIdList);
        return power;
    }
}
