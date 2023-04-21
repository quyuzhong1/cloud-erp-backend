package com.erp.model.msg.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: MessageChannelEnum
 * @Description: 消息渠道
 * @CreateTime: 2023-04-19  10:08
 * @Author: zhangchunlin
 */
public enum MessageChannelEnum {

    FEISHU("feishu", "飞书"),
    MAIL("mail", "邮件"),
    ;
    private String code;

    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    MessageChannelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MessageChannelEnum of(String code) {
        return Arrays.stream(MessageChannelEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
