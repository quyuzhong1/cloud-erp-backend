package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 发货单箱子信息表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class WmsCartonDetailDTO implements Serializable {

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * first_mile_carton表id
        */
        private String cartonId;

        /**
        * 产品id
        */
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
        * 装箱数量
        */
        private Integer packQty;

        /**
        * 待装箱数量
        */
        private Integer waitPackQty;

        /**
        * 来源类型
        */
        private String sourceType;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * first_mile_carton表id
        */
        private String cartonId;

        /**
        * 产品id
        */
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 装箱数量
        */
        private Integer packQty;
    }

    /**
     * 装箱清单产品信息
     */
    @Data
    @NoArgsConstructor
    public static class ListPackingDetailDTO {
        private String id;
        /**
         * sku
         */
        private String sku;
        /**
         * 箱号
         */
        private String boxNo;
        /**
         * 箱子包装尺寸
         */
        private String boxSize;
        /**
         * 箱子包装重量
         */
        private String packageWeight;
        /**
         * 装箱SKU
         * 例：（sku*qty+sku*qty+...）
         */
        private String boxDesc;

        private BigDecimal multiplySize;

        private BigDecimal length;

        private BigDecimal width;

        private BigDecimal height;
    }

}