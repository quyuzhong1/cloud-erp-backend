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
public class UserKingdeePostImportExcelDTO implements Serializable {



    /**
     * 员工金蝶编号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "员工编号", index = 0)
    @FieldValid(fieldName = "员工编号",isNotBlank = true,maxLength =30 )
    private String kingdeeUserCode;


    /**
     * 员工
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "员工", index = 1)
    @FieldValid(fieldName = "员工",isNotBlank = true,maxLength =30 )
    private String userName;

    /**
     * 员工岗位
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "员工金蝶岗位编码", index = 2)
    @FieldValid(fieldName = "员工金蝶岗位编码",isNotBlank = true,maxLength =30 )
    private String kingdeePostCode;

    /**
     * 使用组织
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "使用组织", index = 3)
    @FieldValid(fieldName = "使用组织",isNotBlank = true,maxLength =50 )
    private String useOrgCode;

    /**
     * 使用组织
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "使用组织名", index = 4)
    @FieldValid(fieldName = "使用组织名",isNotBlank = true,maxLength =50 )
    private String useOrgName;






    /**
     * 员工岗位名
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "员工金蝶岗位名称", index = 9)
    @FieldValid(fieldName = "员工金蝶岗位名称",isNotBlank = true,maxLength =50 )
    private String postName;


    /**
     * 员工部门code
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "员工金蝶部门code", index = 6)
    @FieldValid(fieldName = "员工金蝶部门code",isNotBlank = true,maxLength =50 )
    private String kingdeeDeptCode;

    /**
     * 员工部门名
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "员工金蝶部门名称", index = 7)
    @FieldValid(fieldName = "员工金蝶部门名称",isNotBlank = true,maxLength =50 )
    private String kingdeeDeptName;


    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =12)
    @ColumnWidth(50)
    private String  errorMsg;



}
