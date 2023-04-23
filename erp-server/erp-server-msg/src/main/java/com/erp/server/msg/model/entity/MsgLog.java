package com.erp.server.msg.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Classname: MsgLog
 * @Description: TODO
 * @CreateTime: 2023-04-23  11:54
 * @Author: zhangchunlin
 */
@Data
public class MsgLog implements Serializable {

    /**
     * 标识一次发送方的请求id
     */
    private String msgId;

    /**
     * 发送MQ消息主题
     */
    private String mqTopic;

    /**
     * 发送MQ消息tag
     */
    private String mqTag;

    /**
     * 发送渠道代码
     */
    private String sendChannelCode;

    /**
     * 消息发送方内容
     */
    private String msgSourceContent;

    /**
     * 消息渠道请求内容
     */
    private String msgChannelContent;

    /**
     * 消息渠道请求结果码
     */
    private String channelResultCode;

    /**
     * 消息渠道请求结果描述
     */
    private String channelResultMsg;

    /**
     * 是否需要重新发送
     */
    private Boolean needResend;

    /**
     * 已重试次数
     */
    private Integer retryTimes;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}