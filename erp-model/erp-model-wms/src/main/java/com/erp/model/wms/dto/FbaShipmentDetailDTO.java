package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * FBI拣货明细表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@NoArgsConstructor
public class FbaShipmentDetailDTO implements Serializable {




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
         * 图片地址
         */
        private String imageUrl;

        /**
        * 主表id
        */
        private String mainId;

        /**
        * 平台sku
        */
        private String asin;

        /**
        * 卖家sku
        */
        private String msku;

        /**
        * FNSKU
        */
        private String fnSku;

        /**
        * ERP的SKU
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 申报数量
        */
        private Integer declareQty;

        /**
        * 签收数量
        */
        private Integer deliveryQty;

        /**
        * 收发差异
        */
        private Integer diffQty;

        /**
         * 是否组合品
         */
        private Boolean isCombination;
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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 64,message = "平台sku最大长度不能超过64位")
        private String asin;

        /**
        * 卖家sku
        */
        @NotBlank(message = "卖家sku不能为空")
        @Size(max = 64,message = "卖家sku最大长度不能超过64位")
        private String msku;

        /**
        * FNSKU
        */
        @NotBlank(message = "FNSKU不能为空")
        @Size(max = 64,message = "FNSKU最大长度不能超过64位")
        private String fnSku;

        /**
        * ERP的SKU
        */
        @NotBlank(message = "ERP的SKU不能为空")
        @Size(max = 64,message = "ERP的SKU最大长度不能超过64位")
        private String skuNo;

        /**
        * 申报数量
        */
        @NotNull(message = "申报数量不能为空")
        private Integer declareQty;

        /**
        * 签收数量
        */
        @NotNull(message = "签收数量不能为空")
        private Integer deliveryQty;

        /**
        * 收发差异
        */
        @NotNull(message = "收发差异不能为空")
        private Integer diffQty;

        /**
        * 是否组合品
        */
        @NotNull(message = "是否组合品不能为空")
        private Boolean isCombination;


    }


}