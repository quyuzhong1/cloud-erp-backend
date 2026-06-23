package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售出库导出字段定义
 */
@Data
@NoArgsConstructor
public class SoOutstockExportExcelDTO implements Serializable {

    @ExcelProperty(value = "出库单号", index = 0)
    private String code;

    @ExcelProperty(value = "物流单号", index = 1)
    private String trackNos;

    @ExcelProperty(value = "销售单号", index = 2)
    private String soCode;

    @ExcelProperty(value = "平台订单号", index = 3)
    private String platformCode;

    @ExcelProperty(value = "第三方单据编号", index = 4)
    private String thirdCode;

    @ExcelProperty(value = "发货通知单号", index = 5)
    private String sourceCode;

    @ExcelProperty(value = "单据类型", index = 6)
    private String orderTypeName;

    @ExcelProperty(value = "销售平台", index = 7)
    private String dictPlatformName;

    @ExcelProperty(value = "单据状态", index = 8)
    private String approveStatusName;

    @ExcelProperty(value = "作废状态", index = 9)
    private String invalidStatusName;

    @ExcelProperty(value = "客户", index = 10)
    private String customerName;

    @ExcelProperty(value = "收货国家", index = 11)
    private String countryName;

    @ExcelProperty(value = "客户订单号", index = 12)
    private String customerOrderNo;

    @ExcelProperty(value = "销售员", index = 13)
    private String sellerName;

    @ExcelProperty(value = "销售部门", index = 14)
    private String salesDeptName;

    @ExcelProperty(value = "库存组织", index = 15)
    private String warehouseOrgName;

    @ExcelProperty(value = "销售组织", index = 16)
    private String salesOrgName;

    @ExcelProperty(value = "sku", index = 17)
    private String skuNo;

    @ExcelProperty(value = "产品名称", index = 18)
    private String productName;

    @ExcelProperty(value = "销售单价", index = 19)
    private BigDecimal price;

    @ExcelProperty(value = "销售单价（本位币）", index = 20)
    private BigDecimal cnyPrice;

    @ExcelProperty(value = "含税单价", index = 21)
    private BigDecimal taxPrice;

    @ExcelProperty(value = "含税单价（本位币）", index = 22)
    private BigDecimal cnyTaxPrice;

    @ExcelProperty(value = "价税合计", index = 23)
    private BigDecimal taxAmount;

    @ExcelProperty(value = "价税合计（本位币）", index = 24)
    private BigDecimal allAmountLocalCurrency;

    @ExcelProperty(value = "应发数量", index = 25)
    private Integer planQty;

    @ExcelProperty(value = "实发数量", index = 26)
    private Integer actualQty;

    @ExcelProperty(value = "库存单位", index = 27)
    private String unit;

    @ExcelProperty(value = "出货仓库", index = 28)
    private String warehouseName;

    @ExcelProperty(value = "虚拟仓", index = 29)
    private String virtualWarehouseName;

    @ExcelProperty(value = "出库日期", index = 30)
    private LocalDate billDate;

    @ExcelProperty(value = "预计发货日期", index = 31)
    private LocalDate planDeliveryDate;

    @ExcelProperty(value = "完成打包日期", index = 32)
    private LocalDate packDate;

    @ExcelProperty(value = "实际发货日期", index = 33)
    private LocalDateTime actualDeliveryDate;

    @ExcelProperty(value = "审核人", index = 34)
    private String approveUserName;

    @ExcelProperty(value = "单据备注", index = 35)
    private String remark;

    @ExcelProperty(value = "明细备注", index = 36)
    private String detailRemark;

    @ExcelProperty(value = "客户备注", index = 37)
    private String customerRemark;

    @ExcelProperty(value = "客户PO", index = 38)
    private String customerPO;

    @ExcelProperty(value = "创建人", index = 39)
    private String createUserName;

    @ExcelProperty(value = "创建时间", index = 40)
    private LocalDateTime createTime;
}
