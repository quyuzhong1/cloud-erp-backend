package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname WarehouseExcelDTO
 * @Description TODO
 * @Date 2023-03-22 17:42
 * @Created by yl
 */
@Data
public class WarehouseExcelDTO  implements Serializable {

    /**
     * 仓库名称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "仓库名称", index = 0)
    @FieldValid(fieldName = "仓库名称",isNotBlank = true)
    private String name;


    /**
     * 仓库类型
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓库类型", index = 1)
    @FieldValid(fieldName = "仓库类型",isNotBlank = true)
    private String typeName;


    /**
     * 仓库组织
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "仓库组织", index = 2)
    @FieldValid(fieldName = "仓库组织",isNotBlank = true)
    private String orgName;

    /**
     * 是否虚拟仓
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "是否虚拟仓", index = 3)
    @FieldValid(fieldName = "是否虚拟仓",isNotBlank = true)
    private String isVirtual;


    /**
     * 仓库负责人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "仓库负责人", index = 4)
    @FieldValid(fieldName = "仓库负责人",isNotBlank = true)
    private String chargeName;


    /**
     * 联系人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "联系人", index = 5)
    @FieldValid(fieldName = "联系人",isNotBlank = true)
    private String contacts;


    /**
     * 联系人电话
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "联系人电话", index = 6)
    @FieldValid(fieldName = "联系人电话",isNotBlank = true)
    private String contactTelNumber;



    /**
     * 仓库地址
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓库地址", index = 7)
    @FieldValid(fieldName = "仓库地址",isNotBlank = true)
    private String address;


    /**
     * 仓库状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "仓库状态", index = 8)
    @FieldValid(fieldName = "仓库状态",isNotBlank = true)
    private String enabled;


    /**
     * 错误信息
     */
    @ColumnWidth(200)
    @ExcelProperty(value = "错误信息", index = 9)
    private String errorMsg;


}
