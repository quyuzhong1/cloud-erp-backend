package com.erp.model.bi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2022/12/30 10:34
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateCostVO {

    /**
     * 时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate groupDate;

    /**
     * 成本类型
     */
    private String costType;

    /**
     * 成本值
     */
    private BigDecimal costValue;
}
