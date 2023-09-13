package com.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;


/**
 * 日志状态类型
 *
 * @author Jim
 */
@Getter
@AllArgsConstructor
public enum LogStatusEnum {

    // 默认（找不到对应菜单类型情况）
    SUCCESS("success", "正常"),
    ERROR("error", "异常"),
    ;

    @EnumValue
    private final String code;
    private final String name;

    public static LogStatusEnum getByCode(String code) {
        return Arrays.stream(values()).filter(value -> value.getCode().equals(code))
                .findFirst().orElse(null);
    }
}
