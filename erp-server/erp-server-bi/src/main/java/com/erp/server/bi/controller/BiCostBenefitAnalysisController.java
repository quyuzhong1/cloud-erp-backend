package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.ChartVO;
import com.erp.model.bi.vo.PieChartVO;
import com.erp.model.bi.vo.SeriesVO;
import com.erp.server.bi.service.BiDataSourceCostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * BI成本利润分析
 *
 * @Author Cloud
 * @Date 2022/12/29 18:08
 **/
@RestController
@RequestMapping("bi/cost/benefit")
public class BiCostBenefitAnalysisController extends BaseController {

    @Resource
    private BiDataSourceCostService biDataSourceCostService;


    /**
     * 事业部成本/利润 图表
     */
    @PostMapping("/dept/profit")
    public ApiResult<List<SeriesVO>> getDeptProfitChart(@RequestBody BiFilterDTO dto) {
        List<SeriesVO> vo = biDataSourceCostService.getDeptCostProfit(dto);
        return success(vo);
    }

    /**
     * 利润TOP20% 店铺 图表
     */
    @PostMapping("/shop/profit")
    public ApiResult<List<SeriesVO>> getShopProfitChart(@RequestBody BiFilterDTO dto) {
        List<SeriesVO> vo = biDataSourceCostService.getShopCostProfit(dto);
        return success(vo);
    }
    /**
     * 指标趋势 图表
     */
    @PostMapping("/month/profit")
    public ApiResult<List<SeriesVO>> getMonthProfitChart(@RequestBody BiFilterDTO dto) {
        List<SeriesVO> vo = biDataSourceCostService.getMonthCostProfit(dto);
        return success(vo);
    }

    /**
     * 各平台毛利润占比分析 图表 PieChartVO
     */
    @PostMapping("/platform/percent")
    public ApiResult<List<PieChartVO>> getPlatformPercentChart(@RequestBody BiFilterDTO dto) {
        List<PieChartVO> vo = biDataSourceCostService.getPlatformCostPercent(dto);
        return success(vo);
    }
    /**
     * 利润TOP20%平台  图表
     */
    @PostMapping("/platform/profit")
    public ApiResult<List<SeriesVO>> getPlatformProfitChart(@RequestBody BiFilterDTO dto) {
        List<SeriesVO> vo = biDataSourceCostService.getPlatformCostProfit(dto);
        return success(vo);
    }
    /**
     * 店铺成本/利润 表格
     */

    /**
     * 平台成本/利润
     */

    /**
     * 事业部成本/利润  表格
     */

    /**
     *
     * 成员成本/利润  表格
     */

    /**
     * 新品成本/利润  表格
     */

    /**
     * 新/老平台成本/利润  表格
     */

}