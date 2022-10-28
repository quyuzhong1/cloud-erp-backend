package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.TemplateCopySourceDTO;
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
        if (CollectionUtils.isNotEmpty(projectTaskList)) {
            List<TemplateTaskEntity> saveList = new ArrayList<>();
            for (ProjectTaskEntity item : projectTaskList) {
                TemplateTaskEntity entity = new TemplateTaskEntity();
                BeanMapper.copy(item, entity);
                entity.setTemplateId(templateId);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
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

    /**
     * 复制模板任务
     *
     * @param templateId
     * @param productId
     * @param projectId
     * @return void
     * @author yl
     * @date 2022-10-28 14:22
     */
    @Override
    public List<TemplateCopySourceDTO> copyTemplateTask(String templateId, String productId, String projectId, List<TemplateCopySourceDTO> phaseSourceList) {
        List<TemplateTaskEntity> list = this.getByTemplateId(templateId);
        //来源信息
        List<TemplateCopySourceDTO> sourceList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<ProjectTaskEntity> copyList = new ArrayList<>(list.size());
            for (TemplateTaskEntity item : list) {
                TemplateCopySourceDTO source = new TemplateCopySourceDTO();
                source.setTemplateDataId(item.getId());
                String taskId = IdWorker.getIdStr();
                ProjectTaskEntity taskEntity = new ProjectTaskEntity();
                BeanMapper.copy(item, taskEntity);
                taskEntity.setProductId(productId);
                taskEntity.setProjectId(projectId);
                taskEntity.setId(taskId);
                source.setNewCreateId(taskId);
                TemplateCopySourceDTO phase = phaseSourceList.stream().filter(p -> p.getTemplateDataId()
                        .equals(item.getPhaseId())).findFirst().orElse(null);
                if (phase != null) {
                    taskEntity.setPhaseId(phase.getNewCreateId());
                } else {
                    taskEntity.setPhaseId("");
                }
                copyList.add(taskEntity);
                sourceList.add(source);
            }

            //更改父id
            for (ProjectTaskEntity task : copyList) {
                //这个pid 还是 模板数据的pid
                String pid = task.getPid();
                if (!pid.equals("0")) {
                    TemplateCopySourceDTO source = sourceList.stream().
                            filter(s -> s.getTemplateDataId().equals(pid)).findFirst().orElse(null);
                    if (source != null) {
                        task.setPid(source.getNewCreateId());
                    } else {
                        task.setPid("0");
                    }
                }
            }

        }
        return sourceList;
    }


    public List<TemplateTaskEntity> getByTemplateId(String templateId) {
        LambdaQueryWrapper<TemplateTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTaskEntity::getTemplateId, templateId);
        queryWrapper.orderByAsc(TemplateTaskEntity::getPid);
        return this.list(queryWrapper);
    }
}
