package com.erp.model.wms.dto;

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
 * B2B三方发货单明细请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
*/
@Data
@NoArgsConstructor
public class B2bThirdDeliveryDetailDTO implements Serializable {




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
         * 销售明细id
         */
        private String soDetailId;

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
        * 销售数量
        */
        private Integer saleQty;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 单箱数量
        */
        private Integer perBoxQty;

        /**
        * 发货sku
        */
        private String deliverySkuNo;

        /**
        * 发货skuid
        */
        private String deliverySkuId;

        /**
        * 三方仓SKU
        */
        private String warehousePlatformSku;

        /**
        * 发货箱数
        */
        private Integer boxQty;

        /**
        * 规格编号（ZXGG0001）
        */
        private String boxSpecNo;

        /**
        * 序号
        */
        private Integer sort;


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
//        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;
        /**
         * 销售订单明细id
         */
        private String soDetailId;

        /**
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19,message = "产品id最大长度不能超过19位")
        private String skuId;
        /**
         * sku编码
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;

        /**
        * 销售数量
        */
//        @NotNull(message = "销售数量不能为空")
        private Integer saleQty;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 单箱数量
        */
        @NotNull(message = "单箱数量不能为空")
        private Integer perBoxQty;

        /**
        * 发货sku
        */
        @NotBlank(message = "发货sku不能为空")
        @Size(max = 255,message = "发货sku最大长度不能超过255位")
        private String deliverySkuNo;

        /**
        * 发货skuid
        */
        @NotBlank(message = "发货skuid不能为空")
        @Size(max = 255,message = "发货skuid最大长度不能超过255位")
        private String deliverySkuId;

        /**
        * 三方仓SKU
        */
//        @NotBlank(message = "库存sku不能为空")
        @Size(max = 255,message = "三方仓SKU最大长度不能超过255位")
        private String warehousePlatformSku;

        /**
        * 发货箱数
        */
        @NotNull(message = "发货箱数不能为空")
        private Integer boxQty;

        /**
        * 规格编号（ZXGG0001）
        */
//        @NotBlank(message = "规格编号（ZXGG0001）不能为空")
        @Size(max = 50,message = "规格编号（ZXGG0001）最大长度不能超过50位")
        private String boxSpecNo;

        /**
        * 序号
        */
        @NotNull(message = "序号不能为空")
        private Integer sort;


    }


}