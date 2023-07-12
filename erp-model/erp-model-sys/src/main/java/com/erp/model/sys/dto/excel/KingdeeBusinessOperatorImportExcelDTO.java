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
public class KingdeeBusinessOperatorImportExcelDTO implements Serializable {

    /**
     * 类型id
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "类型id", index = 0)
    @FieldValid(fieldName = "类型id",isNotBlank = true,maxLength =50 )
    private String kingdeeTypeId;


    @ColumnWidth(25)
    @ExcelProperty(value = "类型名", index = 1)
    @FieldValid(fieldName = "类型名",isNotBlank = true,maxLength =50 )
    private String typeName;


    @ColumnWidth(25)
    @ExcelProperty(value = "金蝶用户id", index = 7)
    @FieldValid(fieldName = "金蝶用户id",isNotBlank = true,maxLength =50 )
    private String kingdeeUserId;
    /**
     * 业务员类型
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "业务员类型", index = 8)
    @FieldValid(fieldName = "业务员类型",isNotBlank = true,maxLength =30 )
    private String kingdeeType;


    /**
     * 使用组织
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "业务组织", index = 9)
    @FieldValid(fieldName = "业务组织",isNotBlank = true,maxLength =50 )
    private String kingdeeOrgCode;

    /**
     * 使用组织名
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "业务组织名", index = 10)
    @FieldValid(fieldName = "业务组织名",isNotBlank = true,maxLength =50 )
    private String kingdeeOrgName;


    /**
     * 业务员名称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "业务员名称", index = 12)
    @FieldValid(fieldName = "业务员名称",isNotBlank = true,maxLength =30 )
    private String kingdeeUserName;

    /**
     * 业务员code
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "业务员code", index = 13)
    @FieldValid(fieldName = "业务员code",isNotBlank = true,maxLength =30 )
    private String kingdeeUserCode;


    /**
     * 业务员岗位编码
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "业务员岗位编码", index = 14)
    @FieldValid(fieldName = "业务员岗位编码",isNotBlank = true,maxLength =30 )
    private String kingdeePostCode;

    /**
     * 是否禁用
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "是否禁用", index = 17)
    @FieldValid(fieldName = "是否禁用",isNotBlank = true,maxLength =30 )
    private String disabled;





    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index =23)
    @ColumnWidth(50)
    private String  errorMsg;



}
