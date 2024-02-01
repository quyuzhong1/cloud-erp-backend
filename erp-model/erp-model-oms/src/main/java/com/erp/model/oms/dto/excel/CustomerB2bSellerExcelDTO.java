package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author liuruipeng
 * @date 2024年02月01日 16:15
 */
@Data
public class CustomerB2bSellerExcelDTO {

    @ColumnWidth(20)
    @ExcelProperty(value = "客户编码", index = 0)
    private String code;

    /**
     * 客户名称
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "客户名称", index = 1)
    private String name;

    /**
     * 简称
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "简称", index = 2)
    private String shortName;

    /**
     * 原销售员名称
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "原销售员", index = 3)
    private String originSellerName;

    /**
     * 变更后销售员名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "变更后销售员", index = 4)
    private String changeSellerName;

    /**
     * 审核状态
     */
    @ExcelIgnore
    private String approveStatus;

    /**
     * 审核状态中文
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "审核状态", index = 5)
    private String approveStatusName;

    /**
     * 最新审核人
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "最新审核人", index = 6)
    private String approveUserName;

    /**
     * 创建人
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "创建人", index = 7)
    private String createUserName;

    /**
     * 创建时间
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "创建时间", index = 8)
    private LocalDateTime createTime;

    /**
     * 变更启用日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "变更启用日期", index = 9)
    private LocalDate startDate;
}
