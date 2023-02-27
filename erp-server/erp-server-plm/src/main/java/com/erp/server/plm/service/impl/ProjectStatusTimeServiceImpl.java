package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProjectStatusTimeEntity;
import com.erp.server.plm.mapper.ProjectStatusTimeMapper;
import com.erp.server.plm.service.ProjectStatusTimeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    @Override
    public void saveOrUpdateProjectStatusTime(String projectId, String productId, Integer projectStatus) {
        ProjectStatusTimeEntity projectStatusTimeEntity = getByProjectIdAndStatus(projectId, projectStatus);
        if (ObjectUtils.isNotEmpty(projectStatusTimeEntity)) {
            //存在则更新状态时间
            projectStatusTimeEntity.setStatusTime(LocalDateTime.now());
        } else {
            //不存在则新增
            projectStatusTimeEntity = new ProjectStatusTimeEntity();
            projectStatusTimeEntity.setProjectId(projectId);
            projectStatusTimeEntity.setProductId(productId);
            projectStatusTimeEntity.setStatusTime(LocalDateTime.now());
            projectStatusTimeEntity.setStatus(String.valueOf(projectStatus));
        }
         this.saveOrUpdate(projectStatusTimeEntity);
    }

    /**
     * @description: 根据项目id和状态查询
     * @author Will
     * @date: 2023/2/24 10:59
     * @param projectId
     * @param approvalStatus
     * @return ProductStatusTimeEntity
     */
    private ProjectStatusTimeEntity getByProjectIdAndStatus(String projectId, Integer approvalStatus) {
        LambdaQueryWrapper<ProjectStatusTimeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectStatusTimeEntity::getProjectId,projectId);
        queryWrapper.eq(ProjectStatusTimeEntity::getStatus,String.valueOf(approvalStatus));
        return this.getOne(queryWrapper);
    }
}
