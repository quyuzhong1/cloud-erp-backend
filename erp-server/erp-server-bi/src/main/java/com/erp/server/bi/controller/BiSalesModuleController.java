package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.SkuFilterDTO;
import com.erp.model.bi.vo.*;
import com.erp.server.bi.service.BiComprehensiveAnalyseService;
import com.erp.server.bi.service.SalesOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 销售额模块Api 接口
 *
 * @Classname BiSalesBusinessDivisionController
 * @Description TODO
 * @Date 2022-12-15 11:34
 * @Created by yl
 */
@RestController
@RequestMapping("bi/sales")
public class BiSalesModuleController extends BaseController {


    @Resource
    private SalesOrderService salesOrderService;

    @Resource
    private BiComprehensiveAnalyseService biComprehensiveAnalyseService;

    /**
     * 销售额- 一级模块-月销售额趋势
     *
     * @return
     */
    @PostMapping("/byMonth")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byMonth",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult<StatisticalDataVO> getMonth(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO statistical = salesOrderService.getMonthSales(dto);
        return success(statistical);
    }


    /**
     * 销售额- 一级模块-SKU销售额
     *
     * @return
     */
    @PostMapping("/bySku")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:bySku",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesVO>> getBySku(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesVO> resultList = salesOrderService.getBySku(dto);
        return success(resultList);
    }


    /**
     * 销售额- 一级模块-SKU销售额
     *
     * @return
     */
    @PostMapping("/bySpu")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:bySpu",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesVO>> getBySpu(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesVO> resultList = salesOrderService.getBySpu(dto);
        return success(resultList);
    }


    /**
     * 销售相关-一级模块-SKU国家销售额
     * @param dto
     * @return
     */
    @PostMapping("/bySkuCountry")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesByCountryVO>> getByCountry(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesByCountryVO> resultList = salesOrderService.getByCountry(dto);
        return success(resultList);
    }


    /**
     * 销售相关-一级模块-TOB/TOC销售额
     * @param dto
     * @return
     */
    @PostMapping("/byPlatformRatio")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byCountry",
//            tableAlias = "o"
//    )
    public ApiResult<StatisticalDataVO> byPlatformRatio(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.getByPlatformRatio(dto);
        return success(result);
    }



    /**
     * 销售相关-一级模块-店铺销售额
     * @param dto
     * @return
     */
    @PostMapping("/byShop")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<ShopSalesVO>> byShop(@RequestBody @Validated BiFilterDTO dto) {
        List<ShopSalesVO> resultList = salesOrderService.getByShop(dto);
        return success(resultList);
    }


    /**
     * 销售相关-一级模块-店铺销售额
     * @param dto
     * @return
     */
    @PostMapping("/byTopShop")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byCountry",
