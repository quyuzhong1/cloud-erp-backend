package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum PdaTabFlagPcEnum {
    ALL("all", "全部"),
    WAIT_SUBMIT("waitSubmit", "待提交"),
    REJECT("reject", "审核不通过"),
    APPROVE_ING("approveIng", "审核中"),
    APPROVE("approve", "已审核");

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    PdaTabFlagPcEnum(String code, String name) {
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
            for (PdaTabFlagPcEnum item : PdaTabFlagPcEnum.values()) {
                if (state.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    public static PdaTabFlagPcEnum getByStatus(String status){
        return Arrays.stream(values()).filter(a -> a.getCode().equals(status))
                .findFirst().orElse(null);
    }
}
