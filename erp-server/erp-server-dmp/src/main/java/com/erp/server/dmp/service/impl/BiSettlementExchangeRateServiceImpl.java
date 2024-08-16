package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;
import com.erp.server.dmp.mapper.BiSettlementExchangeRateMapper;
import com.erp.server.dmp.service.BiSettlementExchangeRateService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Resource
    private RedisUtil redisUtil;


    @Override
    public BigDecimal findByCurrencyAndDate(String date, String sourceCurrencyCode) {
        String targetCurrencyCode = CurrencyEnum.CNY.getCurrencyCode();
        BigDecimal exchangeRate = listRedisByCurrencyCode(date, targetCurrencyCode, sourceCurrencyCode);
        return exchangeRate;
    }

    @Override
    public BigDecimal listRedisByCurrencyCode (String date, String targetCurrencyCode, String sourceCurrencyCode) {
        //数据验证
        checkNotBlank(date,targetCurrencyCode,sourceCurrencyCode);

        //如果目标币别和来源币别一致则直接返回1
        if (StrUtil.equals(targetCurrencyCode,sourceCurrencyCode)) {
            return BigDecimal.ONE;
        }
        //查询redis中存储的成本信息
        String existKey = StrUtil.format(RedisKeyConstant.SETTLEMENT_EXCHANGE_RATE,CurrencyEnum.CNY.getCurrencyCode(),sourceCurrencyCode);
        List<BiSettlementExchangeRateEntity> rateList = (List<BiSettlementExchangeRateEntity>) redisUtil.get(existKey);
        if (CollectionUtils.isEmpty(rateList)) {
            //查询库中数据添加缓存
            rateList = baseMapper.listByCurrencyCode(targetCurrencyCode, sourceCurrencyCode);
            if (CollectionUtils.isNotEmpty(rateList)) {
                //添加缓存
                redisUtil.set(existKey,rateList);
            }
        }
        if (CollectionUtils.isEmpty(rateList)) {
            return null;
        }
        //格式化日期
        LocalDate localDate = LocalDateUtil.parseStrToLocalDate(date);
        //汇率
        BigDecimal exchangeRate = rateList.stream().filter(obj -> obj.getSettlementDateBegin().isEqual(localDate)
                        || obj.getSettlementDateEnd().isEqual(localDate)
                        || (obj.getSettlementDateBegin().isBefore(localDate) && obj.getSettlementDateEnd().isAfter(localDate)))
                .sorted(Comparator.comparing(BiSettlementExchangeRateEntity::getUpdateTime, Comparator.reverseOrder()))
                .map(BiSettlementExchangeRateEntity::getExchangeRate)
                .findFirst().orElse(null);
        return exchangeRate;
    }

    /**
     * 数据验证
     * @author will
     * @date 2024/8/7 17:21
     * @param date
     * @param targetCurrencyCode
     * @param sourceCurrencyCode
     */
    private void checkNotBlank (String date, String targetCurrencyCode, String sourceCurrencyCode) {
        if (StrUtil.isBlank(date)) {
            throw new ServiceException("汇率查询时间不能为空");
        }
        if (StrUtil.isBlank(targetCurrencyCode)) {
            throw new ServiceException("汇率查询目标币别不能为空");
        }
        if (StrUtil.isBlank(sourceCurrencyCode)) {
            throw new ServiceException("汇率查询来源币别不能为空");
        }
    }
}
