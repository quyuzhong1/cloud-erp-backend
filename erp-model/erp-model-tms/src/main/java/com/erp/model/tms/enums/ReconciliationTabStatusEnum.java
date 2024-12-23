package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: 对账状态枚举
 * @date 2023/11/13 15:38
 */
public enum ReconciliationTabStatusEnum implements EnumMessage {


	PAY_CONFIRM("payConfirm","付款待确认"),
	PAY_CONFIRMED("payConfirmed", "付款已确认"),
	REFUND_CONFIRM("refundConfirm","退款待确认"),
	REFUND_CONFIRMED("refundConfirmed","退款已确认"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    ReconciliationTabStatusEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ReconciliationTabStatusEnum typeEnums : ReconciliationTabStatusEnum.values()) {
            if (code.equals(typeEnums.getCode())) {
                return typeEnums.getName();
            }
        }
        return "";
    }
}
