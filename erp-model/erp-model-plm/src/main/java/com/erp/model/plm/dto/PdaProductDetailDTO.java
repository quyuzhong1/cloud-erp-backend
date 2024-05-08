package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class PdaProductDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class View {
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * sku名称
         */
        private String skuName;
        /**
         * spu编号
         */
        private String spuNo;
        /**
         * spu名称
         */
        private String spuName;
        /**
         * 图片
         */
        private String imagesUrl;
        /**
         * 变体信息
         */
        private String variantProperty;
        /**
         * 条码
         */
        private String ean;

        /**
         * 毛重
         */
        private String grossWeight;

        /**
         * 净重
         */
        private String netWeight;

        /**
         * 产品尺寸长
         */
        private BigDecimal productLength;
        /**
         * 产品尺寸宽
         */
        private BigDecimal productWidth;
        /**
         * 产品尺寸高
         */
        private BigDecimal productHeight;
        /**
         * 箱规长
         */
        private BigDecimal boxLength;
        /**
         * 箱规宽
         */
        private BigDecimal boxWidth;
        /**
         * 箱规高
         */
        private BigDecimal boxHeight;

        /**
         * 单箱重量
         */
        private String boxWeight;

        /**
         * 单箱数量
         */
        private Integer boxQty;

        /**
         * 产品等级
         */
        private String grade;

        /**
         * 产品经理
         */
        private String chargeName;

        /**
         * 主要材质
         */
        private String materials;

        /**
         * 辅料
         */
        private String accessories;

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
         * bom父级sku
         */
        private List<ParentSkuDTO> parentSkuDTOList;
    }


    @Data
    @NoArgsConstructor
    public static class ParentSkuDTO {
        /**
         * 父sku编号
         */
        private String skuNo;
        /**
         *
         * 子sku集合
         */
        private List<SonSkuDTO> childSkuList;

    }

    @Data
    @NoArgsConstructor
    public static class SonSkuDTO {
        /**
         * 父sku编号
         */
        private String skuNo;
        /**
         * 数量
         */
        private Integer quantity;
    }

}
