package com.erp.model.dmp.mabang.item;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class RefundOrderItemEntity {
    /**
     * 退款商品ID
     */
    private Integer refundStockId;
    /**
     * 退款商品SKU
     */
    private String refundStock;
    /**
     * 退款商品数量
     */
    private Integer refund_num;
    /**
     * 订单原始商品数量
     */
    private Integer stock_quantity;
    /**
     * 平台SKU
     */
    private String platformSku;
    /**
     * 是否属于组合SKU
     */
    private Integer isCombo;
}
