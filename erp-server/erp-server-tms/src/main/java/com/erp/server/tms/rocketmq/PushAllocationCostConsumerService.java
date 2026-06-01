package com.erp.server.tms.rocketmq;

import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.enums.TmsAsyncTaskMethodTypeEnum;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.TMS_PUSH_ALLOCATION_COST_TOPIC,
        selectorExpression = RocketMqNewTag.TMS_PUSH_ALLOCATION_COST_TAG,
        consumerGroup = RocketMqConsumerGroup.TMS_PUSH_ALLOCATION_COST_CONSUMER)
public class PushAllocationCostConsumerService implements RocketMQListener<TmsAsyncTaskRecordDTO.PushParamsDTO> {

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Override
    public void onMessage(TmsAsyncTaskRecordDTO.PushParamsDTO dto) {
        if (Objects.isNull(dto)) {
            log.warn("下推分摊MQ消息为空，跳过消费");
            return;
        }
        String taskId = dto.getTaskId();
        String businessType = dto.getBusinessType();
        if(StringUtils.isBlank(taskId)){
            log.warn("下推分摊MQ消息的taskId为空，跳过消费");
            return;
        }
        if(StringUtils.isBlank(businessType)){
            log.warn("下推分摊MQ消息的businessType为空，跳过消费");
            return;
        }

        String methodType = dto.getMethodType();
        log.info("开始消费下推分摊任务，taskId: {}, businessType: {}, methodType: {}", taskId, businessType, methodType);
        if (Objects.equals(businessType, SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())) {
            firstMileCostAllocationService.pushFirstMileCostAllocation(dto);
        } else if (Objects.equals(businessType, SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())) {
            // 同一单据类型下按 methodType 二级分发；methodType 为空兜底为下推分摊（兼容历史/在途消息）
            if (StringUtils.isBlank(methodType) || Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode())) {
                logisticsBillCostService.pushSmallBagCostAllocation(dto);
            } else {
                log.warn("下推分摊MQ方法类型不支持，跳过消费，taskId: {}, methodType: {}", taskId, methodType);
                asyncTaskRecordService.finishTaskWithError(taskId, "下推分摊MQ方法类型不支持: " + methodType);
                return;
            }
        } else {
            log.warn("下推分摊MQ业务类型不支持，跳过消费，taskId: {}, businessType: {}", taskId, businessType);
            asyncTaskRecordService.finishTaskWithError(taskId, "下推分摊MQ业务类型不支持: " + businessType);
            return;
        }
        log.info("下推分摊任务消费完成，taskId: {}, businessType: {}, methodType: {}", taskId, businessType, methodType);
    }



}
