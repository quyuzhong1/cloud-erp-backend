package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 季度销售额返回实体
 *
 * @Author Cloud
 * @Date 2022/12/20 17:50
 **/
@Data
@NoArgsConstructor
public class SalesCompletionInfoVO {

    /**
     * 排行
     */
    private Integer ranking;

    /**
     * 维度
     */
    private String dimension;

    /**
     * 品名
     */
    private String productName;
    /**
     * 销量
     */
    private Integer salesVolume;

    /**
     * 目标销量
     */
    private Integer targetVolume;
    /**
     * 销量完成率
     */
    private BigDecimal salesVolumeCompletionRate;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;
    /**
     * 目标销售额
     */
    private BigDecimal targetAmount;
    /**
     * 销售额完成率
     */
    private BigDecimal salesAmountCompletionRate;


    public SalesCompletionInfoVO(String key, BigDecimal targetAmount, Integer targetVolume, BigDecimal realAmount, Integer realVolume, String skuName) {
        this.dimension = key;
        this.targetAmount = null == targetAmount ? BigDecimal.ZERO : targetAmount.setScale(4, BigDecimal.ROUND_DOWN).stripTrailingZeros();
        this.targetVolume = null == targetVolume ? 0 : targetVolume;
        this.salesAmount = realAmount.setScale(4, BigDecimal.ROUND_DOWN).stripTrailingZeros();
        this.salesVolume = realVolume;
        this.salesVolumeCompletionRate = this.targetVolume == 0 ? BigDecimal.ZERO : new BigDecimal(this.salesVolume).divide(new BigDecimal(this.targetVolume), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).stripTrailingZeros();
        this.salesAmountCompletionRate = BigDecimal.ZERO.compareTo(this.targetAmount) == 0 ? BigDecimal.ZERO : this.salesAmount.divide(this.targetAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).stripTrailingZeros();
        this.productName = skuName;
    }
}
