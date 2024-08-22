package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 发货计划详情表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class WmsDeliveryPlanDetailDTO implements Serializable {

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
        * 主键id
        */
        private String id;

        /**
        * 主表id
        */
        private String mainId;

        /**
         * 产品图片
         */
        private String imageUrl;

        /**
         * 第三方仓SKU
         */
        private String platformSku;

        /**
         * 第三方仓产品名称
         */
        private String platformSkuName;

        /**
        * 产品id
        */
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 计划数量
        */
        private Integer qty;

        /**
        * 是否组合品
        */
        private Boolean isCombination;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 库存sku
        */
        private String stockSku;

        /**
        * 库存sku名称
        */
        private String stockSkuName;
        /**
         * mSKU
         */
        private String mSKU;

        /**
         * FNSKU
         */
        private String fnSku;

        /**
         * ASIN
         */
        private String asin;

        /**
         * 单箱数量
         */
        private Integer boxQty;
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
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 产品id
        */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
         * 库存sku
         */
        private String stockSku;

        /**
         * 库存sku名称
         */
        private String stockSkuName;

        /**
        * 计划数量
        */
        private Integer qty;
        /**
         * 平台sku
         */
        private String platformSku;

        /**
         * 平台sku名称
         */
        private String platformSkuName;

        /**
         * mSKU
         */
        private String mSKU;

        /**
         * FNSKU
         */
        private String fnSku;

        /**
         * ASIN
         */
        private String asin;
        /**
         * 单箱数量
         */
        private Integer boxQty;

    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<WmsDeliveryPlanDetailDTO.ViewDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }
}