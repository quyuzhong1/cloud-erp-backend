package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.*;
import com.erp.server.plm.mapper.TemplateTaskRefSkuConfigMapper;
import com.erp.server.plm.service.TaskRefSkuConfigService;
import com.erp.server.plm.service.TemplateTaskRefSkuConfigService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板任务与sku字段配置关系表(TemplateTaskRefSkuConfig)表服务实现类
 *
 * @author yl
 * @since 2022-11-24 16:19:15
 */
@Service
public class TemplateTaskRefSkuConfigServiceImpl extends ServiceImpl<TemplateTaskRefSkuConfigMapper, TemplateTaskRefSkuConfigEntity> implements TemplateTaskRefSkuConfigService {

    @Autowired
    private TaskRefSkuConfigService taskRefSkuConfigService;

    /**
     * 保存模板任务与sku 配置关系
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-20 15:40
     */
    @Override
    @Transactional
    public void saveTemplateTaskRefSkuConfig(String templateId, String productId) {
        List<TaskRefSkuConfigEntity> refSkuConfigList = taskRefSkuConfigService.getByProductId(productId);
        if (CollectionUtils.isNotEmpty(refSkuConfigList)) {
            List<TemplateTaskRefSkuConfigEntity> saveList = new ArrayList<>();
            for (TaskRefSkuConfigEntity item : refSkuConfigList) {
                TemplateTaskRefSkuConfigEntity entity = new TemplateTaskRefSkuConfigEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        }
    }

    /**
     * 从模板复制 任务sku 配置关系
     *
     * @param templateId
     * @param productId
     * @param taskSourceList
     * @return void
     * @author yl
     * @date 2022-11-24 17:22
     */
    @Override
    public void copyTemplateTaskSkuConfig(String templateId, String productId, List<CopySourceDTO> taskSourceList) {
        List<TemplateTaskRefSkuConfigEntity> list = getByTemplateId(templateId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<TaskRefSkuConfigEntity> copyList = new ArrayList<>();
            for (TemplateTaskRefSkuConfigEntity item : list) {
                CopySourceDTO taskSource = taskSourceList.stream().filter(t ->
                        t.getDataId().equals(item.getTaskId())).findFirst().orElse(null);
                if (taskSource != null) {
                    TaskRefSkuConfigEntity entity = new TaskRefSkuConfigEntity();
                    entity.setProductId(productId);
                    entity.setFieldConfigType(item.getFieldConfigType());
                    entity.setTaskId(taskSource.getNewCreateId());
                    entity.setFieldJson(item.getFieldJson());
                    copyList.add(entity);
                }
            }

            taskRefSkuConfigService.saveBatch(copyList);
        }


    }


    public List<TemplateTaskRefSkuConfigEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskRefSkuConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskRefSkuConfigEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }
}
