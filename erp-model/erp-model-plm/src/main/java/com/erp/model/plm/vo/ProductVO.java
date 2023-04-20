package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Lambda
 * @Classname ProductVO
 * @Description TODO
 * @Date 2023-04-17 15:47
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductVO implements Serializable {


    /**
     * 产品包装信息
     */
    @Data
    @NoArgsConstructor
    public static class ProductPackVO {



        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;
        /**
         * sku id
         */
        private String skuId;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 产品等级
         */
        private String productGrade;

        /**
         * 产品长度
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
         * 外箱长
         */
        private BigDecimal boxLength;

        /**
         * 外箱宽
         */
        private BigDecimal boxWidth;

        /**
         * 外箱高
         */
        private BigDecimal boxHeight;

        /**
         * 产品净重
         */
        private BigDecimal productNetWeight;

        /**
         * 外箱重量
         */
        private BigDecimal boxWeight;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 产品名称
         */
        private String productName;


        /**
         * sku图片 集合
         */
        private List<String> skuImageUrlList;

        /**
         * 外箱图片集合
         */
        private List<String> boxImageUrlList;


    }
}


