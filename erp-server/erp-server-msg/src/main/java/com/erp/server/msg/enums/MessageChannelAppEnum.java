package com.erp.server.msg.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: MessageChannelEnum
 * @Description: 渠道消息应用
 * @CreateTime: 2023-04-19  10:08
 * @Author: zhangchunlin
 */
public enum MessageChannelAppEnum {

    PLM("plm", "产品研发应用"),
    SCM("scm", "供应链应用"),
    ;
    private String code;

    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    MessageChannelAppEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MessageChannelAppEnum of(String code) {
        return Arrays.stream(MessageChannelAppEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
