package com.erp.model.plm.dto;


import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.plm.enums.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 产品sku信息导入
 * @Author Luo_WG
 * @Date 2022/9/27 16:23
 **/
@Data
@NoArgsConstructor
public class ProductDetailExcelDTO {
    /**
     * spuNo
     */
    private String spuNo;

    /**
     * sku编号
     */
    @FieldValid(fieldName = "sku编号", isNotBlank = true)
    private String skuNo;

    /**
     * 一级分类
     */
    @FieldValid(fieldName = "一级分类", isNotBlank = true)
    private String mainCategory;

    /**
     * 二级分类
     */
    @FieldValid(fieldName = "二级分类")
    private String secondaryCategory;

    /**
     * 应用分类名
     */
    @FieldValid(fieldName = "应用分类", isNotBlank = true)
    private String applicationCategoryName;

    /**
     * 产品经理
     */
    @FieldValid(fieldName = "产品经理", isNotBlank = true,maxLength = 20)
    private String chargeName;

    /**
     * 销售方式
     */
    @FieldValid(fieldName = "销售方式",isNotBlank = true,enumClass = SaleMethodEnum.class)
    private String saleMethod;

    /**
     * 品名
     */
    @FieldValid(fieldName = "品名", isNotBlank = true, maxLength = 255)
    private String name;

    /**
     * 品名（英文）
     */
    @FieldValid(fieldName = "品名（英文）", isNotBlank = true, maxLength = 255)
    private String nameEn;


    /**
     * 产品类型
     */
    @FieldValid(fieldName = "产品类型", isNotBlank = true, maxLength = 50,enumClass = ProductTypeEnum.class)
    private String typeName;

    /**
     * 关联产品
     */
    @FieldValid(fieldName = "关联产品", maxLength = 64)
    private String iterateRefSkuNo;

    /**
     * 产品属性
     */
    @FieldValid(fieldName = "产品属性", isNotBlank = true, maxLength = 255)
    private String property;

    /**
     * 品牌
     */
    @FieldValid(fieldName = "品牌", isNotBlank = true, maxLength = 30)
    private String brandName;

    /**
     * 产品开发状态
     */
    @FieldValid(fieldName = "产品开发状态",enumClass = ProductDetailStateEnum.class)
    private String productStateName;

    /**
     * 产品等级
     */
    @FieldValid(fieldName = "产品等级",enumClass = GradeEnum.class)
    private String grade;

    /**
     * 销售渠道
     */
    @FieldValid(fieldName = "销售渠道", isNotBlank = true)
    private String salesChannel;

    /**
     * 是否客户定制
     */
    @FieldValid(fieldName = "是否客户定制", isNotBlank = true, fieldValues = "是,否")
    private String isCustomized;

