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
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.server.msg.config.MsgContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


/**
 * @Classname: TestController
 * @Description: TODO
 * @CreateTime: 2023-04-20  19:21
 * @Author: zhangchunlin
 */
@Slf4j
@RestController
@RequestMapping(value = "/test")
public class TestController extends BaseController {

    @Autowired
    private MsgContext msgContext;

    @Autowired
    private MQProducerService<NoticeMsgInfoDTO> mqProducerService;

    @Autowired
    private RedissonClient redisson;

    /**
     * 发送消息
     */
    @RequestMapping("/sendMsg")
    public ApiResult sendMsg() {
        NoticeMsgInfoDTO noticeMsgInfoDTO = new NoticeMsgInfoDTO();
        noticeMsgInfoDTO.setReceiverUserIds(new ArrayList<>(Arrays.asList("1645710077245652993")));
        noticeMsgInfoDTO.setTitle("产品提醒: 张三 新建产品名称【iphone14】");
        noticeMsgInfoDTO.setContent("**产品名称: **iphone14\n**产品日期：**2023-04-20");
        noticeMsgInfoDTO.setUrgent(true);
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.SCM_TASK);
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
        //noticeMsgInfoDTO.setSendChannels(CollUtil.newArrayList(MessageChannelEnum.FEISHU));
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.SCM_TASK);
        // 默认tag请指定为msg_notice_default_tag，可以根据不同业务自行指定
        /*
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                noticeMsgInfoDTO, IdUtil.simpleUUID());
        if (!SendStatus.SEND_OK .equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
         */
        SendResult sendResult = mqProducerService.sendNoticeMsg(noticeMsgInfoDTO, null);
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
        noticeMsgInfoDTO.setNoticeTypeEnum(NoticeTypeEnum.SCM_TASK);
        // 默认tag请指定为msg_notice_default_tag，可以根据不同业务自行指定
        String tagName = RocketMqTagEnum.MSG_NOTICE_TAG.getName();
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.NOTICE_MSG_TOPIC, tagName,
                noticeMsgInfoDTO, IdUtil.simpleUUID());
        if (!SendStatus.SEND_OK .equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
        return success();
    }

    @GetMapping(value = "/testLock")
    public ApiResult<String> testLock() {
        String lockKey = "456";
        log.info("线程" + Thread.currentThread().getName() + "进来");
        RLock rLock = redisson.getLock(lockKey);
        try{
            /**
             * 尝试获取锁，并且会自动续期
             * waitTimeout 尝试获取锁的最大等待时间，超过这个值，则认为获取锁失败
             * leaseTime   锁的持有时间,超过这个时间锁会自动失效（值应设置为大于业务处理的时间，确保在锁有效期内业务能处理完），不配置默认为30S
             */
            //rLock.tryLock(60,TimeUnit.SECONDS);//不加waitTimeout默认为-1，则会一直尝试获取锁直到其他线程锁释放
            Boolean isLock = rLock.tryLock(5, TimeUnit.SECONDS);
            log.info("线程" + Thread.currentThread().getName() + "获取锁" + isLock);
            if(!isLock) {
                throw new RuntimeException("服务拥挤，请稍后再试");
            }
            //执行具体业务逻辑
            //控制器不使用Thread.sleep会阻塞
            //Thread.sleep(10000);
            while(1 == 1) {
                if( 2 > 3) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("操作失败，请稍后再试");
        } finally {
            log.info("线程" + Thread.currentThread().getName() + "尝试释放锁");
            if(rLock.isLocked() && rLock.isHeldByCurrentThread()){ // 锁是否存在，是当前执行线程的锁
                rLock.unlock(); // 释放锁
            }
        }
        return new ApiResult(200, "");
    }


}