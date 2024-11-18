package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 收货明细DTO
 * @Author Luo_WG
 * @Date 2023/4/6 17:18
 **/
@Data
public class WarehouseReceiveDetailDTO {
    private WarehouseReceiveDetailDTO() {
        throw new IllegalStateException("Utility WarehouseReceiveDetailDTO class");
    }
    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable {
        private static final long serialVersionUID = 1905122041950251207L;
        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 收货数量
         */
        @NotNull(message = "收货数量不能为空")
        @Min(value = 1,message = "收货数量最小值为1")
        @Max(value = 999999999,message = "收货数量最大值为999999999")
        private Integer receiveQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 采购订单明细表id
         */
        private String purchaseOrderDetailId;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 收货单详情表id
         */
        private String id;

        /**
         * 收货单主表id
         */
        private String mainId;

        /**
         * 采购单详情表id
         */
        private String PurchaseOrderDetailId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 收货数量
         */
        @NotNull(message = "收货数量不能为空")
        @Min(value = 1,message = "收货数量最小值为1")
        @Max(value = 999999999,message = "收货数量最大值为999999999")
        private Integer receiveQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 查询详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * 收货单明细表id
         */
        private String id;

        /**
         * 收货单主表id
         */
        private String mainId;

        /**
         * skuId
         */
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
         * 变体属性
         */
        private String variantProperty;

        /**
         * 计划交货时间
         */
        private LocalDate planDeliveryDate;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 未签收数量
         */
        private Integer unReceiveQty;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 送货数量
         */
        private Integer deliveryQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 有效入库数量
         */
        private Integer effectiveStockInQty;

        /**
         * 未入库数量
         */
        private Integer unStockInQty;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 采购单详情表id
         */
        private String PurchaseOrderDetailId;
    }

}