    /**
     * 模具成本(￥)
     */
    @FieldValid(fieldName = "模具成本(￥)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String moldCost;

    /**
     * 委托开发成本(￥)
     */
    @FieldValid(fieldName = "模具成本(￥)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String entrustedDevelopCost;

    /**
     * 产品卖点
     */
    private String sellSpot;

    /**
     * 产品功能描述
     */
    private String functionDesc;

    /**
     * 产品用途
     */
    private String usageDesc;

    /**
     * 计划上市时间
     */
    private String planListingTimeStr;

    /**
     * 存在侵权风险(是/否)
     */
    @FieldValid(fieldName = "存在侵权风险", fieldValues = "是,否")
    private String pirateRisk;

    /**
     * 单位
     */
    private String unitName;

    /**
     * 主要材质
     */
    private String materials;

    /**
     * 预计立项成本(￥)
     */
    @FieldValid(fieldName = "预计立项成本(￥)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String projectApprovalCost;

    /**
     * 实际量产成本(￥)
     */
    @FieldValid(fieldName = "实际量产成本(￥)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String massCost;

    /**
     * 预计项目成本(￥)
     */
    @FieldValid(fieldName = "预计项目成本(￥)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String projectCost;

    /**
     *税率
     */
    @FieldValid(fieldName = "税率", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    private String taxRate;

    /**
     *目标含税成本(￥)
     */
    @FieldValid(fieldName = "目标含税成本(￥)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String targetTaxCost;

    /**
     *目标不含税成本(￥)
     */
    @FieldValid(fieldName = "目标不含税成本(￥)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String targetNoTaxCost;

    /**
     *标准零售价(￥)
     */
    @FieldValid(fieldName = "标准零售价(￥)", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String retailPrice;

    /**
     * ean码
     */
    private String ean;

    /**
     * MOQ(最小起订量)
     */
    @FieldValid(fieldName = "MOQ(最小起订量)",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String moq;

    /**
     * 试产数量
     */
    @FieldValid(fieldName = "试产数量",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String trialProductionQty;

    /**
     * 首批量产数量
     */
    @FieldValid(fieldName = "首批量产数量",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String firstMassQty;

    /**
     * 计划首批下单量
     */
    @FieldValid(fieldName = "计划首批下单量",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String planOrderQty;

    /**
     * 实际首批到货量
     */
    @FieldValid(fieldName = "实际首批到货量",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String actualArrivalQty;

    /**
     * 预计首批到货时间
     */
    @FieldValid(fieldName = "预计首批到货时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String planArrivalTimeStr;

    /**
     * 实际首批到货时间
     */
    @FieldValid(fieldName = "实际首批到货时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String actualArrivalTimeStr;

    /**
     * 首批下单时间
     */
    @FieldValid(fieldName = "首批下单时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String placeOrderTimeStr;

    /**
     * 交货周期(天)
     */
    @FieldValid(fieldName = "交货周期(天)",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String deliveryCycle;

    /**
     * 采购员
     */
    private String purchaseUser;

    /**
     * 首批到货状态
     */
    @FieldValid(fieldName = "首批到货状态",enumClass = PurchaseStateEnum.class)
    private String arrivalState;

    /**
     * 一级供应商
     */
    private String mainSupplier;

    /**
     * 二级供应商
     */
    private String secondSupplier;

    /**
     * 年目标销量
     */
    @FieldValid(fieldName = "年目标销售量", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String yearSaleQty;

    /**
     * 年目标销售额（￥）
     */
    @FieldValid(fieldName = "年目标销售额（￥）", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String yearSaleAmount;

    /**
     * 目标月销售量
     */
    @FieldValid(fieldName = "目标月销售量", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String monthSaleQty;

    /**
     * 目标月销售额（￥）
     */
    @FieldValid(fieldName = "目标月销售额（￥）", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    private String monthSaleAmount;

    /**
     * 首季度目标销量
     */
    @FieldValid(fieldName = "首季度目标销量", isNotBlank = true, formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    private String targetSalesQty;

    /**
     * 销售国家
     */
    @FieldValid(fieldName = "销售国家", isNotBlank = true)
    private String saleCountry;

    /**
     * 图片是否完成
     */
    @FieldValid(fieldName = "图片是否完成", isNotBlank = true, fieldValues = "是,否")
    private String isFinishedImg;

    /**
     * 视频是否完成
     */
    @FieldValid(fieldName = "视频是否完成", isNotBlank = true, fieldValues = "是,否")
    private String isFinishedVideo;

    /**
     * 退市时间
     */
    @FieldValid(fieldName = "退市时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    private String delistingTimeStr;

    /**
     * 销售状态
     */
    @FieldValid(fieldName = "销售状态", isNotBlank = true, enumClass = SaleStateEnum.class)
    private String saleState;

    /**
     * 产品上市(含培训)资料链接
     */
    private String dataUrl;

    /**
     * 是否可销售
     */
    @FieldValid(fieldName = "是否可销售", isNotBlank = true, fieldValues = "是,否")
    private String isMarketable;

    /**
     * 销售平台
     */
    @FieldValid(fieldName = "销售平台", isNotBlank = true, enumClass = ProductSalesPlatformEnum.class)
    private String salesPlatform;

    /**
     * 报关产品属性
     */
    private String productProperty;

    /**
     * 保险属性
     */
    @FieldValid(fieldName = "保险属性", isNotBlank = true)
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
    @FieldValid(fieldName = "产品尺寸(长)(cm)")
    private String productLength;

    /**
     * 产品尺寸(宽)
     */
    @FieldValid(fieldName = "产品尺寸(宽)(cm)")
    private String productWidth;

    /**
     * 产品尺寸(高)
     */
    @FieldValid(fieldName = "产品尺寸(高)(cm)")
    private String productHeight;

    /**
     * 箱规(长)
     */
    @FieldValid(fieldName = "箱规(长)(cm)")
    private String boxLength;

    /**
     * 箱规(宽)
     */
    @FieldValid(fieldName = "箱规(宽)(cm)")
    private String boxWidth;

    /**
     * 箱规(高)
     */
    @FieldValid(fieldName = "箱规(高)(cm)")
    private String boxHeight;

    /**
     * 毛重
     */
    @FieldValid(fieldName = "毛重(g)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String grossWeight;

    /**
     * 净重
     */
    @FieldValid(fieldName = "净重(g)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    private String netWeight;

    /**
     * 单箱重量
     */
    @FieldValid(fieldName = "单箱重量(kg)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
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
