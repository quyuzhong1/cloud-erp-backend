package com.erp.server.dmp.controller.api;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.message.service.mq.MQConsumerDemoService;
import com.common.message.service.mq.MQProducerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/2/17 12:39
 **/

@RestController
@RequestMapping("producer")
public class RocketMQProducerController extends BaseController {

    @Resource
    private MQProducerService<MQConsumerDemoService.ProducerDto.EntityDto> mQProducerService;


    @PostMapping("/syncClassMsg")
    public ApiResult syncClassMsg(@RequestBody MQConsumerDemoService.ProducerDto dto){
        mQProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, dto.getTag(), dto.getEntity(), dto.getEntity().getId().toString());
        return success();
    }

    @PostMapping("/oneWaySendMsg")
    public ApiResult oneWaySendMsg(@RequestBody MQConsumerDemoService.ProducerDto dto){
        mQProducerService.oneWaySendMsg( dto.getEntity().getId().toString(),RocketMqTopic.DMP_ERP_ORDER_TOPIC, dto.getTag(), "test", JSONUtil.toJsonStr(dto.getEntity()));
        return success();
    }

}
