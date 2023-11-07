package com.erp.model.plm.dto;/**
 * @author Lambda
 * @Classname LogisticsProductDTO
 * @Description TODO
 * @Date 2023-11-06 14:05
 * @Created by yl
 */

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-06 14:05
 */
@Data
@NoArgsConstructor
public class LogisticsProductDTO {


    @Data
    @NoArgsConstructor
    public static class PagingVO {

        /**
         * sku id
         */
        private String id;

        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 分类名
         */
        private String categoryName;


        /**
         * sku no
         */
        private String skuNo;


        /**
         * sku id
         */
        private String skuId;


        /**
         * spu
         */
        private String spuNo;

        /**
         * 产品名称
         */
        private String productName;
        /**
         * 中文报关名
         */
        private String declareChineseName;
        /**
         * 产品属性
         */
        private String productPropertyName;


        /**
         * 报关申报价
         */
        private BigDecimal declarePrice;

        /**
         * 报关申报价币种
         */
        private String declareCurrency;

        /**
         * 报关申报价币种符号
         */
        private String declareCurrencySymbol;

        /**
         * 目的国申报价
         */
        private BigDecimal destDeclarePrice;

        /**
         * 目的国申报价币种
         */
        private String destCurrency;

        /**
         * 目的国申报价币种符号
         */
        private String destCurrencySymbol;

        /**
         * 申报要素
         */
        private String declareElement;


        /**
         * 报关型号
         */
        private String declareModel;


        /**
         * 原产国
         */
        private String sourceCountry;

        /**
         * sku 审核状态
         */
        private Integer approveStatus;

        /**
         * sku 审核状态名
         */
        private String approveStatusName;

        /**
         * 销售状态
         */
        private Integer salesStatus;

        /**
         * 销售状态名
         */
        private String salesStatusName;

        /**
         * 品牌名
         */
        private String brandName;

        /**
         * 产品经理
         */
        private String chargeName;

        /**
         * 产品经理id
         */
        private String chargeId;

        /**
         * 是否组合品
         */
        private Boolean isCombination;


    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 分类id
         */
        private List<String> categoryIdList;

        /**
         * skulist
         */
        private List<String> skuNoList;


        /**
         * 品名
         */
        private String productName;

        /**
         * 中文报关名
         */
        private String declareNameCn;

        /**
         * 产品属性id
         */
        private List<String> productPropertyIdList;

        /**
         * 原产国
         */
        private String sourceCountry;

        /**
         * 产品经理
         */
        private List<String> productChargeIdList;

        /**
         * spu
         */
        private String spuNo;


        /**
         * 报关型号
         */
        private String declareModel;

        /**
         * 申报要素
         */
        private String declareElement;

        /**
         * 销售状态
         */
        private List<Integer> salesStatusList;

        /**
         * 创建时间
         */
        private List<LocalDateTime> createTimeList;

        /**
         * 修改时间
         */
        private List<LocalDateTime> updateTimeList;


    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        /**
         * 产品基本信息
         */
        private ProductBaseInfoDTO productBaseInfo;


        /**
         * 报关信息
         */
        private DeclareInfoDTO declareInfo;


        /**
         * 目的国海关编码
         */
        private List<ProductCustomsDTO.ViewDTO> customsList;


    }


    /**
     * 产品基础信息
     */
    @Data
    @NoArgsConstructor
    public static class ProductBaseInfoDTO {

        /**
         * sku no
         */
        private String skuNo;


        /**
         * 产品图片
         */
        private String imagesUrl;


        private String categoryId;

        /**
         * 分类名
         */
        private String categoryName;

        /**
         * 销售状态
         */
        private Integer salesStatus;

        /**
         * 销售状态名
         */
        private String salesStatusName;


        /**
         * spu
         */
        private String spuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 产品经理
         */
        private String chargeName;


        /**
         * 品牌名
         */
        private String brandName;

        /**
         * 产品属性
         */
        private String propertyName;

        /**
         * ENA 吗
         */
        private String ena;

        /**
         * 不含税成本
         */
        private BigDecimal actualNoTaxCost;

        /**
         * 含税成本
         */
        private BigDecimal actualTaxCost;


        /**
         * 产品尺寸
         */
        private String productSize;
        /**
         * 毛重
         */
        private BigDecimal grossWeight;

        /**
         * 净重
         */
        private BigDecimal netWeight;


        /**
         * 材质
         */
        private String materials;

        /**
         * 用途
         */
        private String usageDesc;

    }

    /**
     * 报关信息
     */
    @Data
    @NoArgsConstructor
    public static class DeclareInfoDTO {

        /**
         * id
         */
        private String id;


        /**
         * sku id
         */
        private String skuId;


        /**
         * 中文报关名
         */
        private String declareChineseName;

        /**
         * 英文报关名
         */
        private String declareEnglishName;

        /**
         * 报关型号
         */
        private String declareModel;


        /**
         * 报关申报价
         */
        private BigDecimal declarePrice;

        /**
         * 报关申报价币种
         */
        private String declareCurrency;


        /**
         * 报关单位
         */
        private String declareUnit;


        /**
         * 中国海关编码
         */
        private String customsCode;


        /**
         * 目的国申报价
         */
        private BigDecimal destDeclarePrice;

        /**
         * 目的国申报价币种
         */
        private String destCurrency;


        /**
         * 申报要素
         */
        private String declareElement;


        /**
         * 境内货源地
         */
        private String sourceCargo;


        /**
         * 征免
         */
        private String exemption;

        /**
         * 原产国
         */
        private String sourceCountry;


        /**
         * 组合品申报类型
         * split 拆分
         * combine 合并
         */
        private String combinationDeclareType;


    }
}
