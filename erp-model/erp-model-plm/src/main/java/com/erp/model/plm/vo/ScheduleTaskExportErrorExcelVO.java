package com.erp.model.plm.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname
 * @Description TODO
 * @Date 2023-02-21 18:55
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ScheduleTaskExportErrorExcelVO extends ScheduleTaskExportExcelVO {



    @ColumnWidth(100)
    @ExcelProperty(value = "错误信息", index = 6)
    private String errorMsg;

}
