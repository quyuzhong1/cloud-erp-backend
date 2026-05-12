package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.server.wms.service.FirstMileDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 头程发货单自动生成报关明细消费者
 *
 * @author jack
 * @date 2026-05-12
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.WMS_FIRST_MILE_DECLARE_AUTO_GENERATE_TOPIC,
        selectorExpression = "wms_first_mile_declare_auto_generate_tag",
        consumerGroup = RocketMqConsumerGroup.WMS_FIRST_MILE_DECLARE_AUTO_GENERATE_CONSUMER)
public class FirstMileDeclareAutoGenerateConsumer implements RocketMQListener<AutoGenerateBillDTO> {

    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    /**
     * 消费头程发货单自动生成报关明细消息
     *
     * @param dto 自动生成参数
     * @return void
     * @throws ServiceException 自动生成失败时抛出
     */
    @Override
    public void onMessage(AutoGenerateBillDTO dto) {
        log.error("头程发货单自动生成报关明细任务开始，dto={}", JSONUtil.toJsonStr(dto));
        Boolean result = firstMileDeliveryService.consumeDeclareAutoGenerateTask(dto);
        if (!Boolean.TRUE.equals(result)) {
            throw new ServiceException("头程发货单自动生成报关明细任务处理失败");
        }
        log.error("头程发货单自动生成报关明细任务完成，id={}", dto.getId());
    }
}
