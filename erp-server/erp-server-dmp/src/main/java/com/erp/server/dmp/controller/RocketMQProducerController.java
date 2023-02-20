package com.erp.server.dmp.controller;

import cn.hutool.json.JSONUtil;
import com.common.core.constant.RocketMqTopic;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.server.dmp.service.mq.MQConsumerDemoService;
import com.erp.server.dmp.service.mq.MQProducerService;
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
@RequestMapping("dmp/producer")
public class RocketMQProducerController extends BaseController {

    @Resource
    private MQProducerService<MQConsumerDemoService.ProducerDto.EntityDto> mQProducerService;

    @PostMapping("/syncClassMsg")
    public ApiResult syncClassMsg(@RequestBody MQConsumerDemoService.ProducerDto dto){
        mQProducerService.syncClassMsg(RocketMqTopic.DMP_TOPIC, dto.getTag(), dto.getEntity(), dto.getEntity().getId().toString());
        return success();
    }

    @PostMapping("/oneWaySendMsg")
    public ApiResult oneWaySendMsg(@RequestBody MQConsumerDemoService.ProducerDto dto){
        mQProducerService.oneWaySendMsg( dto.getEntity().getId().toString(),RocketMqTopic.DMP_TOPIC, dto.getTag(), "test", JSONUtil.toJsonStr(dto.getEntity()));
        return success();
    }

}
