package com.erp.server.msg.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.server.msg.model.MsgSendChannelWrapParam;

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
    ApiResult doSendMsg(MsgSendChannelWrapParam msgInfo);

    /**
     * 发送渠道
     * @return
     */
    MessageChannelEnum channel();

}
