package com.erp.server.tms.rocketmq;


import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.mapper.ReportPeriodMonthMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.TMS_PUSH_ALLOCATION_COST_TOPIC,
        selectorExpression = RocketMqNewTag.TMS_PUSH_ALLOCATION_COST_TAG,
        consumerGroup = RocketMqConsumerGroup.TMS_PUSH_ALLOCATION_COST_CONSUMER)
public class PushAllocationCostConsumerService implements RocketMQListener<TmsAsyncTaskRecordDTO.TaskDTO> {

    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

    @Resource
    private TmsAsyncTaskRecordService asyncTaskRecordService;

    @Resource
    private TmsAsyncTaskDetailService asyncTaskDetailRecordService;

    @Resource
    private ReportPeriodMonthMapper reportPeriodMonthMapper;

    //发货单
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;
    @Resource
    private CfgSettingService cfgSettingService;

    @Autowired
    @Qualifier("costAllocationPool")
    private ExecutorService costAllocationPool;

    @Override
    public void onMessage(TmsAsyncTaskRecordDTO.TaskDTO dto) {
        String businessType = dto.getBusinessType();
        if(Objects.equals(businessType,SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
            //下推费用分摊
            pushFirstMileCostAllocation(dto);
        }else {
            //下推小包费用分摊
            pushSmallBagCostAllocation(dto);
        }
    }

    private void pushFirstMileCostAllocation(TmsAsyncTaskRecordDTO.TaskDTO dto) {
        String taskId = dto.getTaskId();
        String reportDate = dto.getReportDate();
        LocalDate reportPeriodMonth = LocalDate.parse(reportDate + "-01");

        TmsAsyncTaskRecordEntity tmsAsyncTaskRecordEntity = asyncTaskRecordService.getById(taskId);
        if(Objects.isNull(tmsAsyncTaskRecordEntity)){
            log.error("任务记录不存在，taskId: {}", taskId);
            asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),ApiError.LOGISTICS_PENDING_COST_NOT_FOUND.getMsg());
            return;
        }

