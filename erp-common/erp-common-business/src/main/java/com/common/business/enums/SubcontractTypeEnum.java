package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Will
 * @version 1.0
 * @description: 委外父子级枚举
 * @date 2023/6/15 16:41
 */
@NoArgsConstructor
public enum SubcontractTypeEnum {

    ENUM_CHILD("child", "子级"),
    ENUM_PARENT("parent", "父级")
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

    SubcontractTypeEnum(String type, String name) {
        this.code = type;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(String type) {
        for (SubcontractTypeEnum item : SubcontractTypeEnum.values()) {
            if (type.equals(item.getCode())) {
                return item.name();
            }
        }
        return "";
    }

    public static SubcontractTypeEnum getByCode(String code) {
        return Arrays.stream(SubcontractTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }
}
