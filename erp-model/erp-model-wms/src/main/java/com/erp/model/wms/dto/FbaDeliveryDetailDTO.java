package com.erp.model.wms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * FBI发货单明细表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@NoArgsConstructor
public class FbaDeliveryDetailDTO implements Serializable {




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
        * 平台sku
        */
        private String asin;

        /**
        * 卖家sku
        */
        private String mSku;

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
        private String mSku;

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
        * 库存sku
        */
        private String stockSku;

        /**
        * 申报数量
        */
        @NotNull(message = "申报数量不能为空")
        private Integer declareQty;

        /**
        * 应发数量
        */
        @NotNull(message = "应发数量不能为空")
        private Integer planQty;

        /**
        * 实发数量
        */
        @NotNull(message = "实发数量不能为空")
        private Integer deliveryQty;

        /**
        * 是否组合品
        */
        @NotNull(message = "是否组合品不能为空")
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
    }


}