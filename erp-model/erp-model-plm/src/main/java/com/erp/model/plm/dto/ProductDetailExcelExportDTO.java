package com.erp.model.plm.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
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
    private byte[] image;

    /**
     * spuNo
     */
    @ExcelProperty("spuNo")
    private String spuNo;
    /**
     * sku编号
     */
    @ExcelProperty("sku编号")
    private String skuNo;

    /**
     * 一级分类
     */
    @ExcelProperty("一级分类")
    private String mainCategory;

    /**
     * 二级分类
     */
    @ExcelProperty("二级分类")
    private String secondaryCategory;

    /**
     * 产品经理
     */
    @ExcelProperty("产品经理")
    private String chargeName;

    /**
     * 销售方式
     */
    @ExcelProperty("销售方式")
    private String saleMethod;

    /**
     * 品名
     */
    @ExcelProperty("品名")
    private String name;

    /**
     * 品名（英文）
     */
    @ExcelProperty("品名（英文）")
    private String nameEn;
    /**
     * 关联产品
     */
    @ExcelProperty("关联产品")
    private String iterateRefSkuNo;

    /**
     * 产品属性
     */
    @ExcelProperty("产品属性")
    private String property;

    /**
     * 品牌
     */
    @ExcelProperty("品牌")
    private String brandName;

    /**
     * 产品开发状态
     */
    @ExcelProperty("产品开发状态")
    private String productStateName;

    /**
     * 产品等级
     */
    @ExcelProperty("产品等级")
    private String grade;

    /**
     * 销售渠道
     */
    @ExcelProperty("销售渠道")
    private String salesChannel;

    /**
     * 是否客户定制
     */
    @ExcelProperty("是否客户定制")
    private String isCustomized;

    /**
     * 模具成本(￥)
     */
    @ExcelProperty("模具成本(￥)")
    private String moldCost;

    /**
     * 委托开发成本(￥)
     */
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
    @ExcelProperty("预计立项成本(￥)")
    private String projectApprovalCost;

    /**
     * 实际量产成本(￥)
     */
    @ExcelProperty("实际量产成本(￥)")
    private String massCost;

    /**
     * 预计项目成本(￥)
     */
    @ExcelProperty("预计项目成本(￥)")
    private String projectCost;

    /**
     *税率
     */
    @ExcelProperty("税率")
    private String taxRate;

    /**
     *目标含税成本(￥)
     */
    @ExcelProperty("目标含税成本(￥)")
    private String targetTaxCost;

    /**
     *目标不含税成本(￥)
     */
    @ExcelProperty("目标不含税成本(￥)")
    private String targetNoTaxCost;

    /**
     *标准零售价(￥)
     */
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
    @ExcelProperty("MOQ(最小起订量)")
    private String moq;

    /**
     * 试产数量
     */
    @ExcelProperty("试产数量")
    private String trialProductionQty;

    /**
     * 首批量产数量
     */
    @ExcelProperty("首批量产数量")
    private String firstMassQty;

    /**
     * 计划首批下单量
     */
    @ExcelProperty("计划首批下单量")
    private String planOrderQty;

    /**
     * 实际首批到货量
     */
    @ExcelProperty("实际首批到货量")
    private String actualArrivalQty;

    /**
     * 预计首批到货时间
     */
    @ExcelProperty("预计首批到货时间")
    private String planArrivalTime;

    /**
     * 实际首批到货时间
     */
    @ExcelProperty("实际首批到货时间")
    private String actualArrivalTime;

    /**
     * 首批下单时间
     */
    @ExcelProperty("首批下单时间")
    private String placeOrderTime;

    /**
     * 交货周期(天)
     */
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
    @ExcelProperty("年目标销量")
    private String yearSaleQty;

    /**
     * 年目标销售额（￥）
     */
    @ExcelProperty("年目标销售额（￥）")
    private String yearSaleAmount;

    /**
     * 目标月销售量
     */
    @ExcelProperty("目标月销售量")
    private String monthSaleQty;

    /**
     * 目标月销售额（￥）
     */
    @ExcelProperty("目标月销售额（￥）")
    private String monthSaleAmount;

    /**
     * 首季度目标销量
     */
    @ExcelProperty("首季度目标销量")
    private String targetSalesQty;

    /**
     * 销售国家
     */
    @ExcelProperty("销售国家")
    private String saleCountry;

    /**
     * 图片是否完成
     */
    @ExcelProperty("图片是否完成")
    private String isFinishedImg;

    /**
     * 视频是否完成
     */
    @ExcelProperty("视频是否完成")
    private String isFinishedVideo;

    /**
     * 退市时间
     */
    @ExcelProperty("退市时间")
    private String delistingTime;

    /**
     * 销售状态
     */
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
    @ExcelProperty("是否可销售")
    private String isMarketable;

    /**
     * 销售平台
     */
    @ExcelProperty("销售平台")
    private String salesPlatform;

    /**
     * 报关产品属性
     */
    @ExcelProperty("报关产品属性")
    private String productProperty;

    /**
     * 报关申报价（$）
     */
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
    @ExcelProperty("产品尺寸(长)")
    private String productLength;

    /**
     * 产品尺寸(宽)
     */
    @ExcelProperty("产品尺寸(宽)")
    private String productWidth;

    /**
     * 产品尺寸(高)
     */
    @ExcelProperty("产品尺寸(高)")
    private String productHeight;

    /**
     * 箱规(长)
     */
    @ExcelProperty("箱规(长)")
    private String boxLength;

    /**
     * 箱规(宽)
     */
    @ExcelProperty("箱规(宽)")
    private String boxWidth;

    /**
     * 箱规(高)
     */
    @ExcelProperty("箱规(高)")
    private String boxHeight;

    /**
     * 毛重
     */
    @ExcelProperty("毛重")
    private String grossWeight;

    /**
     * 净重
     */
    @ExcelProperty("净重")
    private String netWeight;

    /**
     * 单箱重量
     */
    @ExcelProperty("单箱重量")
    private String boxWeight;

    /**
     * 单箱数量
     */
    @ExcelProperty("单箱数量")
    private String boxQty;

    /**
     * 仓位
     */
    @ExcelProperty("推荐仓位(小货区)")
    private String warehouseLocation;

    /**
     * 仓位
     */
    @ExcelProperty("推荐仓位(大货区)")
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
