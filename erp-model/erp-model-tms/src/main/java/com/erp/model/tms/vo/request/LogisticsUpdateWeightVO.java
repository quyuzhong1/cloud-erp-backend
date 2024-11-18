package com.erp.model.tms.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 物流更新重量请求实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LogisticsUpdateWeightVO extends LogisticsQueryBaseVO implements Serializable {
    /**
     * 重量（g）
     */
    @NotNull(message = "重量不能为空")
    private BigDecimal weight;
}
