package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 出入库流水导出Excel
 * @CreateTime: 2023-05-18  14:44
 * @Author: zhangchunlin
 */
@Data
public class ExportTransactionFlowDTO implements Serializable {

    /**
     * 出入库时间
     */
    @ExcelProperty(value = "出入库时间", index = 0)
    private LocalDateTime tradeTime;

    /**
     * 单据日期（业务日期）
     */
    @ExcelProperty(value = "业务日期", index = 1)
    private LocalDate billDate;

    /**
     * 单据名称
     */
    @ExcelProperty(value = "单据名称", index = 2)
    private String sourceTypeName;

    /**
     * 单据编号
     */
    @ExcelProperty(value = "单据编号", index = 3)
    private String sourceCode;

    /**
     * 操作类型
     */
    @ExcelProperty(value = "操作类型", index = 4)
    private String operationModeName;

    /**
     * 库尊组织名称
     */
    @ExcelProperty(value = "库尊组织", index = 5)
    private String orgName;

    /**
     * 仓库名称
     */
    @ExcelProperty(value = "仓库名称", index = 6)
    private String warehouseName;

    /**
     * sku编号
     */
    @ExcelProperty(value = "SKU", index = 7)
    private String skuNo;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称[品名]", index = 8)
    private String productName;

    /**
     * spu编号
     */
    @ExcelProperty(value = "SPU型号", index = 9)
    private String spuNo;

    /**
     * 仓位
     */
    @ExcelProperty(value = "仓位", index = 10)
    private String warehouseLocation;

    /**
     * 库存状态名称
     */
    @ExcelProperty(value = "库存状态", index = 11)
    private String inventoryStatusName;

    /**
     * 出入库数量
     */
    @ExcelProperty(value = "出入库数量", index = 12)
    private Integer qty;

    /**
     * 操作后库存数量
     */
    @ExcelProperty(value = "操作后库存", index = 13)
    private Integer afterQty;

    /**
     * 批次日期
     */
    @ExcelProperty(value = "批次日期", index = 14)
    private LocalDate instockBatchDate;

}