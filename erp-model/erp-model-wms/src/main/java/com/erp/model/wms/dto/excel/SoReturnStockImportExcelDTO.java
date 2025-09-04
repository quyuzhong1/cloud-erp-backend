package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.enums.OrderTypeEnum;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.wms.enums.ReturnReasonEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

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
    private String customerName;

    /**
     * 退货仓库
     */
    @ExcelProperty(value = "*退货仓库", index = 1)
    @FieldValid(fieldName = "退货仓库", isNotBlank = true, maxLength = 50)
    private String warehouseName;

    /**
     * 退货仓库
     */
    @ExcelProperty(value = "*退货类型", index = 2)
    @FieldValid(fieldName = "*退货类型", isNotBlank = true, maxLength = 50)
    private String returnType;
    /**
     * 入库日期
     */
    @ExcelProperty(value = "入库日期", index = 3)
    @FieldValid(fieldName = "入库日期")
    private String billDateStr;
    @ExcelIgnore
    private LocalDate billDate;

    /**
     * 单据类型
     */
    @ExcelProperty(value = "单据类型", index = 4)
    @FieldValid(fieldName = "单据类型", enumClass = OrderTypeEnum.class)
    private String typeName;
    /**
     * 退货物流单号
     */
    @ExcelProperty(value = "退货物流单号", index = 5)
    @FieldValid(fieldName = "退货物流单号")
    private String returnLogisticCode;
    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 6)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 32)
    private String skuNo;

    /**
     * 上架数量
     */
    @ExcelProperty(value = "*上架数量", index = 7)
    @FieldValid(fieldName = "上架数量", isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String realQtyStr;

    /**
     * 仓位
     */
    @ExcelProperty(value = "仓位", index = 8)
    @FieldValid(fieldName = "仓位", maxLength = 50)
    private String warehouseLocationName;

    /**
     * 退货金额
     */
    @ExcelProperty(value = "退货金额", index = 9)
    @FieldValid(fieldName = "退货金额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String returnAmountStr;

    /**
     * 含税退货金额
     */
    @ExcelProperty(value = "含税退货金额", index = 10)
    @FieldValid(fieldName = "含税退货金额",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String taxReturnAmountStr;

    /**
     * 币种
     */
    @ExcelProperty(value = "币种", index = 11)
    @FieldValid(fieldName = "币种", maxLength = 50)
    private String currencyStr;

    /**
     * 退货原因
     */
    @ExcelProperty(value = "*退货原因", index = 12)
    @FieldValid(fieldName = "退货原因",maxLength = 200 , enumClass = ReturnReasonEnum.class )
    private String returnReasonDictStr;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 13)
    @FieldValid(fieldName = "备注", maxLength = 200)
    private String remark;


    /**
     * 错误数据
     */
    private String errorMsg;

}
