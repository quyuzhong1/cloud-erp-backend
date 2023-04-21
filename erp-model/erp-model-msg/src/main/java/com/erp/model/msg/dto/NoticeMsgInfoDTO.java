package com.erp.model.msg.dto;

import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.model.msg.enums.NoticeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname: MsgInfo
 * @Description: 对外暴露的发送消息内容体，由对应的渠道解析
 * @CreateTime: 2023-04-20  10:22
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeMsgInfoDTO implements Serializable {

    /**
     * 消息接收者id集合，如果只有一个接收者则走单条发送
     */
    @NotNull(message = "消息接收人不能为空")
    private List<String> receiverUserIds;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     * 说明：飞书如果某些内容要加粗，请使用**仲景**，飞书请参考文档：https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/im-v1/message/create
     */
    @NotEmpty(message = "消息内容不能为空")
    private String content;

    /**
     * 消息来源，消息服务根据此判断需要发送的平台和消息类型（文本，卡片）
     */
    @NotNull(message = "消息来源不能为空")
    private NoticeTypeEnum noticeTypeEnum;

    /**
     * 是否加急，仅支持飞书
     */
    private Boolean urgent = Boolean.FALSE;

    /**
     * 发送渠道，如果指定了则优先按照这个配置的来；否则按照消息来源中定义的取
     */
    private List<MessageChannelEnum> sendChannels;

    // TODO 泛型扩展实体，便于后面扩展

    //------------MQ调用方法时传入，此处只是标识---------------------

    /**
     * 发送MQ消息的主题，取值从类RocketMqTopic.NOTICE_MSG_TOPIC，发送消息时会替换环境变量
     */
    // private String rocketMqTopic = "${spring.profiles.active}-notice_msg_topic";

    /**
     * 发送MQ消息的tag，不指定请赋值为msg_notice_default_tag，取值从类RocketMqTagEnum.MSG_NOTICE_TAG，可以不同的业务使用不同的tag，需替换*号
     */
    // private String rocketMqTag = "msg_notice_default_tag";


}