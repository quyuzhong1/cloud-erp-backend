package com.erp.server.mrp.mapper;

import com.erp.model.mrp.dto.ReplenishmentResultDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SalesMapper {
    List<ReplenishmentResultDTO.SalesInfoDTO> listSalesBySob2c(@Param("skuId") String skuId, @Param("shopId") String shopId, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName,@Param("localDate") LocalDate localDate);

    List<ReplenishmentResultDTO.SalesInfoDTO> listSalesBySoOutStock(@Param("skuId") String skuId, @Param("shopId") String shopId, @Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName, @Param("soB2cName") String soB2cName,@Param("localDate") LocalDate localDate);
}
