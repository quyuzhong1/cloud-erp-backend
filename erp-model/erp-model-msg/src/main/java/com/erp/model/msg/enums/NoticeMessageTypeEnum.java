package com.erp.model.msg.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: FeishuMessageTypeEnum
 * @Description: 对外统一的消息类型（映射在其他枚举）
 * @CreateTime: 2023-04-19  10:22
 * @Author: zhangchunlin
 */
public enum NoticeMessageTypeEnum {

    TEXT("text", "文本"),
    ACTION_CARD("action_card", "卡片"),
    ;
    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    NoticeMessageTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static NoticeMessageTypeEnum of(String code) {
        return Arrays.stream(NoticeMessageTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }


}
