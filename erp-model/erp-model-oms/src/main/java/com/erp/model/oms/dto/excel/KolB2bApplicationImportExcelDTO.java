package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.oms.enums.CustomerAddressTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * B2B寄样申请导入Excel实体
 * @author will
 * @date 2025/12/3 10:11
 */
@Data
@NoArgsConstructor
public class KolB2bApplicationImportExcelDTO implements Serializable {


    /**
     * 序号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号",isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String no;


    /**
     * 申请日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*申请日期", index = 1)
    @FieldValid(fieldName = "*申请日期",isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String dateStr;
    @ExcelIgnore
    private LocalDate date;


    /**
     * 寄样类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*寄样类型", index = 2)
    @FieldValid(fieldName = "*寄样类型",isNotBlank = true)
    private String type;

    /**
     * 客户
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*客户", index = 3)
    @FieldValid(fieldName = "*客户",isNotBlank = true)
    private String customerName;
    @ExcelIgnore
    private String customerId;

    /**
     * 申请说明
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "申请说明", index = 4)
    @FieldValid(fieldName = "申请说明",isNotBlank = true)
    private String applyRemark;

    /**
     * 申请人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*申请人", index = 5)
    @FieldValid(fieldName = "*申请人",isNotBlank = true)
    private String applyUserName;
    @ExcelIgnore
    private String applyUserId;

    /**
     * 申请部门
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "申请部门", index = 6)
    @FieldValid(fieldName = "申请部门")
    private String applyDeptName;
    @ExcelIgnore
    private String applyDeptId;

    /**
     * 收货人
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "收货人", index = 7)
    @FieldValid(fieldName = "收货人",maxLength = 64)
    private String receiverName;

    /**
     * 联系电话
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "联系电话", index = 8)
    @FieldValid(fieldName = "联系电话",maxLength = 64)
    private String telNumber;

    /**
     * 收货地址
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*收货地址", index = 9)
    @FieldValid(fieldName = "*收货地址",isNotBlank = true ,maxLength = 255)
    private String receiveAddress;

    /**
     * 地址类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*地址类型", index = 10)
    @FieldValid(fieldName = "*地址类型",isNotBlank = true ,maxLength = 32,enumClass = CustomerAddressTypeEnum.class)
    private String addressType;


    /**
     * 备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "备注", index = 11)
    @FieldValid(fieldName = "备注" ,maxLength = 255)
    private String remark;


    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 12)
    @FieldValid(fieldName = "*SKU",isNotBlank = true ,maxLength =64)
    private String skuNo;

    /**
     * 申请数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*申请数量", index = 13)
    @FieldValid(fieldName = "*申请数量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String qty;

    /**
     * 预计回片日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "预计回片日期", index = 14)
    @FieldValid(fieldName = "*预计回片日期",isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planFeedbackDateStr;
    @ExcelIgnore
    private LocalDate planFeedbackDate;

    /**
     * 项目名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "项目名称", index = 15)
    @FieldValid(fieldName = "项目名称",maxLength =64)
    private String projectTag;

    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "明细备注", index = 16)
    @FieldValid(fieldName = "明细备注",maxLength = 255)
    private String detailRemark;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 17)
    @ColumnWidth(50)
    private String  errorMsg = "";
}

