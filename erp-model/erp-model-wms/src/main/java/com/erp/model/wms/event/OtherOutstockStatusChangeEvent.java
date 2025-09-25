package com.erp.model.wms.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 其他出库单状态变更事件
 * 
 * @author wuhaotian
 * @since 2025-08-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtherOutstockStatusChangeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 其他出库单ID
     */
    private String outboundOrderId;

    /**
     * 其他出库单编号
     */
    private String outboundOrderCode;

    /**
     * 旧状态
     */
    private String oldStatus;

    /**
     * 新状态
     */
    private String newStatus;

    /**
     * 状态变更时间
     */
    private LocalDateTime changeTime;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 明细变更列表
     */
    private List<OtherOutstockDetailChangeEvent> detailChanges;

    /**
     * 其他出库单明细变更事件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtherOutstockDetailChangeEvent implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 明细ID
         */
        private String detailId;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * 实发数量
         */
        private Integer actualQty;

        /**
         * 来源明细ID
         */
        private String sourceDetailId;
    }
}
