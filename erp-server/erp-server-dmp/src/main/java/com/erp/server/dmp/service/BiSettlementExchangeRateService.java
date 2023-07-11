package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.dmp.entity.BiSettlementExchangeRateEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/19 10:16
 */
public interface BiSettlementExchangeRateService  extends IService<BiSettlementExchangeRateEntity> {

    /**
     * 根据日期和原币种查询汇率
     * @param date
     * @param sourceCurrencyCode
     * @return
     */
    BigDecimal findByCurrencyAndDate(LocalDate date, String sourceCurrencyCode);
}
