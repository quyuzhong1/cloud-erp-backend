package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author jack
 * @Date 2025-12-08
 */
@Data
@NoArgsConstructor
public class KolB2cApplicationDetailImportExcelDTO implements Serializable {
    /**
     * 序号
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号", isNotBlank = true)
    private String no;

    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 1)
    @FieldValid(fieldName = "*SKU", isNotBlank = true)
    private String skuNo;
    @ExcelIgnore
    private String skuId;

    /**
     * 达人昵称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*达人昵称", index = 2)
    @FieldValid(fieldName = "*达人昵称", isNotBlank = true)
    private String nickname;
    @ExcelIgnore
    private String partnerId;

    /**
     * 申请数量
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "*申请数量", index = 3)
    @FieldValid(fieldName = "*申请数量", isNotBlank = true)
    private Integer applyQty;

    /**
     * 预计回片日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "预计回片日期", index = 4)
    @FieldValid(fieldName = "预计回片日期")
    private String planFeedbackDateStr;
    @ExcelIgnore
    private LocalDate planFeedbackDate;

    /**
     * 项目名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "项目名称", index = 5)
    @FieldValid(fieldName = "项目名称")
    private String projectTagName;
    @ExcelIgnore
    private String projectTag;

    /**
     * 备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "备注", index = 6)
    @FieldValid(fieldName = "备注")
    private String remark;

    /**
     * 品牌id
     */
    @ExcelIgnore
    private String brandId;
    /**
     * 品牌
     */
    @ExcelIgnore
    private String brandName;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 7)
    @ColumnWidth(50)
    private String errorMsg = "";

}
