package com.erp.model.mrp.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CfgRuleSalesEstimateFileDTO {

    @Getter
    @Setter
    public static class PagingView {
        /**
         * 文件名
         */
        private String fileName;
        /**
         * 文件地址
         */
        private String fileUrl;
        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    @Getter
    @Setter
    public static class PagingParamDTO {

        private String cfgRuleSalesQtyId;
    }

    @Getter
    @Setter
    public static class ExcelDTO {
        /**
         * 平台
         */
        @ExcelProperty(value = "*平台", index = 0)
        @FieldValid(fieldName = "平台", isNotBlank = true, maxLength = 32)
        private String platformName;

        private String platform;

        /**
         * 店铺
         */
        @ExcelProperty(value = "*店铺", index = 1)
        @FieldValid(fieldName = "店铺", isNotBlank = true, maxLength = 32)
        private String shopName;

        private String shopId;

        /**
         * SKU
         */
        @ExcelProperty(value = "*SKU", index = 2)
        @FieldValid(fieldName = "SKU", isNotBlank = true, maxLength = 32)
        private String skuNo;

        private String skuId;

        /**
         * 日期
         */
        @ExcelProperty(value = "*日期", index = 3)
        @FieldValid(fieldName = "日期", isNotBlank = true, formatPattern= FieldFormatPatternTypeEnum.DATE)
        private String date;

        /**
         * 预估日销量
         */
        @ExcelProperty(value = "*预估日销量", index = 4)
        @FieldValid(fieldName = "预估日销量", isNotBlank = true)
        private String salesQty;

        /**
         * 错误数据
         */
        private String errorMsg;
    }
}
