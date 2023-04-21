package com.erp.server.msg.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: MessageChannelEnum
 * @Description: 单条，多条，不区分单条多条消息实体
 * @CreateTime: 2023-04-19  10:08
 * @Author: zhangchunlin
 */
public enum MessageMultipleEnum {

    SINGLE("single", "单条"),
    MULTIPLE("multiple", "多条"),
    SAME("same", "不区分单条多条"),
    ;
    private String code;

    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    MessageMultipleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MessageMultipleEnum of(String code) {
        return Arrays.stream(MessageMultipleEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
