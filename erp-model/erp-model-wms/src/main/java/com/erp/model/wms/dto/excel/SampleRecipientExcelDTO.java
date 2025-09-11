package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 样品领用单Excel导入DTO
 * @author wuhaotian
 * @since 2025-08-22
 */
@Data
@NoArgsConstructor
public class SampleRecipientExcelDTO implements Serializable {

    /**
     * 序号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号",isNotBlank = true )
    private String serialNumber;

    /**
     * 领用日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*领用日期", index = 1)
    @FieldValid(fieldName = "*领用日期",isNotBlank = true)
    private String recipientDateStr;
    @ExcelIgnore
    private LocalDate recipientDate;

    /**
     * 用途
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*用途", index = 2)
    @FieldValid(fieldName = "*用途",isNotBlank = true)
    private String usageStr;
    @ExcelIgnore
    private String usage;

    /**
     * 发货仓库
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*发货仓库", index = 3)
    @FieldValid(fieldName = "*发货仓库",isNotBlank = true)
    private String warehouseName;
    @ExcelIgnore
    private String warehouseId;

    /**
     * 领用人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*领用人", index = 4)
    @FieldValid(fieldName = "*领用人",isNotBlank = true)
    private String userName;
    @ExcelIgnore
    private String userId;

    /**
     * 领用部门
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*领用部门", index = 5)
    @FieldValid(fieldName = "*领用部门",isNotBlank = true)
    private String deptName;
    @ExcelIgnore
    private String deptId;

    /**
     * 领料组织
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*领料组织", index = 6)
    @FieldValid(fieldName = "*领料组织",isNotBlank = true)
    private String pickOrgName;
    @ExcelIgnore
    private String pickOrgId;

    /**
     * 使用范围
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*使用范围", index = 7)
    @FieldValid(fieldName = "*使用范围",isNotBlank = true)
    private String usageScopeStr;
    @ExcelIgnore
    private String usageScope;
    /**
     * 使用方
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "使用方", index = 8)
    @FieldValid(fieldName = "使用方",isNotBlank = true)
    private String useUserName;
    @ExcelIgnore
    private String useUserId;


    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 9)
    @FieldValid(fieldName = "备注",maxLength =200)
    private String remark;

    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 10)
    @FieldValid(fieldName = "*SKU",isNotBlank = true)
    private String skuNo;
    @ExcelIgnore
    private String skuId;
    @ExcelIgnore
    private String productName;

    /**
     * 领用数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*领用数量", index = 11)
    @FieldValid(fieldName = "*领用数量",isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private String recipientQtyStr;
    @ExcelIgnore
    private Integer recipientQty;

    /**
     * 明细备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "明细备注", index = 12)
    @FieldValid(fieldName = "明细备注",maxLength =200)
    private String detailRemark;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 13)
    @ColumnWidth(50)
    private String errorMsg;
    /**
     * 行号
     */
    private Integer rowNum;
}
