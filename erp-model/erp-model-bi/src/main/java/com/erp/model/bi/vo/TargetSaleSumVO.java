package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售指标数据统计金额返回模型
 *
 * @Author Cloud
 * @Date 2022/12/13 14:28
 */
@Data
@NoArgsConstructor
public class TargetSaleSumVO {

    private BigDecimal value;

    public TargetSaleSumVO(BigDecimal value) {
        this.value = value;
    }
}
