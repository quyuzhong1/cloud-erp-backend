package com.erp.model.msg.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
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

    SYS_EXCEPTION("sys_exception", "系统异常预警"),

    ;

    /**
     * 不同的预警业务，发送到不同的飞书消息群
     */
    private String code;

    private String name;

    public static WarnMsgTypeEnum of(String code) {
        return Arrays.stream(WarnMsgTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }


}
