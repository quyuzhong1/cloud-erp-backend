package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.vo.*;
import com.erp.model.dmp.entity.BiOrderInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Classname SalesOrderService

 * @Date 2022-12-16 11:08
 * @Created by yl
 */
public interface SalesOrderService extends IService<BiOrderInfoEntity> {

    /**
     * 月度趋势
     * @return
     */
    StatisticalDataVO getMonthSales(BiFilterDTO dto);

    /**
     * 一级模块 sku 销售额 分页
     * @param dto
     * @return
     */
    PagingVO<SkuSalesDTO.PagingSalesInfoDTO> queryByPageBySku(PagingDTO<SkuSalesDTO.SearchSkuDTO> dto);

    XyAxesResultVO getByCountry(BiFilterDTO dto);

    /**
     * 一级销售模块更具平台分
     * @param dto
     * @return
     */
    StatisticalDataVO getByPlatformRatio(BiFilterDTO dto);


    /**
     * 一级销售模块 TOB/TOC销售额
     * @param dto
     * @return
     */
    StatisticalDataVO byTobToc(BiFilterDTO dto);

    /**
     * 一级销售模块 店铺销售额
     * @author yl
     * @date 2022-12-21 10:33
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.ShopSalesVO>
     */
    List<ShopSalesVO> getByShop(BiFilterDTO dto);


    /**
     * 一级销售模块 销售额TOP20店铺
     * @author yl
     * @date 2022-12-21 10:33
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.ShopSalesVO>
     */
    StatisticalDataVO byTopShop(BiFilterDTO dto);

    /**
     * 二级销售模块 店铺-国家销售额
     * @author yl
     * @date 2022-12-26 10:10
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.SalesGroupVO>
     */
    XyAxesResultVO byShopCountry(BiFilterDTO dto);

    /**
     * 二级销售模块 店铺的新/老品销售额
     * @author yl
     * @date 2022-12-26 10:10
     * @param dto
     * @return java.util.List<com.erp.model.bi.vo.SalesGroupVO>
     */
    List<ShopNewAndOldSalesVO> byShopNewAndOld(BiFilterDTO dto);

    /**
     * 一级模块  国家销售额
     * @param dto
     * @return
     */
    List<SalesCountVO> byCountry(BiFilterDTO dto);

    /**
     * 一级模块  一级类目销售额
     * @param dto
     * @return
     */
    StatisticalDataVO byCategory(BiCategoryDTO.FirstCategoryParamsDTO dto);

    /**
     * 品类销售额
     * @param dto
     * @return
     */
    XyAxesResultVO byShopCategory(BiFilterDTO dto);

    List<SalesCountVO> byBrand(BiFilterDTO dto);

    List<SalesCountVO> byPlatform(BiFilterDTO dto);

    StatisticalDataVO byHomeAndAbroad(BiFilterDTO dto);

    List<SalesCountVO> byPeople(BiFilterDTO dto);

    List<PeopleSalesRankVO> byPeopleWeekRank(BiFilterDTO dto);

    List<SalesCountVO> byDept(BiFilterDTO dto);

    List<SalesCountVO> byNewAndOld(BiFilterDTO dto);

    List<ProductNewAndOldVO> byPlatformNewAndOld(BiFilterDTO dto);

    List<ProductNewAndOldVO> byPeopleNewAndOld(BiFilterDTO dto);

    List<ProductNewAndOldVO> byCategoryNewAndOld(BiFilterDTO dto);

    List<SalesCountVO> bySite(BiFilterDTO dto);

    StatisticalDataVO byProductType(BiFilterDTO dto);

    StatisticalDataVO byOldProductTop(BiFilterDTO dto);

    StatisticalDataVO byNewProductTop(BiFilterDTO dto);

    List<SalesCountVO> byMarketingCenter(BiFilterDTO dto);

    StatisticalDataVO byEuropeAndJapanSite(BiFilterDTO dto);

    List<PeopleSalesRankVO> byPeopleMonthRank(BiFilterDTO dto);

    List<PeopleSalesRankVO> byPeopleQuarterRank(BiFilterDTO dto);

    List<PeopleSalesRankVO> byPeopleYearRank(BiFilterDTO dto);

    List<ProductNewAndOldVO> byDeptNewAndOld(BiFilterDTO dto);

    StatisticalDataVO byDate(DateSalesTrendDTO.SearchDTO searchDTO);

    /**
     * 新老品销售额
     * @Author Luo_WG
     * @Date 2023/9/15 14:15
     * @param dto
     * @return java.util.List<com.erp.model.bi.dto.NewAndOldSalesSearchDTO.PagingDTO>
     **/
    List<NewAndOldSalesSearchDTO.PagingDTO> newAndOldSalesAmount(NewAndOldSalesSearchDTO.SearchDTO dto);

    /**
     * 新老品销售额导出excel
     * @Author Luo_WG
     * @Date 2023/9/21 9:46
     * @param dto
     * @param response
     * @return void
     **/
    Boolean newAndOldSalesExportExcel(NewAndOldSalesSearchDTO.SearchDTO dto, HttpServletResponse response);

    /**
     * 战略目标达成
     * @author yl
     * @date 2023-09-18 11:01
     * @param dto
     * @return java.util.List<com.erp.model.bi.dto.BiTargetYearDTO.TargetMetricsFinishDTO>
     */
    List<BiTargetYearDTO.TargetMetricsFinishDTO> listTargetMetrics(BiTargetYearDTO.SearchDTO dto);

    /**
     * 导出sku 销售额
     * @param params
     * @return
     */
    Boolean exportSkuSalesExcel(SkuSalesDTO.SearchSkuDTO params, HttpServletResponse response);

    /**
     * 产品等级销售分析
     * @param dto
     * @return
     */
    StatisticalDataVO productGradeSales(BiFilterDTO dto);

    /**
     * 部门完成率排行
     * @Author Luo_WG
     * @Date 2023/9/21 10:37
     * @param dto
     * @return java.util.List<com.erp.model.bi.dto.CompletionRateRankingDTO.PagingDTO>
     **/
    List<CompletionRateRankingDTO.PagingDTO> listCompletionRateRanking(CompletionRateRankingDTO.SearchDTO dto);

    /**
     *
     * @Author Luo_WG
     * @Date 2023/9/21 15:57
     * @param dto
     * @param response
     * @return java.lang.Boolean
     **/
    Boolean completionRateRankingExportExcel(CompletionRateRankingDTO.SearchDTO dto, HttpServletResponse response);


    /**
     * 毛利额 毛利率 模块
     * @author yl
     * @date 2023-09-21 17:18
     * @param dto
     * @return com.erp.model.bi.vo.StatisticalDataVO
     */
    StatisticalDataVO grossProfit(BiDataSourceCostDTO.GrossProfitDTO dto);


    /**
     * B2B客户属性分析
     * @param biFilterDTO
     * @return
     */
    StatisticalDataVO customerPropertyAnalysis(BiFilterDTO biFilterDTO);


    /**
     * B2B客户等级占比
     * @param biFilterDTO
     * @return
     */
    StatisticalDataVO customerLevelProportion(BiFilterDTO biFilterDTO);

    /**
     * 获取sku对应item名称
     * @return
     */
    Map<String, String> getSkuItemName();

    /**
     * 导出销量数据
     */
    PagingVO<SkuSalesDTO.PagingSalesInfoDTO> exportSkuSales(PagingDTO<SkuSalesDTO.SearchSkuDTO> dto);
}
