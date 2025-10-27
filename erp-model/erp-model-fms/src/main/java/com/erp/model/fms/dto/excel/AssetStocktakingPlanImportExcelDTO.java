package com.erp.model.fms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;

/**
 * 资产盘点方案导入Excel DTO
 * 
 * @author wuht
 * @date 2025-10-27
 */
@Data
public class AssetStocktakingPlanImportExcelDTO {

    /**
     * 资产组织
     */
    @ExcelProperty(value = "*资产组织", index = 0)
    @FieldValid(fieldName = "*资产组织", isNotBlank = true)
    private String assetOrgName;

    /**
     * 资产组织ID
     */
    @ExcelIgnore
    private String assetOrgId;

    /**
     * 盘点方案名称
     */
    @ExcelProperty(value = "*盘点方案名称", index = 1)
    @FieldValid(fieldName = "*盘点方案名称", isNotBlank = true)
    private String planName;

    /**
     * 描述
     */
    @ExcelProperty(value = "描述", index = 2)
    private String remark;

    /**
     * 资产类别（多个用逗号分隔）
     */
    @ExcelProperty(value = "资产类别", index = 3)
    private String assetCategories;

    /**
     * 使用部门（多个用逗号分隔）
     */
    @ExcelProperty(value = "使用部门", index = 4)
    private String useDeptNames;

    /**
     * 使用部门ID（多个用逗号分隔）
     */
    @ExcelIgnore
    private String useDeptIds;

    /**
     * 资产位置（多个用逗号分隔）
     */
    @ExcelProperty(value = "资产位置", index = 5)
    private String assetLocationNames;

    /**
     * 资产位置ID（多个用逗号分隔）
     */
    @ExcelIgnore
    private String assetLocationIds;

    /**
     * 起始卡片编码
     */
    @ExcelProperty(value = "起始卡片编码", index = 6)
    private String cardCodeStart;

    /**
     * 结束卡片编码
     */
    @ExcelProperty(value = "结束卡片编码", index = 7)
    private String cardCodeEnd;

    /**
     * 创建人ID
     */
    @ExcelIgnore
    private String createUserId;

    /**
     * 创建人姓名
     */
    @ExcelIgnore
    private String createUserName;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 8)
    @ColumnWidth(50)
    private String errorMsg = "";
}

