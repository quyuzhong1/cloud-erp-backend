package com.erp.model.sys.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname UserKingdeePostImportExcelDTO

 * @Date 2023-06-02 16:08
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DeptKingdeeImportExcelDTO implements Serializable {


    /**
     * 金蝶部门id
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "金蝶部门id", index = 0)
    @FieldValid(fieldName = "金蝶部门id",isNotBlank = true,maxLength =50 )
    private String syncKingdeeId;




    /**
     * 金蝶部门金编号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "金蝶部门编号", index = 3)
    @FieldValid(fieldName = "金蝶部门编号",isNotBlank = true,maxLength =30 )
    private String kingdeeDeptCode;

    /**
     * 使用组织
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "使用组织", index = 4)
    @FieldValid(fieldName = "使用组织",isNotBlank = true,maxLength =50 )
    private String useOrgCode;

    /**
     * 使用组织
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "使用组织名", index = 5)
    @FieldValid(fieldName = "使用组织名",isNotBlank = true,maxLength =50 )
    private String useOrgName;

    /**
     * 金蝶部门名称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "金蝶部门名称", index = 6)
    @FieldValid(fieldName = "金蝶部门名称",isNotBlank = true,maxLength =30 )
    private String kingdeeDeptName;




    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =22)
    @ColumnWidth(50)
    private String  errorMsg;



}
