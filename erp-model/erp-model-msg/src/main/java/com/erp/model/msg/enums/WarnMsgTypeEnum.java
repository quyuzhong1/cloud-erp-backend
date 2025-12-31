package com.erp.model.msg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: NoticeTypeEnum
 * @Description: 消息预警业务类型配置
 * @CreateTime: 2023-04-20  15:59
 * @Author: zhangchunlin
 */
@Getter
@AllArgsConstructor
public enum WarnMsgTypeEnum {

    /**
     * 需在nacos中对应配置
     */

    SYS_EXCEPTION("sys_exception", "系统预警"),
    MACHINING_SKU_NOTICE("machining_sku_notice", "系统通知"),
    IMPLEMENT_GROUP_NOTICE("implement_group_notice", "通知实施群"),
    CUSTOM_GROUP("custom_group", "自定义群通知"),
    ;

    /**
     * 不同的预警业务，发送到不同的飞书消息群
     */
    private String code;

    /**
     * 异常关键字（飞书机器人那里的需要跟这里一样）
     */
    private String name;

    public static WarnMsgTypeEnum getByCode(String code) {
        return Arrays.stream(WarnMsgTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }


}
