package com.erp.server.bi.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.BiSkuDetailTopDTO;
import com.erp.model.bi.dto.SkuDateFilterDTO;
import com.erp.model.bi.dto.SkuDetailDTO;
import com.erp.model.bi.vo.SaleDetailVO;
import com.erp.model.bi.vo.SalesPriceRangeVO;
import com.erp.model.bi.vo.SkuDateSaleTrendVO;
import com.erp.model.bi.vo.StatisticalDataVO;
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
@RequestMapping("comprehensive")
public class BiComprehensiveAnalyseController extends BaseController {
    @Resource
    private BiComprehensiveAnalyseService biComprehensiveAnalyseService;

    /**
     * SKU矩阵
     *
     * @param biFilterDTO biFilterDTO
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.vo.SkuMatrixVO>
     * @Author Luo_WG
     * @Date 2022/12/26 10:42
     **/
    @PostMapping("/skuMatrix")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<List<Object>>> skuMatrix(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<List<Object>> skuMatrixList = biComprehensiveAnalyseService.skuMatrix(biFilterDTO);
        return success(skuMatrixList);
    }

    /**
     * 店铺矩阵
     *
     * @param biFilterDTO biFilterDTO
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.vo.SkuMatrixVO>
     * @Author Luo_WG
     * @Date 2022/12/26 10:42
     **/
    @PostMapping("/shopMatrix")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<List<Object>>> shopMatrix(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<List<Object>> matrixVOPagingVO = biComprehensiveAnalyseService.shopMatrix(biFilterDTO);
        return success(matrixVOPagingVO);
    }

    /**
     * 平台店铺对比趋势
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ShopContrastTrendVO>
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     **/
    @PostMapping("/shopContrastTrend")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<List<Object>>> shopContrastTrend(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<List<Object>> list = biComprehensiveAnalyseService.shopContrastTrend(biFilterDTO);
        return success(list);
    }

    /**
     * 品类矩阵
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ShopContrastTrendVO>
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     **/
    @PostMapping("/categoryMatrix")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<List<Object>>> categoryMatrix(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<List<Object>> matrixVOPagingVO = biComprehensiveAnalyseService.categoryMatrix(biFilterDTO);
        return success(matrixVOPagingVO);
    }

    /**
     * 销售明细表-SKU
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     **/
    @PostMapping("/saleDetailSku")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SaleDetailVO>> saleDetailSku(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<SaleDetailVO> saleDetailVOList = biComprehensiveAnalyseService.saleDetailSku(biFilterDTO);
        return success(saleDetailVOList);
    }

    /**
     * 销售单价分布
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author zdy
     * @Date 2022/12/27 10:41
     **/
    @PostMapping("/salePriceDistribution")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SalesPriceRangeVO>> salePriceDistribution(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<SalesPriceRangeVO> salesPriceRangeVOS = biComprehensiveAnalyseService.salePriceDistribution(biFilterDTO);
        return success(salesPriceRangeVOS);
    }

    /**
     * 销售明细表-店铺
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     **/
    @PostMapping("/saleDetailShop")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SaleDetailVO>> saleDetailShop(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<SaleDetailVO> saleDetailVOList = biComprehensiveAnalyseService.saleDetailShop(biFilterDTO);
        return success(saleDetailVOList);
    }

    /**
     * 销售明细表-用户
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     **/
    @PostMapping("/saleDetailUser")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SaleDetailVO>> saleDetailUser(@RequestBody @Validated BiFilterDTO biFilterDTO) {
        List<SaleDetailVO> saleDetailVOList = biComprehensiveAnalyseService.saleDetailUser(biFilterDTO);
        return success(saleDetailVOList);
    }

    /**
     * 单商品-SKU日期销售额趋势表-日期
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     **/
    @PostMapping("/saleDetailDate")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SaleDetailVO>> saleDetailDate(@RequestBody @Validated SkuDateFilterDTO biFilterDTO) {
        biFilterDTO.setEndTime(biFilterDTO.getEndTime(), 1);
        biFilterDTO.setTimeType(null == biFilterDTO.getTimeType() ? 0 : biFilterDTO.getTimeType());
        biFilterDTO.setSettleMethod(null == biFilterDTO.getSettleMethod() ? 0 : biFilterDTO.getSettleMethod());
        List<SaleDetailVO> saleDetailVOList = biComprehensiveAnalyseService.saleDetailDate(biFilterDTO);
        return success(saleDetailVOList);
    }

    /**
     * 单商品-SKU日期销售额趋势图
     *
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     **/
    @PostMapping("/skuDateSaleTrend")
    //@DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:module:content", tableAlias = "doi")
    public ApiResult<List<SkuDateSaleTrendVO>> skuDateSaleTrend(@RequestBody @Validated SkuDateFilterDTO biFilterDTO) {
        biFilterDTO.setEndTime(biFilterDTO.getEndTime(), 1);
        biFilterDTO.setTimeType(null == biFilterDTO.getTimeType() ? 0 : biFilterDTO.getTimeType());
        biFilterDTO.setSettleMethod(null == biFilterDTO.getSettleMethod() ? 0 : biFilterDTO.getSettleMethod());
        List<SkuDateSaleTrendVO> skuDateSaleTrendVOS = biComprehensiveAnalyseService.skuDateSaleTrend(biFilterDTO);
        return success(skuDateSaleTrendVOS);
    }

    /**
     * 单商品-SKU详情顶部信息
     *
     * @param dto SkuDetailDTO
     * @return java.util.List<com.erp.model.bi.dto.BiSkuDetailTopVO>
     * @Author Jim
     * @Date 2023/09/15
     **/
    @PostMapping("/skuDetail")
    public ApiResult<BiSkuDetailTopDTO> skuDateSaleTrend(@RequestBody @Validated SkuDetailDTO dto) {
        BiSkuDetailTopDTO vo = biComprehensiveAnalyseService.skuDetailTop(dto);
        return success(vo);
    }
}
