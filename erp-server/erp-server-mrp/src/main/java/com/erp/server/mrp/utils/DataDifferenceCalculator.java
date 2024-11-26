package com.erp.server.mrp.utils;

import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DataDifferenceCalculator {
    // 计算方式的枚举类型
    public enum CalculationType {
        EUCLIDEAN,
        MANHATTAN,
        COSINE
    }


    // 计算欧氏距离
    private static BigDecimal calculateEuclideanDistance(BigDecimal[] data1, BigDecimal[] data2) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < data1.length; i++) {
            BigDecimal diff = data1[i].subtract(data2[i]);
            sum = sum.add(diff.pow(2));
        }
        return sqrt(sum);
    }

    // 计算曼哈顿距离
    private static BigDecimal calculateManhattanDistance(BigDecimal[] data1, BigDecimal[] data2) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < data1.length; i++) {
            sum = sum.add(data1[i].subtract(data2[i]).abs());
        }
        return sum;
    }

    // 计算余弦相似度
    private static BigDecimal calculateCosineSimilarity(BigDecimal[] data1, BigDecimal[] data2) {
        BigDecimal dotProduct = BigDecimal.ZERO;
        BigDecimal normA = BigDecimal.ZERO;
        BigDecimal normB = BigDecimal.ZERO;

        for (int i = 0; i < data1.length; i++) {
            dotProduct = dotProduct.add(data1[i].multiply(data2[i]));
            normA = normA.add(data1[i].pow(2));
            normB = normB.add(data2[i].pow(2));
        }

        normA = sqrt(normA);
        normB = sqrt(normB);

        if (normA.compareTo(BigDecimal.ZERO) == 0 || normB.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // 防止除零错误
        }

        return dotProduct.divide(normA.multiply(normB), 10, RoundingMode.HALF_UP);
    }

    // 计算平方根
    private static BigDecimal sqrt(BigDecimal value) {
        BigDecimal x = BigDecimal.valueOf(Math.sqrt(value.doubleValue()));
        return x.setScale(10, RoundingMode.HALF_UP);
    }

    // 动态计算上下界
    private static BigDecimal[] calculateMinValues(BigDecimal[] baseData, BigDecimal factor) {
        BigDecimal[] minValues = new BigDecimal[baseData.length];
        for (int i = 0; i < baseData.length; i++) {
            BigDecimal minValue = baseData[i].subtract(baseData[i].multiply(factor)).max(BigDecimal.ZERO);
            minValues[i] = minValue;
        }
        return minValues;
    }

    // 校验倍数
    private static void validateFactor(BigDecimal factor) {
        if (factor.compareTo(BigDecimal.ZERO) < 0 || factor.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("倍数必须在0到100之间");
        }
        if (factor.scale() > 4) {
            throw new IllegalArgumentException("倍数小数位不能超过4位");
        }
    }

    private static BigDecimal[] calculateMaxValues(BigDecimal[] baseData, BigDecimal factor) {
        BigDecimal[] maxValues = new BigDecimal[baseData.length];
        for (int i = 0; i < baseData.length; i++) {
            BigDecimal maxValue = baseData[i].add(baseData[i].multiply(factor));
            maxValues[i] = maxValue;
        }
        return maxValues;
    }

    // 计算与基准数据的吻合率
    public static List<CalcSalesInfoDimDTO.LineDTO> calculateMatchRates(
            List<CalcSalesInfoDimDTO.LineDTO> calcList, List<BigDecimal> baseData, BigDecimal factor, CalculationType type) {

        validateFactor(factor);

        BigDecimal[] baseArray = baseData.toArray(new BigDecimal[0]);
        BigDecimal[] minValues = calculateMinValues(baseArray, factor);
        BigDecimal[] maxValues = calculateMaxValues(baseArray, factor);

        BigDecimal maxEuclideanDistance = calculateEuclideanDistance(minValues, maxValues);
        BigDecimal maxManhattanDistance = calculateManhattanDistance(minValues, maxValues);

        List<CalcSalesInfoDimDTO.LineDTO> results = new ArrayList<>();

        for (CalcSalesInfoDimDTO.LineDTO line : calcList) {
            BigDecimal[] dataArray = line.getQty().toArray(new BigDecimal[0]);
            if (type == CalculationType.EUCLIDEAN) {
                BigDecimal euclideanDistance = calculateEuclideanDistance(baseArray, dataArray);
                BigDecimal euclideanMatchRate = BigDecimal.ONE.subtract(
                        euclideanDistance.divide(maxEuclideanDistance, 10, RoundingMode.HALF_UP));
                line.setSimilarity(euclideanMatchRate);
            }
            if (type == CalculationType.MANHATTAN) {
                BigDecimal manhattanDistance = calculateManhattanDistance(baseArray, dataArray);
                BigDecimal manhattanMatchRate = BigDecimal.ONE.subtract(
                        manhattanDistance.divide(maxManhattanDistance, 10, RoundingMode.HALF_UP));
                line.setSimilarity(manhattanMatchRate);
            }
            if (type == CalculationType.COSINE) {
                BigDecimal cosineSimilarity = calculateCosineSimilarity(baseArray, dataArray);
                line.setSimilarity(cosineSimilarity);
            }
            results.add(line);
        }

        results.sort(getComparator());
        return results;
    }


    private static Comparator<CalcSalesInfoDimDTO.LineDTO> getComparator() {
        return (a, b) -> b.getSimilarity().compareTo(a.getSimilarity());
    }

    public static void main(String[] args) {
        List<CalcSalesInfoDimDTO.LineDTO> calcList = Arrays.asList(
                createLine("A", Arrays.asList(new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"))),
                createLine("B", Arrays.asList(new BigDecimal("15"), new BigDecimal("25"), new BigDecimal("35"))),
                createLine("C", Arrays.asList(new BigDecimal("5"), new BigDecimal("15"), new BigDecimal("25")))
        );
        List<BigDecimal> baseData = Arrays.asList(new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("3.0"));
        BigDecimal factor = new BigDecimal("1.0");
        CalculationType type = CalculationType.COSINE;

        List<CalcSalesInfoDimDTO.LineDTO> results = calculateMatchRates(calcList, baseData, factor, type);

        results.forEach(result -> System.out.printf(
                "模板名称：%s，相似度：%.2f%%%n",
                result.getName(),
                result.getSimilarity().multiply(new BigDecimal("100"))
        ));
    }

    private static CalcSalesInfoDimDTO.LineDTO createLine(String name, List<BigDecimal> qty) {
        CalcSalesInfoDimDTO.LineDTO line = new CalcSalesInfoDimDTO.LineDTO();
        line.setName(name);
        line.setQty(qty);
        return line;
    }
}