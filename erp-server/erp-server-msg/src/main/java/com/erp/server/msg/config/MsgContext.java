package com.erp.server.msg.config;

import cn.hutool.core.collection.CollUtil;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.server.msg.service.IMessageSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Classname: MsgHolder
 * @Description: 消息控制
 * @CreateTime: 2023-04-19  14:14
 * @Author: zhangchunlin
 */
@Slf4j
@Component
public class MsgContext {

    private static Map<MessageChannelEnum, IMessageSendService>  HOLDER = new HashMap<>(10);

    public void put(MessageChannelEnum messageChannelEnum, IMessageSendService messageSendService) {
        HOLDER.putIfAbsent(messageChannelEnum, messageSendService);
    }

    /**
     * 路由发送消息，根据渠道来
     * @param msgInfo
     */
    public void routeSend(NoticeMsgInfoDTO msgInfo) {
        List<MessageChannelEnum> sendChannels = msgInfo.getSendChannels();
        if(CollUtil.isNotEmpty(sendChannels)) {
            log.info("消息发送者指定了发送渠道，优先使用该渠道发送，并且默认使用文本方式消息类型");
            sendChannels = msgInfo.getSendChannels();
        } else {
            log.info("消息发送者没有指定发送渠道，通过消息来源获取配置的发送渠道及其他信息");
            NoticeTypeEnum noticeTypeEnum = msgInfo.getNoticeTypeEnum();
            sendChannels = new ArrayList<>(Arrays.asList(noticeTypeEnum.getMessageChannels()));
        }
        sendChannels.stream().forEach(sendChannel-> {
            // TODO 此处需判断数据库表中配置的发送渠道是否在枚举类中能找到，找不到需要走默认
            HOLDER.get(sendChannel).doSendMsg(msgInfo);
        });
    }


}