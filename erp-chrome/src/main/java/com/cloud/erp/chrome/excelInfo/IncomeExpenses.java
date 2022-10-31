package com.cloud.erp.chrome.excelInfo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname IncomeExpenses
 * @Description TODO
 * @Date 2022-08-23 18:04
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class IncomeExpenses {


    @ExcelProperty(index = 0)
    private String orderNo;

    @ExcelProperty(index = 1)
    private String tradeNo;
}
