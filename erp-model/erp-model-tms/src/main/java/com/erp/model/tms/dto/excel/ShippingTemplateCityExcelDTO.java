package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 运费模板城市导入DTO
 * @date 2023/11/9 10:13
 */
@Data
public class ShippingTemplateCityExcelDTO implements Serializable {

    /**
     * 国家
     */
    @ExcelProperty(value = "*国家")
    @FieldValid(fieldName = "国家",isNotBlank = true,maxLength = 50)
    private String  country;

    /**
     * 分区
     */
    @ExcelProperty(value = "*分区")
    @FieldValid(fieldName = "分区",isNotBlank = true,maxLength = 50)
    private String  region;

    /**
     * 城市名称
     */
    @ExcelProperty(value = "*城市名称")
    @FieldValid(fieldName = "城市名称",isNotBlank = true,maxLength = 50)
    private String city;

    /**
     * 错误信息
     */
    private String errorMsg;

}
