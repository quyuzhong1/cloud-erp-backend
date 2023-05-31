package com.erp.server.msg.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.enums.ApiError;
import com.common.core.utils.StrUtils;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.model.sys.dto.SysUserSimpleDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.msg.config.properties.EMailProperties;
import com.erp.server.msg.enums.MessageChannelAppEnum;
import com.erp.server.msg.model.*;
import com.erp.server.msg.service.BaseMessageSendService;
import com.erp.server.msg.utils.MailSendUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname: EmailSendServiceImpl
 * @Description: 邮件发送消息业务类
 * @CreateTime: 2023-04-20  20:49
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class EmailSendServiceImpl extends BaseMessageSendService {

    @Autowired
    private SysUserFeign sysUserFeign;

    @Resource
    private EMailProperties eMailProperties;

    @Override
    public MsgResultVO sendMsg(MsgSendChannelWrapParam noticeMsgInfo) {
        log.info("通过邮件发送消息，消息内容：{}", JSONObject.toJSONString(noticeMsgInfo));
        NoticeMsgWrapInfoDTO noticeMsgWrapInfoDTO = noticeMsgInfo.getNoticeMsgWrapInfoDTO();
        if(CollUtil.isEmpty(noticeMsgWrapInfoDTO.getReceiverUserIds())) {
            log.error("邮件接收人为空，本次不发生邮件");
            return null;
        }
        // TODO 暂不未完善发送附件和抄送人
        List<String> receiverUserIds = noticeMsgWrapInfoDTO.getReceiverUserIds();
        List<SysUserSimpleDTO> sysUserSimpleDTOS = sysUserFeign.getUserSimpleInfoByIds(receiverUserIds);
        if(CollUtil.isEmpty(sysUserSimpleDTOS)) {
            log.warn("未找到可用的用户信息，本次不发生邮件,用户id集合：{}",JSONObject.toJSONString(receiverUserIds));
            return null;
        }
        // 提取出邮箱地址
        List<String> receiverEmails = sysUserSimpleDTOS.stream().filter(r-> StrUtils.isNotEmpty(r.getEmail())).map(SysUserSimpleDTO::getEmail).distinct().collect(Collectors.toList());
        if(CollUtil.isEmpty(receiverEmails)) {
            log.warn("未找到可用的用户邮箱信息，本次不发生邮件，用户id集合：{}",JSONObject.toJSONString(receiverUserIds));
            return null;
        }
        MessageChannelAppEnum messageChannelAppEnum = noticeMsgInfo.getChannelApp();
        String msgChannelAppCode = Objects.isNull(messageChannelAppEnum) ? "" : messageChannelAppEnum.getCode();
        Map<String, MailConfigParam> configs = eMailProperties.getConfigs();
        // 如果没有找到应用，需使用默认值
        if(StrUtils.isEmpty(msgChannelAppCode)) {
            for (Map.Entry<String, MailConfigParam> configParamEntry : configs.entrySet()) {
                if(Objects.equals(configParamEntry.getValue().getIsDefault(),Boolean.TRUE)) {
                    msgChannelAppCode = configParamEntry.getKey();
                    break;
                }
            }

        }
        MailConfigParam mailConfigParam = configs.get(msgChannelAppCode);
        if(Objects.isNull(mailConfigParam)) {
            log.error("Nacos未配置邮箱应用或者未配置默认应用或系统配置的应用代码错误，应用代码：{}，不发送邮件",msgChannelAppCode);
            return null;
        }
        MsgResultVO msgResult = new MsgResultVO();
        MailAccount mailAccount = MailSendUtil.wrapSendMailAccount(mailConfigParam);
        String sendResult = null;
        try {
            sendResult = MailUtil.send(mailAccount, receiverEmails, noticeMsgWrapInfoDTO.getTitle(), noticeMsgWrapInfoDTO.getContent(), false);
        } catch (Exception e) {
            log.error("发送邮件异常", e);
            msgResult.setNeedReSend(Boolean.TRUE);
        }
        if(StrUtils.isNotEmpty(sendResult)) {
            log.info("发送邮件给邮箱：{}成功", JSONObject.toJSONString(receiverEmails));
            msgResult.setCode(200);
            msgResult.setMsg("操作成功");
        } else {
            ApiError sendMailError = ApiError.ERROR_1010;
            msgResult.setCode(sendMailError.code);
            msgResult.setMsg(sendMailError.msg);
        }
        return msgResult;
    }

    @Override
    public void doSendWarnMsg(WarnMsgInfoDTO msgInfo) {

    }

    @Override
    public MessageChannelEnum channel() {
        return MessageChannelEnum.MAIL;
    }
}