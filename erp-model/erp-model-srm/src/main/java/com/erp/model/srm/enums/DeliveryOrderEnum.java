package com.erp.model.srm.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;

public enum DeliveryOrderEnum {
    ;
    /**
     * 收货状态枚举
     */
    @Getter
    public enum ReceiptStatusEnum implements EnumMessage {
        WAIT_CONFIRMED("waitConfirmed","待确认"),
        CONFIRMED("confirmed","已确认"),
        ;
        private final String code;
        private final String name;
        ReceiptStatusEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum SourceTypeEnum implements EnumMessage {
        PURCHASE_ORDER("purchaseOrder", "采购订单"),
        ;
        private final String code;
        private final String name;
        SourceTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum SearchTypeEnum implements EnumMessage {
        ALL("all", "全部"),
        WAIT_RECEIVE_AND_PRINT("waitReceiveAndPrint", "待收货-未打印"),
        WAIT_RECEIVE_AND_PRINTED("waitReceiveAndPrinted", "待收货-已打印"),
        WAIT_RECEIVE("waitReceive", "待收货"),
        RECEIVED("received", "已收货"),
        QTY_DIFFERENCE("qtyDifference", "收发差异"),
        ;
        private final String code;
        private final String name;
        SearchTypeEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
}
