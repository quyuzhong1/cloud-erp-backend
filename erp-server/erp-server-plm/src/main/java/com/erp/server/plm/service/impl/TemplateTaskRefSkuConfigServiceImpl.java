package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.entity.*;
import com.erp.server.plm.mapper.TemplateTaskRefSkuConfigMapper;
import com.erp.server.plm.service.TaskRefSkuConfigService;
import com.erp.server.plm.service.TemplateTaskRefSkuConfigService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    @Transactional(rollbackFor = Exception.class)
    public void saveTemplateTaskRefSkuConfig(String templateId, String productId,List<CopySourceDTO> taskSourceList) {
        List<TaskRefSkuConfigEntity> refSkuConfigList = taskRefSkuConfigService.getByProductId(productId);
        if (CollectionUtils.isNotEmpty(refSkuConfigList)) {
            List<TemplateTaskRefSkuConfigEntity> saveList = new ArrayList<>();
            for (TaskRefSkuConfigEntity item : refSkuConfigList) {
                TemplateTaskRefSkuConfigEntity entity = new TemplateTaskRefSkuConfigEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                entity.setId(IdWorker.getIdStr());
                String taskId=taskSourceList.stream().filter(t->t.getDataId().equals(item.getTaskId())).findFirst().
                        flatMap(obj-> Optional.ofNullable(obj.getNewCreateId())).orElse("");
                entity.setTaskId(taskId);
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
    public List<String> copyTemplateTaskSkuConfig(String templateId, String productId, List<CopySourceDTO> taskSourceList) {
        //添加过的 sku 配置的列表
        List<String> addTaskSkuConfigList = new ArrayList<>();
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
                    addTaskSkuConfigList.add(taskSource.getNewCreateId());
                }
            }
            if (CollectionUtils.isNotEmpty(copyList)) {
                 taskRefSkuConfigService.saveBatch(copyList);
            }
        }
        return addTaskSkuConfigList;

    }


    /**
     * @param taskId          任务id
     * @param templateId      模板id
     * @param fieldConfigType 属性
     * @param fieldJson
     * @return void
     * @author yl
     * @date 2022-11-30 10:34
     */
    @Override
    public void addTemplateTaskRefSkuConfig(String taskId, String templateId, String fieldConfigType, String fieldJson) {
        if (StringUtils.isNotBlank(fieldConfigType)) {
            TemplateTaskRefSkuConfigEntity existEntity = getByTaskId(taskId);
            TemplateTaskRefSkuConfigEntity addEntity = new TemplateTaskRefSkuConfigEntity();
            if (existEntity != null) {
                addEntity.setId(existEntity.getId());
            }
            addEntity.setFieldJson(fieldJson);
            addEntity.setTaskId(taskId);
            addEntity.setTemplateId(templateId);
            addEntity.setFieldConfigType(fieldConfigType);
            this.saveOrUpdate(addEntity);
        }

    }

    /**
     * 根据任务id 获取到对应的关系
     *
     * @param taskId
     * @return com.erp.model.plm.entity.TemplateTaskRefSkuConfigEntity
     * @author yl
     * @date 2022-11-30 10:35
     */
    @Override
    public TemplateTaskRefSkuConfigEntity getByTaskId(String taskId) {
        LambdaQueryWrapper<TemplateTaskRefSkuConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskRefSkuConfigEntity::getTaskId, taskId);
        queryWrapper.last("LIMIT 1");
        return getOne(queryWrapper);
    }


    /**
     * 修改任务的时候  如果选择不关联就要删除sku 与任务的关系
     * @author yl
     * @date 2023-03-10 17:27
     * @param taskId
     * @return void
     */
    @Override
    public void removeByTaskId(String taskId) {
        LambdaQueryWrapper<TemplateTaskRefSkuConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskRefSkuConfigEntity::getTaskId, taskId);
         this.remove(queryWrapper);
    }


    public List<TemplateTaskRefSkuConfigEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskRefSkuConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskRefSkuConfigEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }
}
