package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 头程发货单明细表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@NoArgsConstructor
public class FirstMileDeliveryDetailDTO implements Serializable {

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
        * 主表id
        */
        private String mainId;

        /**
         * 图片地址
         */
        private String imageUrl;

        /**
         * 第三方仓SKU
         */
        private String thirdWarehouseSku;

        /**
        * 平台sku
        */
        private String platformSpuNo;

        /**
        * 卖家sku
        */
        private String platformSkuNo;

        /**
        * FNSKU
        */
        private String fnSku;

        /**
        * ERP的SKUId
        */
        private String skuId;

        /**
        * ERP的SKU
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 库存sku
        */
        private String stockSku;

        /**
        * 申报数量
        */
        private Integer declareQty;

        /**
        * 应发数量
        */
        private Integer planQty;

        /**
        * 实发数量
        */
        private Integer deliveryQty;

        /**
        * 已发货数量
        */
        private Integer useDeliveryQty;

        /**
        * 单品净重
        */
        private BigDecimal netWeight;

        /**
        * 产品尺寸（长）
        */
        private BigDecimal productSizeLength;

        /**
        * 产品尺寸（宽）
        */
        private BigDecimal productSizeWidth;

        /**
        * 产品尺寸（高）
        */
        private BigDecimal productSizeHeight;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 来源详情id
         */
        private String sourceDetailId;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 平台sku
        */
        private String platformSpuNo;

        /**
        * 卖家sku
        */
        private String platformSkuNo;

        /**
        * FNSKU
        */
        private String fnSku;

        /**
        * ERP的SKU
        */
        private String skuId;

        /**
        * ERP的SKU
        */
        private String skuNo;

        /**
        * 申报数量
        */
        private Integer declareQty;

        /**
        * 应发数量
        */
        private Integer planQty;

        /**
        * 实发数量
        */
        private Integer deliveryQty;

        /**
        * 是否组合品
        */
        private Boolean isCombination;

        /**
        * 单品净重
        */
        private BigDecimal netWeight;

        /**
        * 产品尺寸（长）
        */
        private BigDecimal productSizeLength;

        /**
        * 产品尺寸（宽）
        */
        private BigDecimal productSizeWidth;

        /**
        * 产品尺寸（高）
        */
        private BigDecimal productSizeHeight;

        /**
        * 仓位
        */
        private String warehouseLocation;

        /**
        * 来源详情Id
        */
        private String sourceDetailId;

        /**
         * FBA货件编码
         */
        private String fbaShipmentCode;
    }



    @Data
    @NoArgsConstructor
    public static class listFirstMileDTO {
        /**
         * 主表id
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 备货类型
         */
        private String demandType;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
    }
}