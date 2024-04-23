package com.erp.model.tms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 对账字段导出
 *
 * @author Jim
 * {@code @date:} 2024-03-25
 */
@Data
public class CfgReconciliationFieldExportExcelDTO implements Serializable {

    /**
     * 核对类型名称
     */
    @ExcelProperty(value = "核对类型", index = 0)
    private String reconciliationTypeName;

    /**
     * 第三方名称
     */
    @ExcelProperty(value = "物流商", index = 1)
    private String thirdName;

    /**
     * 第三方字段名称
     */
    @ExcelProperty(value = "物流商字段", index = 2)
    private String thirdFieldName;

    /**
     * ERP字段名称
     */
    @ExcelProperty(value = "数大臣字段", index = 3)
    private String erpFieldName;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人", index = 4)
    private String createUserName;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 5)
    private String createTime;
}
