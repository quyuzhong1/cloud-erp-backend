package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.DocsPermissionEntity;
import com.erp.server.plm.mapper.DocsPermissionEntityMapper;
import com.erp.server.plm.service.DocsPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-09-23 15:13
     */
    @Override
    public List<String> getDocsIdsByUserId(String uid) {
        LambdaQueryWrapper<DocsPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DocsPermissionEntity::getDeliveryDocsId);
        queryWrapper.eq(DocsPermissionEntity::getQueryUserId,uid);
        return this.listObjs(queryWrapper,Object::toString);
    }
}
