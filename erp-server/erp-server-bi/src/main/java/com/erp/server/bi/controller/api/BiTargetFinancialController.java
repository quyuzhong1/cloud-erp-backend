package com.erp.server.bi.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.TargetSaleSumVO;
import com.erp.server.bi.service.BiDataSourceCostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * BI报表指标模块财务数据
 * @author Cloud
 */
@RestController
@RequestMapping("indicator/financial")
public class BiTargetFinancialController extends BaseController {


    @Resource
    private BiDataSourceCostService biDataSourceCostService;
    
    /**
     * 销售毛利润
     *
     * code = indicator_sales_profit
     * @param dto
     * @return ApiResult<TargetSaleSumVO>
     */
    @PostMapping("/sales/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<TargetSaleSumVO> countSales(@RequestBody BiFilterDTO dto){
        TargetSaleSumVO vo = biDataSourceCostService.sumSalesProfit(dto);
        return success(vo);
    }

    /**
     * 统计主营收入
     *
     * code = indicator_main_revenue
     * @param dto
     * @return ApiResult<TargetSaleSumVO>
     */

    @PostMapping("/main/revenue")
    //    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<TargetSaleSumVO> countMainRevenue(@RequestBody BiFilterDTO dto){
        TargetSaleSumVO vo = biDataSourceCostService.sumMainRevenue(dto);
        return success(vo);
    }
    /**
     * 统计销售成本
     *
     * code = indicator_sales_cost
     * @param dto
     * @return ApiResult<TargetSaleSumVO>
     */
    @PostMapping("/sales/cost")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<TargetSaleSumVO> countSalesCost(@RequestBody BiFilterDTO dto){
        TargetSaleSumVO vo = biDataSourceCostService.sumSalesCost(dto);
        return success(vo);
    }

    /**
     * 毛利率
     *
     * code = indicator_sales_ratio
     * @param dto
     * @return ApiResult<TargetSaleSumVO>
     */
    @PostMapping("/sales/ratio")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",
//            tableAlias = "bi_data_source_cost"
//    )
    public ApiResult<TargetSaleSumVO> count(@RequestBody BiFilterDTO dto){
        TargetSaleSumVO vo = biDataSourceCostService.sumSalesRatio(dto);
        return success(vo);
    }




}
