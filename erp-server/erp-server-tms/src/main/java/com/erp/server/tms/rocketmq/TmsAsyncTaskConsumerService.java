package com.erp.server.tms.rocketmq;


import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * tms 异步任务消费者
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.TMS_ASYNC_TASK_RECORD_TOPIC,
        selectorExpression = RocketMqNewTag.TMS_ASYNC_TASK_RECORD_TAG,
        consumerGroup = RocketMqConsumerGroup.TMS_ASYNC_TASK_RECORD_CONSUMER)
public class TmsAsyncTaskConsumerService implements RocketMQListener<TmsAsyncTaskRecordDTO.PushParamsDTO> {

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;

    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Resource
    private TransferDeclareService transferDeclareService;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Override
    public void onMessage(TmsAsyncTaskRecordDTO.PushParamsDTO dto) {
        if (Objects.isNull(dto)) {
            log.warn("TMS异步任务MQ消息为空，跳过消费");
            return;
        }
        String taskId = dto.getTaskId();
        String businessType = dto.getBusinessType();
        log.info("开始消费TMS异步任务，taskId: {}, businessType: {}", taskId, businessType);
        //头程对账单
        if(Objects.equals(businessType,SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode())){
            //创建任务明细等相关内容
            if ( tmsFirstMileReconciliationDetailService.addTaskDetailByFirstMileReconciliation(dto)) return;
            //下推头程对账单
            tmsFirstMileReconciliationDetailService.pushFirstMileReconciliation(dto);
        }

        //报关对账
        else if(Objects.equals(businessType,SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode())){
            //创建任务明细等相关内容
            if ( tmsB2cDeclareReconciliationDetailService.addTaskDetailByDeclareReconciliation(dto)) return;
            //下推报关对账
            tmsB2cDeclareReconciliationDetailService.pushDeclareReconciliation(dto);
        }
        //头程分摊
        else if(Objects.equals(businessType,SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
            //下推费用分摊
            firstMileCostAllocationService.pushFirstMileCostAllocation(dto);
        }
        //小包分摊
        else if(Objects.equals(businessType,SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())){
            //下推小包费用分摊
            logisticsBillCostService.pushSmallBagCostAllocation(dto);
        }
        //中转分摊
        else if(Objects.equals(businessType,SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())){
            //创建任务明细等相关内容
            if (transferDeclareService.addTaskDetailByTransferDeclare(dto)) return;
            //下推小包费用分摊
            transferDeclareService.pushTransferDeclare(dto);
        }
        else {
            log.warn("下推分摊MQ业务类型不支持，跳过消费，taskId: {}, businessType: {}", taskId, businessType);
            asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),"下推分摊MQ业务类型不支持，跳过消费");
            return;
        }
        log.info("TMS异步任务消费完成，taskId: {}, businessType: {}", taskId, businessType);
    }


}
