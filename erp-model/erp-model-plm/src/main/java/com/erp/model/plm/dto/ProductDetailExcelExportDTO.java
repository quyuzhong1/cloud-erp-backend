package com.erp.model.plm.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.common.business.annotation.MenuCode;
import com.common.core.anno.FieldValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.plm.enums.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 产品sku信息导出
 **/
@Data
@NoArgsConstructor
@ColumnWidth(16)
@HeadRowHeight(40)
@ContentRowHeight(45)
public class ProductDetailExcelExportDTO {

    /**
     * 图片路径
     */
    @ExcelIgnore
    private String imageUrl;
    /**
     * 图片
     */
    @ExcelProperty("图片")
    @ColumnWidth(12)
    @MenuCode("plm:product:detail:listProinfo")
    private byte[] image;

    /**
     * spuNo
     */
    @ExcelProperty("spuNo")
    @MenuCode("plm:product:detail:listProinfo")
    private String spuNo;
    /**
     * sku编号
     */
    @ExcelProperty("sku编号")
    @MenuCode("plm:product:detail:listProinfo")
    private String skuNo;

    /**
     * 一级分类
     */
    @ExcelProperty("一级分类")
    @MenuCode("plm:product:detail:listProinfo")
    private String mainCategory;

    /**
     * 二级分类
     */
    @ExcelProperty("二级分类")
    @MenuCode("plm:product:detail:listProinfo")
    private String secondaryCategory;

    /**
     * 产品经理
     */
    @ExcelProperty("产品经理")
    @MenuCode("plm:product:detail:listProinfo")
    private String chargeName;

    /**
     * 销售方式
     */
    @ExcelProperty("销售方式")
    @MenuCode("plm:product:detail:listProinfo")
    private String saleMethod;

    /**
     * 品名
     */
    @ExcelProperty("品名")
    @MenuCode("plm:product:detail:listProinfo")
    private String name;

    /**
     * 品名（英文）
     */
    @ExcelProperty("品名（英文）")
    @MenuCode("plm:product:detail:listProinfo")
    private String nameEn;
    /**
     * 关联产品
     */
    @ExcelProperty("关联产品")
    @MenuCode("plm:product:detail:listProinfo")
    private String iterateRefSkuNo;

    /**
     * 产品属性
     */
    @ExcelProperty("产品属性")
    @MenuCode("plm:product:detail:listProinfo")
    private String property;

    /**
     * 品牌
     */
    @ExcelProperty("品牌")
    @MenuCode("plm:product:detail:listProinfo")
    private String brandName;

    /**
     * 产品开发状态
     */
    @ExcelProperty("产品开发状态")
    @MenuCode("plm:product:detail:listProinfo")
    private String productStateName;

    /**
     * 产品等级
     */
    @ExcelProperty("产品等级")
    @MenuCode("plm:product:detail:listProinfo")
    private String grade;

    /**
     * 销售渠道
     */
    @ExcelProperty("销售渠道")
    @MenuCode("plm:product:detail:listProinfo")
    private String salesChannel;

    /**
     * 是否客户定制
     */
    @ExcelProperty("是否客户定制")
    @MenuCode("plm:product:detail:listProinfo")
    private String isCustomized;

    /**
     * 模具成本(￥)
     */
    @ExcelProperty("模具成本(￥)")
    @MenuCode("plm:product:detail:listProinfo")
    private String moldCost;

    /**
     * 委托开发成本(￥)
     */
    @ExcelProperty("委托开发成本(￥)")
    @MenuCode("plm:product:detail:listProinfo")
    private String entrustedDevelopCost;

    /**
     * 产品卖点
     */
    @ExcelProperty("产品卖点")
    @MenuCode("plm:product:detail:listProinfo")
    private String sellSpot;

    /**
     * 产品功能描述
     */
    @ExcelProperty("产品功能描述")
    @MenuCode("plm:product:detail:listProinfo")
    private String functionDesc;

    /**
     * 产品用途
     */
    @ExcelProperty("产品用途")
    @MenuCode("plm:product:detail:listProinfo")
    private String usageDesc;

    /**
     * 计划上市时间
     */
    @ExcelProperty("计划上市时间")
    @MenuCode("plm:product:detail:listProinfo")
    private String planListingTime;

    /**
     * 存在侵权风险(是/否)
     */
    @ExcelProperty("存在侵权风险(是/否)")
    @MenuCode("plm:product:detail:listProinfo")
    private String pirateRisk;

    /**
     * 单位
     */
    @ExcelProperty("单位")
    @MenuCode("plm:product:detail:listProinfo")
    private String unitName;

