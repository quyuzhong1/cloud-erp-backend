package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.CurrencyEnum;
import com.erp.model.dmp.entity.BiSettlementExchangeRateEntity;
import com.erp.server.dmp.mapper.BiSettlementExchangeRateMapper;
import com.erp.server.dmp.service.BiSettlementExchangeRateService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/19 10:00
 */
@Service
public class BiSettlementExchangeRateServiceImpl extends ServiceImpl<BiSettlementExchangeRateMapper, BiSettlementExchangeRateEntity>
        implements BiSettlementExchangeRateService {


    @Override
    public BigDecimal findByCurrencyAndDate(String date, String sourceCurrencyCode) {
        //如果来源币别为人民币则返回1
        if (StrUtil.equals(CurrencyEnum.CNY.getCurrencyCode(),sourceCurrencyCode)) {
            return BigDecimal.ONE;
        }
        LocalDate parseDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<BiSettlementExchangeRateEntity> biSettlementExchangeRateEntityList = this.baseMapper.findByCurrencyAndDate(parseDate, sourceCurrencyCode);
        if(CollUtil.isEmpty(biSettlementExchangeRateEntityList)) {
            return null;
        }
        biSettlementExchangeRateEntityList.sort(Comparator.comparing(BiSettlementExchangeRateEntity::getUpdateTime, Comparator.reverseOrder()));
        BiSettlementExchangeRateEntity biSettlementExchangeRateEntity = biSettlementExchangeRateEntityList.get(0);
        return biSettlementExchangeRateEntity.getExchangeRate();
    }

}
