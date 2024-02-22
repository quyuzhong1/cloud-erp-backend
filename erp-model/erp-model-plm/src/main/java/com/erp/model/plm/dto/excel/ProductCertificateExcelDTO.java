package com.erp.model.plm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 产品认证
 * @author Will
 * @date: 2024/2/20 10:02
 */
@Data
public class ProductCertificateExcelDTO implements Serializable {

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 0)
    @FieldValid(fieldName = "SKU",isNotBlank = true,maxLength = 64)
    private String skuNo;

    /**
     * 证书类型
     */
    @ExcelProperty(value = "*证书类型", index = 1)
    @FieldValid(fieldName = "证书类型",isNotBlank = true)
    private String typeName;

    /**
     * 证书项目
     */
    @ExcelProperty(value = "*证书项目", index = 2)
    @FieldValid(fieldName = "证书项目",isNotBlank = true)
    private String dictProjectName;

    /**
     * 文件名称
     */
    @ExcelProperty(value = "*文件名称", index = 3)
    @FieldValid(fieldName = "文件名称",isNotBlank = true,maxLength = 255)
    private String pathUrl;

    /**
     * 有效期
     */
    @ExcelProperty(value = "有效期", index = 4)
    @FieldValid(fieldName = "有效期",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String certificateValidTimeStr;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 5)
    @FieldValid(fieldName = "备注",maxLength = 255)
    private String remark;

    /**
     * 错误信息
     */
    private String errorMsg;
}
