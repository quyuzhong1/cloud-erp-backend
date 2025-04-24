package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.wms.enums.InstockTypeEnum;
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
public class OtherInStockImportExcelDTO implements Serializable {

    /**
     * 明细id
     */
    @ExcelIgnore
    private String detailId;

    /**
     * 出库类型
     */
    @ExcelProperty(value = "*入库类型", index = 0)
    @FieldValid(fieldName = "入库类型", isNotBlank = true, maxLength = 32, enumClass = InstockTypeEnum.class)
    private String type;

    /**
     * 入库日期[不填默认今天]
     */
    @ExcelProperty(value = "*入库日期[不填默认今天]", index = 1)
    @FieldValid(fieldName = "入库日期[不填默认今天]", formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String billDateStr;

    /**
     * 收货仓库
     */
    @ExcelProperty(value = "*收货仓库", index = 2)
    @FieldValid(fieldName = "收货仓库", isNotBlank = true, maxLength = 50)
    private String warehouseName;

    /**
     * 库存方向
     */
    @ExcelProperty(value = "*库存方向", index = 3)
    @FieldValid(fieldName = "库存方向", isNotBlank = true, maxLength = 50, enumClass = InventoryDirectionEnum.class)
    private String inventoryDirection;

    /**
     * 验收员
     */
    @ExcelProperty(value = "验收员", index = 4)
    @FieldValid(fieldName = "验收员", maxLength = 32)
    private String receiverName;

    /**
     * 部门
     */
    @ExcelProperty(value = "*部门", index = 5)
    @FieldValid(fieldName = "部门", isNotBlank = true, maxLength = 50)
    private String deptName;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 6)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 50)
    private String skuNo;

    /**
     * 实发数量
     */
    @ExcelProperty(value = "*实发数量", index = 7)
    @FieldValid(fieldName = "实发数量",  isNotBlank = true ,formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER,maxLength = 16)
    private String actualQtyStr;

    /**
     * 仓位
     */
    @ExcelProperty(value = "仓位", index = 8)
    @FieldValid(fieldName = "仓位", maxLength = 200)
    private String warehouseLocation;

    /**
     * 出库备注
     */
    @ExcelProperty(value = "*入库备注", index = 9)
    @FieldValid(fieldName = "入库备注", maxLength = 200)
    private String remark;

    /**
     * 错误数据
     */
    private String errorMsg;

}
