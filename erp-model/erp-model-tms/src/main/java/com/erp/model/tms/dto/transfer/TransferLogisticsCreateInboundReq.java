package com.erp.model.tms.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@Data
@AllArgsConstructor
@Builder
public class TransferLogisticsCreateInboundReq {

    /**
     * 客户参考号
     */
    private String referenceCode;

    /**
     * 是否提货：0否 1是
     */
    private Boolean isDelivery;

    /**
     * 总件数
     */
    private Integer packQty;

    /**
     * 包裹重量
     */
    private BigDecimal grossWeight;

    /**
     * 入库单状态：0删除,1草稿,2确认,3待审核,
     */
    private String receivingStatus;

    /**
     * 详情
     */
    private List<ReceiveItem> receiveItemList;


    @Data
    @AllArgsConstructor
    @Builder
    public static class ReceiveItem {

        /**
         * 服务商的订单编号
         */
        private String orderCode;

        /**
         * 包裹重量
         */
        private BigDecimal grossWeight;

        /**
         * 包袋号
         */
        private String packNum;
    }
}
