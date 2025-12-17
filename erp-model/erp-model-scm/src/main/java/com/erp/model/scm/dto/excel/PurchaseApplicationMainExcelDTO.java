package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.plm.enums.FirstMassProductTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 采购申请主表信息导入
 * @author will
 * @date 2025/7/30 18:08
 */
@Data
public class PurchaseApplicationMainExcelDTO implements Serializable {


    /**
     * 序号
     */
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "序号", isNotBlank = true, maxLength = 50)
    private String index;

    /**
     * 申请日期
     */
    @ExcelProperty(value = "*申请日期", index = 1)
    @FieldValid(fieldName = "申请日期", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String applyDateStr;

    /**
     * SKU
     */
    @ExcelProperty(value = "*SKU", index = 2)
    @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 50)
    private String skuNo;

    /**
     * 是否加急
     */
    @ExcelProperty(value = "是否加急", index = 3)
    @FieldValid(fieldName = "是否加急" , maxLength = 50 ,fieldValues = "是,否")
    private String isUrgentStr;

    /**
     * 计划交期
     */
    @ExcelProperty(value = "计划交期", index = 4)
    @FieldValid(fieldName = "计划交期", formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planDeliveryDateStr;

    /**
     * 申请数量
     */
    @ExcelProperty(value = "*申请数量", index = 5)
    @FieldValid(fieldName = "申请数量" , isNotBlank = true,formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String applyQtyStr;
    /**
     * 目的仓库名称
     */
    @ExcelProperty(value = "*目的仓库", index = 6)
    @FieldValid(fieldName = "目的仓库", isNotBlank = true, maxLength = 50)
    private String destWarehouseName;
    /**
     * 采购组织
     */
    @ExcelProperty(value = "*采购组织", index = 7)
    @FieldValid(fieldName = "采购组织", isNotBlank = true, maxLength = 50)
    private String purchaseOrgName;

    /**
     * 新品首批
     */
    @ExcelProperty(value = "*新品首批", index = 8)
    @FieldValid(fieldName = "新品首批", isNotBlank = true , maxLength = 50 ,enumClass = FirstMassProductTypeEnum.class)
    private String firstMassProductName;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 9)
    @FieldValid(fieldName = "备注" , maxLength = 255 )
    private String remark;

    /**
     * 错误数据
     */
    private String errorMsg;

}
