package com.erp.server.dmp.service;

import com.erp.model.dmp.dto.DmpExchangeRateDTO;

/**
 * @author Will
 * @version 1.0
 * @description: 直接调拨单同步service
 * @date 2023/6/29 11:07
 */
public interface SyncExchangeRateService {

    /**
     * @param entity
     * @description: 同步直接调拨单
     * @author Will
     * @date: 2023/6/29 11:08
     */
    void syncKingdeeExchangeRate(DmpExchangeRateDTO entity);
}
