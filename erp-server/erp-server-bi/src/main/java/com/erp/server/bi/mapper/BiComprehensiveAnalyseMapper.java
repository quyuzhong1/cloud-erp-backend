package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.SkuDateFilterDTO;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BiComprehensiveAnalyseMapper extends BaseMapper<DmpOrderInfoEntity> {

    /**
     * SKU矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    List<MatrixVO> skuMatrix(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 店铺矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    List<MatrixVO> shopMatrix(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 平台店铺对比趋势
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    List<ContrastTrendVO> shopContrastTrend(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 品类矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    List<MatrixVO> categoryMatrix(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 销售明细表-SKU
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SaleDetailVO> saleDetailSku(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 查询sku年销售额
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param startTime startDate
     * @param endTime endDate
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuYearSaleAmountVO> skuYearSaleAmount(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 查询年销售额
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param startTime startDate
     * @param endTime endDate
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    BigDecimal yearSaleAmount(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 根据时间查询退货金额
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param startTime startDate
     * @param endTime endDate
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuYearSaleAmountVO> returnOrderAmountByDate(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 销售明细表-店铺
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SaleDetailVO> saleDetailShop(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 查询店铺年销售额
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param startTime startDate
     * @param endTime endDate
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuYearSaleAmountVO> shopYearSaleAmount(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 销售明细表-人员
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SaleDetailVO> saleDetailUser(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 查询人员年销售额
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param startTime startDate
     * @param endTime endDate
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuYearSaleAmountVO> userYearSaleAmount(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 销售明细表-日期
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SaleDetailVO> saleDetailDate(@Param("params") BiFilterDTO biFilterDTO);

    /**
     * 查询sku年销售额
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param startTime startDate
     * @param endTime endDate
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuYearSaleAmountVO> dateYearSaleAmountBySku(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("sku") String sku);

    /**
     * 根据sku查询年销售额
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param startTime startDate
     * @param endTime endDate
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    BigDecimal yearSaleAmountBySku(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("sku") String sku);

    /**
     * 根据时间查询退货金额
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param startTime startDate
     * @param endTime endDate
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuYearSaleAmountVO> dateReturnOrderAmountByDate(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("sku") String sku);

    /**
     * SKU日期销售额趋势图-日
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuDateSaleTrendVO> skuDaySaleTrend(@Param("params") SkuDateFilterDTO biFilterDTO);

    /**
     * SKU日期销售额趋势图-周
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuDateSaleTrendVO> skuWeekSaleTrend(@Param("params") SkuDateFilterDTO biFilterDTO);

    /**
     * SKU日期销售额趋势图-月
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuDateSaleTrendVO> skuMonthSaleTrend(@Param("params") SkuDateFilterDTO biFilterDTO);

    /**
     * SKU日期销售额趋势图-季度
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuDateSaleTrendVO> skuQuarterSaleTrend(@Param("params") SkuDateFilterDTO biFilterDTO);

    /**
     * SKU日期销售额趋势图-年
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuDateSaleTrendVO> skuYearSaleTrend(@Param("params") SkuDateFilterDTO biFilterDTO);

}
