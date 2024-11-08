package com.erp.server.scm.service;

import java.util.Map;

import com.common.business.dto.DmpSyncMqDTO;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/10/30 11:51
 */
public interface SyncTaskService {
    /**
     * @description: 查询发送
     * @author Will
     * @date: 2023/10/30 11:46
     * @param syncParamDTO
     */
    void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);
    
    Map<String , Map<String, Object>> newFindDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);
}
