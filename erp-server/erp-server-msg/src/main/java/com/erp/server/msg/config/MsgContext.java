package com.erp.server.msg.config;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.IdUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.model.msg.enums.NoticeMessageTypeEnum;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.sys.vo.MsgChannelConfigDTO;
import com.erp.model.sys.vo.MsgConfigDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.msg.enums.MessageChannelAppEnum;
import com.erp.server.msg.model.MsgSendChannelWrapParam;
import com.erp.server.msg.service.IMessageSendService;
import com.erp.server.msg.utils.MsgConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname: MsgHolder
 * @Description: 消息控制
 * @CreateTime: 2023-04-19  14:14
 * @Author: zhangchunlin
 */
@Slf4j
@Component
public class MsgContext {

    @Autowired
    private SysUserFeign sysUserFeign;

    /**
     * 找不到发送渠道时默认的发送渠道
     */
    @Value("${default_send_channel:'feishu'}")
    private String defaultSendChannel;

    private static final NoticeMessageTypeEnum DEFAULT_MESSAGE_TYPE = NoticeMessageTypeEnum.ACTION_CARD;

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
        MsgConfigDTO msgConfigDTO = null;
        NoticeTypeEnum noticeTypeEnum = msgInfo.getNoticeTypeEnum();
        if(Objects.nonNull(noticeTypeEnum)) {
            msgConfigDTO = sysUserFeign.getMsgConfigById(noticeTypeEnum.getCode());
        }
        if(CollUtil.isEmpty(sendChannels)) {
            log.info("消息发送者没有指定发送渠道，通过消息来源获取配置的发送渠道及其他信息，消息内容：{}", JSONObject.toJSONString(msgInfo));
            sendChannels = wrapMessageChannelNotExists(msgConfigDTO, msgInfo);
            if(null == sendChannels) {
                return;
            }
        }
        log.info("本次消息发送渠道：【{}】，发送内容：【{}】",JSONObject.toJSONString(sendChannels), msgInfo);
        List<MsgSendChannelWrapParam> msgSendChannelWrapParams = wrapSendChannelWithApps(msgConfigDTO, msgInfo, sendChannels);
        if(CollUtil.isNotEmpty(msgSendChannelWrapParams)) {
            msgSendChannelWrapParams.stream().forEach(msgSendChannelWrapParam -> {
                HOLDER.get(msgSendChannelWrapParam.getSendChannel()).doSendMsg(msgSendChannelWrapParam);
            });
        }
    }

    public void routeSendWarnMsg(WarnMsgInfoDTO warnMsgInfo) {
        HOLDER.get(MessageChannelEnum.FEISHU).doSendWarnMsg(warnMsgInfo);
    }

    /**
     * 获取发送渠道信息
     * @param msgConfigDTO
     * @param msgInfo
     * @return
     */
    public List<MessageChannelEnum> wrapMessageChannelNotExists(MsgConfigDTO msgConfigDTO, NoticeMsgInfoDTO msgInfo) {
        NoticeTypeEnum noticeTypeEnum = msgInfo.getNoticeTypeEnum();
        List<MessageChannelEnum> sendChannels = Lists.newArrayList();
        if(Objects.nonNull(noticeTypeEnum)) {
            if(Objects.isNull(msgConfigDTO) || StrUtils.isEmpty(msgConfigDTO.getId())) {
                log.warn("系统未配置消息来源：{}，取平台配置的发送渠道",msgInfo.getNoticeTypeEnum().getCode());
                MessageChannelEnum messageChannelEnum = MessageChannelEnum.of(defaultSendChannel);
                sendChannels.add(messageChannelEnum);
                return sendChannels;
            }
            if(!Objects.equals(msgConfigDTO.getSendFlag(), Boolean.TRUE)) {
                log.warn("系统配置的消息来源：{}不需要发送消息，不发送消息",msgInfo.getNoticeTypeEnum().getCode());
                return null;
            }
            String[] channelCodes = StrUtils.null2EmptyWithTrim(msgConfigDTO.getChannel()).split(",");
            for(String channelCode : channelCodes) {
                MessageChannelEnum messageChannelEnum = MessageChannelEnum.of(channelCode);
                if(Objects.nonNull(messageChannelEnum)) {
                    sendChannels.add(messageChannelEnum);
                }
            }
        } else {
            MessageChannelEnum messageChannelEnum = MessageChannelEnum.of(defaultSendChannel);
            log.warn("调用方没有传递消息来源信息，无法定位通过什么渠道发送消息，默认从配置中心取：{}",messageChannelEnum);
            if(Objects.isNull(messageChannelEnum)) {
                log.warn("调用方没有传递消息来源信息，无法定位通过什么渠道发送消息，从配置中心配置的渠道不存在：{}，本次将不发送消息",defaultSendChannel);
                return null;
            }
            sendChannels.add(messageChannelEnum);
        }
        if(CollUtil.isNotEmpty(sendChannels)) {
            return sendChannels;
        } else {
            log.warn("系统配置的发送渠道错误，从配置中心取默认的配置渠道");
            sendChannels.add(MessageChannelEnum.of(defaultSendChannel));
            return sendChannels;
        }
    }

    /**
     * 根据渠道填充发送的应用
     * @param msgConfigDTO
     * @param msgInfo
     * @param messageChannelEnums
     * @return
     */
    private List<MsgSendChannelWrapParam> wrapSendChannelWithApps(MsgConfigDTO msgConfigDTO, NoticeMsgInfoDTO msgInfo, List<MessageChannelEnum> messageChannelEnums) {
        String msgId = IdUtils.simpleUUID();
        List<MsgSendChannelWrapParam> sendChannelApps = Lists.newArrayList();
        NoticeTypeEnum noticeTypeEnum = msgInfo.getNoticeTypeEnum();
        NoticeMessageTypeEnum noticeMessageTypeEnum;
        if(Objects.isNull(msgConfigDTO) || StrUtils.isEmpty(msgConfigDTO.getMsgType())) {
            noticeMessageTypeEnum = DEFAULT_MESSAGE_TYPE;
            log.warn("请求的消息来源未找到消息配置信息或未传请求来源参数，消息来源类型：{}，消息类型默认取：{}",msgInfo.getNoticeTypeEnum(),noticeMessageTypeEnum);
        } else {
            noticeMessageTypeEnum = NoticeMessageTypeEnum.of(msgConfigDTO.getMsgType());
        }
        List<MsgChannelConfigDTO> msgChannelConfigDTOS;
        Boolean fallBack = Boolean.FALSE;
        if(Objects.nonNull(noticeTypeEnum)) {
            msgChannelConfigDTOS = sysUserFeign.findByMsgConfigId(noticeTypeEnum.getCode());
            if(CollUtil.isEmpty(msgChannelConfigDTOS)) {
                log.warn("请求的消息来源在数据库中未找到消息渠道配置信息，请求的消息来源：{}",msgInfo.getNoticeTypeEnum());
                fallBack = Boolean.TRUE;
            } else {
                // 有可能某个渠道未在数据库中配置，即存在部分
                Map<String,List<MsgChannelConfigDTO>> msgChannelConfigMap = msgChannelConfigDTOS.stream().collect(Collectors.groupingBy(MsgChannelConfigDTO::getChannelCode));
                if(CollUtil.isEmpty(msgChannelConfigMap)) {
                    fallBack = Boolean.TRUE;
                } else {
                    messageChannelEnums.stream().forEach(messageChannelEnum -> {
                        String channelCode = messageChannelEnum.getCode();
                        if(msgChannelConfigMap.containsKey(channelCode)) { // 数据库配置中存在
                            List<MsgChannelConfigDTO> channelConfigs = msgChannelConfigMap.get(channelCode);
                            channelConfigs.stream().forEach(channelConfig->{
                                if(Objects.equals(channelConfig.getSendFlag(), Boolean.TRUE)) {
                                    MessageChannelAppEnum messageChannelAppEnum = MessageChannelAppEnum.of(channelConfig.getChannelAppCode());
                                    MsgSendChannelWrapParam msgSendChannelWrapParam = MsgConvertUtil.wrapMsgBody(messageChannelEnum, messageChannelAppEnum, noticeMessageTypeEnum, msgInfo);
                                    sendChannelApps.add(msgSendChannelWrapParam);
                                } else {
                                    log.warn("消息来源【{}】对应的渠道【{}】未开启发送消息，不发送消息",noticeTypeEnum.getName(),messageChannelEnum.getName());
                                }
                            });
                        } else { // 存在部分配置
                            log.warn("数据库配置中不存在消息来源【{}】,消息渠道【{}】的配置",noticeTypeEnum.getName(), messageChannelEnum.getName());
                            MsgSendChannelWrapParam msgSendChannelWrapParam = MsgConvertUtil.wrapMsgBody(messageChannelEnum, null, noticeMessageTypeEnum, msgInfo);
                            sendChannelApps.add(msgSendChannelWrapParam);
                        }
                    });
                }
            }
        } else {
            fallBack = Boolean.TRUE;
        }
        if(fallBack) {
            messageChannelEnums.stream().forEach(messageChannelEnum -> {
                // 此处无法定位发送渠道，取配置中心配置的默认渠道，对应渠道的应用编码为空，具体实现类中去取默认值
                MsgSendChannelWrapParam msgSendChannelWrapParam = MsgConvertUtil.wrapMsgBody(messageChannelEnum, null, noticeMessageTypeEnum, msgInfo);
                sendChannelApps.add(msgSendChannelWrapParam);
            });
        }
        if(CollUtil.isNotEmpty(sendChannelApps)) {
            sendChannelApps.stream().forEach(data->data.setMsgId(msgId));
        }
        return sendChannelApps;
    }

}