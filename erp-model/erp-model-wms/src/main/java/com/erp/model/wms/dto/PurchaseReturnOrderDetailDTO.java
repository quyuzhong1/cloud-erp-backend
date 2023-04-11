package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Date;

/**
 * 收货明细DTO
 * @Author Luo_WG
 * @Date 2023/4/6 17:18
 **/
@Data
@NoArgsConstructor
public class PurchaseReturnOrderDetailDTO {

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 退货单主表id
         */
        private String returnOrderId;

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
         * 实退数量
         */
        private Integer realityReturnQty;

        /**
         * 补货数量
         */
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        private Integer deductAmountQty;

        /**
         * 含税单价
         */
        private String taxPrice;

        /**
         * 总价
         */
        private String totalPrice;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 退货单信息表id
         */
        private String id;

        /**
         * 实退数量
         */
        private Integer realityReturnQty;

        /**
         * 补货数量
         */
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        private Integer deductAmountQty;

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
         * 退货单明细表id
         */
        @TableField("id")
        private String id;

        /**
         * 退货单主表id
         */
        @TableField("return_order_id")
        private String returnOrderId;

        /**
         * skuId
         */
        @TableField("sku_id")
        private String skuId;

        /**
         * sku编码
         */
        @TableField("sku_no")
        private String skuNo;

        /**
         * 产品名称
         */
        @TableField("product_name")
        private String productName;

        /**
         * 采购数量
         */
        @TableField("purchase_qty")
        private Integer purchaseQty;

        /**
         * 实退数量
         */
        @TableField("reality_return_qty")
        private Integer realityReturnQty;

        /**
         * 补货数量
         */
        @TableField("replenish_qty")
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        @TableField("deduct_amount_qty")
        private Integer deductAmountQty;

        /**
         * 含税单价
         */
        @TableField("tax_price")
        private String taxPrice;

        /**
         * 总价
         */
        @TableField("total_price")
        private String totalPrice;

        /**
         * 备注
         */
        @TableField("remark")
        private String remark;
    }

}
