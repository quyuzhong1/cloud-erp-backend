package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.entity.ProjectRoleEntity;
import com.erp.model.plm.entity.TemplateRoleEntity;

import com.erp.server.plm.mapper.TemplateRoleMapper;
import com.erp.server.plm.service.ProjectRoleService;
import com.erp.server.plm.service.TemplateRoleService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Service
public class TemplateRoleServiceImpl extends ServiceImpl<TemplateRoleMapper, TemplateRoleEntity>
        implements TemplateRoleService {

    @Autowired
    private ProjectRoleService projectRoleService;


    //保存模板项目角色
    @Override
    public void saveTemplateRole(String templateId, String productId) {
        List<ProjectRoleEntity> list = projectRoleService.listByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateRoleEntity> saveList = new ArrayList<>();
            for (ProjectRoleEntity role : list) {
                TemplateRoleEntity entity = new TemplateRoleEntity();
                BeanMapper.copy(role, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }
}




