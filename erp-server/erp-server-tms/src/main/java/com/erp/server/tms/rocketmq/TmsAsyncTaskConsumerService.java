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
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.mapper.ReportPeriodMonthMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * tms 异步任务消费者
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.TMS_ASYNC_TASK_RECORD_TOPIC,
        selectorExpression = RocketMqNewTag.TMS_ASYNC_TASK_RECORD_TAG,
        consumerGroup = RocketMqConsumerGroup.TMS_ASYNC_TASK_RECORD_CONSUMER)
public class TmsAsyncTaskConsumerService implements RocketMQListener<TmsAsyncTaskRecordDTO.TaskDTO> {

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
        //头程对账单
        if(Objects.equals(businessType,SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode())){
        }
        //报关对账
        if(Objects.equals(businessType,SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode())){
        }
        //头程分摊
        if(Objects.equals(businessType,SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
        }
        //小包分摊
        if(Objects.equals(businessType,SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())){
        }
        //中转分摊
        if(Objects.equals(businessType,SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())){
        }
    }

    private void pushFirstMileCostAllocation(TmsAsyncTaskRecordDTO.TaskDTO dto) {
        String taskId = dto.getTaskId();
        String reportDate = dto.getReportDate();
        LocalDate reportPeriodMonth = LocalDate.parse(reportDate + "-01");

        // 头程重量分摊-费用状态为{未分摊，部分分摊}+本期账单数据 判断是否进入头程费用分摊表
        List<FirstMileWeightAllocationEntity> list = firstMileWeightAllocationService.listBySourceIds(null, null);

        if (CollectionUtils.isEmpty(list)) {
            asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),"重量分摊记录不存在");
            return;
        }
        List<String> deliveryIds = list.stream().map(FirstMileWeightAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliveryIds)) {
            asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),"发货单关联记录为空");
            return;
        }else {
            asyncTaskRecordService.lambdaUpdate().set(TmsAsyncTaskRecordEntity::getDetailCount,deliveryIds.size()).eq(TmsAsyncTaskRecordEntity::getId,taskId).update();
        }
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(deliveryIds);
        if (CollectionUtils.isEmpty(firstMileDeliveryEntityList)) {
            asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),"发货单记录不存在");
            return;
        }
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = wmsFirstMileDeliveryFeign.listDetailByMainIds(deliveryIds);
        if (CollectionUtils.isEmpty(deliveryDetailEntityList)) {
            asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),"发货单明细记录不存在");
            return;
        }


        List<TmsAsyncTaskDetailEntity> detailList = new ArrayList<>();
        for (FirstMileDeliveryEntity firstMileDeliveryEntity : firstMileDeliveryEntityList) {
            TmsAsyncTaskDetailEntity detail = new TmsAsyncTaskDetailEntity();
            detail.setMainId(taskId);
            detail.setBusinessType(dto.getBusinessType());
            detail.setBusinessId(firstMileDeliveryEntity.getId());
            detail.setBusinessCode(firstMileDeliveryEntity.getCode());
            detail.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
            detailList.add(detail);
        }
        asyncTaskDetailRecordService.saveBatch(detailList);

        for (TmsAsyncTaskDetailEntity detail : detailList) {
            LocalDateTime now = LocalDateTime.now();
            String taskDetailId = detail.getId();
            String businessId = detail.getBusinessId();

            FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(businessId)).findFirst().orElse(null);
            if (Objects.isNull(deliveryEntity)) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),"发货单记录不存在");
                continue;
            }
            List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList1 = deliveryDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(businessId)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(deliveryDetailEntityList1)) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),"发货单明细记录不存在");
                continue;
            }

            costAllocationPool.execute(() -> {

                try {
                    //构造数据
                    FirstMileCostAllocationEntity entity = new FirstMileCostAllocationEntity()
                            .setSourceId(deliveryEntity.getId()).setSourceCode(deliveryEntity.getCode()).setReportPeriodMonth(reportPeriodMonth);
                    BatchResultDTO result = firstMileCostAllocationService.calcAllocatedCost(entity, deliveryEntity, deliveryDetailEntityList1);
                    if(!result.getSuccess()){
                        asyncTaskDetailRecordService.updateDetail(taskDetailId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),result.getMsg());
                    }else {
                        asyncTaskDetailRecordService.updateDetail(taskDetailId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),"");
                    }
                } catch (Exception e) {
                    log.error("asyncPushAllocatedCost id: 【{}】, 异常: 【{}】",taskDetailId,e);
                    //把Exception e 转字符串
                    String errorMsg = ExceptionUtils.getStackTrace(e);
                    asyncTaskDetailRecordService.updateDetail(taskDetailId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),errorMsg);
                } finally {
                    asyncTaskRecordService.updateTaskFinally(taskId);
                }
            });

        }
    }


    private void pushSmallBagCostAllocation(TmsAsyncTaskRecordDTO.TaskDTO dto) {
        String taskId = dto.getTaskId();
//        List<LogisticsBillCostEntity> list = logisticsBillCostService.listByCanPushAllocation(dto.getType() ,dto.getReportDate());
        List<String> ids = logisticsBillCostService.listByCanPushAllocation(dto);
        List<LogisticsBillCostEntity> list = logisticsBillCostService.listByIds(ids);

        if(CollUtil.isEmpty(list)) {
            asyncTaskRecordService.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),ApiError.LOGISTICS_PENDING_COST_NOT_FOUND.getMsg());
            return;
        }else {
            asyncTaskRecordService.lambdaUpdate().set(TmsAsyncTaskRecordEntity::getDetailCount,list.size()).eq(TmsAsyncTaskRecordEntity::getId,taskId).update();
        }
        LocalDateTime now = LocalDateTime.now();
        for(LogisticsBillCostEntity logisticsBillCostEntity : list) {
            TmsAsyncTaskDetailEntity detail = new TmsAsyncTaskDetailEntity();
            detail.setMainId(taskId);
            detail.setBusinessType(dto.getBusinessType());
            detail.setBusinessId(logisticsBillCostEntity.getId());
            detail.setStatus(TmsAsyncTaskRecordStatusEnum.ING.getCode());
            detail.setStartTime(now);
            asyncTaskDetailRecordService.save(detail);

            String id = detail.getId();

            costAllocationPool.execute(() -> {
                try {
                    BatchResultDTO result = logisticsBillCostService.pushAllocation(logisticsBillCostEntity.getId(), dto.getReportDate());
                    if(result.getSuccess()){
                        asyncTaskDetailRecordService.updateDetail(id, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),"");
                    }else {
                        asyncTaskDetailRecordService.updateDetail(id, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),result.getMsg());
                    }
                } catch (Exception e) {
                    log.error("asyncPushAllocation id: 【{}】, 异常: 【{}】",logisticsBillCostEntity.getId(),e);
                    //把Exception e 转字符串
                    String errorMsg = ExceptionUtils.getStackTrace(e);
                    asyncTaskDetailRecordService.updateDetail(id, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),errorMsg);
                } finally {
                    asyncTaskRecordService.updateTaskFinally(taskId);
                }
            });
        }
    }


}
