package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.BiDataSourceCostDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.entity.BiDataSourceCostDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * 获取月份对应的值
     * @param costType
     * @param dto
     * @param limitNum
     * @return
     */
    List<BiDataSourceCostDTO.DataValueDTO> listMonthByCostType(@Param("costType") String costType, @Param("params") BiFilterDTO dto,@Param("limitNum") Integer limitNum);

    /**
     * 获取年份对应的值
     * @param costType
     * @param dto
     * @param limitNum
     * @return
     */
    List<BiDataSourceCostDTO.DataValueDTO> listYearByCostType(@Param("costType") String costType, @Param("params")BiDataSourceCostDTO.GrossProfitDTO dto, @Param("limitNum")Integer limitNum);

    /**
     * 获取季度值
     * @author yl
     * @date 2023-09-22 15:18
     * @param costType
     * @param dto
     * @param limitNum
     * @return java.util.List<com.erp.model.bi.dto.BiDataSourceCostDTO.DataValueDTO>
     */

    List<BiDataSourceCostDTO.DataValueDTO> listQuarterByCostType(@Param("costType") String costType, @Param("params")BiDataSourceCostDTO.GrossProfitDTO dto, @Param("limitNum")Integer limitNum);
}
