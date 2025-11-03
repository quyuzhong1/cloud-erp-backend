package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 多渠道订单明细请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class SoMultiChannelDetailDTO implements Serializable {




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
        * 单据明细id
        */
        private String soDetailId;

        /**
        * skuId
        */
        private String skuId;

        /**
        * 产品sku编号
        */
        private String skuNo;

        /**
        * 平台sku
        */
        private String platformSkuNo;

        /**
        * 平台产品id
        */
        private String platformSpuNo;

        /**
        * 平台产品名称
        */
        private String platformProductName;

        /**
        * 销售订单数量
        */
        private Integer qty;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * FNSKU
        */
        private String fnSku;


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
        * 单据明细id
        */
//        @NotBlank(message = "单据明细id不能为空")
        @Size(max = 19,message = "单据明细id最大长度不能超过19位")
        private String soDetailId;

        /**
        * skuId
        */
//        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;
        private String skuNo;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 100,message = "平台sku最大长度不能超过100位")
        private String platformSkuNo;

        /**
        * 平台产品id
        */
        @NotBlank(message = "平台产品id不能为空")
        @Size(max = 100,message = "平台产品id最大长度不能超过100位")
        private String platformSpuNo;

        /**
        * 平台产品名称
        */
        @NotBlank(message = "平台产品名称不能为空")
        @Size(max = 100,message = "平台产品名称最大长度不能超过100位")
        private String platformProductName;

        /**
        * 销售订单数量
        */
//        @NotNull(message = "销售订单数量不能为空")
        private Integer qty;

        /**
        * 发货数量
        */
//        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 产品名称
        */
//        @NotBlank(message = "产品名称不能为空")
        @Size(max = 100,message = "产品名称最大长度不能超过100位")
        private String productName;

        /**
        * FNSKU
        */
//        @NotBlank(message = "FNSKU不能为空")
        @Size(max = 100,message = "FNSKU最大长度不能超过100位")
        private String fnSku;
        /**
         * 出库状态
         */
        private String outstockStatus;
    }


    @Data
    @NoArgsConstructor
    public static class OutstockQtyDTO {
        /**
         * 订单明细id
         */
        private String soDetailId;
        /**
         * 发货单编号
         */
        private String deliveryCode;
        /**
         * 平台订单明细id
         */
        private String platformDetailId;
        /**
         * 出库数量
         */
        private Integer outstockQty;
    }
}