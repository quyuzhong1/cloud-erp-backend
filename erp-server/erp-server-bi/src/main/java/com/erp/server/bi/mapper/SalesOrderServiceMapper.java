package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Classname SalesOrderServiceMapper

 * @Date 2022-12-16 11:09
 * @Created by yl
 */
@Mapper
public interface SalesOrderServiceMapper  extends BaseMapper<BiOrderInfoEntity> {

    List<SalesFlagVO> getMonthSales(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate, @Param("timeFlag") String timeFlag);

    /**
     * 获取sku 销售额
     *
     * @param dto
     * @param settleRate
     * @return
     */
    IPage<SkuSalesDTO.PagingSalesInfoDTO> getBySku(Page<Object> query, @Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    /**
     * 导出sku 销售额列表
     *
     * @param params
     * @param settleRate
     * @return
     */
    List<SkuSalesDTO.PagingSalesInfoDTO> listSkuSalesExcel(@Param("params") SkuSalesDTO.SearchSkuDTO params, @Param("settleRate") String settleRate);
    Page<SkuSalesDTO.PagingSalesInfoDTO> listSkuSalesExcel(@Param("page") Page<SkuSalesDTO.PagingSalesInfoDTO> page, @Param("params") SkuSalesDTO.SearchSkuDTO params, @Param("settleRate") String settleRate);

    /**
     * 获取最近天数的销售数据
     *
     * @param dto
     * @param settleRate
     * @return
     */
    List<SalesBaseVO> getLastDays(@Param("params") SkuSalesDTO.SearchSkuDTO dto, @Param("settleRate") String settleRate);


    List<SalesVO> getBySpu(@Param("params") BiFilterDTO dto);

    /**
     * 根据国家查询销售额
     *
     * @param dto
     * @return
     */
    List<SalesByCountryVO> getByCountry(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    /**
     * 根据平台查询销售额
     *
     * @param dto
     * @return
     */
    @MapKey("platform")
    List<Map<String, Object>> getPlatformSales(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    /**
     * 根据店铺查询销售额
     *
     * @param dto
     * @return
     */
    List<ShopSalesVO> getByShop(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesBaseVO> getShopLastDays(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    @MapKey("shop")
    List<Map<String, Object>> byTopShop(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<ShopSalesVO> byShopCountry(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    /**
     * 获取国家
     *
     * @return
     */
    List<CountryCountVO> getCountryList();

    /**
     * 店铺新老品 销售量 销售额
     *
     * @param dto
     * @return
     */
    List<ShopSalesVO> byShopNewAndOld(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesCountVO> byCountry(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<ShopSalesVO> byShopCategory(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    /**
     * 一级分类的 销售额
     *
     * @param dto
     * @param settleRate
     * @return
     */
    List<SalesBaseVO> byCategory(@Param("params") BiCategoryDTO.FirstCategoryParamsDTO dto, @Param("settleRate") String settleRate);

    List<SalesCountVO> byPlatform(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesCountVO> byHomeAndAbroad(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesBaseVO> byPeople(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesBaseVO> byDept(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesCountVO> byNewAndOld(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesFlagVO> byPlatformNewAndOld(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesFlagVO> byCategoryNewAndOld(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<ShopSalesVO> bySite(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesBaseVO> byProductTop(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);
    List<SalesBaseVO> byOldProductTop(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesBaseVO> byNewProductTop(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<ShopSalesVO> byShop(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesCountVO> byBrand(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);
    List<SkuItemVO> getSkuItemName();

    List<SalesBaseVO> byPeopleRank(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesFlagVO> byPeopleNewAndOld(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesFlagVO> byDeptNewAndOld(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesFlagVO> byMarketingCenter(@Param("params") BiFilterDTO dto, @Param("timeFlag") String timeFlag, @Param("settleRate") String settleRate, @Param("year") String thisYear);

    List<SalesFlagVO> byLastYear(@Param("params") BiFilterDTO dto, @Param("timeFlag") String timeFlag, @Param("settleRate") String settleRate, @Param("year") String year);

    SalesFlagVO byLastMonth(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesFlagVO> byTobToc(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    List<SalesFlagVO> getSalesByReport(@Param("params") DateSalesTrendDTO.SearchDTO biFilterDTO, @Param("timeFlag") String timeFlag, @Param("settleRate") String settleRate, @Param("searchType") String searchType, @Param("groupName") String groupName);

    List<SalesFlagVO> newAndOldSalesAmount(@Param("params") NewAndOldSalesSearchDTO.SearchDTO dto, @Param("settleRate") String settleRate,@Param("dataType") String dataType);


    /**
     * 获取月度销量
     *
     * @param dto
     * @return
     */
    BigDecimal getQty(@Param("params") BiTargetYearDTO.SearchDTO dto);


    /**
     * 获取月度销售额
     *
     * @param dto
     * @return
     */
    BigDecimal getAmount(@Param("params") BiTargetYearDTO.SearchDTO dto, @Param("settleRate") String settleRate);


    /**
     * 产品等级的销售额
     *
     * @param dto
     * @return
     */
    @MapKey("grade")
    List<Map<String, Object>> listProductGradeSales(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    /**
     * 部门销售额排行榜
     *
     * @param dto
     * @param settleRate
     * @return
     */
    List<CompletionRateRankingDTO.PagingDTO> deptCompletionRateRanking(@Param("params") CompletionRateRankingDTO.SearchDTO dto, @Param("settleRate") String settleRate);

    /**
     * 用户销售额排行榜
     *
     * @param dto
     * @param settleRate
     * @return
     */
    List<CompletionRateRankingDTO.PagingDTO> userCompletionRateRanking(@Param("params") CompletionRateRankingDTO.SearchDTO dto, @Param("settleRate") String settleRate);

    /**
     * 计算客户销售额
     *
     * @param filterDTOS
     * @param settleRate
     * @return
     */
    List<CustomerSaleVO> customerLevelProportion(@Param("dtos") List<CustomerBiFilterDTO> filterDTOS, @Param("settleRate") String settleRate);

    /**
     * 净销售额
     * @author yl
     * @date 2023-10-08 15:49
     * @param dto
     * @param settleRate
     * @return java.math.BigDecimal
     */
    BigDecimal netSalesAmount(@Param("params") BiTargetYearDTO.SearchDTO dto, @Param("settleRate") String settleRate);

    /**
     * 获取时间维度列表
     * @param startTime
     * @param endTime
     * @param dateType
     * @return
     */
    List<DateDimensionVO> getDateList(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("dateType") String dateType);
}
