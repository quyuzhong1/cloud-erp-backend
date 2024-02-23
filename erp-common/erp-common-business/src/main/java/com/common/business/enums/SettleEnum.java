package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @description: 结算方式
 * @author Will
 * @date: 2024/1/18 11:48
 */
public enum SettleEnum {
    MONTHLY("monthly", "月结"),
    CASH("cash", "现结"),
    PREPAY("prepay", "预付"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    SettleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String state) {
        if (StringUtils.isNotBlank(state)) {
            for (SettleEnum item : SettleEnum.values()) {
                if (state.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static SettleEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getCode().equals(status))
                .findFirst().orElse(null);
    }
}
