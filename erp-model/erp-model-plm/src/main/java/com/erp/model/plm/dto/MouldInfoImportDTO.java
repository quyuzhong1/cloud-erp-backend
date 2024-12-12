package com.erp.model.plm.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.plm.enums.RefundStandardEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MouldInfoImportDTO {


    /**
     * 错误的url
     */
    private String errorUrl;
    /**
     * 导入正确数据
     */
    private List<MouldDetailDTO.ViewDTO> successList = new ArrayList<>();
    /**
     * 导入数据，用于判断导入是否为空
     */
    private List<MouldInfoExcelDTO> allList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    private List<MouldInfoExcelDTO> errorList = new ArrayList<>();


    @Getter
    @Setter
    @EqualsAndHashCode
    public static class MouldInfoExcelDTO {

        /**
         * 序号
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "序号", index = 0)
        @FieldValid(fieldName = "序号", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
        private String num;
        /**
         * 模具编号(供应商)
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "模具编号(供应商)", index = 1)
        @FieldValid(fieldName = "模具编号(供应商)")
        private String thirdMouldNo;
        /**
         * 模具类型
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "模具类型", index = 2)
        @FieldValid(fieldName = "模具类型", isNotBlank = true)
        private String typeName;
        /**
         * 模具穴数
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "模具穴数", index = 3)
        @FieldValid(fieldName = "模具穴数", isNotBlank = true)
        private String mouldHoles;
        /**
         * 产品名称
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "产品名称", index = 4)
        @FieldValid(fieldName = "产品名称", isNotBlank = true)
        private String productName;
        /**
         * 模具尺寸.长(cm)
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "模具尺寸.长(cm)", index = 5)
        @FieldValid(fieldName = "模具尺寸.长(cm)", formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
        private String length;
        /**
         * 模具尺寸.宽(cm)
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "模具尺寸.宽(cm)", index = 6)
        @FieldValid(fieldName = "模具尺寸.宽(cm)", formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
        private String width;
        /**
         * 模具尺寸.高(cm)
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "模具尺寸.高(cm)", index = 7)
        @FieldValid(fieldName = "模具尺寸.高(cm)", formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
        private String height;
        /**
         * 模具材质
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "模具材质", index = 8)
        @FieldValid(fieldName = "模具材质", isNotBlank = true)
        private String material;
        /**
         * 模具寿命(万)(啤)
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "模具寿命(万)(啤)", index = 9)
        @FieldValid(fieldName = "模具寿命(万)(啤)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
        private String lifeCycle;
        /**
         * 开模周期(自然日)
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "开模周期(自然日)", index = 10)
        @FieldValid(fieldName = "开模周期(自然日)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
        private String developCycle;
        /**
         * 模具启用日期
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "模具启用日期", index = 11)
        @FieldValid(fieldName = "模具启用日期", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DATE_S)
        private String enableDate;
        /**
         * 数量(套)
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "数量(套)", index = 12)
        @FieldValid(fieldName = "数量(套)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
        private String qty;
        /**
         * 含税单价(￥)
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "含税单价(￥)", index = 13)
        @FieldValid(fieldName = "含税单价(￥)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
        private String taxPrice;
        /**
         * 税率(%)
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "税率(%)", index = 14)
        @FieldValid(fieldName = "税率(%)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
        private String taxRate;
        /**
         * 付款方式
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "付款方式", index = 15)
        @FieldValid(fieldName = "付款方式", isNotBlank = true)
        private String payMethodName;

        /**
         * 付款条件
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "付款条件", index = 16)
        @FieldValid(fieldName = "付款条件", isNotBlank = true)
        private String paymentConditionName;
        /**
         * 费用返还
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "费用返还", index = 17)
        @FieldValid(fieldName = "费用返还", fieldValues = "是,否")
        private String isNeedRefundName;
        /**
         * 返还金额(￥)
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "返还金额(￥)", index = 18)
        @FieldValid(fieldName = "返还金额(￥)", formatPattern = FieldFormatPatternTypeEnum.AMOUNT2)
        private String refundAmount;
        /**
         * 返还标准
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "返还标准", index = 19)
        @FieldValid(fieldName = "返还标准", enumClass = RefundStandardEnum.class)
        private String refundStandardName;
        /**
         * 返还单量
         */
        @ColumnWidth(25)
        @ExcelProperty(value = "返还单量", index = 20)
        @FieldValid(fieldName = "返还单量", formatPattern = FieldFormatPatternTypeEnum.POSITIVEINTEGER)
        private String refundOrderQty;

        /**
         * 错误信息
         */
        @ColumnWidth(50)
        @ExcelProperty(value = "导入错误说明", index = 21)
        private String errorMsg;
    }
}
