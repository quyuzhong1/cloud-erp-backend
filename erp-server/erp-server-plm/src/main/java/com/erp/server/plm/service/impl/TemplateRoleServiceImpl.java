package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.CopySourceDTO;
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


    /**
     * 方法说明
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return void
     * @author yl
     * @date 2022-10-28 11:24
     */
    @Override
    public List<CopySourceDTO> copyTemplateRole(String templateId, String productId, String projectId) {
        List<TemplateRoleEntity> list = getByTemplateId(templateId);
        List<CopySourceDTO> sourceList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<ProjectRoleEntity> copyList = new ArrayList<>();
            for (TemplateRoleEntity item : list) {
                CopySourceDTO sourceDTO = new CopySourceDTO();
                ProjectRoleEntity entity = new ProjectRoleEntity();
                BeanMapper.copy(item, entity);
                entity.setProjectId(projectId);
                entity.setProductId(productId);
                String id = IdWorker.getIdStr();
                entity.setId(id);
                sourceDTO.setNewCreateId(id);
                sourceDTO.setDataId(item.getId());
                copyList.add(entity);
                sourceList.add(sourceDTO);
            }
            projectRoleService.saveBatch(copyList);
        }
        return sourceList;
    }

    public List<TemplateRoleEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateRoleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateRoleEntity::getTemplateId, templateId);
        return this.list(queryWrapper);

    }
}




