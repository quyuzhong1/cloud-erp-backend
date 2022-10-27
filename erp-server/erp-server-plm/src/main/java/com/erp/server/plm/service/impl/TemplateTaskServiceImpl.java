package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TemplateTaskEntity;
import com.erp.server.plm.mapper.TemplateTaskMapper;
import com.erp.server.plm.service.PreTaskService;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.TaskDeliveryService;
import com.erp.server.plm.service.TemplateTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname TemplateTaskServiceImpl
 * @Description TODO
 * @Date 2022-09-20 15:35
 * @Created by yl
 */
@Service
public class TemplateTaskServiceImpl extends ServiceImpl<TemplateTaskMapper, TemplateTaskEntity> implements TemplateTaskService {

    @Autowired
    private ProjectTaskService taskService;

    @Autowired
    private TaskDeliveryService taskDeliveryService;


    @Autowired
    private PreTaskService preTaskService;

    /**
     * 保存模板任务
     *
     * @param templateId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-20 15:40
     */
    @Override
    @Transactional
    public void saveTemplateTask(String templateId, String productId) {
        List<ProjectTaskEntity> projectTaskList = taskService.getByProductId(productId);

        List<ProjectTaskEntity> parentProjectTaskList=projectTaskList.stream().filter(p->p.getPid().equals("0")).collect(Collectors.toList());
        //从父级开始
        if (CollectionUtils.isNotEmpty(parentProjectTaskList)) {
            for (ProjectTaskEntity item : projectTaskList) {
                String taskId = item.getId();
                TemplateTaskEntity entity = new TemplateTaskEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                String templateTaskId = IdWorker.getIdStr();
                entity.setId(templateTaskId);
                entity.setPhaseId("");
                entity.setPhaseName("");
                String pid=item.getPid();
                boolean flag = this.save(entity);
                if (flag) {
                    taskDeliveryService.saveTaskDeliveryDocs(templateId, templateTaskId, item.getId());
                    //这是前置任务
                    List<ProjectTaskEntity> preTaskList = preTaskService.getPreTaskList(taskId);
                    saveTemplatePreTask(templateId, preTaskList, templateTaskId);


                }

            }


        }

    }


    /**
     * 保存前置任务
     *
     * @param preTaskList
     * @param templateTaskId
     * @return void
     * @author yl
     * @date 2022-10-27 10:07
     */
    public void saveTemplatePreTask(String templateId, List<ProjectTaskEntity> preTaskList, String templateTaskId) {
        List<String> preTaskListId = new ArrayList<>();
        for (ProjectTaskEntity preTask : preTaskList) {
            TemplateTaskEntity entity = new TemplateTaskEntity();
            BeanMapper.copy(preTask, entity);
            entity.setTemplateId(templateId);
            String tempTaskId = IdWorker.getIdStr();
            entity.setId(tempTaskId);
            entity.setPhaseId("");
            entity.setPhaseName("");
            boolean flag = this.save(entity);
            if (flag) {
                preTaskListId.add(tempTaskId);
                taskDeliveryService.saveTaskDeliveryDocs(templateId, tempTaskId, preTask.getId());
            }
        }
        if (CollectionUtils.isNotEmpty(preTaskListId)) {
            preTaskService.savePreTask(templateTaskId, preTaskListId);
        }

    }

    /**
     * 根据模板 获取项目任务
     *
     * @param flagTemplateId
     * @return java.util.List<com.erp.model.plm.entity.TemplateTaskEntity>
     * @author yl
     * @date 2022-09-21 9:57
     */
    @Override
    public List<TemplateTaskEntity> getTaskByTemplateId(String flagTemplateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, flagTemplateId);
        queryWrapper.isNull(TemplateTaskEntity::getQuoteSysTaskId);
        return this.list(queryWrapper);
    }
}
