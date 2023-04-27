package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/18 20:19
 */
@Data
@NoArgsConstructor
public class ExportQcPersonnelExcelDTO {

    /**
     * 质检员
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检员", index = 0)
    private String qcUserName;

    /**
     * 质检单数量
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检单数量", index = 1)
    private Integer qcTotalQty;

    /**
     * 完成质检
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "完成质检", index = 2)
    private Integer qcFinishQty;

    /**
     * 待质检
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "待质检", index = 3)
    private Integer qcWaitQty;

    /**
     * 免检
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "免检", index = 4)
    private Integer qcFreeQty;

    /**
     * 质检及时
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检及时", index = 5)
    private Integer qcTimelyQty;

    /**
     * 质检超时（已完成）
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检超时（已完成）", index = 6)
    private Integer qcTimeOutQty;

    /**
     * 质检超时（未完成）
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "质检超时（未完成）", index = 7)
    private Integer unQcTimeOutQty;

}
