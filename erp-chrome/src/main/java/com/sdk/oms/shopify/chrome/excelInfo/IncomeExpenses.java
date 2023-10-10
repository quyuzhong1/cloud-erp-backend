package com.sdk.oms.shopify.chrome.excelInfo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname IncomeExpenses

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
