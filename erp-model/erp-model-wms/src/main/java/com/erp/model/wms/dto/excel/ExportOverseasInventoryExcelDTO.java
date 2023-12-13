package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导出海外仓Excel数据体
 * @author Jim
 * @date: 2023-11-21
 */
@Data
@NoArgsConstructor
public class ExportOverseasInventoryExcelDTO implements Serializable {

    /**
     * 仓库名称
     */
    @ExcelProperty(value = "仓库名称", index = 0)
    private String name;

    /**
     * 库存sku
     */
    @ExcelProperty(value = "库存sku", index = 1)
    private String platformSku;

    /**
     * 库存产品名称
     */
    @ExcelProperty(value = "库存产品名称", index = 2)
    private String platformSkuName;

    /**
     * ERP的SKU
     */
    @ExcelProperty(value = "SKU", index = 3)
    private String skuNo;

    /**
     * ERP系统产品名称
     */
    @ExcelProperty(value = "产品名称", index = 4)
    private String productName;

    /**
     * 发货在途数量
     */
    @ExcelProperty(value = "发货在途数量", index = 5)
    private Integer deliverOnwayQty;

    /**
     * 待上架数量
     */
    @ExcelProperty(value = "待上架数量", index = 6)
    private Integer pendingQty;

    /**
     * 可售数量
     */
    @ExcelProperty(value = "可售数量", index = 7)
    private Integer sellableQty;

    /**
     * 不可售数量
     */
    @ExcelProperty(value = "不可售数量", index = 8)
    private Integer unsellableQty;

    /**
     * 待出库数量
     */
    @ExcelProperty(value = "待出库数量", index = 9)
    private Integer reservedQty;

    /**
     * 尾程在途
     */
    @ExcelProperty(value = "尾程在途", index = 10)
    private Integer onwayQty;

    /**
     * 缺货数量
     */
    @ExcelProperty(value = "缺货数量", index = 11)
    private Integer lackQty;

    /**
     * 冻结数量
     */
    @ExcelProperty(value = "冻结数量", index = 12)
    private Integer frozenQty;

    /**
     * 历史出库数量
     */
    @ExcelProperty(value = "历史出库数量", index = 13)
    private Integer shippedQty;

    /**
     * 平台下载更新时间 (更新时间)
     */
    @ExcelProperty(value = "更新时间", index = 14)
    private LocalDateTime downloadTime;


}