package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
     * 单箱数量
     */
    private Integer boxQty;
    /**
     * 销售方式
     */
    private String saleMethod;

    /**
     * 单箱数量
     */
    private Integer unitQty;

    /**
     * sku 名称
     */
    private String skuName;


    /**
     * spu no
     */
    private String spuNo;


    /**
     * spu 名称
     */
    private String spuName;

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
     * 仓位-推荐仓位(小货区)
     */
    private String warehouseLocation;
    /**
     * 推荐仓位(大货区)
     */
    private String warehouseLocationLarge;

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
     * 税率
     */
    private BigDecimal taxRate;
    /**
     * 成本价格（不含税）
     */
    private BigDecimal notTaxCostPrice;
    /**
     * 成本价格来源
     */
    private String costSource;
    //材料成本
    private BigDecimal productCost;
    //头程运费
    private BigDecimal firstMileShippingCost;
    //清关税费
    private BigDecimal clearanceCustomsTax;

    /**
     * 标准零售价
     */
    private BigDecimal retailPrice = BigDecimal.ZERO;

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
     * 产品尺寸（长）
     */
    private BigDecimal productLength;

    /**
     * 产品尺寸（宽）
     */
    private BigDecimal productWidth;

    /**
     * 产品尺寸（高）
     */
    private BigDecimal productHeight;

    /**
     * 净重
     */
    private BigDecimal netWeight;

    /**
     * 属性
     */
    private PropertyDTO propertyDTO;


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
    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 审核状态
         */
        private List<Integer> statusList;
    }

    @Data
    @NoArgsConstructor
    public static class PropertyDTO implements Serializable{
        /**
         * 是否带电
         */
        private Boolean isElectric = false;
        /**
         * 带电属性名
         */
        private String electricName;
        /**
         * 是否带磁
         */
        private Boolean isMagnetism = false;

        /**
         * 带磁属性名
         */
        private String magnetismName;
        /**
         * 是否液体
         */
        private Boolean isLiquid = false;

        /**
         * 液体属性名
         */
        private String liquidName;
        /**
         * 是否木
         */
        private Boolean isWood = false;

        /**
         * 木属性名
         */
        private String woodName;
        /**
         * 是否粉末
         */
        private Boolean isPowder = false;

        /**
         * 粉末属性名
         */
        private String powderName;
        /**
         * 是否膏体
         */
        private Boolean isPlaster = false;

        /**
         * 膏体属性名
         */
        private String plasterName;
        /**
         * 是否刀具
         */
        private Boolean isCuttingTool = false;

        /**
         * 刀具属性名
         */
        private String cuttingToolName;
        /**
         * 是否其他
         */
        private Boolean isOther = false;

        /**
         * 其他属性名
         */
        private String otherName;
        /**
         * 汇总标识
         * - 属性包含：带电池，带电池（内置不可拆卸），带电池（内置可拆卸），带电池（纯电），显示电标识
         * - 属性包含：带磁，显示磁标识
         * - 属性包含：液体，显示液标识
         * - 属性包含：木，显示木标识
         * - 属性包含：粉末，显示粉标识
         * - 属性包含：膏体，显示膏标识
         * - 属性包含：刀具，显示刀标识
         * - 属性包含：充电盒CCC，整体CCC，不显示属性标识
         * - 未选择任何属性：不显示属性标识
         */
        private List<String> markList;
    }

    @Data
    @NoArgsConstructor
    public static class ProductChargeInfoDTO {
        /**
         * 产品id
         */
        private String productId;
        /**
         *
         */
        private String spuNo;
        /**
         * 产品名称
         */
        private String spuName;
        /**
         *
         */
        private String skuId;
        /**
         *
         */
        private String skuNo;
        /**
         *
         */
        private String skuName;
        /**
         * 产品经理id
         */
        private String chargeId;
        /**
         * 产品经理
         */
        private String chargeName;
        /**
         * 项目经理id
         */
        private String projectChargeId;
        /**
         * 项目经理
         */
        private String projectChargeName;

    }
}
