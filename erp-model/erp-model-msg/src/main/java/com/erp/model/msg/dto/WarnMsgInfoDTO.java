package com.erp.model.msg.dto;

import com.erp.model.msg.enums.WarnMsgTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 飞书系统预警请求实体
 * @CreateTime: 2023-05-31  18:38
 * @Author: zhangchunlin
 */
@Data
public class WarnMsgInfoDTO implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     * 多行内容请用\n换行
     */
    private String content;

    /**
     * 预警类型
     * 主要用于发送到对应的飞书群
     */
    private WarnMsgTypeEnum warnMsgTypeEnum = WarnMsgTypeEnum.SYS_EXCEPTION;

}