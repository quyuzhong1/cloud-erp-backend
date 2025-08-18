package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 供应商阶段导出
 * @author will
 * @date 2025/7/28 10:55
 */
@Data
@NoArgsConstructor
public class SupplierPhaseExportExcelDTO implements Serializable {

    /**
     * 编号
     */
    @ExcelProperty(value = "供应商编号")
    private String  supplierCode;

    /**
     * 名称
     */
    @ExcelProperty(value = "供应商名称")
    private String supplierName;

    /**
     * 供应商分类
     */
    @ExcelProperty(value = "供应商分类")
    private String categoryName;

    /**
     * 当前阶段名称
     */
    @ExcelProperty(value = "当前阶段")
    private String currentPhaseName;

    /**
     * 目标阶段名称
     */
    @ExcelProperty(value = "目标阶段")
    private String targetPhaseName;

    /**
     * 当前等级名称
     */
    @ExcelProperty(value = "当前等级")
    private String currentGradeName;

    /**
     * 目标等级名称
     */
    @ExcelProperty(value = "目标等级")
    private String targetGradeName;

    /**
     * 审核结果
     */
    @ExcelProperty(value = "审核结果")
    private String approveStatusName;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人")
    private String createUserName;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 审核人
     */
    @ExcelProperty(value = "审核人")
    private String approveUserName;
}
