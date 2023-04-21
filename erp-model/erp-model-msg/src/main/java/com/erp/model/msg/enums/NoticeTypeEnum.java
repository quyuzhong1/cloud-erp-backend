package com.erp.model.msg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: NoticeTypeEnum
 * @Description: 通知类型配置
 * @CreateTime: 2023-04-20  15:59
 * @Author: zhangchunlin
 */
@Getter
@AllArgsConstructor
public enum NoticeTypeEnum {

    //TODO 需要配置到数据库表中

    SCM_TASK("SCM_TASK", "供应链系统业务通知", new MessageChannelEnum[]{MessageChannelEnum.FEISHU},NoticeMessageTypeEnum.ACTION_CARD),
    ;

    /**
     * 消息来源映射，根据消息类型配置确定发送的平台及消息类型
     */
    private String code;

    private String name;

    // 消息发送渠道
    private MessageChannelEnum[] messageChannels;

    // 通知消息类型
    private NoticeMessageTypeEnum noticeMessageType;

    public static NoticeTypeEnum of(String code) {
        return Arrays.stream(NoticeTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }


}
