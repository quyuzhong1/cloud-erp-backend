package com.erp.server.msg.model;

import com.erp.model.msg.enums.NoticeMessageTypeEnum;
import com.erp.model.msg.enums.NoticeTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname: NoticeMsgWrapInfoDTP
 * @Description: TODO
 * @CreateTime: 2023-04-21  15:51
 * @Author: zhangchunlin
 */
@Data
public class NoticeMsgWrapInfoDTO implements Serializable {

    /**
     * 消息接收者id集合，如果只有一个接收者则走单条发送
     */
    private List<String> receiverUserIds;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     * 说明：飞书如果某些内容要加粗，请使用**仲景**，飞书请参考文档：https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/im-v1/message/create
     */
    private String content;

    /**
     * 消息来源，消息服务根据此判断需要发送的平台和消息类型（文本，卡片）
     */
    private NoticeMessageTypeEnum noticeMessageTypeEnum;

    /**
     * 是否加急，仅支持飞书
     */
    private Boolean urgent = Boolean.FALSE;


}