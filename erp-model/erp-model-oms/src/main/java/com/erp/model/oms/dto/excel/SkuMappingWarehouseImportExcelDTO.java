package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lambda
 * @Classname SkuMapingImportExcelDTO
 * @Date 2023-06-28 18:09
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuMappingWarehouseImportExcelDTO {


    /**
     * 库存sku
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "库存SKU", index = 0)
    @FieldValid(fieldName = "库存SKU", isNotBlank = true, maxLength = 100)
    private String warehouseSkuNo;

    /**
     * 库存产品名称
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "库存产品名称", index = 1)
    @FieldValid(fieldName = "库存产品名称", isNotBlank = true, maxLength = 200)
    private String warehouseProductName;
    /**
     * 三方仓商品条码
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "三方仓商品条码", index = 2)
    @FieldValid(fieldName = "三方仓商品条码", maxLength = 200)
    private String thirdBarcode;


    /**
     * 对照关系适用于该服务商所有仓库
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "对照关系适用于该服务商所有仓库", index = 3)
    @FieldValid(fieldName = "对照关系适用于该服务商所有仓库", isNotBlank = true, maxLength = 200)
    private String hasMappingAllStr;

    /**
     * 仓库名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓库名称", index = 4)
    private String warehouseName;

    /**
     * 服务商
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "服务商", index = 5)
    @FieldValid(fieldName = "服务商", maxLength = 200)
    private String platformName;


    /**
     * sku
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "SKU", index = 6)
    @FieldValid(fieldName = "sku", isNotBlank = true, maxLength = 200)
    private String skuNo;

    /**
     * 账号
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "账号", index = 7)
    private String account;

    /**
     * 平台状态
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "平台状态", index = 8)
    @FieldValid(fieldName = "平台状态")
    private String platformStatusName;

    /**
     * 错误信息
     */
    @ColumnWidth(100)
    @ExcelProperty(value = "错误数据", index = 9)
    private String errorMsg;

    @ExcelIgnore
    private String warehouseId;

    @ExcelIgnore
    private String dictPlatform = "";

    @ExcelIgnore
    private String dictPlatformName = "";

    public Boolean convertHasMappingAllStr() {
        if (null == this.getHasMappingAllStr()){
            return null;
        }
        if (this.getHasMappingAllStr().equals("是")) {
            return true;
        }
        if (this.getHasMappingAllStr().equals("否")) {
            return false;
        }
        return null;
    }
}
