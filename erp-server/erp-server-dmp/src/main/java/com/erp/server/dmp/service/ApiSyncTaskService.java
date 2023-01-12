package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.dto.ApiSyncTaskDTO;
import com.erp.model.dmp.entity.ApiSyncTaskEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:38
 */
public interface ApiSyncTaskService extends IService<ApiSyncTaskEntity> {

    /**
     * @description: 新增
     * @author Will
     * @date: 2023/1/11 17:15
     * @param dto
     * @return Boolean
     */
    Boolean insert(ApiSyncTaskDTO dto);

    /**
     * @description: 编辑
     * @author Will
     * @date: 2023/1/11 17:32
     * @param dto
     * @return Boolean
     */
    void update(ApiSyncTaskDTO dto);

    /**
     * @description: 根据平台id、模块类型、业务id查询
     * @author Will
     * @date: 2023/1/11 17:40
     * @param dto
     * @return ApiSyncTaskEntity
     */
    ApiSyncTaskEntity getByApiSyncTask(ApiSyncTaskDTO dto);
    /**
     * @description: 根据平台id、模块类型
     * @author Will
     * @date: 2023/1/12 16:25
     * @param apiSyncTaskDTO
     * @return List<ApiSyncTaskEntity>
     */
    List<ApiSyncTaskEntity> listByApiSyncTask(ApiSyncTaskDTO apiSyncTaskDTO);
}
