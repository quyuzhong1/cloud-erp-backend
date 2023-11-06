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
    public static class PagingParamDTO  extends SortDTO {
        /**
         * 分类id
         */
        private  List<String> categoryIdList;

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
        private String  sourceCountry;

        /**
         * 产品经理
         */
        private List<String>  productChargeIdList;

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
}
