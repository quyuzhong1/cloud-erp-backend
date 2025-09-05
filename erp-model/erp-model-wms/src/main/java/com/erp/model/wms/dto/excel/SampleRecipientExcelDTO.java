package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 样品领用单Excel导入DTO
 * @author wuhaotian
 * @since 2025-08-22
 */
@Data
public class SampleRecipientExcelDTO {

    /**
     * 序号
     */
    @ExcelProperty(value = "序号", index = 0)
    private String serialNumber;

    /**
     * 领用日期
     */
    @ExcelProperty(value = "领用日期", index = 1)
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate recipientDate;

    /**
     * 用途
     */
    @ExcelProperty(value = "用途", index = 2)
    private String usage;

    /**
     * 发货仓库
     */
    @ExcelProperty(value = "发货仓库", index = 3)
    private String warehouseName;
    @ExcelIgnore
    private String warehouseId;

    /**
     * 领用人
     */
    @ExcelProperty(value = "领用人", index = 4)
    private String userName;
    @ExcelIgnore
    private String userId;

    /**
     * 领用部门
     */
    @ExcelProperty(value = "领用部门", index = 5)
    private String deptName;
    @ExcelIgnore
    private String deptId;

    /**
     * 领料组织
     */
    @ExcelProperty(value = "领料组织", index = 6)
    private String pickOrgName;
    @ExcelIgnore
    private String pickOrgId;

    /**
     * 使用范围
     */
    @ExcelProperty(value = "使用范围", index = 7)
    private String usageScope;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 8)
    private String remark;

    /**
     * SKU
     */
    @ExcelProperty(value = "SKU", index = 9)
    private String skuNo;
    @ExcelIgnore
    private String skuId;
    @ExcelIgnore
    private String productName;

    /**
     * 领用数量
     */
    @ExcelProperty(value = "领用数量", index = 10)
    private Integer recipientQty;

    /**
     * 明细备注
     */
    @ExcelProperty(value = "备注", index = 11)
    private String detailRemark;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 行号
     */
    private Integer rowNum;
}
