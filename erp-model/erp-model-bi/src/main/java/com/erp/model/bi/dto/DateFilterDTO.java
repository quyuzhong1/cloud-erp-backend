package com.erp.model.bi.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 日期
 * @Classname
 * @Description TODO
 * @Date 2023-01-06 11:10
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DateFilterDTO extends BiFilterDTO {

    /**
     * 时间类型：日: DAY; 周: WEEK; 月: MONTH; 季度: QUARTER; 年: YEAR
     */
    @StateEnumValue( strValues = {"DAY","MONTH","QUARTER","YEAR"} ,message = "时间类型有误")
    @NotNull(message = "时间类型不能为空")
    private String dateType;

}
