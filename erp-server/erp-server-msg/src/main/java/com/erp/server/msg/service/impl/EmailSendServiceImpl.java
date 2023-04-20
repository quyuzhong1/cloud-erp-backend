package com.erp.server.msg.service.impl;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;
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
    public ApiResult sendMsg(NoticeMsgInfoDTO noticeMsgInfo) {
        return null;
    }

    @Override
    public MessageChannelEnum channel() {
        return MessageChannelEnum.MAIL;
    }
}