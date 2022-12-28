package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class ReturnOrderFilterDTO extends BiFilterDTO {

    /**
     * 时间类型：日: DAY; 周: WEEK; 月: MONTH; 季度: QUARTER; 年: YEAR
     */
    @NotNull(message = "时间类型不能为空")
    private String dateType;
}
