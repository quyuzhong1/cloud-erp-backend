package com.erp.server.tms.service;

import com.common.business.dto.DmpSyncMqDTO;

import java.util.Map;

public interface SyncTaskService {
    Map<String, Map<String, Object>> newFindDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO);
}
