package com.erp.server.srm.service;

import com.common.business.dto.DmpSyncMqDTO;

import java.util.Map;

/**
 * 修改操作金蝶的状态
 * @Author Luo_WG
 * @Date 2023/5/31 14:40
 **/
public interface SyncTaskService {

    /**
     * 参数，code类型编码，businessId业务id，status状态，kingdeeId金蝶id
     */
    void updateBusinessSyncKingdeeStatus(Map<String, Object> params);

    /**
     * 查询同步
     * @author will
     * @date 2025/6/24 14:26
     * @param syncParamDTO
     * @return void
     */
    Map<String, Map<String, Object>> newFindDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);
}
