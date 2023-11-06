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
        private String spu;

        /**
         * 产品名称
         */
        private String productName;
        /**
         * 中文报关名
         */
        private String customsNameCn;
        /**
         * 产品属性
         */
        private String productPropertyName;


        /**
         * 报关申报价
         */
        private String customsPrice;

        /**
         * 报关申报价币种
         */
        private String customsCurrency;

        /**
         * 报关申报价币种符号
         */
        private String currencySymbol;

        /**
         * 目的国申报价
         */
        private BigDecimal destCountryPrice;

        /**
         * 目的国申报价币种
         */
        private String destCountryCurrency;

        /**
         * 目的国申报价币种符号
         */
        private String destCountryCurrencySymbol;

        /**
         * 申报要素
         */
        private String customsElement;

        /**
         * 申报要素名
         */
        private String customsElementName;

        /**
         * 报关型号
         */
        private String customsModel;

        /**
         * 报关型号名
         */
        private String customsModelName;

        /**
         * 原产国
         */
        private String sourceCountry;

        /**
         * sku 审核状态
         */
        private String approveStatus;

        /**
         * sku 审核状态名
         */
        private String approveStatusName;

        /**
         * 销售状态
         */
        private String salesStatus;

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
         * 是否组合品
         */
        private String isCombination;


    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {
        /**
         * 分类id
         */
        private  List<String> categoryIdLis;

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
        private String customsNameCn;

        /**
         * 产品属性id
         */
        private List<String> productPropertyIdList;

        /**
         * 原产国
         */
        private String  sourceCountry;

        /**
         * 产品经理
         */
        private List<String>  productChargeIdList;

        /**
         * spu
         */
        private String spu;


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
}
