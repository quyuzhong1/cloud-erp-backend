package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.TemplatePreTaskEntity;
import com.erp.server.plm.mapper.TemplatePreTaskMapper;
import com.erp.server.plm.service.PreTaskService;
import com.erp.server.plm.service.TemplatePreTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
@Service
public class TemplatePreTaskServiceImpl extends ServiceImpl<TemplatePreTaskMapper, TemplatePreTaskEntity>
        implements TemplatePreTaskService {


    @Autowired
    private PreTaskService preTaskService;

    @Override
    public void saveTemplatePreTask(String templateId, String productId) {
        List<PreTaskEntity> list = preTaskService.getPreTaskByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TemplatePreTaskEntity> saveList = new ArrayList<>();
            for (PreTaskEntity item : list) {
                TemplatePreTaskEntity entity = new TemplatePreTaskEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }

    /**
     * 复制模板的前置任务
     *
     * @param templateId
     * @param productId
     * @param taskSourceList
     * @return void
     * @author yl
     * @date 2022-10-28 15:37
     */
    @Override
    public void copyTemplatePreTask(String templateId, String productId, List<TemplateCopySourceDTO> taskSourceList) {
        List<TemplatePreTaskEntity> list = getByTemplateId(templateId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<PreTaskEntity> copyList = new ArrayList<>();
            //这里是 根据新的任务id  与老的任务id 对应的实体 去保存数据
            for (TemplatePreTaskEntity item : list) {
                TemplateCopySourceDTO taskSource = taskSourceList.stream().filter(t ->
                        t.getTemplateDataId().equals(item.getTaskId())).findFirst().orElse(null);
                TemplateCopySourceDTO preSource = taskSourceList.stream().filter(t ->
                        t.getTemplateDataId().equals(item.getPreTaskId())).findFirst().orElse(null);
                if (taskSource != null && preSource != null) {
                    PreTaskEntity entity = new PreTaskEntity();
                    entity.setProductId(productId);
                    entity.setTaskId(taskSource.getNewCreateId());
                    entity.setPreTaskId(preSource.getNewCreateId());
                    copyList.add(entity);
                }
            }
            preTaskService.saveBatch(copyList);
        }

    }

    public List<TemplatePreTaskEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplatePreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplatePreTaskEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }
}




