package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DateReturnOrderVO {
    /**
     * 时间
     */
    private String dateTime;

    /**
     * 退货率
     */
    private BigDecimal refundOrderRate;

    /**
     * 退货量
     */
    private Integer refundQuantity;
}
