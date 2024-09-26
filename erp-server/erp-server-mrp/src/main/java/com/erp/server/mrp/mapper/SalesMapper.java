package com.erp.server.mrp.mapper;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SalesMapper {
    /**
     * 查询全部销售出库单销量
     */
    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllSalesBySoOutStock(@Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName, @Param("soB2cName") String soB2cName,@Param("localDate") LocalDate localDate);

    /**
     * 查询全部销售订单销量
     */
    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllSalesBySob2c(@Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName,@Param("localDate") LocalDate localDate);
}
