package com.erp.model.msg.enums;

import cn.hutool.core.collection.CollUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public static List<MessageChannelEnum> getByCodes(List<String> channelCodes) {
        if (CollUtil.isEmpty(channelCodes)){
            return Collections.emptyList();
        }
        return Arrays.stream(MessageChannelEnum.values()).filter(r -> channelCodes.contains(r.getCode())).collect(Collectors.toList());
    }


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

    public static MessageChannelEnum getByCode(String code) {
        return Arrays.stream(MessageChannelEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
