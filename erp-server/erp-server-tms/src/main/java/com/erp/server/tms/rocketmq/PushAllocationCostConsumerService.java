package com.erp.server.tms.rocketmq;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.tms.dto.AsyncTaskRecordDTO;
import com.erp.model.tms.dto.ReportPeriodMonthDTO;
import com.erp.model.tms.entity.AsyncTaskDetailRecordEntity;
import com.erp.model.tms.entity.AsyncTaskRecordEntity;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.model.tms.enums.AsyncTaskRecordStatusEnum;
import com.erp.model.tms.enums.CostAllocationStatusEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.mapper.ReportPeriodMonthMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.TMS_PUSH_ALLOCATION_COST_TOPIC,
        selectorExpression = RocketMqNewTag.TMS_PUSH_ALLOCATION_COST_TAG,
        consumerGroup = RocketMqConsumerGroup.TMS_PUSH_ALLOCATION_COST_CONSUMER)
public class PushAllocationCostConsumerService implements RocketMQListener<AsyncTaskRecordDTO.TaskDTO> {

    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

    @Resource
    private AsyncTaskRecordService asyncTaskRecordService;

    @Resource
    private AsyncTaskDetailRecordService asyncTaskDetailRecordService;

    @Resource
    private ReportPeriodMonthMapper reportPeriodMonthMapper;