    /**
     * 主要材质
     */
    @ExcelProperty("主要材质")
    @MenuCode("plm:product:detail:listProinfo")
    private String materials;

    /**
     * 预计立项成本(￥)
     */
    @ExcelProperty("预计立项成本(￥)")
    @MenuCode("plm:product:detail:listCost")
    private String projectApprovalCost;

    /**
     * 实际量产成本(￥)
     */
    @ExcelProperty("实际量产成本(￥)")
    @MenuCode("plm:product:detail:listCost")
    private String massCost;

    /**
     * 预计项目成本(￥)
     */
    @ExcelProperty("预计项目成本(￥)")
    @MenuCode("plm:product:detail:listCost")
    private String projectCost;

    /**
     *税率
     */
    @ExcelProperty("税率")
    @MenuCode("plm:product:detail:listCost")
    private String taxRate;

    /**
     *目标含税成本(￥)
     */
    @ExcelProperty("目标含税成本(￥)")
    @MenuCode("plm:product:detail:listCost")
    private String targetTaxCost;

    /**
     *目标不含税成本(￥)
     */
    @ExcelProperty("目标不含税成本(￥)")
    @MenuCode("plm:product:detail:listCost")
    private String targetNoTaxCost;

    /**
     *标准零售价(￥)
     */
    @ExcelProperty("标准零售价(￥)")
    @MenuCode("plm:product:detail:listCost")
    private String retailPrice;

    /**
     * ean码
     */
    @ExcelProperty("ean码")
    @MenuCode("plm:product:detail:listProinfo")
    private String ean;

    /**
     * MOQ(最小起订量)
     */
    @ExcelProperty("MOQ(最小起订量)")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String moq;

    /**
     * 试产数量
     */
    @ExcelProperty("试产数量")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String trialProductionQty;

    /**
     * 首批量产数量
     */
    @ExcelProperty("首批量产数量")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String firstMassQty;

    /**
     * 计划首批下单量
     */
    @ExcelProperty("计划首批下单量")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String planOrderQty;

    /**
     * 实际首批到货量
     */
    @ExcelProperty("实际首批到货量")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String actualArrivalQty;

    /**
     * 预计首批到货时间
     */
    @ExcelProperty("预计首批到货时间")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String planArrivalTime;

    /**
     * 实际首批到货时间
     */
    @ExcelProperty("实际首批到货时间")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String actualArrivalTime;

    /**
     * 首批下单时间
     */
    @ExcelProperty("首批下单时间")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String placeOrderTime;

    /**
     * 交货周期(天)
     */
    @ExcelProperty("交货周期(天)")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String deliveryCycle;

    /**
     * 采购员
     */
    @ExcelProperty("采购员")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String purchaseUser;

    /**
     * 首批到货状态
     */
    @ExcelProperty("首批到货状态")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String arrivalState;

    /**
     * 一级供应商
     */
    @ExcelProperty("一级供应商")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String mainSupplier;

    /**
     * 二级供应商
     */
    @ExcelProperty("二级供应商")
    @MenuCode("plm:product:detail:listProductPurchase")
    private String secondSupplier;

    /**
     * 年目标销量
     */
    @ExcelProperty("年目标销量")
    @MenuCode("plm:product:detail:listSale")
    private String yearSaleQty;

    /**
     * 年目标销售额（￥）
     */
    @ExcelProperty("年目标销售额（￥）")
    @MenuCode("plm:product:detail:listSale")
    private String yearSaleAmount;

    /**
     * 目标月销售量
     */
    @ExcelProperty("目标月销售量")
    @MenuCode("plm:product:detail:listSale")
    private String monthSaleQty;

    /**
     * 目标月销售额（￥）
     */
    @ExcelProperty("目标月销售额（￥）")
    @MenuCode("plm:product:detail:listSale")
    private String monthSaleAmount;

    /**
     * 首季度目标销量
     */
    @ExcelProperty("首季度目标销量")
    @MenuCode("plm:product:detail:listSale")
    private String targetSalesQty;

    /**
     * 销售国家
     */
    @ExcelProperty("销售国家")
    @MenuCode("plm:product:detail:listSale")
    private String saleCountry;

    /**
     * 图片是否完成
     */
    @ExcelProperty("图片是否完成")
    @MenuCode("plm:product:detail:listSale")
    private String isFinishedImg;

    /**
     * 视频是否完成
     */
    @ExcelProperty("视频是否完成")
    @MenuCode("plm:product:detail:listSale")
    private String isFinishedVideo;

    /**
     * 退市时间
     */
    @ExcelProperty("退市时间")
    @MenuCode("plm:product:detail:listSale")
    private String delistingTime;

