package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;

import java.math.BigDecimal;

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
    BigDecimal findByCurrencyAndDate(String date, String sourceCurrencyCode);

    /**
     * 根据日期、目标币别、来源币别查询汇率信息
     * @author will
     * @date 2024/8/7 17:12
     * @param date
     * @param targetCurrencyCode
     * @param sourceCurrencyCode
     * @return BigDecimal
     */
    BigDecimal listRedisByCurrencyCode (String date, String targetCurrencyCode, String sourceCurrencyCode);
}