//            tableAlias = "o"
//    )
    public ApiResult<StatisticalDataVO> byTopShop(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result = salesOrderService.byTopShop(dto);
        return success(result);
    }


    /**
     * 销售相关-二级模块-店铺国家销售额
     * @param dto
     * @return
     */
    @PostMapping("/byShopCountry")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesGroupVO>> byShopCountry(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesGroupVO> resultList = salesOrderService.byShopCountry(dto);
        return success(resultList);
    }

    /**
     * 销售相关-二级模块-品类销售额
     * @param dto
     * @return
     */
    @PostMapping("/byShopCategory")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesGroupVO>> byShopCategory(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesGroupVO> resultList = salesOrderService.byShopCategory(dto);
        return success(resultList);
    }

    /**
     * 销售相关-二级模块-店铺的新/老品销售额
     * @param dto
     * @return
     */
    @PostMapping("/byShopNewAndOld")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<ShopNewAndOldSalesVO>> byShopNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<ShopNewAndOldSalesVO> resultList=salesOrderService.byShopNewAndOld(dto);
        return success(resultList);
    }


    /**
     * 销售相关-一级模块-国家销售额
     * @param dto
     * @return
     */
    @PostMapping("/byCountry")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesCountVO>> byCountry(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> resultList=salesOrderService.byCountry(dto);
        return success(resultList);
    }

    /**
     * 销售相关-一级模块-品类销售额
     * @param dto
     * @return
     */
    @PostMapping("/byCategory")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<StatisticalDataVO> byCategory(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result=salesOrderService.byCategory(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-品牌销售额
     * @param dto
     * @return
     */
    @PostMapping("/byBrand")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesCountVO>> byBrand(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result=salesOrderService.byBrand(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-平台销售额
     * @param dto
     * @return
     */
    @PostMapping("/byPlatform")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesCountVO>> byPlatform(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result=salesOrderService.byPlatform(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-国内国外占比
     * @param dto
     * @return
     */
    @PostMapping("/byHomeAndAbroad")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<StatisticalDataVO> byHomeAndAbroad(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result=salesOrderService.byHomeAndAbroad(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-人员销售额
     * @param dto
     * @return
     */
    @PostMapping("/byPeople")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesCountVO>> byPeople(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result=salesOrderService.byPeople(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-人员销售额 周排行
     * @param
     * @return
     */
    @PostMapping("/byPeopleWeekRank")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<PeopleSalesRankVO>> byPeopleWeekRank() {
        List<PeopleSalesRankVO> result=salesOrderService.byPeopleWeekRank();
        return success(result);
    }


    /**
     * 销售相关-一级模块-人员销售额 月排行
     * @param
     * @return
     */
    @PostMapping("/byPeopleMonthRank")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<PeopleSalesRankVO>> byPeopleMonthRank() {
        return success();
    }

    /**
     * 销售相关-一级模块-人员销售额 季度排行
     * @param
     * @return
     */
    @PostMapping("/byPeopleQuarterRank")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<PeopleSalesRankVO>> byPeopleQuarterRank() {
        return success();
    }

    /**
     * 销售相关-一级模块-人员销售额 年度排行
     * @param
     * @return
     */
    @PostMapping("/byPeopleYearRank")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<PeopleSalesRankVO>> byPeopleYearRank() {
        return success();
    }


    /**
     * 销售相关-一级模块-日期
     * @param
     * @return
     */
    @PostMapping("/byDate")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SaleDetailVO>> byDate(@RequestBody @Validated SkuFilterDTO biFilterDTO) {
        List<SaleDetailVO> saleDetailVOList = biComprehensiveAnalyseService.saleDetailDate(biFilterDTO);
        return success(saleDetailVOList);
    }


    /**
     * 销售相关-一级模块-事业部销售额
     * @param
     * @return
     */
    @PostMapping("/byDept")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesCountVO>> byDept(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result=salesOrderService.byDept(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-事业部新老品销售额
     * @param
     * @return
     */
    @PostMapping("/byDeptNewAndOld")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesCountVO>> byDeptNewAndOld(@RequestBody @Validated BiFilterDTO dto) {

        return success();
    }

    /**
     * 销售相关-一级模块-新/老 品销售额
     * @param
     * @return
     */
    @PostMapping("/byNewAndOld")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesCountVO>> byNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result=salesOrderService.byNewAndOld(dto);
        return success(result);
    }

    /**
     * 销售相关-二级模块-各个平台新/老品销售额
     * @param
     * @return
     */
    @PostMapping("/byPlatformNewAndOld")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<ProductNewAndOldVO>> byPlatformNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<ProductNewAndOldVO> result=salesOrderService.byPlatformNewAndOld(dto);
        return success(result);
    }


    /**
     * 销售相关-二级模块-各个人员 新/老品销售额
     * @param
     * @return
     */
    @PostMapping("/byPeopleNewAndOld")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<ProductNewAndOldVO>> byPeopleNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<ProductNewAndOldVO> result=salesOrderService.byPeopleNewAndOld(dto);
        return success(result);
    }



    /**
     * 销售相关-二级模块-各个品类新/老品销售额
     * @param
     * @return
     */
    @PostMapping("/byCategoryNewAndOld")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<ProductNewAndOldVO>> byCategoryNewAndOld(@RequestBody @Validated BiFilterDTO dto) {
        List<ProductNewAndOldVO> result=salesOrderService.byCategoryNewAndOld(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-站点销售额
     * @param
     * @return
     */
    @PostMapping("/bySite")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesCountVO>> bySite(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result=salesOrderService.bySite(dto);
        return success(result);
    }

    /**
     * 销售相关-一级模块-新品自研，外采贡献分析
     * @param
     * @return
     */
    @PostMapping("/byProductType")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<StatisticalDataVO> byProductType(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result=salesOrderService.byProductType(dto);
        return success(result);
    }

    /**
     * 销售相关-一级模块-销售额TOP20老品
     * @param
     * @return
     */
    @PostMapping("/byOldProductTop")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<StatisticalDataVO> byOldProductTop(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result=salesOrderService.byOldProductTop(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-销售额TOP20老品
     * @param
     * @return
     */
    @PostMapping("/byNewProductTop")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<StatisticalDataVO> byNewProductTop(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result=salesOrderService.byNewProductTop(dto);
        return success(result);
    }

    /**
     * 销售相关-一级模块-营销中心销售额
     * @param
     * @return
     */
    @PostMapping("/byMarketingCenter")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<List<SalesCountVO>> byMarketingCenter(@RequestBody @Validated BiFilterDTO dto) {
        List<SalesCountVO> result=salesOrderService.byMarketingCenter(dto);
        return success(result);
    }


    /**
     * 销售相关-一级模块-营销中心销售额
     * @param
     * @return
     */
    @PostMapping("/byEuropeAndJapanSite")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byShopCountry",
//            tableAlias = "o"
//    )
    public ApiResult<StatisticalDataVO> byEuropeAndJapanSite(@RequestBody @Validated BiFilterDTO dto) {
        StatisticalDataVO result=salesOrderService.byEuropeAndJapanSite(dto);
        return success(result);
    }

}
