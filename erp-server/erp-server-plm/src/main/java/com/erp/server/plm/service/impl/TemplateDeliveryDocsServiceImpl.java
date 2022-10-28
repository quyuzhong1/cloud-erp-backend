package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import com.erp.model.plm.entity.TemplateDeliveryDocsEntity;

import com.erp.server.plm.mapper.TemplateDeliveryDocsMapper;
import com.erp.server.plm.service.TaskDeliveryService;
import com.erp.server.plm.service.TemplateDeliveryDocsService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@Service
public class TemplateDeliveryDocsServiceImpl extends ServiceImpl<TemplateDeliveryDocsMapper, TemplateDeliveryDocsEntity>
        implements TemplateDeliveryDocsService {


    @Autowired
    private TaskDeliveryService taskDeliveryService;

    /**
     * 交付文档
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-10-27 16:07
     */
    @Override
    public void saveTemplateDeliveryDocs(String templateId, String productId) {
        List<TaskDeliveryDocsEntity> list = taskDeliveryService.getByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplateDeliveryDocsEntity> saveList = new ArrayList<>();
            for (TaskDeliveryDocsEntity item : list) {
                TemplateDeliveryDocsEntity entity = new TemplateDeliveryDocsEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }

    /**
     * 从模板复制交付文档数据
     *
     * @param templateId
     * @param productId
     * @param taskSourceList
     * @param docsNameSourceList
     * @return void
     * @author yl
     * @date 2022-10-28 15:57
     */
    @Override
    public void copyTemplateDeliveryDocs(String templateId, String productId, List<TemplateCopySourceDTO> taskSourceList, List<TemplateCopySourceDTO> docsNameSourceList) {
        List<TemplateDeliveryDocsEntity> list = this.getByTemplateId(templateId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TaskDeliveryDocsEntity> copyList = new ArrayList<>();
            for (TemplateDeliveryDocsEntity item : list) {
                TaskDeliveryDocsEntity entity = new TaskDeliveryDocsEntity();
                BeanMapper.copy(item, entity);
                entity.setProductId(productId);
                TemplateCopySourceDTO docsNameSource = docsNameSourceList.stream().
                        filter(d -> d.getTemplateDataId().equals(item.getDocsNameId())).findFirst().orElse(null);
                if (docsNameSource != null) {
                    entity.setDocsNameId(docsNameSource.getNewCreateId());
                } else {
                    entity.setDocsNameId("");
                }
                TemplateCopySourceDTO taskSource = taskSourceList.stream().
                        filter(d -> d.getTemplateDataId().equals(item.getTaskId())).findFirst().orElse(null);
                if (taskSource != null) {
                    entity.setTaskId(taskSource.getNewCreateId());
                } else {
                    entity.setTaskId("");
                }
            }
            taskDeliveryService.saveBatch(copyList);
        }
    }


    public List<TemplateDeliveryDocsEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateDeliveryDocsEntity::getTemplateId, templateId);
        return this.list(queryWrapper);

    }
}




