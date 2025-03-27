package com.erp.model.plm.dto;


import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.plm.enums.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 产品sku信息导入
 * @Author jack
 * @Date 2025-01-19
 **/
@Data
@NoArgsConstructor
public class ProductDetailUpdateApproveExcelDTO {
    /**
     * spuNo
     */
    private String spuNo;

    /**
     * sku编号
     */
    @FieldValid(fieldName = "sku编号", isNotBlank = true )
    private String skuNo;

    /**
     * 一级分类
     */
    @FieldValid(fieldName = "一级分类")
    private String mainCategory;

    /**
     * 二级分类
     */
    @FieldValid(fieldName = "二级分类")
    private String secondaryCategory;

    /**
     * 应用分类名
     */
    @FieldValid(fieldName = "应用分类")
    private String applicationCategoryName;

    /**
     * 产品经理
     */
    @FieldValid(fieldName = "产品经理" ,maxLength = 20)
    private String chargeName;

    /**
     * 销售方式
     */
    @FieldValid(fieldName = "销售方式" ,enumClass = SaleMethodEnum.class)
    private String saleMethod;

    /**
     * 产品属性
     */
    @FieldValid(fieldName = "产品属性" , maxLength = 255)
    private String property;

    /**
     * 产品开发状态
     */
    @FieldValid(fieldName = "产品开发状态",enumClass = ProductDetailStateEnum.class)
    private String productStateName;

    /**
     * 采购员
     */
    private String purchaseUser;

    /**
     * 一级供应商
     */
    private String mainSupplier;

    /**
     * 二级供应商
     */
    private String secondSupplier;

    /**
     * 销售状态
     */
    @FieldValid(fieldName = "销售状态" , enumClass = SaleStateEnum.class)
    private String saleState;

    /**
     * 是否可销售
     */
    @FieldValid(fieldName = "是否可销售" , fieldValues = "是,否")
    private String isMarketable;

    /**
     * 报关产品属性
     */
    private String productProperty;

    /**
     * 保险属性
     */
    @FieldValid(fieldName = "保险属性")
    private String insuranceProperty;

    /**
     * 报关申报价（$）
     */
    @FieldValid(fieldName = "报关申报价（$）",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String declarePrice;

    /**
     * 报关中文名
     */
    private String declareChineseName;

    /**
     * 报关英文名
     */
    private String declareEnglishName;

    /**
     * 中国海关编码
     */
    private String customsCode;

    /**
     * 报关型号
     */
    private String declareModel;

    /**
     * 报关单位
     */
    private String declareUnit;

    /**
     * 申报要素
     */
    private String declareElement;

    /**
     * 英文材质
     */
    private String englishMaterial;

    /**
     * 英文用途
     */
    private String englishUsage;

    /**
     * 产品尺寸(长)
     */
    @FieldValid(fieldName = "产品尺寸(长)")
    private String productLength;

    /**
     * 产品尺寸(宽)
     */
    @FieldValid(fieldName = "产品尺寸(宽)")
    private String productWidth;

    /**
     * 产品尺寸(高)
     */
    @FieldValid(fieldName = "产品尺寸(高)")
    private String productHeight;

    /**
     * 箱规(长)
     */
    @FieldValid(fieldName = "箱规(长)")
    private String boxLength;

    /**
     * 箱规(宽)
     */
    @FieldValid(fieldName = "箱规(宽)")
    private String boxWidth;

    /**
     * 箱规(高)
     */
    @FieldValid(fieldName = "箱规(高)")
    private String boxHeight;

    /**
     * 毛重
     */
    @FieldValid(fieldName = "毛重",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String grossWeight;

    /**
     * 净重
     */
    @FieldValid(fieldName = "净重",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String netWeight;

    /**
     * 单箱重量
     */
    @FieldValid(fieldName = "单箱重量",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxWeight;

    /**
     * 单箱数量
     */
    @FieldValid(fieldName = "单箱数量",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String boxQty;

    /**
     * 仓位
     */
    @FieldValid(fieldName = "推荐仓位(小货区)")
    private String warehouseLocation;

    /**
     * 仓位
     */
    @FieldValid(fieldName = "推荐仓位(大货区)")
    private String warehouseLocationLarge;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 产品类别id
     */
    private String categoryId;

    /**
     * 产品经理id
     */
    private String chargeId;

    /**
     * 报关产品属性Id
     */
    private String productPropertyId;
}
