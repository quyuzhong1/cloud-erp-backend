package com.erp.model.bi.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 分享身份类型
 *
 * @author Jim
 */
@Getter
@AllArgsConstructor
public enum BiShareIdentityTypeEnum {

    USER("user",  "单用户"),
    ROLE("role",  "角色"),
    ;

    @EnumValue
    @JsonValue
    private final String code;
    private final String name;


    public static BiShareIdentityTypeEnum isRoleCheck(String shareFlag) {
        return Arrays.stream(BiShareIdentityTypeEnum.values())
                .filter(e-> e.getCode().equals(shareFlag))
                .findFirst().orElse(USER);
    }

    /**
     * 通过Code查找
     */
    public static BiShareIdentityTypeEnum getByCode(String code) {
        return Arrays.stream(BiShareIdentityTypeEnum.values())
                .filter(e-> e.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
