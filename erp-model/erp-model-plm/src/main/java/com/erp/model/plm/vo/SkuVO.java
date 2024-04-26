package com.erp.model.plm.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Classname SkuVO

 * @Date 2023-01-11 14:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuVO implements Serializable {

    private String declareElement;
    /**
     * sku版本号
     */
    private Integer version;

    private String skuId;

    private String productId;
    /**
     * ean码
     */
    private String ean;

    /**
     * 规格类型  1：无规格  2：多规格
     */
    private Integer specType;

    /**
     * sku no
     */
    private String skuNo;

    private String skuImagesUrl;

    /**
     * 申报币种符号
     */
    private String declareCurrencySymbol;

    /**
     * 销售方式
     */
    private String saleMethod;

    /**
     * 单箱数量
     */
    private Integer unitQty;

    /**
     * 长
     */
    private BigDecimal length;
    /**
     * 宽
     */
    private BigDecimal width;
    /**
     * 高
     */
    private BigDecimal height;

    /**
     * sku 名称
     */
    private String skuName = "";


    /**
     * spu no
     */
    private String spuNo = "";


    /**
     * spu 名称
     */
    private String spuName = "";

    /**
     * 报关型号
     */
    private String declareModel;

    /**
     * 报关名称
     */
    private String declareName;

    /**
     * 变体信息
     */
    private String variantProperty;

    /**
     * 主要材质
     */
    private String materials;

    /**
     * 功能描述
     */
    private String functionDesc;
    /**
     * 产品属性
     */
    private String productPropertyId;


    /**
     * 产品等级
     */
    private String productGrade;

    /**
     * 产品品牌
     */
    private String brandName;

    /**
     * 分类id
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
     */
    private Integer saleState;

    /**
     * 单位
     */
    private String unitName;

    /**
     * 产品状态
     */
    private Integer status;

    /**
     * 最小起订量
     */
    private Integer moq;

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 仓位
     */
    private String warehouseLocation;

    /**
     * 毛重
     */
    private BigDecimal grossWeight;

    /**
     * 目标含税成本
     */
    private BigDecimal targetTaxCost;

    /**
     * 实际含税成本(含税)
     */
    private BigDecimal actualTaxCost;
    /**
     * 成本价格（不含税）
     */
    private BigDecimal notTaxCostPrice;

    /**
     * 标准零售价
     */
    private BigDecimal retailPrice;

    /**
     * 一级供应商
     */
    private String mainSupplier;

    /**
     * 一级供应商名称
     */
    private String mainSupplierName;

    /**
     * 二级供应商
     */
    private String secondSupplier;

    /**
     * 二级供应商名称
     */
    private String secondSupplierName;

    /**
     * 尺寸
     */
    private String productSize;

    /**
     * 净重
     */
    private BigDecimal netWeight;

    /**
     * 是否是捆绑商品:true=是，false=否
     * (可能字段为null，需添加查询)
     */
    private Boolean isCombination;

    public String checkAndGetSkuImagesUrl() {
        if (StringUtils.isBlank(this.skuImagesUrl)){
            return "";
        }
        return this.skuImagesUrl;
    }
}
