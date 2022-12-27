package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.*;
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


    /**
     * 销售额- 一级模块-月销售额趋势
     *
     * @return
     */
    @GetMapping("/byMonth")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:sales:byMonth",
//            tableAlias = "dmp_order_info"
//    )
    public ApiResult<StatisticalDataVO> getMonth() {
        StatisticalDataVO statistical = salesOrderService.getMonthSales();
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








}
