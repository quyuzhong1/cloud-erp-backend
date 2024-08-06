package com.erp.server.bi.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.vo.*;
import com.erp.server.bi.service.SalesOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 销售相关
 *
 * @Classname BiSalesBusinessDivisionController
 * @Date 2022-12-15 11:34
 * @Created by yl
 */
@RestController
@LogSystemModule("我的仪表盘")
@RequestMapping("sales")
public class BiSalesModuleController extends BaseController {


    @Resource
    private SalesOrderService salesOrderService;


    /**
     * 销售额- 一级模块-月销售额趋势
     *
     * @return
     */
    @PostMapping("/byMonth")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<StatisticalDataVO> getMonth(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO statistical = salesOrderService.getMonthSales(dto);
        return success(statistical);
    }


    /**
     * 销售额- 一级模块-SKU销售额
     *
     * @return
     */
    @PostMapping("/queryByPageBySku")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<PagingVO<SkuSalesDTO.PagingSalesInfoDTO>> queryByPageBySku(@RequestBody @Validated PagingDTO<SkuSalesDTO.SearchSkuDTO> dto) {
        PagingVO<SkuSalesDTO.PagingSalesInfoDTO> pagingVO = salesOrderService.queryByPageBySku(dto);
        return success(pagingVO);
    }

