package com.erp.model.oms.dto.excel;

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
 * @author jack
 * @Date 2025-12-03
 */
@Data
@NoArgsConstructor
public class KolPartnerInfoImportExcelDTO implements Serializable {


    /**
     * 达人昵称
     */
    @ColumnWidth(25)
    @ExcelProperty(value = "*达人昵称", index = 0)
    @FieldValid(fieldName = "*达人昵称",isNotBlank = true )
    private String nickname;


    /**
     * 达人类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "达人类型", index = 1)
    @FieldValid(fieldName = "达人类型")
    private String typeName;
    @ExcelIgnore
    private String type;


    /**
     * 合作类型
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "合作类型", index = 2)
    @FieldValid(fieldName = "合作类型")
    private String cooperationTypeName;
    @ExcelIgnore
    private String cooperationType;

    /**
     * 合作日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "合作日期", index = 3)
    @FieldValid(fieldName = "合作日期")
    private String cooperationDateStr;
    @ExcelIgnore
    private LocalDate cooperationDate;

    /**
     * 国家
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "*国家", index = 4)
    @FieldValid(fieldName = "*国家",isNotBlank = true)
    private String countryName;
    @ExcelIgnore
    private String countryId;

    /**
     * 语言
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "语言", index = 5)
    @FieldValid(fieldName = "语言")
    private String languageName;
    @ExcelIgnore
    private String language;


    /**
     * 邮箱
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "邮箱", index = 6)
    @FieldValid(fieldName = "邮箱")
    private String email;

    /**
     * 电话
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "电话", index = 7)
    @FieldValid(fieldName = "电话")
    private String phone;

    /**
     * 负责人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*负责人", index = 8)
    @FieldValid(fieldName = "*负责人",isNotBlank = true)
    private String chargeName;
    @ExcelIgnore
    private String chargeId;

    /**
     * 负责部门
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "负责部门", index = 9)
    @FieldValid(fieldName = "负责部门")
    private String deptName;
    @ExcelIgnore
    private String deptId;

    /**
     * 备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "备注", index = 10)
    @FieldValid(fieldName = "备注",maxLength = 200)
    private String remark;


    /**
     * *合作平台
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*合作平台", index = 11)
    @FieldValid(fieldName = "*合作平台",isNotBlank = true)
    private String platformName;

    /**
     * 账号ID
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "账号ID", index = 12)
    @FieldValid(fieldName = "账号ID")
    private String platformAccountId;

    /**
     * 账号名称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "账号名称", index = 13)
    @FieldValid(fieldName = "账号名称")
    private String platformAccountName;

    /**
     * 粉丝数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "粉丝数量", index = 14)
    @FieldValid(fieldName = "粉丝数量",formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
    private Integer followerCount;

    /**
     * 主页链接
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*主页链接", index = 15)
    @FieldValid(fieldName = "*主页链接",isNotBlank = true,maxLength = 200)
    private String homepageUrl;

    /**
     * 平台备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "平台备注", index = 16)
    @FieldValid(fieldName = "平台备注",maxLength = 200)
    private String platformRemark;

    /**
     * 国家
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*地址信息国家", index = 17)
    @FieldValid(fieldName = "*地址信息国家",isNotBlank = true)
    private String addressCountryName;
    @ExcelIgnore
    private String addressCountryId;

    /**
     * 联系电话
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*省/州", index = 18)
    @FieldValid(fieldName = "*省/州",isNotBlank = true)
    private String province;
    @ExcelIgnore
    private String provinceId;
    /**
     * 城市
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*城市", index = 19)
    @FieldValid(fieldName = "*城市",isNotBlank = true)
    private String city;
    @ExcelIgnore
    private String cityId;

    /**
     * 区域
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "区域", index = 20)
    @FieldValid(fieldName = "区域")
    private String district;
    @ExcelIgnore
    private String districtId;

    /**
     * 详细地址
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*详细地址", index = 21)
    @FieldValid(fieldName = "*详细地址",isNotBlank = true)
    private String detailAddress;

    /**
     * 联系人
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*联系人", index = 22)
    @FieldValid(fieldName = "*联系人",isNotBlank = true)
    private String contactPerson;

    /**
     * 联系人电话
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*联系人电话", index = 23)
    @FieldValid(fieldName = "*联系人电话",isNotBlank = true)
    private String contactPersonPhone;

    /**
     * 联系人邮编
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "联系人邮编", index = 24)
    @FieldValid(fieldName = "联系人邮编")
    private String zipCode;

    /**
     * 收件人税号
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "收件人税号", index = 25)
    @FieldValid(fieldName = "收件人税号")
    private String receiverTaxNo;

    /**
     * 是否默认地址
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "默认地址", index = 26)
    @FieldValid(fieldName = "默认地址")
    private String isDefaultName;

    /**
     * 启用状态
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "启用状态", index = 27)
    @FieldValid(fieldName = "启用状态")
    private String disabledName;



    /**
     * 地址备注
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "地址备注", index = 28)
    @FieldValid(fieldName = "地址备注")
    private String addressRemark;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 29)
    @ColumnWidth(50)
    private String  errorMsg = "";
}

