package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Will
 * @version 1.0
 * @description: b2c销售订单列表状态枚举
 * @date 2023/8/21 11:34
 */
public enum SoB2cTabEnum {
    ENUM_ALL("all",  "全部"),
    ENUM_PAYMENT("payment",  "待付款"),
    ENUM_PENDING("pending",  "待提审"),
    ENUM_APPROVE_ING("approveIng",  "待审核"),
    ENUM_IN_DISTRIBUTION("inDistribution",  "配货中"),
    ENUM_WAIT_SHIPPED("waitShipped",  "待发货"),
    ENUM_SHIPPED("shipped",  "已发货"),
    ENUM_FROZEN("frozen",  "冻结中"),
    ENUM_INVALID("invalid",  "已作废"),
    ENUM_ORDER_ERROR("orderError",  "异常订单"),

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


    SoB2cTabEnum(String code, String name) {
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
        for (SoB2cTabEnum soB2CTabEnum : SoB2cTabEnum.values()) {
            if (code.equals(soB2CTabEnum.getCode())) {
                return soB2CTabEnum.getName();
            }
        }
        return "";
    }

}
