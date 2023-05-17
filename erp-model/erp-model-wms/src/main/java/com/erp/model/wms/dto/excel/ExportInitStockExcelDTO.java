package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Classname: InitStockExportExcelDTO
 * @Description: TODO
 * @CreateTime: 2023-05-11  12:11
 * @Author: zhangchunlin
 */
@Data
public class ExportInitStockExcelDTO implements Serializable {

    /**
     * 单据编号
     */
    @ExcelProperty(value = "单据编号", index = 0)
    private String code;

    /**
     * sku编号
     */
    @ExcelProperty(value = "SKU编号", index = 1)
    private String skuNo;


    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称[品名]", index = 2)
    private String productName;

    /**
     * 单据状态
     */
    @ExcelProperty(value = "单据状态", index = 3)
    private String approveStatusName;

    /**
     * 作废状态（0未作废，1已作废）
     */
    @ExcelProperty(value = "作废状态", index = 4)
    private String invalidStatusName;

    /**
     * spu型号
     */
    @ExcelProperty(value = "SPU型号", index = 5)
    private String spuNo;

    /**
     * 品牌名称
     */
    @ExcelProperty(value = "品牌", index = 6)
    private String brandName;

    /**
     * 库存组织名称
     */
    @ExcelProperty(value = "库存组织", index = 7)
    private String orgName;

    /**
     * 销售状态名称
     */
    @ExcelProperty(value = "销售状态", index = 8)
    private String saleStatusName;

    /**
     * 仓库名称
     */
    @ExcelProperty(value = "仓库名称", index = 9)
    private String warehouseName;

    /**
     * 期初数量
     */
    @ExcelProperty(value = "期初数量", index = 10)
    private Integer qty;


    /**
     * 仓位
     */
    @ExcelProperty(value = "仓位", index = 11)
    private String warehouseLocation;

}