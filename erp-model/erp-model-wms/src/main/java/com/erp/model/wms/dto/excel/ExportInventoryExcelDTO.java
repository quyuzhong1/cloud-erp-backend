package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @CreateTime: 2023-05-17  14:08
 * @Author: zhangchunlin
 */
@Data
public class ExportInventoryExcelDTO implements Serializable {

    /**
     * sku编号
     */
    @ExcelProperty(value = "SKU编号", index = 0)
    private String skuNo;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称[品名]", index = 1)
    private String productName;


    /**
     * spu型号
     */
    @ExcelProperty(value = "SPU型号", index = 2)
    private String spuNo;

    /**
     * 库存组织名称
     */
    @ExcelProperty(value = "库存组织", index = 3)
    private String orgName;

    /**
     * 销售状态名称
     */
    @ExcelProperty(value = "销售状态", index = 4)
    private String saleStateName;

    /**
     * 仓库名称
     */
    @ExcelProperty(value = "仓库名称", index = 5)
    private String warehouseName;


    /**
     * 实际库存
     */
    @ExcelProperty(value = "实际库存", index = 6)
    private Integer realQty;


    /**
     * 可用库存
     */
    @ExcelProperty(value = "可用库存", index = 7)
    private Integer usableQty;

    /**
     * 冻结库存
     */
    @ExcelProperty(value = "冻结库存", index = 8)
    private Integer frozenQty;

    /**
     * 在途库存
     */
    @ExcelProperty(value = "在途库存", index = 9)
    private Integer intransitQty;

    /**
     * 待检库存
     */
    @ExcelProperty(value = "待检库存", index = 10)
    private Integer waitqcQty;



}