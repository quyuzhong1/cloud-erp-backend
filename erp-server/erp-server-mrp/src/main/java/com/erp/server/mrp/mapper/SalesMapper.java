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
    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllAmzSalesBySoOutStock(@Param("tableName") String tableName,
                                                                             @Param("tableDetailName") String tableDetailName,
                                                                             @Param("soB2cName") String soB2cName,
                                                                             @Param("startDate") LocalDate startDate,
                                                                             @Param("endDate") LocalDate endDate);

    /**
     * 查询全部销售订单销量
     */
    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllAmzSalesBySob2c(@Param("tableName") String tableName,
                                                                        @Param("tableDetailName") String tableDetailName,
                                                                        @Param("startDate") LocalDate startDate,
                                                                        @Param("endDate") LocalDate endDate);

    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllOverseasSalesBySob2c(@Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName,
                                                                             @Param("startDate") LocalDate startDate,
                                                                             @Param("endDate") LocalDate endDate,
                                                                             @Param("soIds") List<String> soIds,
                                                                             @Param("platforms") List<String> platforms);

    List<ReplenishmentResultDTO.SalesInfoAllDTO> listAllOverseasSalesBySoOutStock(@Param("tableName") String tableName, @Param("tableDetailName") String tableDetailName, @Param("soB2cName") String soB2cName,
                                                                                  @Param("startDate") LocalDate startDate,
                                                                                  @Param("endDate") LocalDate endDate,
                                                                                  @Param("soIds") List<String> soIds,
                                                                                  @Param("platforms") List<String> platforms);

    List<String> listIdByChannel(@Param("channelIdList") List<String> channelIdList,@Param("tableName") String tableName);
}
