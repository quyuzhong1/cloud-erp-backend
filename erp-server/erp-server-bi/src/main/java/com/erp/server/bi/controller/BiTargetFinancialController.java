package com.erp.server.bi.controller;


import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.dto.TargetFinancialDTO;
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
@RequestMapping("bi/indicator/financial")
public class BiTargetFinancialController extends BaseController {


    @Resource
    private BiDataSourceCostService biDataSourceCostService;
    
    /**
     * 销售毛利润
     * @param dto
     * @return ApiResult<TargetSaleSumVO>
     */
    @PostMapping("/sales/profit")
    public ApiResult countSales(@RequestBody TargetFinancialDTO dto){
        TargetSaleSumVO vo = biDataSourceCostService.sumSalesProfit(dto);
        return success(vo);
    }

    /**
     * 统计主营收入
     * @param dto
     * @return ApiResult<TargetSaleSumVO>
     */
    @PostMapping("/main/revenue")
    public ApiResult countMainRevenue(@RequestBody TargetFinancialDTO dto){
        TargetSaleSumVO vo = biDataSourceCostService.sumMainRevenue(dto);
        return success(vo);
    }
    /**
     * 统计销售成本
     * @param dto
     * @return ApiResult<TargetSaleSumVO>
     */
    @PostMapping("/sales/cost")
    public ApiResult countSalesCost(@RequestBody TargetFinancialDTO dto){
        TargetSaleSumVO vo = biDataSourceCostService.sumMainRevenue(dto);
        return success(vo);
    }

    /**
     * 毛利率
     * @param dto
     * @return ApiResult<TargetSaleSumVO>
     */
    @PostMapping("/sales/ratio")
    public ApiResult count(@RequestBody TargetFinancialDTO dto){
        TargetSaleSumVO vo = biDataSourceCostService.sumSalesRatio(dto);
        return success(vo);
    }




}
