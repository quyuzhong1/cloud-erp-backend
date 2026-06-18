package com.erp.model.tms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流费用 pay_status 存库值（与 pay_type 组合展示：pay+payment=待付款，refund+payment=待退款）。
 */
public enum LogisticsBillCostPayStateEnum implements EnumMessage {

    PAYMENT("payment", "未支付"),
    PAID("paid", "已支付"),
    ;

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    LogisticsBillCostPayStateEnum(String code, String name) {
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
        for (LogisticsBillCostPayStateEnum stateEnum : LogisticsBillCostPayStateEnum.values()) {
            if (code.equals(stateEnum.getCode())) {
                return stateEnum.getName();
            }
        }
        return "";
    }
}
