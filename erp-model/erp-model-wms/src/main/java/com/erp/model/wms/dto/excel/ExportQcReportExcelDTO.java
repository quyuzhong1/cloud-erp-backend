package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *  质检报告导出
 * @author Lambda
 * @Classname ExportQcReportExcelDTO
 * @Description TODO
 * @Date 2023-04-21 18:58
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ExportQcReportExcelDTO  implements Serializable {


    /**
     * code
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "质检项", index = 0)
    private String qcReportName;

    /**
     * 质检内容
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检内容", index = 1)
    private String qcReportContent;




    /**
     * 质检内容
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "质检说明", index = 2)
    private String description;



    /**
     * 质检内容
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检结果", index = 3)
    private String resultDict;
}
