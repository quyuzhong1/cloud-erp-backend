package com.erp.model.fms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 资产位置Excel导入DTO
 * @author wuht
 * @since 2025-10-13
 */
@Data
@NoArgsConstructor
public class AssetLocationExcelDTO implements Serializable {

    /**
     * 序号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号",isNotBlank = true )
    private String serialNumber;

    /**
     * 位置描述
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "位置描述", index = 1)
    private String description;

    /**
     * 地址
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*地址", index = 2)
    @FieldValid(fieldName = "*地址", isNotBlank = true, maxLength = 200)
    private String address;

    /**
     * 详细地址
     */
    @ColumnWidth(40)
    @ExcelProperty(value = "详细地址", index = 3)
    private String detailedAddress;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 4)
    @ColumnWidth(50)
    private String errorMsg;

    /**
     * 行号
     */
    @ExcelIgnore
    private Integer rowNum;

    /**
     * 创建人ID
     */
    @ExcelIgnore
    private String createUserId;

    /**
     * 创建人姓名
     */
    @ExcelIgnore
    private String createUserName;
}

