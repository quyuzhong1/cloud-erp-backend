package com.erp.server.wms.service;

import com.common.business.dto.DmpSyncMqDTO;

public interface SyncTaskService {
    /**
     * 同步任务
     * @param syncParamDTO
     * @return void
     **/
    void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);
}
