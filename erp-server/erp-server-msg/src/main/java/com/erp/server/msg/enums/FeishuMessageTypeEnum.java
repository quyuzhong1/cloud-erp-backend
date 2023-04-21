package com.erp.server.msg.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: FeishuMessageTypeEnum
 * @Description: 飞书发送消息消息类型
 * @CreateTime: 2023-04-19  10:22
 * @Author: zhangchunlin
 */
public enum FeishuMessageTypeEnum {

    TEXT("text", "文本"),
    IMAGE("image", "图片"),
    POST("post", "富文本"),
    FILE("file", "文件"),
    AUDIO("audio", "音频"),
    MEDIA("media", "多媒体"),
    INTERACTIVE("interactive", "卡片"),
    SHARE_CHAT("share_chat", "分享群名片"),
    SHARE_USER("share_user", "分享用户"),
    ;
    private String code;

    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    FeishuMessageTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static FeishuMessageTypeEnum of(String code) {
        return Arrays.stream(FeishuMessageTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }


}
