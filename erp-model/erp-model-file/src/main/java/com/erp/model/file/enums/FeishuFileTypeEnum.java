package com.erp.model.file.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 三方通知推送失败类型枚举
 * @author jack
 * @date 2025-06-10
 */
public enum FeishuFileTypeEnum implements EnumMessage {
    FILE("file", "飞书资源文件"),
    DOCX("docx", "飞书文件DOCX"),
    XLSX("sheets", "飞书文件XLSX"),
    WIKI("wiki", "飞书文件WIKI"),
    ;

    @EnumValue
    @JsonValue
    private String code;
    private String name;

    FeishuFileTypeEnum(String code, String name) {
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
        for (FeishuFileTypeEnum state : FeishuFileTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state.getName();
            }
        }
        return "";
    }

    public static FeishuFileTypeEnum getByCode(String code) {
        for (FeishuFileTypeEnum state : FeishuFileTypeEnum.values()) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }
        return null;
    }

    public static List<FeishuFileTypeEnum> getAll() {
        return Arrays.stream(FeishuFileTypeEnum.values()).collect(Collectors.toList());
    }
}
