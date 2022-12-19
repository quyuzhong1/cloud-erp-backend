package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.BiSettlementExchangeRateDTO;
import com.erp.model.dmp.entity.BiSettlementExchangeRateEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 10:16
 */
public interface BiSettlementExchangeRateService  extends IService<BiSettlementExchangeRateEntity> {
    /**
     * @description: 批量新增结算汇率
     * @author Will
     * @date: 2022/12/19 10:23
     * @param list
     * @return Boolean
     */
    Boolean batchAddSettlementExchangeRate(List<BiSettlementExchangeRateDTO> list);
}
