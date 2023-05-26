package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Classname: ExportTransferOutExcelDTO
 * @Description: 分布式调拨调出单导出excel
 * @CreateTime: 2023-05-26  12:11
 * @Author: zhangchunlin
 */
@Data
public class ExportTransferOutExcelDTO implements Serializable {

    /**
     * 调拨单号
     */
    @ExcelProperty(value = "调拨单号", index = 0)
    private String code;

    /**
     * 调拨方向
     */
    @ExcelProperty(value = "调拨方向", index = 1)
    private String transferDirectionName;

    /**
     * 单据状态
     */
    @ExcelProperty(value = "单据状态", index = 2)
    private String approveStatusName;

    /**
     * 作废状态（0未作废，1已作废）
     */
    @ExcelProperty(value = "作废状态", index = 3)
    private String invalidStatusName;

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 4)
    private String skuNo;

    /**
     * 产品名称
     */
    @ExcelProperty(value = "产品名称", index = 5)
    private String productName;


    /**
     * 调出日期
     */
    @ExcelProperty(value = "调出日期", index = 6)
    private LocalDate billDate;

    /**
     * 调出数量
     */
    @ExcelProperty(value = "调出数量", index = 7)
    private Integer qty;

    /**
     * 单位
     */
    @ExcelProperty(value = "单位", index = 8)
    private String unit;

    /**
     * 调出仓库
     */
    @ExcelProperty(value = "调出仓库", index = 9)
    private String outWarehouseName;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 10)
    private String remark;

    /**
     * 审核人（最新）
     */
    @ExcelProperty(value = "审核人（最新）", index = 11)
    private String approveUserName;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人", index = 12)
    private String createUserName;


    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 13)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}