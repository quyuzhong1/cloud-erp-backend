package com.erp.server.mrp.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
    public static List<DataDifference> findTopNSimilarData(
            List<List<BigDecimal>> dataSets, List<BigDecimal> baseData, String distanceType, int topN) {
        List<DataDifference> differences = new ArrayList<>();

        for (int i = 0; i < dataSets.size(); i++) {
            BigDecimal difference;
            switch (distanceType) {
                case EUCLIDEAN:
                    difference = calculateEuclideanDistance(baseData, dataSets.get(i));
                    break;
                case MANHATTAN:
                    difference = calculateManhattanDistance(baseData, dataSets.get(i));
                    break;
                case COSINE:
                    // 使用 1 - 相似度值表示差异
                    difference = BigDecimal.ONE.subtract(calculateCosineSimilarity(baseData, dataSets.get(i)))
                            .setScale(6, RoundingMode.HALF_UP);
                    break;
                default:
                    throw new IllegalArgumentException("未知的距离类型: " + distanceType);
            }
            // 将索引和差异值存储到结果列表中
            differences.add(new DataDifference(i, difference));
        }

        // 根据差异从小到大排序
        differences.sort(Comparator.comparing(DataDifference::getDifference));

        // 返回差异最小的前 topN 组数据
        return differences.subList(0, Math.min(topN, differences.size()));
    }

    public static void main(String[] args) {
        // 示例数据
        List<List<BigDecimal>> dataSets = new ArrayList<>();
        dataSets.add(Arrays.asList(BigDecimal.valueOf(1.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(3.0)));
        dataSets.add(Arrays.asList(BigDecimal.valueOf(2.0), BigDecimal.valueOf(3.0), BigDecimal.valueOf(4.0)));
        dataSets.add(Arrays.asList(BigDecimal.valueOf(1.5), BigDecimal.valueOf(2.5), BigDecimal.valueOf(3.5)));
        dataSets.add(Arrays.asList(BigDecimal.valueOf(5.0), BigDecimal.valueOf(5.0), BigDecimal.valueOf(5.0)));
        dataSets.add(Arrays.asList(BigDecimal.valueOf(0.9), BigDecimal.valueOf(2.1), BigDecimal.valueOf(3.0)));

        // 设置基准数据
        List<BigDecimal> baseData = Arrays.asList(BigDecimal.valueOf(1.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(3.0));

        // 选择距离类型: "euclidean", "manhattan", "cosine"
        String distanceType = "euclidean";
        int topN = 3;

        // 找出与基准数据差异最小的前 topN 组数据
        List<DataDifference> topSimilarData = findTopNSimilarData(dataSets, baseData, distanceType, topN);

        // 输出结果
        System.out.println("与基准数据差异最小的前 " + topN + " 组数据及其差异为（按差异从小到大排序）：");
        for (DataDifference data : topSimilarData) {
            System.out.println("数据组索引：" + data.getIndex() + "，差异：" + data.getDifference());
        }
    }

    // 用于存储数据组索引和差异值的类
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataDifference {
        private Integer index;
        private BigDecimal difference;
    }
}