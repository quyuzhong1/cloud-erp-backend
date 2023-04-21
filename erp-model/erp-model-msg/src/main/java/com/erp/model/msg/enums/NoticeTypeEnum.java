package com.erp.model.msg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Classname: NoticeTypeEnum
 * @Description: 消息来源配置
 * @CreateTime: 2023-04-20  15:59
 * @Author: zhangchunlin
 */
@Getter
@AllArgsConstructor
public enum NoticeTypeEnum {

    SCM_NOTICE("SCM_NOTICE", "供应链系统业务通知"),
    PLM_NOTICE("PLM_NOTICE", "产品研发系统业务通知"),
    ;

    /**
     * 消息来源映射，根据消息类型配置确定发送的平台及消息类型
     */
    private String code;

    private String name;

    public static NoticeTypeEnum of(String code) {
        return Arrays.stream(NoticeTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }


}
