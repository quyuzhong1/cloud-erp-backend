package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @description: tabFlag枚举
 * @author Will
 * @date: 2024/1/18 11:48
 */
public enum TabFlagEnum {
    WAIT_SUBMIT("waitSubmit", "待提交"),
    APPROVE_ING("approveIng", "待审核"),
    APPROVE("approve", "已审核"),
    REJECT("reject", "不通过"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    TabFlagEnum(String code, String name) {
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
            for (TabFlagEnum item : TabFlagEnum.values()) {
                if (state.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static TabFlagEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getCode().equals(status))
                .findFirst().orElse(null);
    }
}
