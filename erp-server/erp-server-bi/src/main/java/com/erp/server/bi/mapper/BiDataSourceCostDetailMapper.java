package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.entity.BiDataSourceCostDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/15 18:16
 */
@Mapper
public interface BiDataSourceCostDetailMapper extends BaseMapper<BiDataSourceCostDetailEntity> {

    /**
     * 获取年度成本值
     * @param year
     * @param type
     * @return
     */
    BigDecimal yearByCostType(@Param("year") String year, @Param("type")String type, @Param("params") BiFilterDTO dto);

    /**
     * 获取月的
     * @param yearMonthStr
     * @param costType
     * @param dto
     * @return
     */
    BigDecimal monthByCostType(@Param("yearMonth") String yearMonthStr, @Param("type")String costType,@Param("params") BiFilterDTO dto);
}
