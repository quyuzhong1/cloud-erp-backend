package com.erp.server.plm.service;

import com.common.business.dto.DmpSyncMqDTO;

/**
 * @author Will
 * @version 1.0
 * @description: 同步任务接口
 * @date 2023/10/30 10:44
 */
public interface SyncTaskService {
    /**
     * @description: 同步任务
     * @author Will
     * @date: 2023/10/30 10:45
     * @param syncParamDTO
     */
    void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);
}
