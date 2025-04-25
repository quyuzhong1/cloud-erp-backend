package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 其他出库单导入
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Data
public class SoReturnStockImportExcelDTO implements Serializable {

    /**
     * 退货客户
     */
    @ExcelProperty(value = "*退货客户", index = 0)
    @FieldValid(fieldName = "退货客户", isNotBlank = true, maxLength = 50)
    private String type;

    /**
     * 退货仓库
     */
    @ExcelProperty(value = "*退货仓库", index = 1)
    @FieldValid(fieldName = "退货仓库", isNotBlank = true, maxLength = 50)
    private String ware;

    /**
     * 入库日期
     */
    @ExcelProperty(value = "入库日期", index = 2)
    @FieldValid(fieldName = "入库日期")
    private String billDateStr;

    /**
     * 库存方向
     */
    @ExcelProperty(value = "单据类型", index = 3)
    @FieldValid(fieldName = "单据类型", enumClass = InventoryDirectionEnum.class)
    private String inventoryDirection;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 4)
    @FieldValid(fieldName = "SKU", maxLength = 32)
    private String receiverName;

    /**
     * 上架数量
     */
    @ExcelProperty(value = "*上架数量", index = 5)
    @FieldValid(fieldName = "上架数量", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String receiveOrgName;

    /**
     * 领料部门
     */
    @ExcelProperty(value = "仓位", index = 6)
    @FieldValid(fieldName = "仓位", maxLength = 50)
    private String deptName;

    /**
     * 退货金额
     */
    @ExcelProperty(value = "退货金额", index = 7)
    @FieldValid(fieldName = "退货金额")
    private String processApplyCode;

    /**
     * 含税退货金额
     */
    @ExcelProperty(value = "含税退货金额", index = 8)
    @FieldValid(fieldName = "含税退货金额")
    private String customerName;

    /**
     * 币种
     */
    @ExcelProperty(value = "币种", index = 9)
    @FieldValid(fieldName = "币种", maxLength = 50)
    private String skuNo;

    /**
     * 退货原因
     */
    @ExcelProperty(value = "*退货原因", index = 10)
    @FieldValid(fieldName = "退货原因",maxLength = 200)
    private String actualQtyStr;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 12)
    @FieldValid(fieldName = "备注", maxLength = 200)
    private String remark;


    /**
     * 错误数据
     */
    private String errorMsg;

}
