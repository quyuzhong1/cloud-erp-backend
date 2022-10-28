package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
import com.erp.model.plm.entity.TaskDocsNameEntity;
import com.erp.model.plm.entity.TemplateTaskDocsNameEntity;

import com.erp.server.plm.mapper.TemplateTaskDocsNameMapper;
import com.erp.server.plm.service.TaskDocsNameService;
import com.erp.server.plm.service.TemplateTaskDocsNameService;
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
public class TemplateTaskDocsNameServiceImpl extends ServiceImpl<TemplateTaskDocsNameMapper, TemplateTaskDocsNameEntity>
        implements TemplateTaskDocsNameService {

    @Autowired
    private TaskDocsNameService taskDocsNameService;

    /**
     * 保存模板 文档名
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-27 15:51
     */
    @Override
    public void saveTemplateDocsName(String templateId, String productId) {
        List<TaskDocsNameEntity> list = taskDocsNameService.getDocsNameByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateTaskDocsNameEntity> saveList = new ArrayList<>();
            for (TaskDocsNameEntity item : list) {
                TemplateTaskDocsNameEntity entity = new TemplateTaskDocsNameEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(productId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }


    /**
     * 复制文档名
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return java.util.List<com.erp.model.plm.dto.TemplateCopySourceDTO>
     * @author yl
     * @date 2022-10-28 14:06
     */
    @Override
    public List<TemplateCopySourceDTO> copyTemplateDocsName(String templateId, String productId, String projectId) {
        List<TemplateTaskDocsNameEntity> list = getByTemplateId(templateId);
        List<TemplateCopySourceDTO> sourceList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<TaskDocsNameEntity> copyList = new ArrayList<>();
            for (TemplateTaskDocsNameEntity item : list) {
                TemplateCopySourceDTO source = new TemplateCopySourceDTO();
                TaskDocsNameEntity entity = new TaskDocsNameEntity();
                BeanMapper.copy(item, entity);
                entity.setProductId(productId);
                String id = IdWorker.getIdStr();
                source.setTemplateDataId(item.getId());
                source.setNewCreateId(id);
                copyList.add(entity);
                sourceList.add(source);
            }
            taskDocsNameService.saveBatch(copyList);
        }

        return sourceList;
    }


    public List<TemplateTaskDocsNameEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskDocsNameEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskDocsNameEntity::getTemplateId, templateId);
        return list(queryWrapper);
    }
}




