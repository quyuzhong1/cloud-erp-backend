package com.erp.model.fms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 资产验收表Excel导入DTO
 * @author wuht
 * @since 2025-10-11
 */
@Data
@NoArgsConstructor
public class AssetAcceptExcelDTO implements Serializable {

    /**
     * 行号
     */
    @ExcelIgnore
    private Integer rowNum;

    /**
     * 错误信息
     */
    @ExcelIgnore
    @ExcelProperty(value = "错误信息", index = 19)
    private String errorMsg;

    /**
     * 序号
     */
    @ExcelProperty(value = "*序号", index = 0)
    @ColumnWidth(10)
    @FieldValid(fieldName = "*序号", isNotBlank = true)
    private String serialNumber;

    /**
     * 验收日期（字符串格式）
     */
    @ExcelProperty(value = "*验收日期", index = 1)
    @ColumnWidth(15)
    @FieldValid(fieldName = "*验收日期", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String acceptDateStr;

    /**
     * 验收日期
     */
    @ExcelIgnore
    private LocalDate acceptDate;

    /**
     * 来源单号（模具采购单号）
     */
    @ExcelProperty(value = "模具采购单号", index = 2)
    @ColumnWidth(20)
    private String sourceCode;

    /**
     * 来源类型（默认为模具采购订单，不需要导入）
     */
    @ExcelIgnore
    private String sourceType;

    /**
     * 来源ID（根据来源单号查询，不需要导入）
     */
    @ExcelIgnore
    private String sourceId;

    /**
     * 是否盖章（字符串格式）
     */
    @ExcelProperty(value = "是否盖章", index = 3)
    @ColumnWidth(12)
    private String isNeedSealStr;

    /**
     * 是否盖章
     */
    @ExcelIgnore
    private Boolean isNeedSeal;

    /**
     * 验收组别
     */
    @ExcelProperty(value = "*验收组别", index = 4)
    @ColumnWidth(15)
    @FieldValid(fieldName = "*验收组别", isNotBlank = true)
    private String acceptOrgName;

    /**
     * 验收组织ID
     */
    @ExcelIgnore
    private String acceptOrgId;

    /**
     * 验收人
     */
    @ExcelProperty(value = "验收人", index = 5)
    @ColumnWidth(15)
    private String acceptUserName;

    /**
     * 验收人ID
     */
    @ExcelIgnore
    private String acceptUserId;

    /**
     * 验收部门
     */
    @ExcelProperty(value = "验收部门", index = 6)
    @ColumnWidth(15)
    private String acceptDeptName;

    /**
     * 验收部门ID
     */
    @ExcelIgnore
    private String acceptDeptId;

    /**
     * 验收说明
     */
    @ExcelProperty(value = "验收说明", index = 7)
    @ColumnWidth(20)
    private String acceptDesc;

    /**
     * 采购开发
     */
    @ExcelProperty(value = "采购开发", index = 8)
    @ColumnWidth(15)
    private String purchaseDevName;

    /**
     * 采购开发ID
     */
    @ExcelIgnore
    private String purchaseDevId;

    /**
     * 质量工程师
     */
    @ExcelProperty(value = "质量工程师", index = 9)
    @ColumnWidth(15)
    private String qualityEngineerName;

    /**
     * 质量工程师ID
     */
    @ExcelIgnore
    private String qualityEngineerId;

    /**
     * 结构工程师
     */
    @ExcelProperty(value = "结构工程师", index = 10)
    @ColumnWidth(15)
    private String structureEngineerName;

    /**
     * 结构工程师ID
     */
    @ExcelIgnore
    private String structureEngineerId;

    /**
     * 产品经理
     */
    @ExcelProperty(value = "产品经理", index = 11)
    @ColumnWidth(15)
    private String productManagerName;

    /**
     * 产品经理ID
     */
    @ExcelIgnore
    private String productManagerId;

    /**
     * 项目经理
     */
    @ExcelProperty(value = "项目经理", index = 12)
    @ColumnWidth(15)
    private String projectManagerName;

    /**
     * 项目经理ID
     */
    @ExcelIgnore
    private String projectManagerId;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 13)
    @ColumnWidth(20)
    @FieldValid(fieldName = "*SKU", isNotBlank = true)
    private String skuNo;

    /**
     * SKU ID
     */
    @ExcelIgnore
    private String skuId;

    /**
     * 产品名称
     */
    @ExcelIgnore
    private String productName;

    /**
     * 验收数量（字符串格式）
     */
    @ExcelProperty(value = "*验收数量", index = 14)
    @ColumnWidth(12)
    @FieldValid(fieldName = "*验收数量", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER, maxLength = 16)
    private String acceptQtyStr;

    /**
     * 验收数量
     */
    @ExcelIgnore
    private Integer acceptQty;

    /**
     * 资产位置
     */
    @ExcelProperty(value = "*资产位置", index = 15)
    @ColumnWidth(15)
    @FieldValid(fieldName = "*资产位置", isNotBlank = true)
    private String assetLocationName;

    /**
     * 资产位置ID
     */
    @ExcelIgnore
    private String assetLocationId;

    /**
     * 使用部门
     */
    @ExcelProperty(value = "*使用部门", index = 16)
    @ColumnWidth(15)
    @FieldValid(fieldName = "*使用部门", isNotBlank = true)
    private String useDeptName;

    /**
     * 使用部门ID
     */
    @ExcelIgnore
    private String useDeptId;

    /**
     * 费用项目名称（Excel输入）
     */
    @ExcelProperty(value = "*费用项目", index = 17)
    @ColumnWidth(15)
    @FieldValid(fieldName = "*费用项目", isNotBlank = true)
    private String costTypeName;

    /**
     * 费用项目编码（从名称转换得到）
     */
    @ExcelIgnore
    private String costType;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 18)
    @ColumnWidth(20)
    private String remark;

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
}
