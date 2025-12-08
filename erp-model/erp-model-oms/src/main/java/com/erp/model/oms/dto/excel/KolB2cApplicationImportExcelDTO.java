package com.erp.model.oms.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author jack
 * @Date 2025-12-08
 */
@Data
@NoArgsConstructor
public class KolB2cApplicationImportExcelDTO implements Serializable {

    /**
     * 序号
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号", isNotBlank = true)
    private String no;

    /**
     * 申请日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*申请日期", index = 1)
    @FieldValid(fieldName = "*申请日期", isNotBlank = true)
    private String applyDateStr;
    @ExcelIgnore
    private LocalDate applyDate;

    /**
     * 寄样类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*寄样类型", index = 2)
    @FieldValid(fieldName = "*寄样类型", isNotBlank = true)
    private String sampleTypeName;
    @ExcelIgnore
    private String sampleType;

    /**
     * 店铺
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*店铺", index = 3)
    @FieldValid(fieldName = "*店铺", isNotBlank = true)
    private String shopName;
    @ExcelIgnore
    private String shopId;
    /**
     * 币别
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "*币别", index = 4)
    @FieldValid(fieldName = "*币别", isNotBlank = true)
    private String currencyName;
    @ExcelIgnore
    private String currency;

    /**
     * 业务类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*业务类型", index = 5)
    @FieldValid(fieldName = "*业务类型", isNotBlank = true)
    private String isInternationalName;
    @ExcelIgnore
    private Boolean isInternational;

    /**
     * 发货仓库
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "发货仓库", index = 6)
    @FieldValid(fieldName = "发货仓库")
    private String deliveryWarehouse;

    /**
     * 物流渠道
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "物流渠道", index = 7)
    @FieldValid(fieldName = "物流渠道")
    private String logisticsChannel;

    /**
     * 申请说明
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "申请说明", index = 8)
    @FieldValid(fieldName = "申请说明")
    private String applyDescription;

    /**
     * 申请人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*申请人", index = 9)
    @FieldValid(fieldName = "*申请人", isNotBlank = true)
    private String applicantName;

    /**
     * 申请部门
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "申请部门", index = 10)
    @FieldValid(fieldName = "申请部门")
    private String applyDeptName;
    @ExcelIgnore
    private String applyDeptId;

    /**
     * SKU
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU", index = 11)
    @FieldValid(fieldName = "*SKU", isNotBlank = true)
    private String sku;

    /**
     * 达人昵称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*达人昵称", index = 12)
    @FieldValid(fieldName = "*达人昵称", isNotBlank = true)
    private String kolNickname;

    /**
     * 申请数量
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "*申请数量", index = 13)
    @FieldValid(fieldName = "*申请数量", isNotBlank = true)
    private Integer applyQuantity;

    /**
     * 预计回片日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "预计回片日期", index = 14)
    @FieldValid(fieldName = "预计回片日期")
    private String expectedReturnDate;

    /**
     * 项目名称
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "项目名称", index = 15)
    @FieldValid(fieldName = "项目名称")
    private String projectName;

    /**
     * 备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "备注", index = 16)
    @FieldValid(fieldName = "备注")
    private String remark;

    /**
     * 国家
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*国家", index = 17)
    @FieldValid(fieldName = "*国家", isNotBlank = true)
    private String country;

    /**
     * 省/州
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "省/州", index = 18)
    @FieldValid(fieldName = "省/州")
    private String province;

    /**
     * 城市
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*城市", index = 19)
    @FieldValid(fieldName = "*城市", isNotBlank = true)
    private String city;

    /**
     * 区域
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "区域", index = 20)
    @FieldValid(fieldName = "区域")
    private String area;

    /**
     * 收货人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*收货人", index = 21)
    @FieldValid(fieldName = "*收货人", isNotBlank = true)
    private String consignee;

    /**
     * 邮编
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "邮编", index = 22)
    @FieldValid(fieldName = "邮编")
    private String postalCode;

    /**
     * 收货人电话
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*收货人电话", index = 23)
    @FieldValid(fieldName = "*收货人电话", isNotBlank = true)
    private String consigneePhone;

    /**
     * 详细地址
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "*详细地址", index = 24)
    @FieldValid(fieldName = "*详细地址", isNotBlank = true)
    private String detailedAddress;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 12)
    @ColumnWidth(50)
    private String errorMsg = "";
}
