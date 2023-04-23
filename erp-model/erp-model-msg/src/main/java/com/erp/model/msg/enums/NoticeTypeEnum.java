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

    /**
     * 注意，消息来源对应于数据库中erp_sys库中msg_config的主键字段，确定发送的渠道。
     * 再由msg_channel_config确定发送渠道对应的应用（有可能一个渠道存在多个应用，邮件暂不区分渠道）
     */

    SCM_TASK("SCM_TASK", "供应链系统任务通知"),
    PLM_TASK("PLM_TASK", "产品研发系统任务通知"),
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
