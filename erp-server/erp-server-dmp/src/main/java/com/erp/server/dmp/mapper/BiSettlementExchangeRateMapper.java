package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.dmp.entity.BiSettlementExchangeRateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/19 9:58
 */
@Mapper
public interface BiSettlementExchangeRateMapper extends BaseMapper<BiSettlementExchangeRateEntity> {

    /**
     * 根据币制和日期查询汇率信息
     * @param date
     * @param sourceCurrencyCode
     * @return
     */
    List<BiSettlementExchangeRateEntity> findByCurrencyAndDate(@Param(value = "date") LocalDate date, @Param(value = "sourceCurrencyCode") String sourceCurrencyCode);

}
