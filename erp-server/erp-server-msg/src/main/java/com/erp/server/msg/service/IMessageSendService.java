package com.erp.server.msg.service;

import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.server.msg.model.MsgResultVO;
import com.erp.server.msg.model.MsgSendChannelWrapParam;

/**
 * @Classname: IMessageSend
 * @Description: TODO
 * @CreateTime: 2023-04-19  11:08
 * @Author: zhangchunlin
 */
public interface IMessageSendService {

    /**
     * 发送通知消息
     * @return
     */
    MsgResultVO doSendMsg(MsgSendChannelWrapParam msgInfo);

    /**
     * 发送预警消息
     * @param msgInfo
     */
    void doSendWarnMsg(WarnMsgInfoDTO msgInfo);

    /**
     * 发送渠道
     * @return
     */
    MessageChannelEnum channel();

}
