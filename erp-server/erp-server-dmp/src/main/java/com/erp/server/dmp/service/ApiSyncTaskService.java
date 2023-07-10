package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.dto.ApiSyncTaskDTO;
import com.erp.model.dmp.entity.ApiSyncTaskEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 推送任务业务接口
 * @date 2023/7/10 16:08
 */
public interface ApiSyncTaskService extends IService<ApiSyncTaskEntity> {
    /**
     * @description: 新增推送任务
     * @author Will
     * @date: 2023/7/10 16:23
     * @param dto
     * @return Boolean
     */
    Boolean insert(ApiSyncTaskDTO dto);
    /**
     * @description: 查询推送任务
     * @author Will
     * @date: 2023/7/10 17:26
     * @param apiSyncTaskDTO
     * @return List<ApiSyncTaskEntity>
     */
    List<ApiSyncTaskEntity> listByApiSyncTaskDTO(ApiSyncTaskDTO apiSyncTaskDTO);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/7/10 18:55
     * @param apiSyncTaskDTO
     * @return Boolean
     */
    Boolean update(ApiSyncTaskDTO apiSyncTaskDTO);
}
