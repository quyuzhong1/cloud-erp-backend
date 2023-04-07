package com.erp.server.bi.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.QuarterMonthSalesVO;
import com.erp.model.bi.vo.QuarterMonthSalesVolumeVO;
import com.erp.model.bi.vo.SalesCompletionInfoVO;
import com.erp.model.bi.vo.TargetAnalysisVO;
import com.erp.server.bi.service.DmpOrderInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * BI目标一级模块
 *
 * @Author Cloud
 * @Date 2022/12/20 10:32
 **/
@RestController
@RequestMapping("target/")
public class BiTargetModuleController extends BaseController {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;
    /**
     * 季度销售额完成情况
     */
    @PostMapping("/quarter/sales")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<TargetAnalysisVO<QuarterMonthSalesVO>> quarterSales(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        TargetAnalysisVO<QuarterMonthSalesVO> vo = dmpOrderInfoService.sumQuarterSales(dto);
        return success(vo);
    }


    /**
     * 季度销量完成情况
     */
    @PostMapping("/quarter/sales/volume")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<TargetAnalysisVO<QuarterMonthSalesVolumeVO>> quarterSalesVolume(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        TargetAnalysisVO<QuarterMonthSalesVolumeVO> vo = dmpOrderInfoService.sumQuarterSalesVolume(dto);
        return success(vo);
    }

    /**
     * 月度销售额完成情况
     */
    @PostMapping("/month/sales")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<TargetAnalysisVO<QuarterMonthSalesVO>> monthSales(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        TargetAnalysisVO<QuarterMonthSalesVO> vo = dmpOrderInfoService.sumMonthSales(dto);
        return success(vo);
    }

    /**
     * 月度销售额完成情况
     */
    @PostMapping("/month/sales/volume")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<TargetAnalysisVO<QuarterMonthSalesVolumeVO>> monthSalesVolume(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        TargetAnalysisVO<QuarterMonthSalesVolumeVO> vo = dmpOrderInfoService.sumMonthSalesVolume(dto);
        return success(vo);
    }

    /**
     *
     * 事业部销售额/销量完成情况
     */


    /**
     * 平台销售额/销量完成情况
     */
    @PostMapping("/platform/sales")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<List<SalesCompletionInfoVO>> platformSalesCompletion(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        List<SalesCompletionInfoVO> vo = dmpOrderInfoService.sumPlatformSalesCompletion(dto);
        return success(vo);
    }
    /**
     * 站点销售额/销量完成情况
     */

    /**
     * 店铺销售额/销量完成情况
     */



    /**
     * 品类销售额/销量完成情况
     */
    @PostMapping("/category/sales")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<List<SalesCompletionInfoVO>> categorySalesCompletion(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        List<SalesCompletionInfoVO> vo = dmpOrderInfoService.sumCategorySalesCompletion(dto);
        return success(vo);
    }

    /**
     * 新品销售额/销量完成情况
     */
    @PostMapping("/new/product/sales")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<List<SalesCompletionInfoVO>> newProductSalesCompletion(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        List<SalesCompletionInfoVO> vo = dmpOrderInfoService.sumNewProductSalesCompletion(dto, 1);
        return success(vo);
    }

    /**
     * 老品销售额/销量完成情况
     */
    @PostMapping("/old/product/sales")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<List<SalesCompletionInfoVO>> oldProductSalesCompletion(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        List<SalesCompletionInfoVO> vo = dmpOrderInfoService.sumNewProductSalesCompletion(dto, 0);
        return success(vo);
    }

    /**
     * 产品品类销售额/销量完成情况
     */
    @PostMapping("/product/position/sales")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<List<SalesCompletionInfoVO>> positionSalesCompletion(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        List<SalesCompletionInfoVO> vo = dmpOrderInfoService.sumProductPositionSalesCompletion(dto);
        return success(vo);
    }


    /**
     * 新老品排行
     * @param dto
     * @return
     */
    @PostMapping("/product/type/sales")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<List<SalesCompletionInfoVO>> productTypeSalesCompletion(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        List<SalesCompletionInfoVO> vo = dmpOrderInfoService.sumProductTypeCompletion(dto);
        return success(vo);
    }

    /**
     * SKU销售额/销量完成情况
     */
    @PostMapping("/sku/sales")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<List<SalesCompletionInfoVO>> skuSalesCompletion(@RequestBody @Validated(BiFilterDTO.SelectTargetModule.class) BiFilterDTO dto) {
        List<SalesCompletionInfoVO> vo = dmpOrderInfoService.sumNewProductSalesCompletion(dto, null);
        return success(vo);
    }
    /**
     * 人员达成
     */

}
