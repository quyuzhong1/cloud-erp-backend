package com.sdk.wms.jitu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 极兔出库单创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOutOrderCancelRequest implements Serializable {

    /**
     * 货主编号
     */
    private String customerid;

    /**
     * 仓库编码
     */
    private String warehouseCode;
    /**
     * 单据类型：
     * CGRK-采购入库
     * THRK-退货入库
     * DBRK-调拨入库
     * QTRK-其他入库
     * XSCK-销售出库
     * DBCK-调拨出库
     * QTCK-其他出库
     * TGYS-退供应商
     */
    private String orderType;

    /**
     * 单据编号：当单据类型为 入库单类型时，此字段需要传入库单接口entryOrderCode字段的单号；当单据类型为 出库单类型时，此字段需要传出库单接口txlogisticid字段的单号。
     */
    private String orderCode;

    /**
     * 取消原因
     */
    private String cancelReason;
}