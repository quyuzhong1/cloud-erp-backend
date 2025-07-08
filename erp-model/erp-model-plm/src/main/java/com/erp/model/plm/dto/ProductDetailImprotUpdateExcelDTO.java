package com.erp.model.plm.dto;


import com.alibaba.excel.annotation.ExcelProperty;
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
public class ProductDetailImprotUpdateExcelDTO {
    /**
     * spuNo
     */
    @ExcelProperty("spu")
    private String spuNo;

    /**
     * sku编号
     */
    @FieldValid(fieldName = "sku编号", isNotBlank = true )
    @ExcelProperty("*sku编号")
    private String skuNo;

    /**
     * 一级分类
     */
    @FieldValid(fieldName = "一级分类")
    @ExcelProperty("一级分类")
    private String mainCategory;

    /**
     * 二级分类
     */
    @FieldValid(fieldName = "二级分类")
    @ExcelProperty("二级分类")
    private String secondaryCategory;

    /**
     * 应用分类名
     */
    @FieldValid(fieldName = "应用分类")
    @ExcelProperty("应用分类")
    private String applicationCategoryName;

    /**
     * 产品经理
     */
    @FieldValid(fieldName = "产品经理" ,maxLength = 20)
    @ExcelProperty("产品经理")
    private String chargeName;

    /**
     * 销售方式
     */
    @FieldValid(fieldName = "销售方式" ,enumClass = SaleMethodEnum.class)
    @ExcelProperty("销售方式")
    private String saleMethod;

    /**
     * 品名
     */
    @FieldValid(fieldName = "品名" , maxLength = 255)
    @ExcelProperty("品名")
    private String name;

    /**
     * 品名（英文）
     */
    @FieldValid(fieldName = "品名（英文）" , maxLength = 255)
    @ExcelProperty("品名（英文）")
    private String nameEn;


    /**
     * 产品类型
     */
    @FieldValid(fieldName = "产品类型" , maxLength = 50,enumClass = ProductTypeEnum.class)
    @ExcelProperty("产品类型")
    private String typeName;

    /**
     * 关联产品
     */
    @FieldValid(fieldName = "关联产品", maxLength = 64)
    @ExcelProperty("迭代产品")
    private String iterateRefSkuNo;

    /**
     * 产品属性
     */
    @FieldValid(fieldName = "产品属性" , maxLength = 255)
    @ExcelProperty("产品属性")
    private String property;

    /**
     * 品牌
     */
    @FieldValid(fieldName = "品牌" , maxLength = 30)
    @ExcelProperty("品牌")
    private String brandName;

    /**
     * 产品开发状态
     */
    @FieldValid(fieldName = "产品开发状态",enumClass = ProductDetailStateEnum.class)
    @ExcelProperty("产品开发状态")
    private String productStateName;

    /**
     * 产品等级
     */
    @FieldValid(fieldName = "产品等级",enumClass = GradeEnum.class)
    @ExcelProperty("产品等级")
    private String grade;

    /**
     * 销售渠道
     */
    @FieldValid(fieldName = "销售渠道" )
    @ExcelProperty("销售渠道")
    private String salesChannel;

    /**
     * 是否客户定制
     */
    @FieldValid(fieldName = "是否客户定制" , fieldValues = "是,否")
    @ExcelProperty("是否客户定制")
    private String isCustomized;

