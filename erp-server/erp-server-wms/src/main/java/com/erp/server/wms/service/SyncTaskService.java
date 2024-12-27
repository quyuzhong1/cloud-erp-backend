package com.erp.server.wms.service;

import java.util.List;
import java.util.Map;

import com.common.business.dto.DmpSyncMqDTO;

public interface SyncTaskService {
    /**
     * 同步任务
     * @param syncParamDTO
     * @return void
     **/
    void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);
    
    Map<String , Map<String, Object>> newFindDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);
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

    /**
     * 数帝云销售出库单
     * @param sourceDetailList
     * @return
     */
    Map<String ,Map<String, Object>> newSdySyncSoOutstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList);

    /**
     * 数帝云销售退货入库
     * @param sourceDetailList
     * @return
     */
    Map<String ,Map<String, Object>> newSdySyncSoReturnInstock(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList);
}
