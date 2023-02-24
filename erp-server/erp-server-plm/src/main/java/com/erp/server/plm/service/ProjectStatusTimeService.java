package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.ProjectStatusTimeEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 20:02
 */
public interface ProjectStatusTimeService extends IService<ProjectStatusTimeEntity> {

    /**
     * @description:
     * @author Will
     * @date: 2023/2/21 19:26
     * @param projectId
     * @param projectStatus
     * @return ProjectStatusTimeEntity
     */
    ProjectStatusTimeEntity getByProjectIdAndProjectStatus(String projectId, Integer projectStatus);
    /**
     * @description:
     * @author Will
     * @date: 2023/2/24 11:32
     * @param projectId
     * @param productId
     * @param projectStatus
     */
    void saveOrUpdateProjectStatusTime(String projectId, String productId, Integer projectStatus);
}
