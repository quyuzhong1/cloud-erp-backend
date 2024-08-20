package com.erp.server.wms.service;

import com.common.business.dto.DmpSyncMqDTO;

public interface SyncTaskService {
    /**
     * 同步任务
     * @param syncParamDTO
     * @return void
     **/
    void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);
    /**
     * 查询数据重新发送
     * @param syncParamDTO
     * @return void
     **/
    void findMaBangDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);

    /**
     * 查询旺店通数据重新发送
     * @param syncParamDTO
     * @return
     * @date: 2024-08-15
     * @author: tanmujin
     */
    void findWdtDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);
}
