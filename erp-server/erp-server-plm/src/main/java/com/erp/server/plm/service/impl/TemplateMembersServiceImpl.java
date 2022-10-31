package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.model.plm.entity.TemplateMembersEntity;

import com.erp.server.plm.mapper.TemplateMembersMapper;
import com.erp.server.plm.service.ProjectMembersService;
import com.erp.server.plm.service.TemplateMembersService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Service
public class TemplateMembersServiceImpl extends ServiceImpl<TemplateMembersMapper, TemplateMembersEntity>
        implements TemplateMembersService {

    @Autowired
    private ProjectMembersService projectMembersService;

    /**
     * 保存 模板成员
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-27 15:12
     */
    @Override
    public void saveMember(String templateId, String productId) {
        List<ProjectMembersEntity> list = projectMembersService.getListByProductId(productId);
        List<TemplateMembersEntity> saveList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectMembersEntity item : list) {
                TemplateMembersEntity entity = new TemplateMembersEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }


    /**
     * 从模板复制成员
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return void
     * @author yl
     * @date 2022-10-28 11:13
     */
    @Override
    public void copyTemplateMembers(String templateId, String productId, String projectId) {
        List<TemplateMembersEntity> templateMembers = getByTemplateId(templateId);
        if (CollectionUtils.isNotEmpty(templateMembers)) {
            List<ProjectMembersEntity> copyList = new ArrayList<>();
            for (TemplateMembersEntity item : templateMembers) {
                ProjectMembersEntity entity = new ProjectMembersEntity();
                BeanMapper.copy(item, entity);
                entity.setProductId(productId);
                entity.setProjectId(projectId);
                entity.setId(IdWorker.getIdStr());
                copyList.add(entity);
            }
            projectMembersService.saveBatch(copyList);

        }
    }


    public List<TemplateMembersEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateMembersEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }
}




