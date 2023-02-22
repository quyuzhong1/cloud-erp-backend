package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProjectStatusTimeEntity;
import com.erp.server.plm.mapper.ProjectStatusTimeMapper;
import com.erp.server.plm.service.ProjectStatusTimeService;
import org.springframework.stereotype.Service;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:55
 */
@Service
public class ProjectStatusTimeServiceImpl extends ServiceImpl<ProjectStatusTimeMapper, ProjectStatusTimeEntity>
        implements ProjectStatusTimeService {

    @Override
    public ProjectStatusTimeEntity getByProjectIdAndProjectStatus(String projectId, Integer projectStatus) {
        LambdaQueryWrapper<ProjectStatusTimeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectStatusTimeEntity::getProjectId,projectId);
        queryWrapper.eq(ProjectStatusTimeEntity::getStatus,projectStatus.toString());
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }
}
