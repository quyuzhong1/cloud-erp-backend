package com.erp.server.dmp.entity.mabang;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class RefundOrderItemEntity {
    private Integer refundStockId;
    private String refundStock;
    private Integer refund_num;
    private Integer stock_quantity;
    private String platformSku;
    private Integer isCombo;
}
