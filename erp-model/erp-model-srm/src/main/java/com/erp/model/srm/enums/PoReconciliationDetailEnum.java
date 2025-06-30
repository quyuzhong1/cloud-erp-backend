package com.erp.model.srm.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;

/**
 * @author Will
 * @version 1.0
 * @description: 对账明细枚举
 * @date 2024/1/20 11:41
 */
public enum PoReconciliationDetailEnum {
;
    @Getter
    public enum StatusEnum implements EnumMessage {
        WAIT_RECONCILIATION("waitReconciliation", "待对账"),
        NOT_NEED_RECONCILIATION("notNeedReconciliation", "无需对账"),
        ;
        private final String code;
        private final String name;
        StatusEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
        public static String getNameByCode(String code) {
            PoReconciliationDetailEnum.StatusEnum[] stateEnums = values();
            for (PoReconciliationDetailEnum.StatusEnum stateEnum : stateEnums) {
                if (stateEnum.getCode().equals(code) ) {
                    return stateEnum.getName();
                }
            }
            return "";
        }
    }
}
