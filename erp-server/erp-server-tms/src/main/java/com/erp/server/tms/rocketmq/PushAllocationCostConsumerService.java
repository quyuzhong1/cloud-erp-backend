package com.erp.server.tms.rocketmq;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.tms.dto.AsyncTaskRecordDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.ReportPeriodMonthDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.mapper.ReportPeriodMonthMapper;
import com.erp.server.tms.service.*;
import com.erp.server.tms.service.impl.LogisticsBillCostServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
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

    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private SmallBagCostAllocationMainService smallBagCostAllocationMainService;
    @Resource
    private CfgSettingService cfgSettingService;

    @Override
    public void onMessage(AsyncTaskRecordDTO.TaskDTO dto) {
        String businessType = dto.getBusinessType();
        if(Objects.equals(businessType,SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
            //下推费用分摊
            pushFirstMileCostAllocation(dto);
        }else {
            //下推小包费用分摊
            pushSmallBagCostAllocation(dto);
        }


    }

    private void pushSmallBagCostAllocation(AsyncTaskRecordDTO.TaskDTO dto) {
        String taskId = dto.getTaskId();
        String type ="";
        if(dto.getBusinessType().equals(SourceTypeEnum.LOGISTICS_BILL_COST.getCode())){
            type = DictCostAttributionEnum.SELF_DELIVER.getCode(); //自发货
        }else if(dto.getBusinessType().equals(SourceTypeEnum.LAST_MILE_LOGISTICS_BILL_COST.getCode())){
            type = DictCostAttributionEnum.LAST_MILE.getCode();//尾程
        }else {
            asyncTaskRecordService.updateTask(taskId, AsyncTaskRecordStatusEnum.FAILED.getCode(),"businessType【"+dto.getBusinessType()+"】对应处理方式不存在");
            return ;
        }

        //所有 类型=自发货。状态是账单确认和暂估确认的物流单费用
        LambdaQueryChainWrapper<LogisticsBillCostEntity> wrapper = logisticsBillCostService.lambdaQuery()
                .eq(LogisticsBillCostEntity::getType, type);
        if(CollUtil.isNotEmpty(dto.getIds())){
            wrapper.in(LogisticsBillCostEntity::getId,dto.getIds());
        }else {
            wrapper.in(LogisticsBillCostEntity::getReconciliationStatus, Arrays.asList(ReconciliationStatusEnum.ESTIMATE_CONFIRM.getCode(), ReconciliationStatusEnum.CONFIRMED.getCode()));
        }
        List<LogisticsBillCostEntity> list = wrapper.list();


        if (CollectionUtils.isEmpty(list)){
            asyncTaskRecordService.updateTask(taskId, AsyncTaskRecordStatusEnum.FAILED.getCode(),ApiError.LOGISTICS_PENDING_COST_NOT_FOUND.getMsg());
            return;
        }else {
            asyncTaskRecordService.lambdaUpdate().set(AsyncTaskRecordEntity::getDetailCount,list.size()).eq(AsyncTaskRecordEntity::getId,taskId).update();
        }

        //查询所有小包费用
        List<String> ids = list.stream().map(LogisticsBillCostEntity::getId).collect(Collectors.toList());
        List<SmallBagCostAllocationMainEntity> smallBagCostAllocationMainEntityList = smallBagCostAllocationMainService.lambdaQuery()
                .in(SmallBagCostAllocationMainEntity::getCostId, ids).list();
        Map<String, List<SmallBagCostAllocationMainEntity>> smallBagCostAllocationGroupByCostId = smallBagCostAllocationMainEntityList.stream().collect(Collectors.groupingBy(SmallBagCostAllocationMainEntity::getCostId));

        //物流单
        List<String> logisticsBillIds = list.stream().map(LogisticsBillCostEntity::getLogisticsBillId).collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntities = logisticsBillService.listByIds(logisticsBillIds);
        Map<String, LogisticsBillEntity> logisticsBillMap = logisticsBillEntities.stream().collect(Collectors.toMap(LogisticsBillEntity::getId, Function.identity(), (o1, o2) -> o1));

        //配置表
        CfgSettingEntity byKey = cfgSettingService.getByKey(CfgSettingEnum.ALLOCATION_SETTING.getCode());
        Map<String, String> feeTypeSettingMaps = new HashMap<>();
        CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO = JSON.parseObject(byKey.getDataJson().toJSONString(0), CfgSettingValueDTO.AllocationSettingDTO.class);
        AllocationFeeTypeEnum[] values = AllocationFeeTypeEnum.values();
        for(AllocationFeeTypeEnum allocationFeeTypeEnum : values) {
            if(AllocationFeeTypeEnum.SHIPPING_COST == allocationFeeTypeEnum) {
                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageShippingCost());
            }else if(AllocationFeeTypeEnum.DECLARE_COST == allocationFeeTypeEnum) {
                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageTariffFee());
            }else if(AllocationFeeTypeEnum.OTHER_COST == allocationFeeTypeEnum) {
                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageOtherFee());
            }else if(AllocationFeeTypeEnum.DEDUCTIBLE_TAX == allocationFeeTypeEnum) {
                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageDeductibleTax());
            }
        }
        //汇率
        Map<String, BigDecimal> rateMap = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        for (LogisticsBillCostEntity logisticsBillCostEntity : list) {
            AsyncTaskDetailRecordEntity detail = new AsyncTaskDetailRecordEntity();
            detail.setMainId(taskId);
            detail.setBusinessType(dto.getBusinessType());
            detail.setBusinessId(logisticsBillCostEntity.getId());
            detail.setStatus(AsyncTaskRecordStatusEnum.ING.getCode());
            detail.setStartTime(now);
            asyncTaskDetailRecordService.save(detail);

            List<SmallBagCostAllocationMainEntity> smallBagCostAllocationList = smallBagCostAllocationGroupByCostId.getOrDefault(logisticsBillCostEntity.getId(), new ArrayList<>());

            LogisticsBillEntity logisticsBillEntity = logisticsBillMap.getOrDefault(logisticsBillCostEntity.getLogisticsBillId(), new LogisticsBillEntity());

            logisticsBillCostService.asyncPushAllocation(logisticsBillEntity.getId(),taskId, detail.getId(), dto.getReportDate(), logisticsBillCostEntity, smallBagCostAllocationList, logisticsBillEntity, allocationSettingDTO, feeTypeSettingMaps, rateMap);
        }

    }


    //下推费用分摊
    private void pushFirstMileCostAllocation(AsyncTaskRecordDTO.TaskDTO dto) {
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
            return;
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
            detail.setStatus(AsyncTaskRecordStatusEnum.ING.getCode());
            detail.setStartTime(now);
            asyncTaskDetailRecordService.save(detail);

            String taskDetailId = detail.getId();
            String sourceId = entity.getSourceId();
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(e -> e.getId().equals(sourceId)).findFirst().orElse(null);
            if (Objects.isNull(firstMileDeliveryEntity)) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"费用分摊发货单记录不存在");
                asyncTaskRecordService.updateTaskFinally(taskId);
                continue;
            }
            List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = deliveryDetailEntityList.stream().filter(e -> e.getMainId().equals(sourceId)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(firstMileDeliveryDetailEntityList)) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"费用分摊发货单明细记录不存在");
                asyncTaskRecordService.updateTaskFinally(taskId);
                continue;
            }
            ReportPeriodMonthDTO.SelectDTO reportPeriodMonth = warehouseToReportPeriodMap.getOrDefault(firstMileDeliveryEntity.getDestWarehouseId(), null);
            if (Objects.isNull(reportPeriodMonth)) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"核算周期不存在");
                asyncTaskRecordService.updateTaskFinally(taskId);
                continue;
            }

            if (com.erp.model.srm.enums.ConfirmStatusEnum.CONFIRM.getCode().equals(entity.getStatus())) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"费用分摊核算状态已确认");
                asyncTaskRecordService.updateTaskFinally(taskId);
                continue;
            }

            log.error(StrUtil.format("调用前主线程: 【{}】 ,线程ID: 【{}】", Thread.currentThread().getName(), Thread.currentThread().getId()));

            entity.setReportPeriodMonth(reportPeriodMonth.getReportPeriodMonth());
            entity.setReportPeriodId(reportPeriodMonth.getId());
            firstMileCostAllocationService.asyncPushAllocatedCost(entity.getId(), taskId,detail.getId(), entity, firstMileDeliveryEntity, firstMileDeliveryDetailEntityList);

            log.error(StrUtil.format("调用后主线程: 【{}】 ,线程ID: 【{}】", Thread.currentThread().getName(), Thread.currentThread().getId()));
        }
    }

}
