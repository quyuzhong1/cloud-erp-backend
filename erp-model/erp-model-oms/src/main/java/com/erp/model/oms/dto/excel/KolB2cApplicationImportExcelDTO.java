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
public class KolB2cApplicationImportExcelDTO implements Serializable {

    /**
     * 序号
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号", isNotBlank = true)
    private String no;

    /**
     * 申请日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*申请日期", index = 1)
    @FieldValid(fieldName = "*申请日期", isNotBlank = true)
    private String applyDateStr;
    @ExcelIgnore
    private LocalDate applyDate;

    /**
     * 寄样类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*寄样类型", index = 2)
    @FieldValid(fieldName = "*寄样类型", isNotBlank = true)
    private String sampleTypeName;
    @ExcelIgnore
    private String sampleType;

    /**
     * 店铺
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*店铺", index = 3)
    @FieldValid(fieldName = "*店铺", isNotBlank = true)
    private String shopName;
    @ExcelIgnore
    private String shopId;
    /**
     * 币别
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "*币别", index = 4)
    @FieldValid(fieldName = "*币别", isNotBlank = true)
    private String currencyName;
    @ExcelIgnore
    private String currency;

    /**
     * 业务类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*业务类型", index = 5)
    @FieldValid(fieldName = "*业务类型", isNotBlank = true)
    private String isInternationalName;
    @ExcelIgnore
    private Boolean isInternational;

    /**
     * 发货仓库
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "发货仓库", index = 6)
    @FieldValid(fieldName = "发货仓库")
    private String warehouseName;
    @ExcelIgnore
    private String warehouseId;

    /**
     * 物流渠道
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "物流渠道", index = 7)
    @FieldValid(fieldName = "物流渠道")
    private String logisticsChannelName;
    @ExcelIgnore
    private String logisticsChannelId;

    /**
     * 申请说明
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "申请说明", index = 8)
    @FieldValid(fieldName = "申请说明")
    private String applyRemark;

    /**
     * 申请人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*申请人", index = 9)
    @FieldValid(fieldName = "*申请人", isNotBlank = true)
    private String applyUserName;
    @ExcelIgnore
    private String applyUserId;

    /**
     * 申请部门
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "申请部门", index = 10)
    @FieldValid(fieldName = "申请部门")
    private String applyDeptName;
    @ExcelIgnore
    private String applyDeptId;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 11)
    @ColumnWidth(50)
    private String errorMsg = "";
}
