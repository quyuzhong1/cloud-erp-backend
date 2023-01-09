package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

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
    private List<DateRefundOrderRateVO> dateRefundOrderRateVOList;

    /**
     * 退款率
     */
    private List<DateRefundRate> dateRefundRateList;
}
