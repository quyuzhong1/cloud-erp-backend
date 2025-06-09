package com.erp.server.msg.utils;

import cn.hutool.core.util.ObjUtil;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.model.msg.enums.NoticeMessageTypeEnum;
import com.erp.server.msg.enums.MessageChannelAppEnum;
import com.erp.server.msg.model.FeiShuSendBaseParam;
import com.erp.server.msg.model.MsgSendChannelWrapParam;
import com.erp.server.msg.model.NoticeMsgWrapInfoDTO;
import com.erp.server.msg.model.WarnMsgContentDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname: FeishuUtil

 * @CreateTime: 2023-04-20  17:23
 * @Author: zhangchunlin
 */
public class MsgConvertUtil {
    private MsgConvertUtil() {
        throw new IllegalStateException("Utility MsgConvertUtil class");
    }
    /**
     * 任务通知填充卡片
     * @param noticeMsgInfo
     * @return
     */
    public static FeiShuSendBaseParam.ContentDTO wrapTypicalCard(NoticeMsgWrapInfoDTO noticeMsgInfo) {
        FeiShuSendBaseParam.ContentDTO contentDTO = new FeiShuSendBaseParam.ContentDTO();

        FeiShuSendBaseParam.CardDTO.ConfigDTO  config = new FeiShuSendBaseParam.CardDTO.ConfigDTO();
        config.setWideScreenMode(true);
        contentDTO.setConfig(config);

        FeiShuSendBaseParam.CardDTO.HeaderDTO header = new FeiShuSendBaseParam.CardDTO.HeaderDTO();
        header.setTitle(new FeiShuSendBaseParam.CardDTO.HeaderDTO.TitleDTO("plain_text", noticeMsgInfo.getTitle()));
        contentDTO.setHeader(header);

        List<FeiShuSendBaseParam.CardDTO.ElementsDTO> elements = new ArrayList<>();

        FeiShuSendBaseParam.CardDTO.ElementsDTO fieldElementsDTO = new FeiShuSendBaseParam.CardDTO.ElementsDTO();
        fieldElementsDTO.setTag("div");
        List<FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO> fields = new ArrayList<>();
        FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO fieldsDTO = new FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO(true,
                new FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO.TextDTO("lark_md", noticeMsgInfo.getContent()));
        fields.add(fieldsDTO);
        fieldElementsDTO.setFields(fields);
        elements.add(fieldElementsDTO);

        // 添加"详情"按钮
        if (ObjUtil.isNotEmpty(noticeMsgInfo.getNoticeMsgCardButtonDTO())) {

            FeiShuSendBaseParam.CardDTO.ElementsDTO actionElementsDTO = new FeiShuSendBaseParam.CardDTO.ElementsDTO();
            actionElementsDTO.setTag("action");
            List<FeiShuSendBaseParam.CardDTO.ElementsDTO.ActionsDTO> actions = new ArrayList<>();

            FeiShuSendBaseParam.CardDTO.ElementsDTO.ActionsDTO buttonActionDTO = new FeiShuSendBaseParam.CardDTO.ElementsDTO.ActionsDTO();
            buttonActionDTO.setTag("button");
            buttonActionDTO.setText(new FeiShuSendBaseParam.CardDTO.ElementsDTO.ActionsDTO.TextDTO(noticeMsgInfo.getNoticeMsgCardButtonDTO().getName(),"plain_text"));
            buttonActionDTO.setType("default");
            // 这里是按钮点击后的跳转链接
            buttonActionDTO.setUrl(noticeMsgInfo.getNoticeMsgCardButtonDTO().getUrl());

            actions.add(buttonActionDTO);
            actionElementsDTO.setActions(actions);
            elements.add(actionElementsDTO);

        }

        contentDTO.setElements(elements);
        return contentDTO;
    }

