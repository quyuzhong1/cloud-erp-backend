package com.erp.server.plm.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.CopySourceDTO;
import com.erp.model.plm.dto.PreTaskDTO;
import com.erp.model.plm.entity.PreTaskEntity;
import com.erp.model.plm.entity.TemplatePreTaskEntity;
import com.erp.model.plm.vo.PreTaskVO;
import com.erp.server.plm.mapper.TemplatePreTaskMapper;
import com.erp.server.plm.service.PreTaskService;
import com.erp.server.plm.service.TemplatePreTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public void copyTemplatePreTask(String templateId, String productId, List<CopySourceDTO> taskSourceList) {
        List<TemplatePreTaskEntity> list = getByTemplateId(templateId);
        //是否已存在
        List<PreTaskEntity> existList = preTaskService.getPreTaskByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            List<PreTaskEntity> copyList = new ArrayList<>();
            //这里是 根据新的任务id  与老的任务id 对应的实体 去保存数据
            for (TemplatePreTaskEntity item : list) {
                PreTaskEntity exist = existList.stream().filter(e -> e.getPreTaskId().
                        equals(item.getPreTaskId()) && e.getTaskId().equals(item.getTaskId()))
                        .findFirst().orElse(null);
                //
                if (Objects.isNull(exist)) {
                    CopySourceDTO taskSource = taskSourceList.stream().filter(t ->
                            t.getDataId().equals(item.getTaskId())).findFirst().orElse(null);
                    CopySourceDTO preSource = taskSourceList.stream().filter(t ->
                            t.getDataId().equals(item.getPreTaskId())).findFirst().orElse(null);
                    if (null != taskSource && null != preSource) {
                        PreTaskEntity entity = new PreTaskEntity();
                        entity.setProductId(productId);
                        entity.setTaskId(taskSource.getNewCreateId());
                        entity.setPreTaskId(preSource.getNewCreateId());
                        entity.setRelationship(exist.getRelationship());
                        entity.setIntervalWorkPeriod(exist.getIntervalWorkPeriod());
                        copyList.add(entity);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(copyList)) {
                preTaskService.saveBatch(copyList);
            }

        }

    }

    @Override
    public void saveTemplatePreTaskList(String taskId, List<String> preTaskIdList, String templateId) {
        //先删除前置任务
        removeTemplatePreTask(taskId,templateId, preTaskIdList);
        if (CollectionUtils.isNotEmpty(preTaskIdList)) {
            List<TemplatePreTaskEntity> addList = new ArrayList<>();
            for (String preTaskId : preTaskIdList) {
                TemplatePreTaskEntity entity = new TemplatePreTaskEntity();
                entity.setPreTaskId(preTaskId);
                entity.setTaskId(taskId);
                entity.setTemplateId(templateId);
                addList.add(entity);
            }
            this.saveBatch(addList);
        }
    }

    @Override
    public List<PreTaskVO> getTemplatePreTaskIdList(String taskId, String templateId) {
        LambdaQueryWrapper<TemplatePreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplatePreTaskEntity::getTaskId, taskId);
        queryWrapper.eq(TemplatePreTaskEntity::getTemplateId,templateId);
        queryWrapper.select(TemplatePreTaskEntity::getPreTaskId, TemplatePreTaskEntity::getRelationship, TemplatePreTaskEntity::getIntervalWorkPeriod);
        List<TemplatePreTaskEntity> entityList = this.list(queryWrapper);
        if (CollectionUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        return entityList.stream().map(PreTaskVO::new).collect(Collectors.toList());
    }

    public List<TemplatePreTaskEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplatePreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplatePreTaskEntity::getTemplateId, templateId);
        return this.list(queryWrapper);
    }

    /**
     * @description: 根据任务和模板删除
     * @author Will
     * @date: 2022/11/16 10:28
     * @param taskId
     * @param templateId
     * @param preTaskIdList
     */
    private void removeTemplatePreTask(String taskId,String templateId, List<String> preTaskIdList) {
        LambdaQueryWrapper<TemplatePreTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplatePreTaskEntity::getTaskId, taskId);
        queryWrapper.eq(TemplatePreTaskEntity::getTemplateId, templateId);
        if (CollectionUtils.isNotEmpty(preTaskIdList)) {
            queryWrapper.in(TemplatePreTaskEntity::getPreTaskId, preTaskIdList);
        }
        this.remove(queryWrapper);
    }
}




