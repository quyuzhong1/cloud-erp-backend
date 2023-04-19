package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.DateFilterDTO;
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
    List<SalesFlagVO> getMonthSales(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate,@Param("timeFlag")String timeFlag);

    List<SalesVO> getBySku(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);


    /**
     * 获取最近天数的销售数据
     * @param dto
     * @param settleRate
     * @param findTime
     * @param skuNoList
     * @return
     */
    List<SalesBaseVO> getLastDays(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate ,@Param("findTime") String  findTime,@Param("skuNoList") List<String> skuNoList);


    List<SalesVO> getBySpu(BiFilterDTO dto);

    /**
     * 根据国家查询销售额
     * @param dto
     * @return
     */
    List<SalesByCountryVO> getByCountry(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    /**
     * 根据平台查询销售额
     * @param dto
     * @return
     */
    List<Map<String, Object>> getPlatformSales(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    /**
     * 根据店铺查询销售额
     * @param dto
     * @return
     */
    List<ShopSalesVO> getByShop(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesBaseVO> getShopLastDays(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate,@Param("findTime") String findTime);

    List<Map<String, Object>> byTopShop(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<ShopSalesVO> byShopCountry(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

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
    List<ShopSalesVO> byShopNewAndOld(@Param("params")BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesCountVO> byCountry(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<ShopSalesVO> byShopCategory(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesBaseVO> byCategory(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesCountVO> byPlatform(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesCountVO> byHomeAndAbroad(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesBaseVO> byPeople(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesBaseVO> byDept(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesCountVO> byNewAndOld(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesFlagVO> byPlatformNewAndOld(@Param("params")BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesFlagVO> byCategoryNewAndOld(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<ShopSalesVO> bySite(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesBaseVO> byOldProductTop(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesBaseVO> byNewProductTop(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<ShopSalesVO> byShop(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesCountVO> byBrand(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesBaseVO> byPeopleRank(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesFlagVO> byPeopleNewAndOld(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesFlagVO> byDeptNewAndOld(@Param("params") BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesFlagVO> byMarketingCenter(@Param("params") BiFilterDTO dto,@Param("timeFlag") String  timeFlag,@Param("settleRate") String settleRate);

    List<SalesFlagVO> byLastYear(@Param("params")BiFilterDTO dto, @Param("timeFlag")String timeFlag,@Param("settleRate") String settleRate);

    SalesFlagVO byLastMonth(@Param("params")BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesFlagVO> byTobToc(@Param("params")BiFilterDTO dto,@Param("settleRate") String settleRate);

    List<SalesFlagVO> getByDay(@Param("params")DateFilterDTO biFilterDTO,@Param("timeFlag")String timeFlag,@Param("settleRate") String settleRate);

    List<SalesFlagVO> getByMonth(@Param("params") DateFilterDTO dto,@Param("timeFlag") String timeFlag, @Param("settleRate")String settleRate);

    List<SalesFlagVO> getByQuarter(@Param("params")DateFilterDTO dto, @Param("timeFlag")String timeFlag, @Param("settleRate")String settleRate);

    List<SalesFlagVO> getByYear(@Param("params") DateFilterDTO dto, @Param("timeFlag") String timeFlag, @Param("settleRate")String settleRate);
}