    /**
     * 模具成本(￥)
     */
    @FieldValid(fieldName = "模具成本(￥)" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("模具成本(￥)")
    private String moldCost;

    /**
     * 委托开发成本(￥)
     */
    @FieldValid(fieldName = "委托开发成本(￥)" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("委托开发成本(￥)")
    private String entrustedDevelopCost;

    /**
     * 产品卖点
     */
    @ExcelProperty("产品卖点")
    private String sellSpot;

    /**
     * 产品功能描述
     */
    @ExcelProperty("产品功能描述")
    private String functionDesc;

    /**
     * 产品用途
     */
    @ExcelProperty("产品用途")
    private String usageDesc;

    /**
     * 计划上市时间
     */
    @ExcelProperty("计划上市时间")
    private String planListingTime;

    /**
     * 存在侵权风险(是/否)
     */
    @FieldValid(fieldName = "存在侵权风险", fieldValues = "是,否")
    @ExcelProperty("存在侵权风险(是/否)")
    private String pirateRisk;

    /**
     * 单位
     */
    @ExcelProperty("单位")
    private String unitName;

    /**
     * 主要材质
     */
    @ExcelProperty("主要材质")
    private String materials;

    /**
     * 预计立项成本(￥)
     */
    @FieldValid(fieldName = "预计立项成本(￥)" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("预计立项成本(￥)")
    private String projectApprovalCost;

    /**
     * 实际量产成本(￥)
     */
    @FieldValid(fieldName = "实际量产成本(￥)" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("实际量产成本(￥)")
    private String massCost;

    /**
     * 预计项目成本(￥)
     */
    @FieldValid(fieldName = "预计项目成本(￥)" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("预计项目成本(￥)")
    private String projectCost;

    /**
     *税率
     */
    @FieldValid(fieldName = "税率" , formatPattern = FieldFormatPatternTypeEnum.DECIMAL)
    @ExcelProperty("税率")
    private String taxRate;

    /**
     *目标含税成本(￥)
     */
    @FieldValid(fieldName = "目标含税成本(￥)" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("目标含税成本(￥)")
    private String targetTaxCost;

    /**
     *目标不含税成本(￥)
     */
    @FieldValid(fieldName = "目标不含税成本(￥)" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("目标不含税成本(￥)")
    private String targetNoTaxCost;

    /**
     *标准零售价(￥)
     */
    @FieldValid(fieldName = "标准零售价(￥)" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("标准零售价(￥)")
    private String retailPrice;

    /**
     * ean码
     */
    @ExcelProperty("ean码")
    private String ean;

    /**
     * MOQ(最小起订量)
     */
    @FieldValid(fieldName = "MOQ(最小起订量)",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    @ExcelProperty("MOQ(最小起订量)")
    private String moq;

    /**
     * 试产数量
     */
    @FieldValid(fieldName = "试产数量",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    @ExcelProperty("试产数量")
    private String trialProductionQty;

    /**
     * 首批量产数量
     */
    @FieldValid(fieldName = "首批量产数量",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    @ExcelProperty("首批量产数量")
    private String firstMassQty;

    /**
     * 计划首批下单量
     */
    @FieldValid(fieldName = "计划首批下单量",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    @ExcelProperty("计划首批下单量")
    private String planOrderQty;

    /**
     * 实际首批到货量
     */
    @FieldValid(fieldName = "实际首批到货量",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    @ExcelProperty("实际首批到货量")
    private String actualArrivalQty;

    /**
     * 预计首批到货时间
     */
    @FieldValid(fieldName = "预计首批到货时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    @ExcelProperty("预计首批到货时间")
    private String planArrivalTime;

    /**
     * 实际首批到货时间
     */
    @FieldValid(fieldName = "实际首批到货时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    @ExcelProperty("实际首批到货时间")
    private String actualArrivalTime;

    /**
     * 首批下单时间
     */
    @FieldValid(fieldName = "首批下单时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    @ExcelProperty("首批下单时间")
    private String placeOrderTime;

    /**
     * 交货周期(天)
     */
    @FieldValid(fieldName = "交货周期(天)",formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    @ExcelProperty("交货周期(天)")
    private String deliveryCycle;

    /**
     * 采购员
     */
    @ExcelProperty("采购员")
    private String purchaseUser;

    /**
     * 首批到货状态
     */
    @FieldValid(fieldName = "首批到货状态",enumClass = PurchaseStateEnum.class)
    @ExcelProperty("首批到货状态")
    private String arrivalState;

    /**
     * 一级供应商
     */
    @ExcelProperty("一级供应商")
    private String mainSupplier;

    /**
     * 二级供应商
     */
    @ExcelProperty("二级供应商")
    private String secondSupplier;

    /**
     * 年目标销量
     */
    @FieldValid(fieldName = "年目标销售量" , formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    @ExcelProperty("年目标销量")
    private String yearSaleQty;

    /**
     * 年目标销售额（￥）
     */
    @FieldValid(fieldName = "年目标销售额（￥）" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("年目标销售额（￥）")
    private String yearSaleAmount;

    /**
     * 目标月销售量
     */
    @FieldValid(fieldName = "目标月销售量" , formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    @ExcelProperty("目标月销售量")
    private String monthSaleQty;

    /**
     * 目标月销售额（￥）
     */
    @FieldValid(fieldName = "目标月销售额（￥）" , formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("目标月销售额（￥）")
    private String monthSaleAmount;

    /**
     * 首季度目标销量
     */
    @FieldValid(fieldName = "首季度目标销量" , formatPattern = FieldFormatPatternTypeEnum.INTEGER)
    @ExcelProperty("首季度目标销量")
    private String targetSalesQty;

    /**
     * 销售国家
     */
    @FieldValid(fieldName = "销售国家" )
    @ExcelProperty("销售国家")
    private String saleCountry;

    /**
     * 图片是否完成
     */
    @FieldValid(fieldName = "图片是否完成" , fieldValues = "是,否")
    @ExcelProperty("图片是否完成")
    private String isFinishedImg;

    /**
     * 视频是否完成
     */
    @FieldValid(fieldName = "视频是否完成" , fieldValues = "是,否")
    @ExcelProperty("视频是否完成")
    private String isFinishedVideo;

    /**
     * 退市时间
     */
    @FieldValid(fieldName = "退市时间",formatPattern = FieldFormatPatternTypeEnum.DATE)
    @ExcelProperty("退市时间")
    private String delistingTime;

    /**
     * 销售状态
     */
    @FieldValid(fieldName = "销售状态" , enumClass = SaleStateEnum.class)
    @ExcelProperty("销售状态")
    private String saleState;

    /**
     * 产品上市(含培训)资料链接
     */
    @ExcelProperty("产品上市(含培训)资料链接")
    private String dataUrl;

    /**
     * 是否可销售
     */
    @FieldValid(fieldName = "是否可销售" , fieldValues = "是,否")
    @ExcelProperty("是否可销售")
    private String isMarketable;

    /**
     * 销售平台
     */
    @FieldValid(fieldName = "销售平台" , enumClass = ProductSalesPlatformEnum.class)
    @ExcelProperty("销售平台")
    private String salesPlatform;

    /**
     * 报关产品属性
     */
    @ExcelProperty("报关产品属性")
    private String productProperty;

    /**
     * 保险属性
     */
    @FieldValid(fieldName = "保险属性")
    @ExcelProperty("保险属性")
    private String insuranceProperty;

    /**
     * 报关申报价（$）
     */
    @FieldValid(fieldName = "报关申报价（$）",formatPattern = FieldFormatPatternTypeEnum.AMOUNT)
    @ExcelProperty("报关申报价（$）")
    private String declarePrice;

    /**
     * 报关中文名
     */
    @ExcelProperty("报关中文名")
    private String declareChineseName;

    /**
     * 报关英文名
     */
    @ExcelProperty("报关英文名")
    private String declareEnglishName;

    /**
     * 中国海关编码
     */
    @ExcelProperty("中国海关编码")
    private String customsCode;

    /**
     * 报关型号
     */
    @ExcelProperty("报关型号")
    private String declareModel;

    /**
     * 报关单位
     */
    @ExcelProperty("报关单位")
    private String declareUnit;

    /**
     * 申报要素
     */
    @ExcelProperty("申报要素")
    private String declareElement;

    /**
     * 英文材质
     */
    @ExcelProperty("英文材质")
    private String englishMaterial;

    /**
     * 英文用途
     */
    @ExcelProperty("英文用途")
    private String englishUsage;

    /**
     * 产品尺寸(长)
     */
    @FieldValid(fieldName = "产品尺寸(长)(cm)")
    @ExcelProperty("产品尺寸(长)(cm)")
    private String productLength;

    /**
     * 产品尺寸(宽)
     */
    @FieldValid(fieldName = "产品尺寸(宽)(cm)")
    @ExcelProperty("产品尺寸(宽)(cm)")
    private String productWidth;

    /**
     * 产品尺寸(高)
     */
    @FieldValid(fieldName = "产品尺寸(高)(cm)")
    @ExcelProperty("产品尺寸(高)(cm)")
    private String productHeight;

    /**
     * 箱规(长)
     */
    @FieldValid(fieldName = "箱规(长)(cm)")
    @ExcelProperty("箱规(长)(cm)")
    private String boxLength;

    /**
     * 箱规(宽)
     */
    @FieldValid(fieldName = "箱规(宽)(cm)")
    @ExcelProperty("箱规(宽)(cm)")
    private String boxWidth;

    /**
     * 箱规(高)
     */
    @FieldValid(fieldName = "箱规(高)(cm)")
    @ExcelProperty("箱规(高)(cm)")
    private String boxHeight;

    /**
     * 毛重
     */
    @FieldValid(fieldName = "毛重(g)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    @ExcelProperty("毛重(g)")
    private String grossWeight;

    /**
     * 净重
     */
    @FieldValid(fieldName = "净重(g)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    @ExcelProperty("净重(g)")
    private String netWeight;

    /**
     * 单箱重量
     */
    @FieldValid(fieldName = "单箱重量(kg)",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    @ExcelProperty("单箱重量(kg)")
    private String boxWeight;

    /**
     * 单箱数量
     */
    @FieldValid(fieldName = "单箱数量",formatPattern = FieldFormatPatternTypeEnum.NUMBER)
    @ExcelProperty("单箱数量")
    private String boxQty;

    /**
     * 仓位
     */
    @FieldValid(fieldName = "推荐仓位(小货区)")
    @ExcelProperty("推荐仓位(小货区)")
    private String warehouseLocation;

    /**
     * 仓位
     */
    @FieldValid(fieldName = "推荐仓位(大货区)")
    @ExcelProperty("推荐仓位(大货区)")
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
