package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class DateBarAndLineVO {
    /**
     * 时间
     */
    private List<String> dateTime;

    /**
     * 退货率
     */
    private List<BigDecimal> dateRefundOrderRateVOList;

    /**
     * 退货量
     */
    private List<Integer> dateRefundQuantityList;
}
