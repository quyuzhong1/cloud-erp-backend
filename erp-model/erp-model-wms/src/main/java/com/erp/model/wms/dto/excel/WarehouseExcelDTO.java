package com.erp.model.wms.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname WarehouseExcelDTO

 * @Date 2023-03-22 17:42
 * @Created by yl
 */
@Data
public class WarehouseExcelDTO  implements Serializable {



    /**
     * 金蝶仓库编号
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "金蝶仓库编号", index = 0)
    @FieldValid(fieldName = "金蝶仓库编号",isNotBlank = true,maxLength=30)
    private String kingdeeWarehouseCode;


    /**
     * 仓库名称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "仓库名称", index = 1)
    @FieldValid(fieldName = "仓库名称",isNotBlank = true,maxLength=50)
    private String name;


    /**
     * 仓库类型
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓库类型", index = 2)
    @FieldValid(fieldName = "仓库类型")
    private String typeName;


    /**
     * 仓库组织
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "仓库组织", index = 3)
    @FieldValid(fieldName = "仓库组织",isNotBlank = true)
    private String orgName;

    /**
     * 是否虚拟仓
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "是否虚拟仓", index = 4)
    @FieldValid(fieldName = "是否虚拟仓",isNotBlank = true,fieldValues = "是,否")
    private String isVirtual;


    /**
     * 仓库负责人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "仓库负责人", index = 5)
    @FieldValid(fieldName = "仓库负责人")
    private String chargeName;


    /**
     * 联系人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "联系人", index = 6)
    @FieldValid(fieldName = "联系人",maxLength = 50)
    private String contacts;


    /**
     * 联系人电话
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "联系人电话", index = 7)
    @FieldValid(fieldName = "联系人电话",maxLength = 20)
    private String contactTelNumber;



    /**
     * 在途仓库名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "在途仓库名称", index = 8)
    @FieldValid(fieldName = "在途仓库名称",maxLength = 50)
    private String onwayWarehouseName;

    /**
     * 第三方仓库名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "第三方仓库名称", index = 9)
    @FieldValid(fieldName = "第三方仓库名称",maxLength = 50)
    private String thirdWarehouseName;
    /**
     * 仓库地址
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "仓库地址", index = 10)
    @FieldValid(fieldName = "仓库地址",maxLength = 200)
    private String address;

    /**
     * 仓库状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "仓库状态", index = 11)
    @FieldValid(fieldName = "仓库状态",isNotBlank = true,fieldValues = "启用,停用")
    private String enabled;

    /**
     * 仓库经营类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "仓库经营类型", index = 12)
    @FieldValid(fieldName = "仓库经营类型",isNotBlank = true)
    private String warehouseManageTypeName;


    /**
     * 仓库地理位置
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "仓库地理位置", index = 13)
    @FieldValid(fieldName = "仓库地理位置",isNotBlank = true)
    private String geographyLocationName;
    
    /**
     * 所属渠道
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "所属渠道", index = 14)
    @FieldValid(fieldName = "所属渠道",isNotBlank = true)
    private String channelAffiliation;
    
    /**
     * 发货组织
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "发货组织", index = 15)
    @FieldValid(fieldName = "发货组织",isNotBlank = true)
    private String shippingOrganization;
    
    /**
     * 财务组织
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "财务组织", index = 16)
    @FieldValid(fieldName = "财务组织",isNotBlank = true)
    private String financialOrganization;
    
    /**
     * 启用日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "启用日期", index = 17)
    @FieldValid(fieldName = "启用日期",isNotBlank = true)
    private String openTime;
    
    /**
     * 停用日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "停用日期", index = 18)
    @FieldValid(fieldName = "停用日期",isNotBlank = true)
    private String closeTime;


    /**
     * 错误信息
     */
    @ColumnWidth(200)
    @ExcelProperty(value = "错误信息", index = 19)
    private String errorMsg;


}
