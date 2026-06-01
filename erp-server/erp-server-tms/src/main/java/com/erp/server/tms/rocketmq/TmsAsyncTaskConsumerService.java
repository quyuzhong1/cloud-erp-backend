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
    private SmallBagCostAllocationService smallBagCostAllocationService;

    @Resource
    private TransferDeclareService transferDeclareService;

    @Resource
    private TransferDeclareCostAllocationService transferDeclareCostAllocationService;

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
            String methodType = dto.getMethodType();
            if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode())) {
                firstMileCostAllocationService.pushUpdateStatus(dto);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode())) {
                firstMileCostAllocationService.pushReAllocationCalcCost(dto);
            } else if (StringUtils.isBlank(methodType) || Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode())) {
                firstMileCostAllocationService.pushFirstMileCostAllocation(dto);
            } else {
                log.warn("头程分摊MQ方法类型不支持，跳过消费，taskId: {}, methodType: {}", taskId, methodType);
                asyncTaskRecordService.finishTaskWithError(taskId, "头程分摊MQ方法类型不支持: " + methodType);
                return;
            }
        }
        //小包分摊
        else if(Objects.equals(businessType,SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())){
            String methodType = dto.getMethodType();
            // 同一单据类型下按 methodType 二级分发；methodType 为空兜底为下推分摊（兼容历史/在途消息）
            if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode())) {
                //批量更新核算状态
                smallBagCostAllocationService.pushUpdateReportStatus(dto);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode())) {
                smallBagCostAllocationService.pushReAllocation(dto);
            } else if (StringUtils.isBlank(methodType) || Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode())) {
                //下推小包费用分摊
                logisticsBillCostService.pushSmallBagCostAllocation(dto);
            } else {
                log.warn("小包分摊MQ方法类型不支持，跳过消费，taskId: {}, methodType: {}", taskId, methodType);
                asyncTaskRecordService.finishTaskWithError(taskId, "小包分摊MQ方法类型不支持: " + methodType);
                return;
            }
        }
        //中转分摊
        else if(Objects.equals(businessType,SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())){
            String methodType = dto.getMethodType();
            if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode())) {
                transferDeclareCostAllocationService.pushUpdateReportStatus(dto);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode())) {
                transferDeclareCostAllocationService.pushReAllocation(dto);
            } else if (StringUtils.isBlank(methodType) || Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode())) {
                //创建任务明细等相关内容
                if (transferDeclareService.addTaskDetailByTransferDeclare(dto)) return;
                //下推中转费用分摊
                transferDeclareService.pushTransferDeclare(dto);
            } else {
                log.warn("中转分摊MQ方法类型不支持，跳过消费，taskId: {}, methodType: {}", taskId, methodType);
                asyncTaskRecordService.finishTaskWithError(taskId, "中转分摊MQ方法类型不支持: " + methodType);
                return;
            }
        }
        else {
            log.warn("TMS异步任务MQ业务类型不支持，跳过消费，taskId: {}, businessType: {}", taskId, businessType);
            asyncTaskRecordService.finishTaskWithError(taskId, "TMS异步任务MQ业务类型不支持: " + businessType);
            return;
        }
        log.info("TMS异步任务消费完成，taskId: {}, businessType: {}", taskId, businessType);
    }


}
