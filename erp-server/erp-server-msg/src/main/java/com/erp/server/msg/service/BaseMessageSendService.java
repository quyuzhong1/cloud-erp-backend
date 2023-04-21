package com.erp.server.msg.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.server.msg.config.MsgContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

/**
 * @Classname: BaseMessageSendService
 * @Description: TODO
 * @CreateTime: 2023-04-19  14:18
 * @Author: zhangchunlin
 */
@Slf4j
public abstract class BaseMessageSendService implements IMessageSendService, InitializingBean {

    @Resource
    private MsgContext msgContext;


    /**
     * 初始化时把平台对应的处理类放到一个map中
     */
    public void afterPropertiesSet() throws Exception {
        msgContext.put(channel(), this);
    }

    /**
     * 对外提供的方法
     * @return
     */
    @Override
    public ApiResult doSendMsg(NoticeMsgInfoDTO noticeMsgInfo) {
        ApiResult sendResult = sendMsg(noticeMsgInfo);
        MessageChannelEnum channelEnum = channel();
        log.info("通过渠道【{}】发送消息【{}】", channelEnum.getName(), sendResult.isSuccess() ? "成功" : "失败");
        return sendResult;
    }

    /**
     * 具体子类实现逻辑
     * @return
     */
    public abstract ApiResult sendMsg(NoticeMsgInfoDTO noticeMsgInfo);

}