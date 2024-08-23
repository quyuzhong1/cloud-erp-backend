package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiSettlementExchangeRateDTO;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;
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
     * @description: 分页查询
     * @author Will
     * @date: 2023/8/15 9:36
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<BiSettlementExchangeRateDTO.ListDTO> paging(Page query,@Param("params") BiSettlementExchangeRateDTO.SearchParamDTO params);
    /**
     * @description: 根据币制和日期查询汇率信息
     * @author Will
     * @date: 2023/8/24 17:44
     * @param date
     * @param sourceCurrencyCode
     * @return List<BiSettlementExchangeRateEntity>
     */
    List<BiSettlementExchangeRateEntity> findByCurrencyAndDate(@Param(value = "date") LocalDate date, @Param(value = "sourceCurrencyCode") String sourceCurrencyCode);
    /**
     * 根据目标币别和来源币别查询已审核启用汇率信息
     * @author will
     * @date 2024/8/7 16:35
     * @param targetCurrencyCode
     * @param sourceCurrencyCode
     * @return List<BiSettlementExchangeRateEntity>
     */
    List<BiSettlementExchangeRateEntity> listByCurrencyCode(@Param("targetCurrencyCode") String targetCurrencyCode,@Param("sourceCurrencyCode") String sourceCurrencyCode);
}
