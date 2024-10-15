package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.*;

/**
 * <p>
 * 要货申请单明细表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class RequisitionApplicationDetailDTO implements Serializable {




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
        * 产品图片
        */
        private String imageUrl;

        /**
        * 产品id
        */
        private String skuId;

        /**
         * 第三方仓SKU
         */
        private String thirdWarehouseSku;

        /**
         * MSKU
         */
        private String platformSku;

        /**
         * 平台产品名称
         */
        private String platformSkuName;

        /**
         * FNSKU
         */
        private String platformFnSku;

        /**
         * 产品编号
         */
        private String asin;

        /**
        * 产品编号
        */
        private String skuNo;

        /**
        * 是否组合品
        */
        private Boolean isCombination;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * bom版本
        */
        private String bomVersion;

        /**
        * 要货数量
        */
        private Integer requisitionQty;

        /**
        * 批准数量
        */
        private Integer approveQty;

        /**
        * 拣货数量
        */
        private Integer pickingQty;

        /**
        * 可用库存
        */
        private Integer usableQty;

        /**
        * 可用库存
        */
        private String requisitionWarehouseLocation;
        /**
         * 虚拟仓id
         */
        private String fromVirtualWarehouseId;
        /**
         * 虚拟仓名称
         */
        private String fromVirtualWarehouseName;
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
        * 产品id
        */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
         * 平台sku
         */
        private String platformSku;

        /**
         * 平台spu（asin）
         */
        private String asin;

        /**
         * fnSku
         */
        private String platformFnSku;

        /**
         * 平台产品名称
         */
        private String platformSkuName;

        /**
        * 产品编码
        */
        private String skuNo;

        /**
        * bom版本
        */
        private String bomVersion;

        /**
        * 要货数量
        */
        @NotNull(message = "要货数量不能为空")
        @DecimalMax(value = "999999999", message = "最大值为999999999")
        @DecimalMin(value = "1", message = "要货数量必须大于0")
        private Integer requisitionQty;

        /**
        * 批准数量
        */
        private Integer approveQty;

        /**
        * 拣货数量
        */
        private Integer pickingQty;

        /**
        * 来源详情id
        */
        private String sourceDetailId;

        /**
        * 要货仓位
        */
        private String requisitionWarehouseLocation;

        /**
         * 虚拟仓id
         */
        private String fromVirtualWarehouseId;
        /**
         * 虚拟仓name
         */
        private String fromVirtualWarehouseName;
    }


}