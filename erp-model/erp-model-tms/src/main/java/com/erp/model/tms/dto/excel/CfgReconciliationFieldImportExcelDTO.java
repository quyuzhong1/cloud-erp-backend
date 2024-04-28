package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.erp.model.tms.enums.CfgReconciliationTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 其他出库单导入
 *
 * @author Jim
 * {@code @date:} 2024/03/22
 */
@Data
public class CfgReconciliationFieldImportExcelDTO implements Serializable {

    /**
     * id
     */
    @ExcelIgnore
    private String id;

    /**
     * 核对类型
     */
    @ExcelProperty(value = "*核对类型", index = 0)
    @FieldValid(fieldName = "核对类型", isNotBlank = true, maxLength = 32, enumClass = CfgReconciliationTypeEnum.class)
    private String reconciliationTypeName;

    /**
     * 物流商
     */
    @ExcelProperty(value = "*物流商", index = 1)
    @FieldValid(fieldName = "物流商", isNotBlank = true, maxLength = 32)
    private String thirdName;

    /**
     * 物流商字段
     */
    @ExcelProperty(value = "*物流商字段", index = 2)
    @FieldValid(fieldName = "物流商字段", isNotBlank = true, maxLength = 32)
    private String thirdFieldName;

    /**
     * 数大臣字段
     */
    @ExcelProperty(value = "*数大臣字段", index = 3)
    @FieldValid(fieldName = "数大臣字段", isNotBlank = true, maxLength = 32)
    private String erpFieldName;

    /**
     * 错误数据
     */
    private String errorMsg;

}