    /**
     * 导出 sku 销售额
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出sku销售额")
    @PostMapping("/exportSkuSales")
    public ApiResult<String> exportSkuSales(@RequestBody @Valid SkuSalesDTO.SearchSkuDTO params, HttpServletResponse response) {
        salesOrderService.exportSkuSalesExcel(params,response);
        return success();

    }

    /**
     * 销售相关-一级模块-SKU国家销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/bySkuCountry")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<XyAxesResultVO> getByCountry(@RequestBody @Validated BiFilterDTO dto) {
        XyAxesResultVO result = salesOrderService.getByCountry(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-TOB/TOC销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byTobToc")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<StatisticalDataVO> byTobToc(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.byTobToc(dto);
        return success(result);
    }


    @PostMapping("/byPlatformRatio")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "bi_order_info"
    )
    public ApiResult<StatisticalDataVO> byPlatformRatio(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.getByPlatformRatio(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-店铺销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byShop")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<ShopSalesVO>> byShop(@RequestBody @Validated BiFilterDTO dto) {
        List<ShopSalesVO> resultList = salesOrderService.getByShop(dto);
        return success(resultList);
    }


    /**
     * 销售相关-一级模块-店铺销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byTopShop")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<StatisticalDataVO> byTopShop(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.byTopShop(dto);
        return success(result);
    }


    /**
     * 销售相关-二级模块-店铺国家销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byShopCountry")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<XyAxesResultVO> byShopCountry(@RequestBody @Validated BiFilterDTO dto) {
        XyAxesResultVO resultList = salesOrderService.byShopCountry(dto);
        return success(resultList);
    }

    /**
     * 销售相关-二级模块-品类销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byShopCategory")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<XyAxesResultVO> byShopCategory(@RequestBody @Validated BiFilterDTO dto) {
        XyAxesResultVO result = salesOrderService.byShopCategory(dto);
        return success(result);
    }

    /**
     * 销售相关-二级模块-店铺的新/老品销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byShopNewAndOld")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<ShopNewAndOldSalesVO>> byShopNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<ShopNewAndOldSalesVO> resultList = salesOrderService.byShopNewAndOld(dto);
        return success(resultList);
    }

    /**
     * 销售相关-一级模块-国家销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byCountry")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<SalesCountVO>> byCountry(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> resultList = salesOrderService.byCountry(dto);
        return success(resultList);
    }

    /**
     * 销售相关-一级模块-品类销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byCategory")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<StatisticalDataVO> byCategory(@RequestBody @Validated BiCategoryDTO.FirstCategoryParamsDTO dto) {
        StatisticalDataVO result = salesOrderService.byCategory(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-品牌销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byBrand")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<SalesCountVO>> byBrand(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result = salesOrderService.byBrand(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-平台销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byPlatform")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<SalesCountVO>> byPlatform(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result = salesOrderService.byPlatform(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-国内国外占比
     *
     * @param dto
     * @return
     */
    @PostMapping("/byHomeAndAbroad")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "bi_order_info"
    )
    public ApiResult<StatisticalDataVO> byHomeAndAbroad(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.byHomeAndAbroad(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-人员销售额
     *
     * @param dto
     * @return
     */
    @PostMapping("/byPeople")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<SalesCountVO>> byPeople(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result = salesOrderService.byPeople(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-人员销售额 周排行
     *
     * @param
     * @return
     */
    @PostMapping("/byPeopleWeekRank")
    public ApiResult<List<PeopleSalesRankVO>> byPeopleWeekRank(@RequestBody @Validated BiFilterDTO dto) {
        List<PeopleSalesRankVO> result = salesOrderService.byPeopleWeekRank(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-人员销售额 月排行
     *
     * @param
     * @return
     */
    @PostMapping("/byPeopleMonthRank")
    public ApiResult<List<PeopleSalesRankVO>> byPeopleMonthRank(@RequestBody @Validated BiFilterDTO dto) {
        List<PeopleSalesRankVO> result = salesOrderService.byPeopleMonthRank(dto);
        return success(result);
    }

    /**
     * 销售相关-一级模块-人员销售额 季度排行
     *
     * @param
     * @return
     */
    @PostMapping("/byPeopleQuarterRank")
    public ApiResult<List<PeopleSalesRankVO>> byPeopleQuarterRank(@RequestBody @Validated BiFilterDTO dto) {
        List<PeopleSalesRankVO> result = salesOrderService.byPeopleQuarterRank(dto);
        return success(result);
    }

    /**
     * 销售相关-一级模块-人员销售额 年度排行
     *
     * @param
     * @return
     */
    @PostMapping("/byPeopleYearRank")
    public ApiResult<List<PeopleSalesRankVO>> byPeopleYearRank(@RequestBody @Validated BiFilterDTO dto) {
        List<PeopleSalesRankVO> result = salesOrderService.byPeopleYearRank(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-日期-销售趋势
     *
     * @param
     * @return
     */
    @PostMapping("/byDate")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<StatisticalDataVO> byDate(@RequestBody @Validated DateSalesTrendDTO.SearchDTO searchDTO) {
        StatisticalDataVO result = salesOrderService.byDate(searchDTO);
        return success(result);
    }


    /**
     * 销售相关-一级模块-事业部销售额
     * @param
     * @return
     */
    @PostMapping("/byDept")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<SalesCountVO>> byDept(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result = salesOrderService.byDept(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-事业部新老品销售额
     *
     * @param
     * @return
     */
    @PostMapping("/byDeptNewAndOld")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<ProductNewAndOldVO>> byDeptNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<ProductNewAndOldVO> result = salesOrderService.byDeptNewAndOld(dto);
        return success(result);
    }

    /**
     * 销售相关-一级模块-新/老 品销售额
     *
     * @param
     * @return
     */
    @PostMapping("/byNewAndOld")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<SalesCountVO>> byNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result = salesOrderService.byNewAndOld(dto);
        return success(result);
    }

    /**
     * 销售相关-二级模块-各个平台新/老品销售额
     *
     * @param
     * @return
     */
    @PostMapping("/byPlatformNewAndOld")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<ProductNewAndOldVO>> byPlatformNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<ProductNewAndOldVO> result = salesOrderService.byPlatformNewAndOld(dto);
        return success(result);
    }


    /**
     * 销售相关-二级模块-各个人员 新/老品销售额
     *
     * @param
     * @return
     */
    @PostMapping("/byPeopleNewAndOld")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<ProductNewAndOldVO>> byPeopleNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<ProductNewAndOldVO> result=salesOrderService.byPeopleNewAndOld(dto);
        return success(result);
    }


    /**
     * 销售相关-二级模块-各个品类新/老品销售额
     *
     * @param
     * @return
     */
    @PostMapping("/byCategoryNewAndOld")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<ProductNewAndOldVO>> byCategoryNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<ProductNewAndOldVO> result = salesOrderService.byCategoryNewAndOld(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-站点销售额
     *
     * @param
     * @return
     */
    @PostMapping("/bySite")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<SalesCountVO>> bySite(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result = salesOrderService.bySite(dto);
        return success(result);
    }

    /**
     * 销售相关-一级模块-新品自研，外采贡献分析
     *
     * @param
     * @return
     */
    @PostMapping("/byProductType")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<StatisticalDataVO> byProductType(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.byProductType(dto);
        return success(result);
    }

    /**
     * 销售相关-一级模块-销售额TOP20老品
     *
     * @param
     * @return
     */
    @PostMapping("/byOldProductTop")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<StatisticalDataVO> byOldProductTop(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.byOldProductTop(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-销售额TOP20老品
     *
     * @param
     * @return
     */
    @PostMapping("/byNewProductTop")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<StatisticalDataVO> byNewProductTop(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.byNewProductTop(dto);
        return success(result);
    }

    /**
     * 销售相关-一级模块-营销中心销售额
     *
     * @param
     * @return
     */
    @PostMapping("/byMarketingCenter")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<List<SalesCountVO>> byMarketingCenter(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result = salesOrderService.byMarketingCenter(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-亚马逊欧美日占比趋势分析
     *
     * @param
     * @return
     */
    @PostMapping("/byEuropeAndJapanSite")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "bi:module:content",
            tableAlias = "o"
    )
    public ApiResult<StatisticalDataVO> byEuropeAndJapanSite(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.byEuropeAndJapanSite(dto);
        return success(result);
    }

    /**
     * 新老品销售额
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.bi.dto.NewAndOldSalesSearchDTO.PagingDTO>>
     * @Author Luo_WG
     * @Date 2023/9/15 14:15
     **/
    @PostMapping("/newAndOldSalesAmount")
    public ApiResult<List<NewAndOldSalesSearchDTO.PagingDTO>> newAndOldSalesAmount(@RequestBody @Validated NewAndOldSalesSearchDTO.SearchDTO dto) {
        List<NewAndOldSalesSearchDTO.PagingDTO> result = salesOrderService.newAndOldSalesAmount(dto);
        return success(result);
    }

    /**
     * 新老品销售额-导出excel
     *
     * @param dto
     * @param response
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/9/21 9:46
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出新老品销售额")
    @PostMapping(value = "/newAndOldSalesExportExcel")
    public ApiResult newAndOldSalesExportExcel(@RequestBody NewAndOldSalesSearchDTO.SearchDTO dto, HttpServletResponse response) {
        Boolean flag = salesOrderService.newAndOldSalesExportExcel(dto, response);
        return flag ? success() : failure();
    }

    /**
     * 战略目标达成
     *
     * @return
     */
    @PostMapping("/targetMetrics")
    public ApiResult<List<BiTargetYearDTO.TargetMetricsFinishDTO>> targetMetrics(@RequestBody @Validated BiTargetYearDTO.SearchDTO dto) {
        List<BiTargetYearDTO.TargetMetricsFinishDTO> resultList = salesOrderService.listTargetMetrics(dto);
        return success(resultList);
    }


    /**
     * 产品等级销售占比分析
     */
    @PostMapping("/productGradeSales")
    public ApiResult<StatisticalDataVO> productGradeSales(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.productGradeSales(dto);
        return success(result);
    }

    /**
     * 部门完成率排行
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.bi.dto.CompletionRateRankingDTO.PagingDTO>>
     * @Author Luo_WG
     * @Date 2023/9/21 10:37
     **/
    @PostMapping("/listCompletionRateRanking")
    public ApiResult<List<CompletionRateRankingDTO.PagingDTO>> listCompletionRateRanking(@RequestBody @Validated CompletionRateRankingDTO.SearchDTO dto) {
        List<CompletionRateRankingDTO.PagingDTO> result = salesOrderService.listCompletionRateRanking(dto);
        return success(result);
    }


    /**
     * 部门完成率排行-导出excel
     *
     * @param dto
     * @param response
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/9/21 9:46
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出部门完成率排行")
    @PostMapping(value = "/completionRateRankingExportExcel")
    public ApiResult completionRateRankingExportExcel(@RequestBody CompletionRateRankingDTO.SearchDTO dto, HttpServletResponse response) {
        Boolean flag = salesOrderService.completionRateRankingExportExcel(dto, response);
        return flag ? success() : failure();
    }

    /**
     * 毛利润&毛利率 模块
     *
     * @param dto
     * @return
     * @author yl
     * @date 2023-09-21 17:13
     */
    @PostMapping("/grossProfit")
    public ApiResult<StatisticalDataVO> grossProfit(@RequestBody @Validated BiDataSourceCostDTO.GrossProfitDTO dto) {
        StatisticalDataVO statistical = salesOrderService.grossProfit(dto);
        return success(statistical);
    }
    /**
     * B2B客户属性分析
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author zdy
     * @Date 2022/12/27 10:41
     **/
    @PostMapping("/customerPropertyAnalysis")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<StatisticalDataVO> customerPropertyAnalysis(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        StatisticalDataVO statisticalDataVO = salesOrderService.customerPropertyAnalysis(biFilterDTO);
        return success(statisticalDataVO);
    }

    /**
     * B2B客户等级占比
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author zdy
     * @Date 2022/12/27 10:41
     **/
    @PostMapping("/customerLevelProportion")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<StatisticalDataVO> customerLevelProportion(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        StatisticalDataVO statisticalDataVO = salesOrderService.customerLevelProportion(biFilterDTO);
        return success(statisticalDataVO);
    }

}