        List<TmsAsyncTaskDetailEntity> detailList = asyncTaskDetailRecordService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).list();
        List<String> firstMileDeliveryIds = detailList.stream().map(TmsAsyncTaskDetailEntity::getBusinessId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        Map<String, FirstMileDeliveryEntity> deliveryMap = wmsFirstMileDeliveryFeign
                .listByIds(firstMileDeliveryIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(FirstMileDeliveryEntity::getId, Function.identity(),(o1,o2)->o1));

        Map<String, List<FirstMileDeliveryDetailEntity>> detailMap = wmsFirstMileDeliveryFeign
                .listDetailByMainIds(firstMileDeliveryIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FirstMileDeliveryDetailEntity::getMainId));
        CountDownLatch latch = new CountDownLatch(detailList.size());

        for (TmsAsyncTaskDetailEntity detail : detailList) {
            String taskDetailId = detail.getId();
            String businessId = detail.getBusinessId();

            costAllocationPool.execute(() -> {
                try {
                    processSingleTask(detail, deliveryMap, detailMap, reportPeriodMonth, taskDetailId, businessId);
                } catch (Exception e) {
                    log.error("处理任务失败 taskDetailId: {}", taskDetailId, e);
                    // 统一处理任务失败状态更新
                    updateTaskDetailFailure(taskDetailId, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean await = latch.await(tmsAsyncTaskRecordEntity.getExecTimeout(), TimeUnit.SECONDS);// 等待所有任务完成
            if(await){
                updateTaskFinally(taskId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("任务等待中断", e);
        }
    }

    private void processSingleTask(TmsAsyncTaskDetailEntity detail,
                                   Map<String, FirstMileDeliveryEntity> deliveryMap,
                                   Map<String, List<FirstMileDeliveryDetailEntity>> detailMap,
                                   LocalDate reportPeriodMonth,
                                   String taskDetailId, String businessId) {
            FirstMileDeliveryEntity deliveryEntity = deliveryMap.get(businessId);
            if (Objects.isNull(deliveryEntity)) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId,
                        TmsAsyncTaskRecordStatusEnum.FAILED.getCode(), "发货单记录不存在");
                return;
            }

            List<FirstMileDeliveryDetailEntity> deliveryDetails = detailMap.getOrDefault(businessId, Collections.emptyList());
            if (CollectionUtils.isEmpty(deliveryDetails)) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId,
                        TmsAsyncTaskRecordStatusEnum.FAILED.getCode(), "发货单明细记录不存在");
                return;
            }

            FirstMileCostAllocationEntity entity = new FirstMileCostAllocationEntity()
                    .setSourceId(deliveryEntity.getId())
                    .setSourceCode(deliveryEntity.getCode())
                    .setReportPeriodMonth(reportPeriodMonth);

            asyncTaskDetailRecordService.updateDetail(taskDetailId,
                TmsAsyncTaskRecordStatusEnum.ING.getCode(), "");

            BatchResultDTO result = firstMileCostAllocationService
                    .calcAllocatedCost(entity, deliveryEntity, deliveryDetails);

            if (!result.getSuccess()) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId,
                        TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),
                        StringUtils.substring(result.getMsg(), 0, 1000)); // 限制错误信息长度
            } else {
                asyncTaskDetailRecordService.updateDetail(taskDetailId,
                        TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "");
            }
    }
    /**
     * 任务结束，记录错误数量
     */
    private void updateTaskFinally(String taskId) {
        Integer errorCount = asyncTaskDetailRecordService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode()).count();

        asyncTaskRecordService.lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FINISH.getCode())
                .set(TmsAsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskRecordEntity::getErrorData, "")
                .set(TmsAsyncTaskRecordEntity::getErrorCount,errorCount)
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .update();
    }

    /**
     * 统一处理任务详情失败状态更新
     */
    private void updateTaskDetailFailure(String taskDetailId, Exception e) {
        //把Exception e 转字符串
        String errorMsg = ExceptionUtils.getStackTrace(e);
        if (StringUtils.isBlank(errorMsg)) {
            errorMsg = "未知错误";
        }
        // 限制错误信息长度，避免数据库字段超限
        asyncTaskDetailRecordService.updateDetail(taskDetailId,
                TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),
                StringUtils.substring(errorMsg, 0, 1000));
    }


    private void pushSmallBagCostAllocation(TmsAsyncTaskRecordDTO.TaskDTO dto) {
        String taskId = dto.getTaskId();
        TmsAsyncTaskRecordEntity tmsAsyncTaskRecordEntity = asyncTaskRecordService.getById(taskId);
        if(Objects.isNull(tmsAsyncTaskRecordEntity)){
            log.error("任务记录不存在，taskId: {}", taskId);
            asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),ApiError.LOGISTICS_PENDING_COST_NOT_FOUND.getMsg());
            return;
        }

        List<TmsAsyncTaskDetailEntity> detailList = asyncTaskDetailRecordService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).list();
        LocalDateTime now = LocalDateTime.now();
        CountDownLatch latch = new CountDownLatch(detailList.size());

        for (TmsAsyncTaskDetailEntity detail : detailList) {
            String taskDetailId = detail.getId();
            String businessId = detail.getBusinessId();

            costAllocationPool.execute(() -> {
                try {
                    asyncTaskDetailRecordService.updateDetail(taskDetailId,
                            TmsAsyncTaskRecordStatusEnum.ING.getCode(), "");

                    BatchResultDTO result = logisticsBillCostService.pushAllocation(businessId, dto.getReportDate());

                    if (!result.getSuccess()) {
                        asyncTaskDetailRecordService.updateDetail(taskDetailId,
                                TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),
                                StringUtils.substring(result.getMsg(), 0, 1000)); // 限制错误信息长度
                    } else {
                        asyncTaskDetailRecordService.updateDetail(taskDetailId,
                                TmsAsyncTaskRecordStatusEnum.FINISH.getCode(), "");
                    }
                } catch (Exception e) {
                    log.error("处理任务失败 taskDetailId: {}", taskDetailId, e);
                    // 统一处理任务失败状态更新
                    updateTaskDetailFailure(taskDetailId, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean await = latch.await(tmsAsyncTaskRecordEntity.getExecTimeout(), TimeUnit.SECONDS);// 等待所有任务完成
            if(await){
                updateTaskFinally(taskId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("任务等待中断", e);
        }
    }


}
