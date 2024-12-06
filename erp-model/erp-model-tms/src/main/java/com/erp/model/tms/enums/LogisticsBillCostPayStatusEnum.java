package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @description:
 * @author Will
 * @date: 2024/3/22 16:29
 */
public enum LogisticsBillCostPayStatusEnum implements EnumMessage {

	PAYPAYMENT("pay_payment", "待付款"),
	REFUNDPAYMENT("refund_payment", "待退款"),
	PAYPAID("pay_paid", "已付款"),
	REFUNDPAID("refund_paid", "已退款"),
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


    LogisticsBillCostPayStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (LogisticsBillCostPayStatusEnum statusEnum : LogisticsBillCostPayStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}


