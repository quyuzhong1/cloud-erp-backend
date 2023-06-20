package com.erp.server.plm.service.impl;

import com.erp.model.plm.entity.TemplateTaskConcernEntity;
import com.erp.server.plm.mapper.TemplateTaskConcernMapper;
import com.erp.server.plm.service.TemplateTaskConcernService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 任务关注的人 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-20
 */
@Slf4j
@Service
public class TemplateTaskConcernServiceImpl extends SuperServiceImpl<TemplateTaskConcernMapper, TemplateTaskConcernEntity> implements TemplateTaskConcernService {

    @Override
    public Boolean saveTemplateConcernList(String templateId, String taskId, List<String> concernUserIdList) {
        baseMapper.deleteConcernUser(templateId, taskId);
        List<TemplateTaskConcernEntity> list = new ArrayList<>();
        for (String concernUserId : concernUserIdList) {
            TemplateTaskConcernEntity concernEntity = new TemplateTaskConcernEntity();
            concernEntity.setTemplateId(templateId);
            concernEntity.setTemplateTaskId(taskId);
            concernEntity.setUserId(concernUserId);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean deleteTemplateConcernList(String templateId, String taskId) {
        return baseMapper.deleteConcernUser(templateId, taskId);
    }

    @Override
    public Boolean deleteByTemplateIdAndTaskIds(String templateId, List<String> taskIds) {
        return lambdaUpdate().eq(TemplateTaskConcernEntity::getTemplateId, templateId)
                .in(TemplateTaskConcernEntity::getTemplateTaskId, taskIds)
                .set(TemplateTaskConcernEntity::getIsDeleted, Boolean.TRUE)
                .update();
    }

    @Override
    public List<TemplateTaskConcernEntity> listTemplateConcern(String templateId, List<String> taskIds) {
        return lambdaQuery().eq(TemplateTaskConcernEntity::getTemplateTaskId, taskIds)
                .in(TemplateTaskConcernEntity::getTemplateTaskId, taskIds)
                .list();
    }
}
