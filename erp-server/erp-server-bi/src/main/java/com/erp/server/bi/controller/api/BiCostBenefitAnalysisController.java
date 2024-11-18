package com.erp.server.bi.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.CostProfitAnalyzeRankVO;
import com.erp.model.bi.vo.PieChartVO;
import com.common.business.vo.SeriesVO;
import com.erp.server.bi.service.BiDataSourceCostService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * BI成本利润分析
 *
 * @Author Cloud
 * @Date 2022/12/29 18:08
 **/
@RestController
@RequestMapping("cost/benefit")
public class BiCostBenefitAnalysisController extends BaseController {

    @Resource
    private BiDataSourceCostService biDataSourceCostService;


    /**
     * 事业部成本/利润 图表
     */
    @PostMapping("/dept/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<SeriesVO<String>>> getDeptProfitChart(@RequestBody BiFilterDTO dto) {
        List<SeriesVO<String>> vo = biDataSourceCostService.getDeptCostProfit(dto);
        return success(vo);
    }

    /**
     * 利润TOP20% 店铺 图表
     */
    @PostMapping("/shop/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<SeriesVO<String>>> getShopProfitChart(@RequestBody BiFilterDTO dto) {
        List<SeriesVO<String>> vo = biDataSourceCostService.getShopCostProfit(dto);
        return success(vo);
    }
    /**
     * 指标趋势 图表 月
     */
    @PostMapping("/month/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<SeriesVO<String>>> getMonthProfitChart(@RequestBody BiFilterDTO dto) {
        List<SeriesVO<String>> vo = biDataSourceCostService.getMonthCostProfit(dto);
        return success(vo);
    }
    /**
     * 指标趋势 图表 季度
     */
    @PostMapping("/quarter/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<SeriesVO<String>>> getQuarterProfitChart(@RequestBody BiFilterDTO dto) {
        List<SeriesVO<String>> vo = biDataSourceCostService.getQuarterCostProfit(dto);
        return success(vo);
    }
    /**
     * 指标趋势 图表 年
     */
    @PostMapping("/year/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<SeriesVO<String>>> getYearProfitChart(@RequestBody BiFilterDTO dto) {
        List<SeriesVO<String>> vo = biDataSourceCostService.getYearCostProfit(dto);
        return success(vo);
    }
    /**
     * 各平台毛利润占比分析 图表 PieChartVO
     */
    @PostMapping("/platform/percent")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<PieChartVO>> getPlatformPercentChart(@RequestBody BiFilterDTO dto) {
        List<PieChartVO> vo = biDataSourceCostService.getPlatformCostPercent(dto);
        return success(vo);
    }
    /**
     * 利润TOP20%平台  图表
     */
    @PostMapping("/platform/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<SeriesVO<String>>> getPlatformProfitChart(@RequestBody BiFilterDTO dto) {
        List<SeriesVO<String>> vo = biDataSourceCostService.getPlatformCostProfit(dto);
        return success(vo);
    }

    /**
     * 事业部成本/利润  排名表格
     */
    @PostMapping("/dept/cost/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<CostProfitAnalyzeRankVO>> getDeptCostProfitSheet(@RequestBody BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> vo = biDataSourceCostService.getDeptCostProfitRank(dto);
        return success(vo);
    }

    /**
     * 平台成本/利润
     */
    @PostMapping("/platform/cost/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<CostProfitAnalyzeRankVO>> getPlatformCostProfitSheet(@RequestBody BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> vo = biDataSourceCostService.getPlatformCostProfitRank(dto);
        return success(vo);
    }
    /**
     * 店铺成本/利润 表格
     */
    @PostMapping("/shop/cost/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<CostProfitAnalyzeRankVO>> getShopCostProfitSheet(@RequestBody BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> vo = biDataSourceCostService.getShopCostProfitRank(dto);
        return success(vo);
    }
    /**
     *
     * 成员成本/利润  表格
     */
    @PostMapping("/user/cost/profit")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "bi:module:content",,
//            tableAlias = "bdsc"
//    )
    public ApiResult<List<CostProfitAnalyzeRankVO>> getUserCostProfitSheet(@RequestBody BiFilterDTO dto) {
        List<CostProfitAnalyzeRankVO> vo = biDataSourceCostService.getUserCostProfitRank(dto);
        return success(vo);
    }

    /**
     * 新品成本/利润  表格
     */

    /**
     * 新/老平台成本/利润  表格
     */

}