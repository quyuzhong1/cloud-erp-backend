package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import rx.functions.Func1;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * 成本盈利分析排名
 *
 * @Author Cloud
 * @Date 2022/12/20 17:50
 **/
@Data
@NoArgsConstructor
public class CostProfitAnalyzeRankVO {

    /**
     * 排行
     */
    private Integer ranking;

    /**
     * 维度
     */
    private String dimension;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 销售成本
     */
    private BigDecimal salesCostAmount;

    /**
     * 毛利润
     */
    private BigDecimal grossProfitAmount;

    /**
     * 净利润
     */
    private BigDecimal netProfitAmount;

    /**
     * 毛利率
     */
    private BigDecimal grossProfitRate;
    /**
     * 净利率
     */
    private BigDecimal netProfitRate;

    /**
     * 成本占比
     */
    private BigDecimal costRatio;

    /**
     * 利润占比
     */
    private BigDecimal profitRatio;


    public CostProfitAnalyzeRankVO(String deptName, BigDecimal profitAmount, BigDecimal costAmount, BigDecimal salesAmount, BigDecimal mainAmount) {
        this.dimension = deptName;
        this.salesAmount = new BigDecimal(salesAmount.stripTrailingZeros().toPlainString());
        this.salesCostAmount = new BigDecimal(costAmount.stripTrailingZeros().toPlainString());
        this.grossProfitAmount = new BigDecimal(profitAmount.stripTrailingZeros().toPlainString());
        // 毛利润/主营业务收入 * 100
        BigDecimal  grossProfitRate = BigDecimal.ZERO.compareTo(mainAmount) != 0 ?
                this.grossProfitAmount.divide(mainAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).stripTrailingZeros() : BigDecimal.ZERO;
        this.grossProfitRate = new BigDecimal(grossProfitRate.toPlainString());
        // 成本占比  销售成本 / 销售额 * 100
        BigDecimal costRatio = BigDecimal.ZERO.compareTo(salesAmount) != 0 ?
                costAmount.divide(salesAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).stripTrailingZeros() : BigDecimal.ZERO;

        this.costRatio = new BigDecimal(costRatio.toPlainString());
        BigDecimal profitRatio = BigDecimal.ZERO.compareTo(salesAmount) != 0 ?
                profitAmount.divide(salesAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).stripTrailingZeros() : BigDecimal.ZERO;
        // 利润占比  毛利润/ 销售额 * 100
        this.profitRatio = new BigDecimal(profitRatio.toPlainString());
    }
    public static Function<CostProfitAnalyzeRankVO, BigDecimal> getByRankKey(String rankKey) {
        if(StringUtils.isBlank(rankKey)){
            return CostProfitAnalyzeRankVO::getGrossProfitAmount;
        }
        switch (rankKey){
            case "salesAmount":
                return CostProfitAnalyzeRankVO::getSalesAmount;
            case "salesCostAmount":
                return CostProfitAnalyzeRankVO::getSalesCostAmount;
            case "grossProfitAmount":
                return CostProfitAnalyzeRankVO::getGrossProfitAmount;
            case "costRatio":
                return CostProfitAnalyzeRankVO::getCostRatio;
            case "grossProfitRate":
                return CostProfitAnalyzeRankVO::getGrossProfitRate;
            case "profitRatio":
                return CostProfitAnalyzeRankVO::getProfitRatio;
            default:
                return CostProfitAnalyzeRankVO::getGrossProfitAmount;
        }
    }
}
