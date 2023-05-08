package com.erp.model.workflow.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 驳回类型
 * @author Cloud
 */

public enum RejectTypeEnum {
    /**
     * 驳回上个节点
     */
    PREVIOUS("previous", "驳回上个节点"),
    /**
     * 驳回发起人
     */
    START("start", "驳回发起人"),
    /**
     * 驳回指定节点
     */
    APPOINT("appoint", "驳回指定节点"),
    ;

    @EnumValue
    @JsonValue
    private final String code;
    private final String name;

    RejectTypeEnum(String code, String name) {
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
        for (SysClassifyEnum state : SysClassifyEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static RejectTypeEnum getByCode(String code) {
        return Arrays.stream(RejectTypeEnum.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

}
