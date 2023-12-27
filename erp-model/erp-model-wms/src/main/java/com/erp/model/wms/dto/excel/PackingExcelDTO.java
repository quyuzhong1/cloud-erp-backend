package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import jnr.ffi.annotations.In;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 导入装箱信息Excel数据体
 */
@Data
@NoArgsConstructor
public class PackingExcelDTO implements Serializable {

    /**
     * 行号
     */
    @ExcelIgnore
    private Integer rowNum;

    /**
     * 发货单号
     */
    @ExcelProperty(value = "发货单号", index = 0)
    @FieldValid(fieldName = "发货单号", isNotBlank = true)
    private String code;

    /**
     * skuId
     */
    @ExcelIgnore
    private String skuId;

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 1)
    @FieldValid(fieldName = "sku", isNotBlank = true)
    private String sku;

    /**
     * 发货箱号
     */
    @ExcelProperty(value = "发货箱号", index = 2)
    @FieldValid(fieldName = "发货箱号", isNotBlank = true)
    private Integer boxNo;
    /**
     * 单箱数量
     */
    @ExcelProperty(value = "单箱数量", index = 3)
    @FieldValid(fieldName = "单箱数量", isNotBlank = true)
    private Integer singleBoxQuantity;

    /**
     * 每箱实重（KG）
     */
    @ExcelProperty(value = "每箱实重（KG）", index = 4)
    private BigDecimal singleBoxWeight;

    /**
     * 每箱体积长（CM)
     */
    @ExcelProperty(index = 5)
    private BigDecimal singleBoxLength;


    /**
     * 每箱体积宽（CM)
     */
    @ExcelProperty(index = 6)
    private BigDecimal singleBoxWidth;

    /**
     * 每箱体积高（CM)
     */
    @ExcelProperty(value = "每箱体积高",index = 7)
    private BigDecimal singleBoxHeight;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 8)
    private String errorMsg;
}