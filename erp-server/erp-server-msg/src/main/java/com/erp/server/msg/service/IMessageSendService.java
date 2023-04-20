package com.erp.server.msg.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;

/**
 * @Classname: IMessageSend
 * @Description: TODO
 * @CreateTime: 2023-04-19  11:08
 * @Author: zhangchunlin
 */
public interface IMessageSendService {

    /**
     * 发送消息
     * @return
     */
    ApiResult doSendMsg(NoticeMsgInfoDTO msgInfo);

    /**
     * 发送渠道
     * @return
     */
    MessageChannelEnum channel();

}
