package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.erp.model.plm.entity.TemplateTaskFollowerEntity;
import com.erp.server.plm.mapper.TemplateTaskFollowerMapper;
import com.erp.server.plm.service.TemplateTaskFollowerService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

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
public class TemplateTaskFollowerServiceImpl extends SuperServiceImpl<TemplateTaskFollowerMapper, TemplateTaskFollowerEntity> implements TemplateTaskFollowerService {

    @Override
    public Boolean saveTemplateFollowerList(String templateId, String taskId, List<String> followerUserIdList) {
        if (CollectionUtils.isEmpty(followerUserIdList)) {
            return Boolean.FALSE;
        }
        baseMapper.deleteFollowerUser(templateId, taskId);
        List<TemplateTaskFollowerEntity> list = new ArrayList<>();
        for (String concernUserId : followerUserIdList) {
            TemplateTaskFollowerEntity concernEntity = new TemplateTaskFollowerEntity();
            concernEntity.setTemplateId(templateId);
            concernEntity.setTemplateTaskId(taskId);
            concernEntity.setUserId(concernUserId);
            list.add(concernEntity);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean deleteByTemplateIdAndTaskIds(String templateId, List<String> taskIds) {
        return lambdaUpdate().eq(TemplateTaskFollowerEntity::getTemplateId, templateId)
                .in(TemplateTaskFollowerEntity::getTemplateTaskId, taskIds)
                .set(TemplateTaskFollowerEntity::getIsDeleted, Boolean.TRUE)
                .update();
    }

    @Override
    public List<TemplateTaskFollowerEntity> listTemplateFollower(String templateId, List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return new ArrayList<>();
        }
        return lambdaQuery().eq(TemplateTaskFollowerEntity::getTemplateId, templateId)
                .in(TemplateTaskFollowerEntity::getTemplateTaskId, taskIds)
                .list();
    }
}
