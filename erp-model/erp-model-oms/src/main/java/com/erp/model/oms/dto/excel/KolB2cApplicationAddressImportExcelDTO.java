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
public class KolB2cApplicationAddressImportExcelDTO implements Serializable {

    /**
     * 序号
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "*序号", index = 0)
    @FieldValid(fieldName = "*序号", isNotBlank = true)
    private String no;

    /**
     * 达人昵称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*达人昵称", index = 1)
    @FieldValid(fieldName = "*达人昵称", isNotBlank = true)
    private String nickname;
    @ExcelIgnore
    private String partnerId;

    /**
     * 国家
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*国家", index = 2)
    @FieldValid(fieldName = "*国家", isNotBlank = true)
    private String countryName;
    @ExcelIgnore
    private String countryId;

    /**
     * 省/州
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*省/州", index = 3)
    @FieldValid(fieldName = "*省/州", isNotBlank = true)
    private String province;
    @ExcelIgnore
    private String provinceId;

    /**
     * 城市
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*城市", index = 4)
    @FieldValid(fieldName = "*城市", isNotBlank = true)
    private String city;
    @ExcelIgnore
    private String cityId;

    /**
     * 区域
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "区域", index = 5)
    @FieldValid(fieldName = "区域")
    private String district;
    @ExcelIgnore
    private String districtId;

    /**
     * 收货人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*收货人", index = 6)
    @FieldValid(fieldName = "*收货人", isNotBlank = true)
    private String receiverName;


    /**
     * 邮编
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "邮编", index = 7)
    @FieldValid(fieldName = "邮编")
    private String zipCode;

    /**
     * 收货人电话
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*收货人电话", index = 8)
    @FieldValid(fieldName = "*收货人电话", isNotBlank = true)
    private String receiverPhone;

    /**
     * 详细地址
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "*详细地址", index = 9)
    @FieldValid(fieldName = "*详细地址", isNotBlank = true)
    private String detailAddress;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 10)
    @ColumnWidth(50)
    private String errorMsg = "";
}
