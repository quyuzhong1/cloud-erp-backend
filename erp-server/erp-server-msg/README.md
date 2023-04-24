# 1.调用方需maven依赖包erp-model-msg
        <dependency>
            <groupId>com.cloud.erp</groupId>
            <artifactId>erp-model-msg</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>
        
# 2.调用方需要组装实体NoticeMsgInfoDTO
需要通过RocketMQ发送消息
MQ主题为：RocketMqTopic.NOTICE_MSG_TOPIC
MQ Tag为：RocketMqTagEnum.MSG_NOTICE_TAG.getName()


# 3.至少需要填写receiverUserIds、title、content、noticeTypeEnum



# 4.提示：消息服务通过NoticeTypeEnum消息来源获取到需要发送的渠道、消息类型
发送渠道定义在枚举类MessageChannelEnum；渠道应用配置在枚举类MessageChannelAppEnum
配置在erp-sys库中的msg_config、msg_channel_config
msg_config的主键id从NoticeTypeEnum取值

        