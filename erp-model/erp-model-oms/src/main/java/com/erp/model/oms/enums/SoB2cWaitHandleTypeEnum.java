package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * b2c销售订单待处理类型枚举
 * @author Will
 * @version 1.0
 * @date 2024/4/23 19:00
 */
public enum SoB2cWaitHandleTypeEnum {


    APPROVE_REJECT("approveReject",  "审核不通过（自动）"),
    MANUAL_REJECT("manualReject",  "审核不通过（手动）"),
    WAIT_SUBMIT("waitSubmit",  "订单反审核"),
    WAREHOUSE_RULE_REJECT("warehouseRuleReject",  "仓库规则不通过"),
    LOGISTICS_RULE_REJECT("logisticsRuleReject",  "物流规则不通过"),

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


    SoB2cWaitHandleTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (SoB2cWaitHandleTypeEnum typeEnum : SoB2cWaitHandleTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum.getName();
            }
        }
        return "";
    }
}
