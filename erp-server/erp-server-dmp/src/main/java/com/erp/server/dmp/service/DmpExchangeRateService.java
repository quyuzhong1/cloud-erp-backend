package com.erp.server.dmp.service;

import com.erp.model.dmp.dto.DmpExchangeRateDTO;

/**
 * @author Will
 * @version 1.0
 * @description: 汇率
 * @date 2023/8/14 16:43
 */

public interface DmpExchangeRateService {
    /**
     * @description: 生成发送任务
     * @author Will
     * @date: 2023/8/14 16:44
     * @param ext
     */
    void sendSyncTask(DmpExchangeRateDTO ext);
}
