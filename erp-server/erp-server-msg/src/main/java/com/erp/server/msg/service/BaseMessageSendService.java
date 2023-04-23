package com.erp.server.msg.service;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.server.msg.config.MsgContext;
import com.erp.server.msg.constant.MongoTableConstant;
import com.erp.server.msg.model.MsgResultVO;
import com.erp.server.msg.model.MsgSendChannelWrapParam;
import com.erp.server.msg.model.entity.MsgLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.Resource;
import java.util.Objects;

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

    @Autowired
    private MongoTemplate mongoTemplate;


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
    public MsgResultVO doSendMsg(MsgSendChannelWrapParam noticeMsgInfo) {
        MsgResultVO sendResult = sendMsg(noticeMsgInfo);
        MessageChannelEnum channelEnum = channel();
        log.info("通过渠道【{}】发送消息【{}】", channelEnum.getName(), sendResult.isSuccess() ? "成功" : "失败");
        recordLog(noticeMsgInfo, sendResult, channelEnum);
        return sendResult;
    }

    /**
     * 记录日志
     * @param noticeMsgInfo
     * @param sendResult
     * @param channelEnum
     */
    private void recordLog(MsgSendChannelWrapParam noticeMsgInfo, MsgResultVO sendResult, MessageChannelEnum channelEnum) {
        // 记录日志
        MsgLog msgLog = new MsgLog();
        msgLog.setMsgId(noticeMsgInfo.getMsgId());
        msgLog.setMqTopic(RocketMqTopic.NOTICE_MSG_TOPIC);
        msgLog.setMqTag(RocketMqTagEnum.MSG_NOTICE_TAG.getName());
        msgLog.setSendChannelCode(channelEnum.getCode());
        msgLog.setMsgSourceContent(JSONObject.toJSONString(noticeMsgInfo.getSourceMsgInfo()));
        if(Objects.nonNull(sendResult)) {
            msgLog.setMsgChannelContent(sendResult.getRequestBody());
            msgLog.setChannelResultCode(StrUtils.null2EmptyWithTrim(sendResult.getCode()));
            msgLog.setChannelResultMsg(sendResult.getMsg());
            msgLog.setNeedResend(sendResult.getNeedReSend());
            msgLog.setRetryTimes(0);
            msgLog.setNeedResend(sendResult.getNeedReSend());
        }
        log.info("通过渠道【{}】发送消息【{}】，消息日志：【{}】", channelEnum.getName(), sendResult.isSuccess() ? "成功" : "失败", JSONObject.toJSONString(msgLog));
        try {
            mongoTemplate.insert(msgLog, MongoTableConstant.MSG_LOG);
        } catch (Exception e) {
            log.error("记录消息日志到mongodb异常",e);
        }
    }

    /**
     * 具体子类实现逻辑
     * @return
     */
    public abstract MsgResultVO sendMsg(MsgSendChannelWrapParam noticeMsgInfo);

}