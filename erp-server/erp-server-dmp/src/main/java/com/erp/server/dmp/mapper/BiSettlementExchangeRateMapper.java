package com.erp.server.dmp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.BiSettlementExchangeRateDTO;
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
     *
     * @param date
     * @param sourceCurrencyCode
     * @return
     */
    List<BiSettlementExchangeRateEntity> findByCurrencyAndDate(@Param(value = "date") LocalDate date, @Param(value = "sourceCurrencyCode") String sourceCurrencyCode);

    /**
     * 根据目标币别和来源币别查询已审核启用汇率信息
     *
     * @param targetCurrencyCode
     * @param sourceCurrencyCode
     * @return List<BiSettlementExchangeRateEntity>
     * @author will
     * @date 2024/8/7 16:47
     */
    List<BiSettlementExchangeRateEntity> listByCurrencyCode(@Param("targetCurrencyCode") String targetCurrencyCode, @Param("sourceCurrencyCode") String sourceCurrencyCode);

    /**
     * 根据目标币别和来源币别集合查询已审核启用汇率信息
     */
    List<BiSettlementExchangeRateEntity> listByCurrencyCodes(@Param("targetCurrencyCode") String targetCurrencyCode,
                                                             @Param("sourceCurrencyCodes") List<String> sourceCurrencyCodes);

    /**
     * @param query
     * @param params
     * @return IPage<ListDTO>
     * @description: 分页查询
     * @author Will
     * @date: 2023/8/15 9:36
     */
    IPage<BiSettlementExchangeRateDTO.ListDTO> paging(Page<Object> query, @Param("params") BiSettlementExchangeRateDTO.SearchParamDTO params);

}