    //发货单
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Override
    public void onMessage(AsyncTaskRecordDTO.TaskDTO dto) {
        String taskId = dto.getTaskId();
        String reportPeriodStr = dto.getReportDate();
        List<FirstMileCostAllocationEntity> entityList = new ArrayList<>();
        if (CollUtil.isNotEmpty(dto.getIds())){
            List<FirstMileWeightAllocationEntity> firstMileWeightAllocationEntities = firstMileWeightAllocationService.listByIds(dto.getIds());
            List<String> sourceIds = firstMileWeightAllocationEntities.stream().filter(Objects::nonNull).map(FirstMileWeightAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
            entityList = firstMileCostAllocationService.listBySourceIds(sourceIds, null, null, null);
        }else if (CharSequenceUtil.isNotBlank(reportPeriodStr)){
            List<FirstMileWeightAllocationEntity> firstMileWeightAllocationEntities = firstMileWeightAllocationService.lambdaQuery()
                    .eq(FirstMileWeightAllocationEntity::getCostAllocationStatus, CostAllocationStatusEnum.NOT.getCode())
                    .list();
            List<String> sourceIds = firstMileWeightAllocationEntities.stream().filter(Objects::nonNull).map(FirstMileWeightAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
            entityList = firstMileCostAllocationService.listBySourceIds(sourceIds, null, null, null);
        }
        if (CollectionUtils.isEmpty(entityList)){
            asyncTaskRecordService.updateTask(taskId, AsyncTaskRecordStatusEnum.FAILED.getCode(),ApiError.LOGISTICS_PENDING_COST_NOT_FOUND.getMsg());
            return ;
        }else {
            asyncTaskRecordService.lambdaUpdate().set(AsyncTaskRecordEntity::getDetailCount,entityList.size()).eq(AsyncTaskRecordEntity::getId,taskId).update();
        }

        List<String> sourceIds = entityList.stream().filter(e -> com.erp.model.srm.enums.ConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(e.getStatus())).map(FirstMileCostAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(sourceIds);
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = wmsFirstMileDeliveryFeign.listDetailByMainIds(sourceIds);
        //目的仓ids
        List<String> toWarehouseIds = firstMileDeliveryEntityList.stream().map(FirstMileDeliveryEntity::getDestWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(toWarehouseIds);

        Map<String, String> warehouseToOrgMap = updateDTOS.stream()
                .collect(Collectors.toMap(WarehouseDTO.UpdateDTO::getId, WarehouseDTO.UpdateDTO::getOrgId, (o1, o2) -> o1));

        List<String> orgIds = new ArrayList<>(warehouseToOrgMap.values());
        List<ReportPeriodMonthDTO.SelectDTO> reportPeriodMonthList = reportPeriodMonthMapper.queryList(orgIds)
                .stream()
                .filter(e -> e.getReportPeriodStr().equals(reportPeriodStr))
                .collect(Collectors.toList());

        // 创建 orgId 到 DTO 的映射，提高查找效率
        Map<String, ReportPeriodMonthDTO.SelectDTO> orgToReportPeriodMap = reportPeriodMonthList.stream()
                .collect(Collectors.toMap(ReportPeriodMonthDTO.SelectDTO::getOrgId, Function.identity()));

        // 直接构建最终映射
        Map<String, ReportPeriodMonthDTO.SelectDTO> warehouseToReportPeriodMap = new HashMap<>();
        for (Map.Entry<String, String> entry : warehouseToOrgMap.entrySet()) {
            String warehouseId = entry.getKey();
            String orgId = entry.getValue();
            ReportPeriodMonthDTO.SelectDTO selectDTO = orgToReportPeriodMap.get(orgId);
            if (selectDTO != null) {
                warehouseToReportPeriodMap.put(warehouseId, selectDTO);
            }
        }

        LocalDateTime now = LocalDateTime.now();

        for (FirstMileCostAllocationEntity entity : entityList) {
            AsyncTaskDetailRecordEntity detail = new AsyncTaskDetailRecordEntity();
            detail.setMainId(taskId);
            detail.setBusinessType(SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode());
            detail.setBusinessId(entity.getId());
            detail.setBusinessCode(entity.getBusinessCode());
            detail.setStatus(AsyncTaskRecordStatusEnum.ING.getCode());
            detail.setStartTime(now);
            asyncTaskDetailRecordService.save(detail);


            String sourceId = entity.getSourceId();
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(e -> e.getId().equals(sourceId)).findFirst().orElse(null);
            if (Objects.isNull(firstMileDeliveryEntity)) {
                detail.setStatus(AsyncTaskRecordStatusEnum.FAILED.getCode());
                detail.setEndTime(LocalDateTime.now());
                detail.setErrorData("费用分摊发货单记录不存在");
                asyncTaskDetailRecordService.updateById(detail);
                continue;
            }
            List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = deliveryDetailEntityList.stream().filter(e -> e.getMainId().equals(sourceId)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(firstMileDeliveryDetailEntityList)) {
                detail.setStatus(AsyncTaskRecordStatusEnum.FAILED.getCode());
                detail.setErrorData("费用分摊发货单明细记录不存在");
                asyncTaskDetailRecordService.updateById(detail);
                continue;
            }
            ReportPeriodMonthDTO.SelectDTO reportPeriodMonth = warehouseToReportPeriodMap.getOrDefault(firstMileDeliveryEntity.getDestWarehouseId(), null);
            if (Objects.isNull(reportPeriodMonth)) {
                detail.setStatus(AsyncTaskRecordStatusEnum.FAILED.getCode());
                detail.setEndTime(LocalDateTime.now());
                detail.setErrorData("核算周期不存在");
                asyncTaskDetailRecordService.updateById(detail);
                continue;
            }

            if (com.erp.model.srm.enums.ConfirmStatusEnum.CONFIRM.getCode().equals(entity.getStatus())) {
                detail.setStatus(AsyncTaskRecordStatusEnum.FAILED.getCode());
                detail.setEndTime(LocalDateTime.now());
                detail.setErrorData("费用分摊核算状态已确认");
                asyncTaskDetailRecordService.updateById(detail);
                continue;
            }

            log.error(StrUtil.format("调用前主线程: 【{}】 ,线程ID: 【{}】", Thread.currentThread().getName(), Thread.currentThread().getId()));

            entity.setReportPeriodMonth(reportPeriodMonth.getReportPeriodMonth());
            entity.setReportPeriodId(reportPeriodMonth.getId());
            firstMileCostAllocationService.asyncPushAllocatedCost(entity.getId(), detail.getId(), entity, firstMileDeliveryEntity, firstMileDeliveryDetailEntityList);

            log.error(StrUtil.format("调用后主线程: 【{}】 ,线程ID: 【{}】", Thread.currentThread().getName(), Thread.currentThread().getId()));
        }
    }

}
