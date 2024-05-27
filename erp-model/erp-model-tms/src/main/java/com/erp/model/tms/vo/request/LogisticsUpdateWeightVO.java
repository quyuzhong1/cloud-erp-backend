package com.erp.model.tms.vo.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

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
