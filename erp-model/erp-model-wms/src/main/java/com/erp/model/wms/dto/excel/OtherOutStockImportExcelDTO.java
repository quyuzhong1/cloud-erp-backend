package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
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
public class OtherOutStockImportExcelDTO implements Serializable {

    /**
     * 明细id
     */
    @ExcelIgnore
    private String detailId;

    /**
     * 业务类型
     */
    @ExcelProperty(value = "*业务类型", index = 0)
    @FieldValid(fieldName = "业务类型", isNotBlank = true, maxLength = 50)
    private String type;

    /**
     * 出库日期[不填默认今天]
     */
    @ExcelProperty(value = "*出库日期[不填默认今天]", index = 1)
    @FieldValid(fieldName = "出库日期[不填默认今天]", formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String billDateStr;

    /**
     * 发货仓库
     */
    @ExcelProperty(value = "*发货仓库", index = 2)
    @FieldValid(fieldName = "发货仓库", isNotBlank = true, maxLength = 50)
    private String warehouseName;

    /**
     * 库存方向
     */
    @ExcelProperty(value = "*库存方向", index = 3)
    @FieldValid(fieldName = "库存方向", isNotBlank = true, enumClass = InventoryDirectionEnum.class)
    private String inventoryDirection;

    /**
     * 领料人
     */
    @ExcelProperty(value = "领料人", index = 4)
    @FieldValid(fieldName = "领料人", maxLength = 32)
    private String receiverName;

    /**
     * 领料组织
     */
    @ExcelProperty(value = "*领料组织", index = 5)
    @FieldValid(fieldName = "领料组织", isNotBlank = true, maxLength = 50)
    private String receiveOrgName;

    /**
     * 领料部门
     */
    @ExcelProperty(value = "*领料部门", index = 6)
    @FieldValid(fieldName = "领料部门", isNotBlank = true, maxLength = 50)
    private String deptName;

    /**
     * 流程申请单号
     */
    @ExcelProperty(value = "*流程申请单号", index = 7)
    @FieldValid(fieldName = "流程申请单号", isNotBlank = true, maxLength = 50)
    private String processApplyCode;

    /**
     * 客户名称
     */
    @ExcelProperty(value = "客户名称", index = 8)
    @FieldValid(fieldName = "客户名称", maxLength = 32)
    private String customerName;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 9)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 50)
    private String skuNo;

    /**
     * 实发数量
     */
    @ExcelProperty(value = "*实发数量", index = 10)
    @FieldValid(fieldName = "实发数量",  isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String actualQtyStr;

    /**
     * 仓位
     */
    @ExcelProperty(value = "仓位", index = 11)
    @FieldValid(fieldName = "仓位", maxLength = 200)
    private String warehouseLocation;

    /**
     * 出库备注
     */
    @ExcelProperty(value = "*出库备注", index = 12)
    @FieldValid(fieldName = "出库备注", maxLength = 200)
    private String remark;

    /**
     * 出库类型
     */
    @ExcelProperty(value = "*出库类型", index = 13)
    @FieldValid(fieldName = "出库类型", isNotBlank = true, maxLength = 50)
    private String outType;

    /**
     * 错误数据
     */
    private String errorMsg;

}
