package com.erp.server.msg.model;

import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.server.msg.enums.MessageChannelAppEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @Classname: MsgSendChannelWrapParam
 * @Description: 消息渠道应用发送
 * @CreateTime: 2023-04-21  15:56
 * @Author: zhangchunlin
 */
@Data
public class MsgSendChannelWrapParam implements Serializable {

    /**
     * 发送渠道
     */
    private MessageChannelEnum sendChannel;

    /**
     * 渠道应用
     */
    private MessageChannelAppEnum channelApp;

    /**
     * 发送内容体
     */
    private NoticeMsgWrapInfoDTO noticeMsgWrapInfoDTO;

}