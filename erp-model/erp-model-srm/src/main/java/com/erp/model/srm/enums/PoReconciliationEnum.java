package com.erp.model.srm.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;

/**
 * @author Will
 * @version 1.0
 * @description: 对账明细吗枚举
 * @date 2024/1/20 11:41
 */
public enum PoReconciliationEnum {
;

    @Getter
    public enum PoReconciliationStatusEnum implements EnumMessage {
        TO_BE_SUPPLIER_CONFIRM("toBeSupplierConfirm", "待供方确认"),
        TO_BE_PURCHASE_CONFIRM("toBePurchaseConfirm", "待采方确认"),
        CONFIRM("confirm", "已确认待完结"),
        RECEIVED("received", "已收单据"),
        ;
        private final String code;
        private final String name;
        PoReconciliationStatusEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    @Getter
    public enum SrmTabFlagEnum implements EnumMessage {
        TO_BE_SUPPLIER_CONFIRM("toBeSupplierConfirm", "待供方确认"),
        TO_BE_CONFIRM("toBeConfirm", "待我确认"),
        CONFIRM("confirm", "已确认"),
        RECEIVED("received", "已收单据"),
        ;
        private final String code;
        private final String name;
        SrmTabFlagEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
}
