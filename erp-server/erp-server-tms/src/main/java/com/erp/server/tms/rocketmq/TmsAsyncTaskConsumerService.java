package com.erp.server.tms.rocketmq;


import cn.hutool.json.JSONUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
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
public class TmsAsyncTaskConsumerService implements RocketMQListener<TmsAsyncTaskRecordEntity> {

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
    private TransferDeclareCostAllocationService transferDeclareCostAllocationService;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    /**
     * 消费 TMS 异步任务消息。
     * <p>
     * 已迁移任务优先按 TaskEnvelope 路由；未迁移任务继续按 PushParamsDTO 解析，
     * 以便小包试点和历史 TMS 异步任务在迁移期间共存。
     *
     * @param taskRecord MQ 任务记录消息
     */
    @Override
    public void onMessage(TmsAsyncTaskRecordEntity taskRecord) {
        if (Objects.isNull(taskRecord) || StringUtils.isBlank(taskRecord.getId())) {
            log.warn("TMS异步任务MQ消息为空，跳过消费");
            return;
        }
        String taskId = taskRecord.getId();
        String businessType = taskRecord.getBusinessType();
        String methodType = taskRecord.getMethodType();
        if (StringUtils.isBlank(businessType)) {
            log.warn("TMS异步任务业务类型为空，跳过消费，taskId: {}", taskId);
            asyncTaskRecordService.finishTaskWithError(taskId, "TMS异步任务业务类型为空");
            return;
        }
        log.info("开始消费TMS异步任务，taskId: {}, businessType: {}", taskId, businessType);
        //头程对账单
        if(Objects.equals(businessType,SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode())){
            if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode())) {
                tmsFirstMileReconciliationDetailService.pushFirstMileReconciliation(taskRecord);
            } else {
                log.warn("头程对账单MQ方法类型不支持，跳过消费，taskId: {}, methodType: {}", taskId, methodType);
                asyncTaskRecordService.finishTaskWithError(taskId, "头程对账单MQ方法类型不支持: " + methodType);
                return;
            }
        }
        //报关对账
        else if(Objects.equals(businessType,SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode())){
            if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode())) {
                tmsB2cDeclareReconciliationDetailService.pushDeclareReconciliation(taskRecord);
            } else {
                log.warn("报关对账MQ方法类型不支持，跳过消费，taskId: {}, methodType: {}", taskId, methodType);
                asyncTaskRecordService.finishTaskWithError(taskId, "报关对账MQ方法类型不支持: " + methodType);
                return;
            }
        }
        //头程分摊
        else if(Objects.equals(businessType,SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
            if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode())) {
                firstMileCostAllocationService.pushUpdateStatus(taskRecord);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode())) {
                firstMileCostAllocationService.pushReAllocationCalcCost(taskRecord);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.DELETE.getCode())) {
                firstMileCostAllocationService.pushDelete(taskRecord);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode())) {
                firstMileCostAllocationService.pushFirstMileCostAllocation(taskRecord);
            } else {
                log.warn("头程分摊MQ方法类型不支持，跳过消费，taskId: {}, methodType: {}", taskId, methodType);
                asyncTaskRecordService.finishTaskWithError(taskId, "头程分摊MQ方法类型不支持: " + methodType);
                return;
            }
        }
        //小包分摊
        else if(Objects.equals(businessType,SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())){
            if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode())) {
                smallBagCostAllocationService.pushUpdateReportStatus(taskRecord);
            } else if (isUpdateReconciliationStatusMethodType(methodType)) {
                logisticsBillCostService.pushUpdateReconciliationStatus(taskRecord);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode())) {
                smallBagCostAllocationService.pushReAllocation(taskRecord);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.DELETE.getCode())) {
                smallBagCostAllocationService.pushDelete(taskRecord);
            } else if (isSmallBagPushAllocationMethodType(methodType)) {
                //下推小包费用分摊
                logisticsBillCostService.pushSmallBagCostAllocation(taskRecord);
            } else {
                log.warn("小包分摊MQ方法类型不支持，跳过消费，taskId: {}, methodType: {}", taskId, methodType);
                asyncTaskRecordService.finishTaskWithError(taskId, "小包分摊MQ方法类型不支持: " + methodType);
                return;
            }
        }
        //中转分摊
        else if(Objects.equals(businessType,SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())){
            if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.UPDATE_REPORT_STATUS.getCode())) {
                transferDeclareCostAllocationService.pushUpdateReportStatus(taskRecord);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.RE_ALLOCATION.getCode())) {
                transferDeclareCostAllocationService.pushReAllocation(taskRecord);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.DELETE.getCode())) {
                transferDeclareCostAllocationService.pushDelete(taskRecord);
            } else if (Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode())) {
                transferDeclareCostAllocationService.pushTransferDeclareCostAllocation(taskRecord);
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

    /**
     * 判断小包费用分摊下推方法类型。
     *
     * @param methodType 方法类型
     * @return true 表示应路由到小包费用分摊下推消费逻辑
     */
    private boolean isSmallBagPushAllocationMethodType(String methodType) {
        return StringUtils.isBlank(methodType)
            || Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.PUSH_ALLOCATION.getCode())
            || Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.SELFDELIVER_PUSH_ALLOCATION.getCode())
            || Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.LASTMILE_PUSH_ALLOCATION.getCode());
    }

    private boolean isUpdateReconciliationStatusMethodType(String methodType) {
        return Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.SELFDELIVER_UPDATE_RECONCILIATION_STATUS.getCode())
            || Objects.equals(methodType, TmsAsyncTaskMethodTypeEnum.LASTMILE_UPDATE_RECONCILIATION_STATUS.getCode());
    }
}
