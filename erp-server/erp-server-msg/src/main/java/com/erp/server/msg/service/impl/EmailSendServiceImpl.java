package com.erp.server.msg.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.server.msg.model.MsgSendChannelWrapParam;
import com.erp.server.msg.service.BaseMessageSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Classname: EmailSendServiceImpl
 * @Description: 邮件发送消息业务类
 * @CreateTime: 2023-04-20  20:49
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class EmailSendServiceImpl extends BaseMessageSendService {

    @Override
    public ApiResult sendMsg(MsgSendChannelWrapParam noticeMsgInfo) {
        log.info("通过邮件发送消息，消息内容：{}", JSONObject.toJSONString(noticeMsgInfo));
        // TODO暂未实现
        return null;
    }

    @Override
    public MessageChannelEnum channel() {
        return MessageChannelEnum.MAIL;
    }
}