    /**
     * 销售状态
     */
    @ExcelProperty("销售状态")
    @MenuCode("plm:product:detail:listSale")
    private String saleState;

    /**
     * 产品上市(含培训)资料链接
     */
    @ExcelProperty("产品上市(含培训)资料链接")
    @MenuCode("plm:product:detail:listSale")
    private String dataUrl;

    /**
     * 是否可销售
     */
    @ExcelProperty("是否可销售")
    @MenuCode("plm:product:detail:listSale")
    private String isMarketable;

    /**
     * 销售平台
     */
    @ExcelProperty("销售平台")
    @MenuCode("plm:product:detail:listSale")
    private String salesPlatform;

    /**
     * 报关产品属性
     */
    @ExcelProperty("报关产品属性")
    @MenuCode("plm:product:detail:listLogistics")
    private String productProperty;

    /**
     * 报关申报价（$）
     */
    @ExcelProperty("报关申报价（$）")
    @MenuCode("plm:product:detail:listLogistics")
    private String declarePrice;

    /**
     * 报关中文名
     */
    @ExcelProperty("报关中文名")
    @MenuCode("plm:product:detail:listLogistics")
    private String declareChineseName;

    /**
     * 报关英文名
     */
    @ExcelProperty("报关英文名")
    @MenuCode("plm:product:detail:listLogistics")
    private String declareEnglishName;

    /**
     * 中国海关编码
     */
    @ExcelProperty("中国海关编码")
    @MenuCode("plm:product:detail:listLogistics")
    private String customsCode;

    /**
     * 报关型号
     */
    @ExcelProperty("报关型号")
    @MenuCode("plm:product:detail:listLogistics")
    private String declareModel;

    /**
     * 报关单位
     */
    @ExcelProperty("报关单位")
    @MenuCode("plm:product:detail:listLogistics")
    private String declareUnit;

    /**
     * 申报要素
     */
    @ExcelProperty("申报要素")
    @MenuCode("plm:product:detail:listLogistics")
    private String declareElement;

    /**
     * 英文材质
     */
    @ExcelProperty("英文材质")
    @MenuCode("plm:product:detail:listLogistics")
    private String englishMaterial;

    /**
     * 英文用途
     */
    @ExcelProperty("英文用途")
    @MenuCode("plm:product:detail:listLogistics")
    private String englishUsage;

    /**
     * 产品尺寸(长)
     */
    @ExcelProperty("产品尺寸(长)")
    @MenuCode("plm:product:detail:listProinfo")
    private String productLength;

    /**
     * 产品尺寸(宽)
     */
    @MenuCode("plm:product:detail:listProinfo")
    @ExcelProperty("产品尺寸(宽)")
    private String productWidth;

    /**
     * 产品尺寸(高)
     */
    @ExcelProperty("产品尺寸(高)")
    @MenuCode("plm:product:detail:listProinfo")
    private String productHeight;

    /**
     * 箱规(长)
     */
    @ExcelProperty("箱规(长)")
    @MenuCode("plm:product:detail:listProinfo")
    private String boxLength;

    /**
     * 箱规(宽)
     */
    @ExcelProperty("箱规(宽)")
    @MenuCode("plm:product:detail:listProinfo")
    private String boxWidth;

    /**
     * 箱规(高)
     */
    @ExcelProperty("箱规(高)")
    @MenuCode("plm:product:detail:listProinfo")
    private String boxHeight;

    /**
     * 毛重
     */
    @ExcelProperty("毛重")
    @MenuCode("plm:product:detail:listProinfo")
    private String grossWeight;

    /**
     * 净重
     */
    @ExcelProperty("净重")
    @MenuCode("plm:product:detail:listProinfo")
    private String netWeight;

    /**
     * 单箱重量
     */
    @ExcelProperty("单箱重量")
    @MenuCode("plm:product:detail:listProinfo")
    private String boxWeight;

    /**
     * 单箱数量
     */
    @ExcelProperty("单箱数量")
    @MenuCode("plm:product:detail:listProinfo")
    private String boxQty;

    /**
     * 仓位
     */
    @ExcelProperty("推荐仓位(小货区)")
    @MenuCode("plm:product:detail:listProinfo")
    private String warehouseLocation;

    /**
     * 仓位
     */
    @ExcelProperty("推荐仓位(大货区)")
    @MenuCode("plm:product:detail:listProinfo")
    private String warehouseLocationLarge;

    /**
     * 产品类别id
     */
    @ExcelIgnore
    private String categoryId;

    /**
     * 产品经理id
     */
    @ExcelIgnore
    private String chargeId;

    /**
     * 报关产品属性Id
     */
    @ExcelIgnore
    private String productPropertyId;
}
