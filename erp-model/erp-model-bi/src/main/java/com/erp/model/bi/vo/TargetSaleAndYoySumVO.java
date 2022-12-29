package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售指标数据统计金额返回模型-包含同比环比
 *
 * @Author Cloud
 * @Date 2022/12/13 14:28
 */
@Data
public class TargetSaleAndYoySumVO {

    private BigDecimal value;

    /**
     * 环比
     */
    private BigDecimal ringRatio;
    /**
     * 同比
     */
    private BigDecimal yoyRatio;

    /**
     * 目标达成率
     */
    private BigDecimal goalRate;

    public TargetSaleAndYoySumVO() {
        this.value = BigDecimal.ZERO;
        this.ringRatio = BigDecimal.ZERO;
        this.yoyRatio = BigDecimal.ZERO;
        this.goalRate = null;
    }

    public TargetSaleAndYoySumVO(TargetSaleSumVO currentVo, TargetSaleSumVO ringVo, TargetSaleSumVO yoyVo) {
        this.value = currentVo.getValue();
        BigDecimal ringAmount = ringVo.getValue();
        this.ringRatio = BigDecimal.ZERO.compareTo(ringAmount)  != 0 ? currentVo.getValue().subtract(ringAmount)
                .divide(ringAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)) : BigDecimal.ZERO;
        BigDecimal yoyAmount = yoyVo.getValue();
        this.yoyRatio = BigDecimal.ZERO.compareTo(yoyAmount)  != 0 ? currentVo.getValue().subtract(yoyAmount)
                .divide(yoyAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)) : BigDecimal.ZERO;
        this.goalRate = null;
    }
}
