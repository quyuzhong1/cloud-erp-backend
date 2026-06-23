package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lambda
 * @Classname QcProductDTO

 * @Date 2023-04-14 15:31
 * @Created by yl
 */
@Data
public class QcProductDTO {
    private QcProductDTO() {
        throw new IllegalStateException("Utility QcProductDTO class");
    }
    /**
     * 暂存 质检产品信息
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable {
        private static final long serialVersionUID = 1905122041950251207L;

        private String id;


        /**
         * sku
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;


        private String id;


        /**
         * 产品名称
         */
        private String productName;


        /**
         * 产品等级
         */
        private String productGrade;

        /**
         * 产品变体信息
         */
        private String variantProperty;


        /**
         * sku
         */
        private String skuId;



        /**
         * sku
         */
        private String skuNo;


        /**
         * 产品长
         */
        private BigDecimal productLength;

        /**
         * 产品宽
         */
        private BigDecimal productWidth;

        /**
         * 产品高
         */
        private BigDecimal productHeight;

        /**
         * 箱长
         */
        private BigDecimal boxLength;

        /**
         * 箱宽
         */
        private BigDecimal boxWidth;

        /**
         * 箱高
         */
        private BigDecimal boxHeight;

        /**
         * 整箱数量
         */
        private Integer boxQty;

        /**
         * 产品净重
         */
        private BigDecimal productNetWeight;

        /**
         * 外箱重量
         */
        private BigDecimal boxWeight;

        /**
         * 外箱图片地址集合
         */
        private List<String> boxImageUrlList;

        /**
         * 外箱图片名称集合
         */
        private List<String> boxImageNameList;


        /**
         * 产品图片地址集合
         */
        private List<String> productImageUrlList;

        /**
         * 产品名称地址集合
         */
        private List<String> productImageNameList;
    }
}
