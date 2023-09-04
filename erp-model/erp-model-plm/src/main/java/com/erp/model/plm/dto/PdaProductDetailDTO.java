package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class PdaProductDetailDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class View {
        /**
         * 条码
         */
        private String goodsBarCode;

        /**
         * 毛重
         */
        private String grossWeight;

        /**
         * 净重
         */
        private String netWeight;

        /**
         * 产品尺寸
         */
        private String productSize;

        /**
         * 箱规
         */
        private String boxSize;

        /**
         * 单箱重量
         */
        private String boxWeight;

        /**
         * 单箱数量
         */
        private String boxQty;

        /**
         * 产品等级
         */
        private String productGrade;

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
         * bom父级sku
         */
        private ParentSkuDTO parentSkuDTO;
    }


    @Data
    @NoArgsConstructor
    public static class ParentSkuDTO {
        /**
         * sku编号
         */
        private String skuNo;
        /**
         *
         * 子sku集合
         */
        private List<String> childSkuList;

    }

}
