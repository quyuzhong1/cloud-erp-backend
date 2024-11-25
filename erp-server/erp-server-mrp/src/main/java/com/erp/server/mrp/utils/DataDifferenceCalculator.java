package com.erp.server.mrp.utils;

import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DataDifferenceCalculator {
    //欧氏距离
    public static final String EUCLIDEAN = "euclidean";
    //曼哈顿距离
    public static final String MANHATTAN = "manhattan";
    //余弦相似度
    public static final String COSINE = "cosine";


    // 计算欧氏距离
    public static BigDecimal calculateEuclideanDistance(List<BigDecimal> data1, List<BigDecimal> data2) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < data1.size(); i++) {
            BigDecimal diff = data1.get(i).subtract(data2.get(i));
            sum = sum.add(diff.pow(2));
        }
        return BigDecimal.valueOf(Math.sqrt(sum.doubleValue())).setScale(6, RoundingMode.HALF_UP);
    }

    // 计算曼哈顿距离
    public static BigDecimal calculateManhattanDistance(List<BigDecimal> data1, List<BigDecimal> data2) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < data1.size(); i++) {
            sum = sum.add(data1.get(i).subtract(data2.get(i)).abs());
        }
        return sum.setScale(6, RoundingMode.HALF_UP);
    }

    // 计算余弦相似度
    public static BigDecimal calculateCosineSimilarity(List<BigDecimal> data1, List<BigDecimal> data2) {
        BigDecimal dotProduct = BigDecimal.ZERO;
        BigDecimal normA = BigDecimal.ZERO;
        BigDecimal normB = BigDecimal.ZERO;

        for (int i = 0; i < data1.size(); i++) {
            dotProduct = dotProduct.add(data1.get(i).multiply(data2.get(i)));
            normA = normA.add(data1.get(i).pow(2));
            normB = normB.add(data2.get(i).pow(2));
        }

        BigDecimal normASqrt = BigDecimal.valueOf(Math.sqrt(normA.doubleValue()));
        BigDecimal normBSqrt = BigDecimal.valueOf(Math.sqrt(normB.doubleValue()));

        return dotProduct.divide(normASqrt.multiply(normBSqrt), 6, RoundingMode.HALF_UP);
    }

    // 找出与基准数据差异最小的前N组数据
    public static List<CalcSalesInfoDimDTO.LineDTO> findTopNSimilarData(
            List<CalcSalesInfoDimDTO.LineDTO> calcList, List<BigDecimal> baseData, String distanceType) {

        // 计算每个 LineDTO 的相似度
        calcList.forEach(line -> {
            BigDecimal similarity = calculateSimilarity(baseData, line.getQty(), distanceType);
            line.setSimilarity(similarity);
        });
        // 按相似度降序排序并取 topN
        return calcList.stream()
                .sorted(Comparator.comparing(CalcSalesInfoDimDTO.LineDTO::getSimilarity).reversed())
                .collect(Collectors.toList());
    }

    private static BigDecimal calculateSimilarity(List<BigDecimal> baseData, List<BigDecimal> qtyData, String distanceType) {
        switch (distanceType.toLowerCase()) {
            case EUCLIDEAN:
                return calculateEuclideanDistance(baseData, qtyData);
            case MANHATTAN:
                return calculateManhattanDistance(baseData, qtyData);
            case COSINE:
                // 使用 1 - 相似度值表示差异
                return BigDecimal.ONE.subtract(calculateCosineSimilarity(baseData, qtyData))
                        .setScale(6, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("未知的距离类型: " + distanceType);
        }
    }

    public static void main(String[] args) {
        // 示例数据
        List<CalcSalesInfoDimDTO.LineDTO> calcList = Arrays.asList(
                createLine("A", Arrays.asList(new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"))),
                createLine("B", Arrays.asList(new BigDecimal("15"), new BigDecimal("25"), new BigDecimal("35"))),
                createLine("C", Arrays.asList(new BigDecimal("5"), new BigDecimal("15"), new BigDecimal("25")))
        );

        List<BigDecimal> baseData = Arrays.asList(new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"));
        List<CalcSalesInfoDimDTO.LineDTO> result = findTopNSimilarData(calcList, baseData, "cosine");

        result.forEach(System.out::println);
    }

    private static CalcSalesInfoDimDTO.LineDTO createLine(String name, List<BigDecimal> qty) {
        CalcSalesInfoDimDTO.LineDTO line = new CalcSalesInfoDimDTO.LineDTO();
        line.setName(name);
        line.setQty(qty);
        return line;
    }
}