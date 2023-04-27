package com.erp.server.msg.enums;

import com.erp.model.msg.enums.NoticeMessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: MessageChannelEnum
 * @Description: 统一消息类型和不同渠道映射
 * @CreateTime: 2023-04-19  10:08
 * @Author: zhangchunlin
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum ChannelSendMsgTypeEnum {

    TEXT(NoticeMessageTypeEnum.TEXT.getCode(), "文本", FeishuMessageTypeEnum.TEXT),
    ACTION_CARD(NoticeMessageTypeEnum.ACTION_CARD.getCode(), "跳转卡片", FeishuMessageTypeEnum.INTERACTIVE),
    ;
    private String code;

    private String name;

    /**
     * 飞书消息类型
     */
    private FeishuMessageTypeEnum feishuMsgType;

    public static ChannelSendMsgTypeEnum of(String code) {
        return Arrays.stream(ChannelSendMsgTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

}
