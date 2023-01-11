package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DateRefundRate {
    /**
     * 时间
     */
    private String dateTime;

    /**
     * 退款率
     */
    private BigDecimal refundRate;
}
