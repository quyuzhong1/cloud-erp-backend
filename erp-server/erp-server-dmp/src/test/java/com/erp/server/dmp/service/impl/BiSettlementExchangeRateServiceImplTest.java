package com.erp.server.dmp.service.impl;

import com.erp.model.dmp.dto.BiSettlementExchangeRateDTO;
import com.erp.model.dmp.entity.BiSettlementExchangeRateEntity;
import com.erp.server.dmp.mapper.BiSettlementExchangeRateMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BiSettlementExchangeRateServiceImplTest {

    private BiSettlementExchangeRateServiceImpl service;
    private BiSettlementExchangeRateMapper mapper;

    @Before
    public void setUp() {
        service = new BiSettlementExchangeRateServiceImpl();
        mapper = Mockito.mock(BiSettlementExchangeRateMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    @Test
    public void findRatesUsesLatestUpdateTimeAndDeduplicatesRequests() {
        when(mapper.listByCurrencyCode("CNY", "USD")).thenReturn(Arrays.asList(
            rate(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31), new BigDecimal("7.1000"), LocalDateTime.of(2026, 7, 1, 10, 0)),
            rate(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31), new BigDecimal("7.2000"), LocalDateTime.of(2026, 7, 1, 11, 0))
        ));

        List<BiSettlementExchangeRateDTO.BatchRateResultDTO> result = service.findRates(Arrays.asList(
            new BiSettlementExchangeRateDTO.BatchRateParamDTO("2026-07-01", "USD"),
            new BiSettlementExchangeRateDTO.BatchRateParamDTO("2026-07-01", "USD")
        ));

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("7.2000"), result.get(0).getExchangeRate());
        verify(mapper, times(1)).listByCurrencyCode("CNY", "USD");
    }

    @Test
    public void findRatesReturnsNullWhenNoActiveRangeMatches() {
        when(mapper.listByCurrencyCode("CNY", "USD")).thenReturn(Collections.singletonList(
            rate(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), new BigDecimal("7.1000"), LocalDateTime.of(2026, 5, 1, 10, 0))
        ));

        List<BiSettlementExchangeRateDTO.BatchRateResultDTO> result = service.findRates(Collections.singletonList(
            new BiSettlementExchangeRateDTO.BatchRateParamDTO("2026-07-01", "USD")
        ));

        assertEquals(1, result.size());
        assertNull(result.get(0).getExchangeRate());
        verify(mapper, times(1)).listByCurrencyCode("CNY", "USD");
    }

    /**
     * 构造测试用结算汇率记录。
     *
     * @param begin 结算开始日期
     * @param end 结算结束日期
     * @param exchangeRate 汇率值
     * @param updateTime 用于校验优先级的更新时间
     * @return 汇率实体
     */
    private BiSettlementExchangeRateEntity rate(LocalDate begin,
                                                LocalDate end,
                                                BigDecimal exchangeRate,
                                                LocalDateTime updateTime) {
        BiSettlementExchangeRateEntity entity = new BiSettlementExchangeRateEntity();
        entity.setSettlementDateBegin(begin);
        entity.setSettlementDateEnd(end);
        entity.setExchangeRate(exchangeRate);
        entity.setUpdateTime(updateTime);
        entity.setSourceCurrencyCode("USD");
        entity.setTargetCurrencyCode("CNY");
        return entity;
    }
}
