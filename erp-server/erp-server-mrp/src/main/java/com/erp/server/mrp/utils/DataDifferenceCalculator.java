package com.erp.server.mrp.utils;

import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DataDifferenceCalculator {

    /**
     * 封装单个预测列 vs. 实际列的误差结果
     */
    @Getter
    @Setter
    public static class MetricsResult {
        /**
         * 标识该列预测的名称（可自定义，如 "模型A", "模型B"）
         */
        private String predLabel;

        // 常见误差指标
        private BigDecimal MAE;
        private BigDecimal MSE;
        private BigDecimal RMSE;
        private BigDecimal MAPE;  // 以 0.16 表示 16%
        private BigDecimal R2;

        // 归一化得分
        private BigDecimal MAEScore;
        private BigDecimal MSEScore;
        private BigDecimal RMSEScore;
        private BigDecimal MAPEScore;
        private BigDecimal R2Score;

        public MetricsResult(String predLabel) {
            this.predLabel = predLabel;
        }
    }

    /**
     * 将“越小越好”的误差映射到 (0,1]：
     * 误差=0 => 得分=1，误差越大 => 得分越接近 0
     */
    private static BigDecimal errorToScore(BigDecimal error) {
        if (error.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }
        return BigDecimal.ONE.divide(BigDecimal.ONE.add(error), 10, RoundingMode.HALF_UP);
    }


    /**
     * 核心方法：对【单列预测值】 vs. 【单列实际值】进行误差计算
     *
     * @param predicted 预测值列表
     * @param actual    实际值列表
     * @param label     给该预测列取一个名称
     * @return 封装在 MetricsResult 对象中
     */
    public static MetricsResult computeMetrics(List<BigDecimal> predicted, List<BigDecimal> actual, String label) {
        MetricsResult result = new MetricsResult(label);

        int n = predicted.size();
        if (n == 0 || n != actual.size()) {
            // 如果没有数据，或与 actual 大小不匹配，可按需处理
            return result;
        }

        BigDecimal sumAbsErr = BigDecimal.ZERO;
        BigDecimal sumSqErr = BigDecimal.ZERO;
        BigDecimal sumMAPE = BigDecimal.ZERO;

        // 1) 计算 MAE, MSE, MAPE
        for (int i = 0; i < n; i++) {
            BigDecimal x = predicted.get(i);
            BigDecimal y = actual.get(i);
            BigDecimal diff = x.subtract(y);
            sumAbsErr = sumAbsErr.add(diff.abs());
            sumSqErr = sumSqErr.add(diff.pow(2));

            if (y.abs().compareTo(new BigDecimal("1E-12")) < 0) {
                // 若实际值 y=0，可根据业务需求处理，这里简单+1
                sumMAPE = sumMAPE.add(BigDecimal.ONE);
            } else {
                sumMAPE = sumMAPE.add(diff.abs().divide(y.abs(), 10, RoundingMode.HALF_UP));
            }
        }

        BigDecimal mae = sumAbsErr.divide(new BigDecimal(n), 10, RoundingMode.HALF_UP);
        BigDecimal mse = sumSqErr.divide(new BigDecimal(n), 10, RoundingMode.HALF_UP);
        BigDecimal rmse = BigDecimal.valueOf(Math.sqrt(mse.doubleValue()));
        BigDecimal mape = sumMAPE.divide(new BigDecimal(n), 10, RoundingMode.HALF_UP);

        // 2) 计算 R^2
        BigDecimal ySum = actual.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal yMean = ySum.divide(new BigDecimal(n), 10, RoundingMode.HALF_UP);

        BigDecimal sst = BigDecimal.ZERO;
        for (BigDecimal val : actual) {
            BigDecimal tmp = val.subtract(yMean);
            sst = sst.add(tmp.pow(2));
        }
        BigDecimal sse = sumSqErr;
        BigDecimal r2 = BigDecimal.ZERO;
        if (sst.abs().compareTo(new BigDecimal("1E-12")) > 0) {
            r2 = BigDecimal.ONE.subtract(sse.divide(sst, 10, RoundingMode.HALF_UP));
        }

        // 3) 填写结果
        result.MAE = mae;
        result.MSE = mse;
        result.RMSE = rmse;
        result.MAPE = mape;
        result.R2 = r2;

        // 4) 归一化分数
        result.MAEScore = errorToScore(mae);
        result.MSEScore = errorToScore(mse);
        result.RMSEScore = errorToScore(rmse);
        result.MAPEScore = errorToScore(mape);

        // R^2 如果 < 0 => 0，否则取原值（也可根据需求改进）
        result.R2Score = r2.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : r2;

        return result;
    }

    /**
     * 扩展方法：对【多列预测值】 vs. 【单列实际值】分别进行比较
     *
     * @param actual      实际值列表
     */
    public static void compareMultiplePredictions(
            List<CalcSalesInfoDimDTO.LineDTO> calcList,
            List<BigDecimal> actual) {
        for (CalcSalesInfoDimDTO.LineDTO dto : calcList) {
            MetricsResult mr = computeMetrics(dto.getQty(), actual, dto.getName());
            dto.setSimilarity(mr.getMAPEScore());
        }
    }
}