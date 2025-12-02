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
 * KOL回片列表Excel导入DTO
 * @author wuhaotian
 * @since 2025-12-01
 */
@Data
@NoArgsConstructor
public class KolFeedbackExcelDTO implements Serializable {

    /**
     * SKU编码
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*SKU编码", index = 0)
    @FieldValid(fieldName = "*SKU编码", isNotBlank = true)
    private String skuNo;
    @ExcelIgnore
    private String skuId;
    @ExcelIgnore
    private String productName;

    /**
     * 数量
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*数量", index = 1)
    @FieldValid(fieldName = "*数量", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String qtyStr;
    @ExcelIgnore
    private Integer qty;

    /**
     * 达人昵称
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "*达人昵称", index = 2)
    @FieldValid(fieldName = "*达人昵称", isNotBlank = true, maxLength = 100)
    private String partnerNickname;
    @ExcelIgnore
    private String partnerId;

    /**
     * 回片链接
     */
    @ColumnWidth(50)
    @ExcelProperty(value = "*回片链接", index = 3)
    @FieldValid(fieldName = "*回片链接", isNotBlank = true, maxLength = 500)
    private String url;
    @ExcelIgnore
    private String urlHash;

    /**
     * 发布形式
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "发布形式", index = 4)
    @FieldValid(fieldName = "发布形式", maxLength = 50)
    private String publishType;

    /**
     * 发布日期
     */
    @ColumnWidth(20)
    @ExcelProperty(value = "发布日期", index = 5)
    @FieldValid(fieldName = "发布日期")
    private String publishDateStr;
    @ExcelIgnore
    private LocalDate publishDate;

    /**
     * 备注
     */
    @ColumnWidth(30)
    @ExcelProperty(value = "备注", index = 6)
    @FieldValid(fieldName = "备注", maxLength = 500)
    private String remark;

    /**
     * 错误数据
     */
    @ExcelProperty(value = "错误数据", index = 9)
    @ColumnWidth(50)
    private String errorMsg = "";

    /**
     * 行号
     */
    @ExcelIgnore
    private Integer rowNum;

    /**
     * 创建人ID
     */
    @ExcelIgnore
    private String createUserId;

    /**
     * 创建人姓名
     */
    @ExcelIgnore
    private String createUserName;

    /**
     * 来源ID
     */
    @ExcelIgnore
    private String sourceId;

    /**
     * 来源类型
     */
    @ExcelIgnore
    private String sourceType;
}

