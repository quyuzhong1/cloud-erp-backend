package com.erp.model.scm.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: wtr
 * @Date: 2025/10/20 11:30
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
public class AssetNoticeImportExcelDTO  implements Serializable {

    @ExcelProperty("*序号")
    @FieldValid(fieldName = "serialNumber",isNotBlank = true)
    private String serialNumber;

    /**
     * 申请日期
     */
    @ExcelProperty("*申请日期")
    @FieldValid(fieldName = "applyDate",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  applyDate;

    /**
     * 申请人
     */
    @ExcelProperty("申请人")
    @FieldValid(fieldName = "applyUserName")
    private String applyUserName;

    /**
     * 申请部门
     */
    @ExcelProperty("申请部门")
    @FieldValid(fieldName = "applyDeptName")
    private String applyDeptName;

    /**
     * 模具编码
     */
    @ExcelProperty("*模具编码")
    @FieldValid(fieldName = "assertCode",isNotBlank = true)
    private String assertCode;

    /**
     * 是否加急
     */
    @ExcelProperty("是否加急")
    @FieldValid(fieldName = "isUrgentName",fieldValues = "是,否")
    private String  isUrgentName;

    /**
     * 计划交期
     */
    @ExcelProperty("计划交期")
    @FieldValid(fieldName = "planDeliveryDateStr",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String  planDeliveryDateStr;


    /**
     * 申请数量
     */
    @ExcelProperty("*申请数量")
    @FieldValid(fieldName = "applyQtyStr", isNotBlank = true ,maxLength = 16)
    private String applyQtyStr;

    /**
     * 采购组织
     */
    @ExcelProperty("*采购组织")
    @FieldValid(fieldName = "purchaseOrgName",isNotBlank = true)
    private String  purchaseOrgName;

    /**
     * 备注
     */
    @ExcelProperty("备注")
    @FieldValid(fieldName = "remark",maxLength = 200)
    private String  remark;

    /**
     * 错误数据
     */
    @ExcelProperty("错误数据")
    private String  errorMsg;
}
