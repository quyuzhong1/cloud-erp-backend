package com.erp.server.dmp.service;

import com.erp.model.dmp.dto.ApiSyncTaskDTO;

/**
 * @author Will
 * @version 1.0
 * @description: 推送任务业务接口
 * @date 2023/7/10 16:08
 */
public interface ApiSyncTaskService {
    /**
     * @description: 新增推送任务
     * @author Will
     * @date: 2023/7/10 16:23
     * @param dto
     * @return Boolean
     */
    Boolean insert(ApiSyncTaskDTO dto);
}
