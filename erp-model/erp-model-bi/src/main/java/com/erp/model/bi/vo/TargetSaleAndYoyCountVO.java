package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售指标数据统计数量返回模型-包含同比环比
 *
 * @Author Cloud
 * @Date 2022/12/13 14:28
 */
@Data
public class TargetSaleAndYoyCountVO {

    /**
     * 基础值
     */
    private Integer value;

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

    public TargetSaleAndYoyCountVO() {
        this.value = 0;
        this.ringRatio = BigDecimal.ZERO;
        this.yoyRatio = BigDecimal.ZERO;
        this.goalRate = null;
    }

    public TargetSaleAndYoyCountVO(TargetSaleCountVO currentVo, TargetSaleCountVO ringVo, TargetSaleCountVO yoyVo) {
        this.value = currentVo.getValue();
        Integer ringAmount = ringVo.getValue();
        BigDecimal currentAmount = new BigDecimal(currentVo.getValue());
        this.ringRatio = ringAmount  != 0 ? currentAmount.subtract(new BigDecimal(ringAmount))
                .divide(new BigDecimal(ringAmount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)) : BigDecimal.ZERO;
        Integer yoyAmount = yoyVo.getValue();
        this.yoyRatio = yoyAmount != 0 ? currentAmount.subtract(new BigDecimal(yoyAmount))
                .divide(new BigDecimal(yoyAmount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)) : BigDecimal.ZERO;
        this.goalRate = null;
    }
}
