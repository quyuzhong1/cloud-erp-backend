package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 收货明细DTO
 * @Author Luo_WG
 * @Date 2023/4/6 17:18
 **/
@Data
public class PurchaseReturnOrderDetailDTO {
    private PurchaseReturnOrderDetailDTO() {
        throw new IllegalStateException("Utility PurchaseReturnOrderDetailDTO class");
    }
    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * sku
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 实退数量
         */
        @NotNull(message = "实退数量不能为空")
        @Min(value = 0, message = "实退数量最小值为0")
        @Max(value = 999999999, message = "实退数量最大值为999999999")
        private Integer returnQty;

        /**
         * 补货数量
         */
        @Max(value = 999999999, message = "补货数量最大值为999999999")
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        @Max(value = 999999999, message = "扣款数量最大值为999999999")
        private Integer deductAmountQty;

        /**
         * 退货单价
         */

        private BigDecimal returnPrice;

        /**
         * 备注
         */
        private String remark;

        /**
         * 单据来源详情表id
         */
        private String sourceDetailId;

        /**
         * 采购订单详情表Id
         */
        private String purchaseOrderDetailId;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 仓位
         */
        private String warehouseLocation;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 退货单详情表id
         */
        private String id;

        /**
         * 退货单主表id
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
         * 实退数量
         */
        @NotNull(message = "实退数量不能为空")
        @Min(value = 0, message = "实退数量最小值为0")
        @Max(value = 999999999, message = "实退数量最大值为999999999")
        private Integer returnQty;

        /**
         * 补货数量
         */
        @Max(value = 999999999, message = "补货数量最大值为999999999")
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        @Max(value = 999999999, message = "扣款数量最大值为999999999")
        private Integer deductAmountQty;

        /**
         * 退货单价
         */
        private BigDecimal returnPrice;

        /**
         * 备注
         */
        private String remark;

        /**
         * 单据来源详情表id
         */
        private String sourceDetailId;

        /**
         * 采购订单详情表Id
         */
        private String purchaseOrderDetailId;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 仓位
         */
        private String warehouseLocation;
    }

    /**
     * 查询详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * 退货单明细表id
         */
        private String id;

        /**
         * 退货单主表id
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
         * spu编号
         */
        private String spuNo;

        /**
         * 单位
         */
        private String unit;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 已入库数量
         */
        private Integer hasStockInQty;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 实退数量
         */
        private Integer returnQty;

        /**
         * 补货数量
         */
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        private Integer deductAmountQty;

        /**
         * 退货单价
         */
        private BigDecimal returnPrice;

        /**
         * 总价
         */
        private BigDecimal totalPrice;

        /**
         * 备注
         */
        private String remark;

        /**
         * 采购订单详情表id
         */
        private String purchaseOrderDetailId;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 变体属性
         */
        private String variantProperty;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 即时库存
         */
        private Integer curInventoryQty;

        /**
         * 一级供应商id
         */
        private String mainSupplierId;

        /**
         * 一级供应商名称
         */
        private String mainSupplierName;
    }

}
