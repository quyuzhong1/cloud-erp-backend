package com.erp.server.msg.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.server.msg.config.MsgContext;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * @Classname: TestController
 * @Description: TODO
 * @CreateTime: 2023-04-20  19:21
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/test")
public class TestController extends BaseController {

    @Autowired
    private MsgContext msgContext;

    @Autowired
    private MQProducerService<NoticeMsgInfoDTO> mqProducerService;

    /**
     * 发送消息
     */
    @RequestMapping("/sendMsg")
    public ApiResult sendMsg() {
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(new ArrayList<String>(Arrays.asList("1645710077245652993")));
        noticeMsgInfoDTO.setTitle("产品提醒: 张三 新建产品名称【iphone14】");
        noticeMsgInfoDTO.setContent("**产品名称: **iphone14\n**产品日期：**2023-04-20");
        noticeMsgInfoDTO.setUrgent(true);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.SCM_NOTICE);
        msgContext.routeSend(noticeMsgInfoDTO);
        return success();
    }

    /**
     * 发送单条消息（MQ）
     */
    @RequestMapping("/sendMsgMq")
    public ApiResult sendMsgMq() {
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(new ArrayList<>(Arrays.asList("1645710077245652993")));
        noticeMsgInfoDTO.setTitle("产品提醒: 张三 新建产品名称【iphone14】");
        // 请注意：飞书中的**和**中间的数据表示加粗
        noticeMsgInfoDTO.setContent("**产品名称: **iphone14\n**产品日期：**2023-04-20");
        noticeMsgInfoDTO.setUrgent(true);
        noticeMsgInfoDTO.setSendChannels(CollUtil.newArrayList(MessageChannelEnum.FEISHU));
        //noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.SCM_NOTICE);
        // 默认tag请指定为msg_notice_default_tag，可以根据不同业务自行指定
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                noticeMsgInfoDTO, IdUtil.simpleUUID());
        if (!SendStatus.SEND_OK .equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
        return success();
    }

    /**
     * 批量发送消息（MQ）
     */
    @RequestMapping("/sendMultiMsgMq")
    public ApiResult sendMultiMsgMq() {
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(CollUtil.newArrayList("1645710077245652993","1631292025469009921"));
        noticeMsgInfoDTO.setTitle("产品提醒: 张三 新建产品名称【iphone14】");
        // 请注意：飞书中的**和**中间的数据表示加粗
        noticeMsgInfoDTO.setContent("**产品名称: **iphone14\n**产品日期：**2023-04-20");
        noticeMsgInfoDTO.setUrgent(true);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.SCM_NOTICE);
        // 默认tag请指定为msg_notice_default_tag，可以根据不同业务自行指定
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                noticeMsgInfoDTO, IdUtil.simpleUUID());
        if (!SendStatus.SEND_OK .equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
        return success();
    }


}