package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.SkuDateFilterDTO;
import com.erp.model.bi.dto.SkuFilterDTO;
import com.erp.model.bi.vo.*;
import com.erp.server.bi.service.BiComprehensiveAnalyseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 综合分析/一级模块
 *
 * @Author Luo_WG
 * @Date 2022/12/16 11:07
 **/
@RestController
@RequestMapping("bi/comprehensive")
public class BiComprehensiveAnalyseController extends BaseController {
    @Resource
    private BiComprehensiveAnalyseService biComprehensiveAnalyseService;

    /**
     * SKU矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:42
     * @param biFilterDTO biFilterDTO
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    @PostMapping("/skuMatrix")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<List<Object>>> skuMatrix(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<List<Object>> skuMatrixList = biComprehensiveAnalyseService.skuMatrix(biFilterDTO);
        return success(skuMatrixList);
    }

    /**
     * 店铺矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:42
     * @param biFilterDTO biFilterDTO
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    @PostMapping("/shopMatrix")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<List<Object>>> shopMatrix(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<List<Object>> matrixVOPagingVO = biComprehensiveAnalyseService.shopMatrix(biFilterDTO);
        return success(matrixVOPagingVO);
    }

    /**
     * 平台店铺对比趋势
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ShopContrastTrendVO>
     **/
    @PostMapping("/shopContrastTrend")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<ShopContrastTrendVO>> shopContrastTrend(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<ShopContrastTrendVO> list = biComprehensiveAnalyseService.shopContrastTrend(biFilterDTO);
        return success(list);
    }

    /**
     * 品类矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ShopContrastTrendVO>
     **/
    @PostMapping("/categoryMatrix")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<List<Object>>> categoryMatrix(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<List<Object>> matrixVOPagingVO = biComprehensiveAnalyseService.categoryMatrix(biFilterDTO);
        return success(matrixVOPagingVO);
    }

    /**
     * 销售明细表-SKU
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    @PostMapping("/saleDetailSku")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SaleDetailVO>> saleDetailSku(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<SaleDetailVO> saleDetailVOList = biComprehensiveAnalyseService.saleDetailSku(biFilterDTO);
        return success(saleDetailVOList);
    }

    /**
     * 销售明细表-店铺
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    @PostMapping("/saleDetailShop")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SaleDetailVO>> saleDetailShop(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<SaleDetailVO> saleDetailVOList = biComprehensiveAnalyseService.saleDetailShop(biFilterDTO);
        return success(saleDetailVOList);
    }

    /**
     * 销售明细表-用户
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    @PostMapping("/saleDetailUser")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SaleDetailVO>> saleDetailUser(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<SaleDetailVO> saleDetailVOList = biComprehensiveAnalyseService.saleDetailUser(biFilterDTO);
        return success(saleDetailVOList);
    }

    /**
     * 单商品-SKU日期销售额趋势表-日期
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    @PostMapping("/saleDetailDate")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SaleDetailVO>> saleDetailDate(@RequestBody @Validated SkuDateFilterDTO biFilterDTO) {
        List<SaleDetailVO> saleDetailVOList = biComprehensiveAnalyseService.saleDetailDate(biFilterDTO);
        return success(saleDetailVOList);
    }

    /**
     * 单商品-SKU日期销售额趋势图
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    @PostMapping("/skuDateSaleTrend")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SkuDateSaleTrendVO>> skuDateSaleTrend(@RequestBody @Validated SkuDateFilterDTO biFilterDTO) {
        List<SkuDateSaleTrendVO> skuDateSaleTrendVOS = biComprehensiveAnalyseService.skuDateSaleTrend(biFilterDTO);
        return success(skuDateSaleTrendVOS);
    }
}
