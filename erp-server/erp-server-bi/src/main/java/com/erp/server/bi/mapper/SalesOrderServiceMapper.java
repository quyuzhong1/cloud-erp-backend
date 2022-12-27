package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Classname SalesOrderServiceMapper
 * @Description TODO
 * @Date 2022-12-16 11:09
 * @Created by yl
 */
@Mapper
public interface SalesOrderServiceMapper  extends BaseMapper<DmpOrderInfoEntity> {
    List<Map<String, Object>> getMonthSales();

    List<SalesVO> getBySku(@Param("params") BiFilterDTO dto);

    List<SalesBaseVO> getLastThirtyDays(@Param("params") BiFilterDTO dto);


    List<SalesVO> getBySpu(BiFilterDTO dto);

    /**
     * 根据国家查询销售额
     * @param dto
     * @return
     */
    List<SalesByCountryVO> getByCountry(@Param("params") BiFilterDTO dto);

    /**
     * 根据平台查询销售额
     * @param dto
     * @return
     */
    List<Map<String, Object>> getPlatformSales(@Param("params") BiFilterDTO dto);

    /**
     * 根据店铺查询销售额
     * @param dto
     * @return
     */
    List<ShopSalesVO> getByShop(@Param("params") BiFilterDTO dto);

    List<SalesBaseVO> getShopLastThirtyDays(@Param("params") BiFilterDTO dto);

    List<Map<String, Object>> byTopShop(@Param("params") BiFilterDTO dto);

    List<ShopSalesVO> byShopCountry(@Param("params") BiFilterDTO dto);

    /**
     * 获取国家
     * @return
     */
    List<CountryCountVO> getCountryList();

    /**
     * 店铺新老品 销售量 销售额
     * @param dto
     * @return
     */
    List<ShopSalesVO> byShopNewAndOld(@Param("params")BiFilterDTO dto);

    List<SalesCountVO> byCountry(@Param("params") BiFilterDTO dto);

    List<ShopSalesVO> byShopCategory(@Param("params") BiFilterDTO dto);

    List<SalesBaseVO> byCategory(@Param("params") BiFilterDTO dto);

    List<SalesCountVO> byPlatform(@Param("params") BiFilterDTO dto);

    List<SalesCountVO> byHomeAndAbroad(@Param("params") BiFilterDTO dto);

    List<SalesCountVO> byPeople(@Param("params") BiFilterDTO dto);

    List<SalesCountVO> byDept(@Param("params") BiFilterDTO dto);

    List<SalesCountVO> byNewAndOld(@Param("params") BiFilterDTO dto);
}