    /**
     * 填充消息内容
     * @param messageChannelEnum
     * @param noticeMessageTypeEnum
     * @param msgInfo
     * @return
     */
    public static MsgSendChannelWrapParam wrapMsgBody(MessageChannelEnum messageChannelEnum, MessageChannelAppEnum messageChannelAppEnum, NoticeMessageTypeEnum noticeMessageTypeEnum, NoticeMsgInfoDTO msgInfo) {
        MsgSendChannelWrapParam msgSendChannelWrapParam = new MsgSendChannelWrapParam();
        msgSendChannelWrapParam.setSendChannel(messageChannelEnum);
        msgSendChannelWrapParam.setChannelApp(messageChannelAppEnum);

        NoticeMsgWrapInfoDTO noticeMsgWrapInfoDTO = new NoticeMsgWrapInfoDTO();
        noticeMsgWrapInfoDTO.setReceiverUserIds(msgInfo.getReceiverUserIds());
        noticeMsgWrapInfoDTO.setTitle(msgInfo.getTitle());
        noticeMsgWrapInfoDTO.setContent(msgInfo.getContent());
        noticeMsgWrapInfoDTO.setUrgent(msgInfo.getUrgent());
        noticeMsgWrapInfoDTO.setNoticeMessageTypeEnum(noticeMessageTypeEnum);
        noticeMsgWrapInfoDTO.setNoticeMsgCardButtonDTO(msgInfo.getNoticeMsgCardButtonDTO());
        msgSendChannelWrapParam.setNoticeMsgWrapInfoDTO(noticeMsgWrapInfoDTO);

        msgSendChannelWrapParam.setSourceMsgInfo(msgInfo);

        return msgSendChannelWrapParam;
    }

    /**
     * 飞书预警信息填充卡片
     * @param warnMsgContentDTO
     * @return
     */
    public static FeiShuSendBaseParam.ContentDTO wrapTypicalCard(WarnMsgContentDTO warnMsgContentDTO) {
        FeiShuSendBaseParam.ContentDTO contentDTO = new FeiShuSendBaseParam.ContentDTO();

        FeiShuSendBaseParam.CardDTO.ConfigDTO  config = new FeiShuSendBaseParam.CardDTO.ConfigDTO();
        config.setWideScreenMode(true);
        contentDTO.setConfig(config);

        FeiShuSendBaseParam.CardDTO.HeaderDTO header = new FeiShuSendBaseParam.CardDTO.HeaderDTO();
        header.setTitle(new FeiShuSendBaseParam.CardDTO.HeaderDTO.TitleDTO("plain_text", warnMsgContentDTO.getTitle()));
        contentDTO.setHeader(header);

        List<FeiShuSendBaseParam.CardDTO.ElementsDTO> elements = new ArrayList<>();

        FeiShuSendBaseParam.CardDTO.ElementsDTO fieldElementsDTO = new FeiShuSendBaseParam.CardDTO.ElementsDTO();
        fieldElementsDTO.setTag("div");
        List<FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO> fields = new ArrayList<>();
        FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO fieldsDTO = new FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO(true,
                new FeiShuSendBaseParam.CardDTO.ElementsDTO.FieldsDTO.TextDTO("lark_md", warnMsgContentDTO.getContent()));
        fields.add(fieldsDTO);
        fieldElementsDTO.setFields(fields);
        elements.add(fieldElementsDTO);

        // 添加"详情"按钮
        if (ObjUtil.isNotEmpty(warnMsgContentDTO.getNoticeMsgCardButtonDTO())) {

            FeiShuSendBaseParam.CardDTO.ElementsDTO actionElementsDTO = new FeiShuSendBaseParam.CardDTO.ElementsDTO();
            actionElementsDTO.setTag("action");
            List<FeiShuSendBaseParam.CardDTO.ElementsDTO.ActionsDTO> actions = new ArrayList<>();

            FeiShuSendBaseParam.CardDTO.ElementsDTO.ActionsDTO buttonActionDTO = new FeiShuSendBaseParam.CardDTO.ElementsDTO.ActionsDTO();
            buttonActionDTO.setTag("button");
            buttonActionDTO.setText(new FeiShuSendBaseParam.CardDTO.ElementsDTO.ActionsDTO.TextDTO(warnMsgContentDTO.getNoticeMsgCardButtonDTO().getName(),"plain_text"));
            buttonActionDTO.setType("default");
            // 这里是按钮点击后的跳转链接
            buttonActionDTO.setUrl(warnMsgContentDTO.getNoticeMsgCardButtonDTO().getUrl());

            actions.add(buttonActionDTO);
            actionElementsDTO.setActions(actions);
            elements.add(actionElementsDTO);

        }

        contentDTO.setElements(elements);
        return contentDTO;
    }

}