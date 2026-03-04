package com.erp.server.tms.rocketmq;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqNewTag;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.tms.dto.AsyncTaskRecordDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.ReportPeriodMonthDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.mapper.ReportPeriodMonthMapper;
import com.erp.server.tms.service.*;
import com.erp.server.tms.service.impl.LogisticsBillCostServiceImpl;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
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

    @Autowired
    @Qualifier("costAllocationPool")
    private ExecutorService costAllocationPool;

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
    private void pushFirstMileCostAllocation(AsyncTaskRecordDTO.TaskDTO dto) {
        String taskId = dto.getTaskId();
        String reportDate = dto.getReportDate();
        LocalDate reportPeriodMonth = LocalDate.parse(reportDate + "-01");

        // 头程重量分摊-费用状态为{未分摊，部分分摊}+本期账单数据 判断是否进入头程费用分摊表
        List<FirstMileWeightAllocationEntity> list = firstMileWeightAllocationService.listBySourceIds(null, null);

        if (CollectionUtils.isEmpty(list)) {
            asyncTaskRecordService.updateTask(taskId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"重量分摊记录不存在");
            return;
        }
        List<String> deliveryIds = list.stream().map(FirstMileWeightAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliveryIds)) {
            asyncTaskRecordService.updateTask(taskId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"发货单关联记录为空");
            return;
        }else {
            asyncTaskRecordService.lambdaUpdate().set(AsyncTaskRecordEntity::getDetailCount,deliveryIds.size()).eq(AsyncTaskRecordEntity::getId,taskId).update();
        }
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(deliveryIds);
        if (CollectionUtils.isEmpty(firstMileDeliveryEntityList)) {
            asyncTaskRecordService.updateTask(taskId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"发货单记录不存在");
            return;
        }
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = wmsFirstMileDeliveryFeign.listDetailByMainIds(deliveryIds);
        if (CollectionUtils.isEmpty(deliveryDetailEntityList)) {
            asyncTaskRecordService.updateTask(taskId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"发货单明细记录不存在");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (String id : deliveryIds) {
            AsyncTaskDetailRecordEntity detail = new AsyncTaskDetailRecordEntity();
            detail.setMainId(taskId);
            detail.setBusinessType(dto.getBusinessType());
            detail.setBusinessId(id);
            detail.setStatus(AsyncTaskRecordStatusEnum.ING.getCode());
            detail.setStartTime(now);
            asyncTaskDetailRecordService.save(detail);

            String taskDetailId = detail.getId();

            FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(deliveryEntity)) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"发货单记录不存在");
                continue;
            }
            List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList1 = deliveryDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(id)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(deliveryDetailEntityList1)) {
                asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"发货单明细记录不存在");
                continue;
            }

            costAllocationPool.execute(() -> {
                try {
                    //构造数据
                    FirstMileCostAllocationEntity entity = new FirstMileCostAllocationEntity()
                            .setSourceId(deliveryEntity.getId()).setSourceCode(deliveryEntity.getCode()).setReportPeriodMonth(reportPeriodMonth);
                    BatchResultDTO result = firstMileCostAllocationService.calcAllocatedCost(entity, deliveryEntity, deliveryDetailEntityList1);
                    if(!result.getSuccess()){
                        asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),result.getMsg());
                    }else {
                        asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.SUCCESS.getCode(),"");
                    }
                } catch (Exception e) {
                    log.error("asyncPushAllocatedCost id: 【{}】, 异常: 【{}】",id,e);
                    //把Exception e 转字符串
                    String errorMsg = ExceptionUtils.getStackTrace(e);
                    asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),errorMsg);
                } finally {
                    asyncTaskRecordService.updateTaskFinally(taskId);
                }
            });

        }
    }


    private void pushSmallBagCostAllocation(AsyncTaskRecordDTO.TaskDTO dto) {
        String taskId = dto.getTaskId();
//        List<LogisticsBillCostEntity> list = logisticsBillCostService.listByCanPushAllocation(dto.getType() ,dto.getReportDate());
        List<String> ids = logisticsBillCostService.listByCanPushAllocation(dto);
        List<LogisticsBillCostEntity> list = logisticsBillCostService.listByIds(ids);

        if(CollUtil.isEmpty(list)) {
            asyncTaskRecordService.updateTask(taskId,AsyncTaskRecordStatusEnum.FAILED.getCode(),ApiError.LOGISTICS_PENDING_COST_NOT_FOUND.getMsg());
            return;
        }else {
            asyncTaskRecordService.lambdaUpdate().set(AsyncTaskRecordEntity::getDetailCount,list.size()).eq(AsyncTaskRecordEntity::getId,taskId).update();
        }

        List<String> logisticsBillIds = list.stream().map(LogisticsBillCostEntity::getLogisticsBillId).filter(StringUtils::isNotBlank).collect(Collectors.toList());

        Map<String, LogisticsBillEntity> logisticsBillMap = logisticsBillService.listByIds(logisticsBillIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LogisticsBillEntity::getId, Function.identity(),(o1,o2)->o1));

        LocalDateTime now = LocalDateTime.now();
        for(LogisticsBillCostEntity logisticsBillCostEntity : list) {
            LogisticsBillEntity logisticsBillEntity = logisticsBillMap.get(logisticsBillCostEntity.getLogisticsBillId());
            if(Objects.isNull(logisticsBillEntity) || Objects.isNull(logisticsBillEntity.getIsAllocateCostRequired()) || Objects.equals(logisticsBillEntity.getIsAllocateCostRequired(),Boolean.FALSE)){
                continue;
            }

            AsyncTaskDetailRecordEntity detail = new AsyncTaskDetailRecordEntity();
            detail.setMainId(taskId);
            detail.setBusinessType(dto.getBusinessType());
            detail.setBusinessId(logisticsBillCostEntity.getId());
            detail.setStatus(AsyncTaskRecordStatusEnum.ING.getCode());
            detail.setStartTime(now);
            asyncTaskDetailRecordService.save(detail);

            String id = detail.getId();

            costAllocationPool.execute(() -> {
                try {
                    BatchResultDTO result = logisticsBillCostService.pushAllocation(logisticsBillCostEntity.getId(), dto.getReportDate());
                    if(result.getSuccess()){
                        asyncTaskDetailRecordService.updateDetail(id,AsyncTaskRecordStatusEnum.SUCCESS.getCode(),"");
                    }else {
                        asyncTaskDetailRecordService.updateDetail(id,AsyncTaskRecordStatusEnum.FAILED.getCode(),result.getMsg());
                    }
                } catch (Exception e) {
                    log.error("asyncPushAllocation id: 【{}】, 异常: 【{}】",logisticsBillCostEntity.getId(),e);
                    //把Exception e 转字符串
                    String errorMsg = ExceptionUtils.getStackTrace(e);
                    asyncTaskDetailRecordService.updateDetail(id,AsyncTaskRecordStatusEnum.FAILED.getCode(),errorMsg);
                } finally {
                    asyncTaskRecordService.updateTaskFinally(taskId);
                }
            });
        }
    }



//    private void pushSmallBagCostAllocation(AsyncTaskRecordDTO.TaskDTO dto) {
//        String taskId = dto.getTaskId();
//
//        List<String> ids = logisticsBillCostService.listByCanPushAllocation(dto);
//        if (CollectionUtils.isEmpty(ids)){
//            asyncTaskRecordService.updateTask(taskId, AsyncTaskRecordStatusEnum.FAILED.getCode(),ApiError.LOGISTICS_PENDING_COST_NOT_FOUND.getMsg());
//            return;
//        }else {
//            asyncTaskRecordService.lambdaUpdate().set(AsyncTaskRecordEntity::getDetailCount,ids.size()).eq(AsyncTaskRecordEntity::getId,taskId).update();
//        }
//
//        List<LogisticsBillCostEntity> list = logisticsBillCostService.listByIds(ids);
//
//        //查询所有小包费用
//        List<SmallBagCostAllocationMainEntity> smallBagCostAllocationMainEntityList = smallBagCostAllocationMainService.lambdaQuery()
//                .in(SmallBagCostAllocationMainEntity::getCostId, ids).list();
//        Map<String, List<SmallBagCostAllocationMainEntity>> smallBagCostAllocationGroupByCostId = smallBagCostAllocationMainEntityList.stream().collect(Collectors.groupingBy(SmallBagCostAllocationMainEntity::getCostId));
//
//        //物流单
//        List<String> logisticsBillIds = list.stream().map(LogisticsBillCostEntity::getLogisticsBillId).collect(Collectors.toList());
//        List<LogisticsBillEntity> logisticsBillEntities = logisticsBillService.listByIds(logisticsBillIds);
//        Map<String, LogisticsBillEntity> logisticsBillMap = logisticsBillEntities.stream().collect(Collectors.toMap(LogisticsBillEntity::getId, Function.identity(), (o1, o2) -> o1));
//
//        //配置表
//        CfgSettingEntity byKey = cfgSettingService.getByKey(CfgSettingEnum.ALLOCATION_SETTING.getCode());
//        Map<String, String> feeTypeSettingMaps = new HashMap<>();
//        CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO = JSON.parseObject(byKey.getDataJson().toJSONString(0), CfgSettingValueDTO.AllocationSettingDTO.class);
//        AllocationFeeTypeEnum[] values = AllocationFeeTypeEnum.values();
//        for(AllocationFeeTypeEnum allocationFeeTypeEnum : values) {
//            if(AllocationFeeTypeEnum.SHIPPING_COST == allocationFeeTypeEnum) {
//                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageShippingCost());
//            }else if(AllocationFeeTypeEnum.DECLARE_COST == allocationFeeTypeEnum) {
//                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageTariffFee());
//            }else if(AllocationFeeTypeEnum.OTHER_COST == allocationFeeTypeEnum) {
//                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageOtherFee());
//            }else if(AllocationFeeTypeEnum.DEDUCTIBLE_TAX == allocationFeeTypeEnum) {
//                feeTypeSettingMaps.put(allocationFeeTypeEnum.getCode(), allocationSettingDTO.getPackageDeductibleTax());
//            }
//        }
//        //汇率
//        Map<String, BigDecimal> rateMap = new HashMap<>();
//
//        LocalDateTime now = LocalDateTime.now();
//        for (LogisticsBillCostEntity logisticsBillCostEntity : list) {
//            AsyncTaskDetailRecordEntity detail = new AsyncTaskDetailRecordEntity();
//            detail.setMainId(taskId);
//            detail.setBusinessType(dto.getBusinessType());
//            detail.setBusinessId(logisticsBillCostEntity.getId());
//            detail.setStatus(AsyncTaskRecordStatusEnum.ING.getCode());
//            detail.setStartTime(now);
//            asyncTaskDetailRecordService.save(detail);
//
//            List<SmallBagCostAllocationMainEntity> smallBagCostAllocationList = smallBagCostAllocationGroupByCostId.getOrDefault(logisticsBillCostEntity.getId(), new ArrayList<>());
//
//            LogisticsBillEntity logisticsBillEntity = logisticsBillMap.getOrDefault(logisticsBillCostEntity.getLogisticsBillId(), new LogisticsBillEntity());
//
//            logisticsBillCostService.asyncPushAllocation(logisticsBillEntity.getId(),taskId, detail.getId(), dto.getReportDate(), logisticsBillCostEntity, smallBagCostAllocationList, logisticsBillEntity, allocationSettingDTO, feeTypeSettingMaps, rateMap);
//        }
//
//    }


    //下推费用分摊
//    private void pushFirstMileCostAllocation(AsyncTaskRecordDTO.TaskDTO dto) {
//        String taskId = dto.getTaskId();
//        String reportPeriodStr = dto.getReportDate();
//        List<FirstMileCostAllocationEntity> entityList = new ArrayList<>();
//        int detailCount = 0;
//        if (CollUtil.isNotEmpty(dto.getIds())){
//            List<FirstMileWeightAllocationEntity> firstMileWeightAllocationEntities = firstMileWeightAllocationService.listByIds(dto.getIds());
//            if(CollUtil.isNotEmpty(firstMileWeightAllocationEntities)){
//                List<String> sourceIds = firstMileWeightAllocationEntities.stream().filter(Objects::nonNull).map(FirstMileWeightAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
//                detailCount = sourceIds.size();
//                entityList = firstMileCostAllocationService.listBySourceIds(sourceIds, null, null, null);
//            }
//        }else if (CharSequenceUtil.isNotBlank(reportPeriodStr)){
//            List<FirstMileWeightAllocationEntity> firstMileWeightAllocationEntities = firstMileWeightAllocationService.lambdaQuery()
//                    .eq(FirstMileWeightAllocationEntity::getCostAllocationStatus, CostAllocationStatusEnum.NOT.getCode())
//                    .list();
//            if(CollUtil.isNotEmpty(firstMileWeightAllocationEntities)){
//                List<String>  sourceIds = firstMileWeightAllocationEntities.stream().filter(Objects::nonNull).map(FirstMileWeightAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
//                detailCount = sourceIds.size();
//                entityList = firstMileCostAllocationService.lambdaQuery()
//                        .eq(FirstMileCostAllocationEntity::getStatus, ConfirmStatusEnum.WAIT_CONFIRM.getCode())
//                        .in(FirstMileCostAllocationEntity::getSourceId,sourceIds)
//                        .list();
//            }
//        }
//        if (detailCount == 0){
//            asyncTaskRecordService.updateTask(taskId, AsyncTaskRecordStatusEnum.FAILED.getCode(),ApiError.LOGISTICS_PENDING_COST_NOT_FOUND.getMsg());
//            return;
//        }else {
//            asyncTaskRecordService.lambdaUpdate().set(AsyncTaskRecordEntity::getDetailCount,detailCount).eq(AsyncTaskRecordEntity::getId,taskId).update();
//        }
//
//        List<String> sourceIds = entityList.stream().filter(e -> com.erp.model.srm.enums.ConfirmStatusEnum.WAIT_CONFIRM.getCode().equals(e.getStatus())).map(FirstMileCostAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
//        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(sourceIds);
//        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = wmsFirstMileDeliveryFeign.listDetailByMainIds(sourceIds);
//        //目的仓ids
//        List<String> toWarehouseIds = firstMileDeliveryEntityList.stream().map(FirstMileDeliveryEntity::getDestWarehouseId).distinct().collect(Collectors.toList());
//        List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(toWarehouseIds);
//
//        Map<String, String> warehouseToOrgMap = updateDTOS.stream()
//                .collect(Collectors.toMap(WarehouseDTO.UpdateDTO::getId, WarehouseDTO.UpdateDTO::getOrgId, (o1, o2) -> o1));
//
//        List<String> orgIds = new ArrayList<>(warehouseToOrgMap.values());
//        List<ReportPeriodMonthDTO.SelectDTO> reportPeriodMonthList = reportPeriodMonthMapper.queryList(orgIds)
//                .stream()
//                .filter(e -> e.getReportPeriodStr().equals(reportPeriodStr))
//                .collect(Collectors.toList());
//
//        // 创建 orgId 到 DTO 的映射，提高查找效率
//        Map<String, ReportPeriodMonthDTO.SelectDTO> orgToReportPeriodMap = reportPeriodMonthList.stream()
//                .collect(Collectors.toMap(ReportPeriodMonthDTO.SelectDTO::getOrgId, Function.identity()));
//
//        // 直接构建最终映射
//        Map<String, ReportPeriodMonthDTO.SelectDTO> warehouseToReportPeriodMap = new HashMap<>();
//        for (Map.Entry<String, String> entry : warehouseToOrgMap.entrySet()) {
//            String warehouseId = entry.getKey();
//            String orgId = entry.getValue();
//            ReportPeriodMonthDTO.SelectDTO selectDTO = orgToReportPeriodMap.get(orgId);
//            if (selectDTO != null) {
//                warehouseToReportPeriodMap.put(warehouseId, selectDTO);
//            }
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//
//        for (FirstMileCostAllocationEntity entity : entityList) {
//            AsyncTaskDetailRecordEntity detail = new AsyncTaskDetailRecordEntity();
//            detail.setMainId(taskId);
//            detail.setBusinessType(SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode());
//            detail.setBusinessId(entity.getId());
//            detail.setStatus(AsyncTaskRecordStatusEnum.ING.getCode());
//            detail.setStartTime(now);
//            asyncTaskDetailRecordService.save(detail);
//
//            String taskDetailId = detail.getId();
//            String sourceId = entity.getSourceId();
//            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(e -> e.getId().equals(sourceId)).findFirst().orElse(null);
//            if (Objects.isNull(firstMileDeliveryEntity)) {
//                asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"费用分摊发货单记录不存在");
//                asyncTaskRecordService.updateTaskFinally(taskId);
//                continue;
//            }
//            List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = deliveryDetailEntityList.stream().filter(e -> e.getMainId().equals(sourceId)).collect(Collectors.toList());
//            if (CollectionUtils.isEmpty(firstMileDeliveryDetailEntityList)) {
//                asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"费用分摊发货单明细记录不存在");
//                asyncTaskRecordService.updateTaskFinally(taskId);
//                continue;
//            }
//            ReportPeriodMonthDTO.SelectDTO reportPeriodMonth = warehouseToReportPeriodMap.getOrDefault(firstMileDeliveryEntity.getDestWarehouseId(), null);
//            if (Objects.isNull(reportPeriodMonth)) {
//                asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"核算周期不存在");
//                asyncTaskRecordService.updateTaskFinally(taskId);
//                continue;
//            }
//
//            if (com.erp.model.srm.enums.ConfirmStatusEnum.CONFIRM.getCode().equals(entity.getStatus())) {
//                asyncTaskDetailRecordService.updateDetail(taskDetailId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"费用分摊核算状态已确认");
//                asyncTaskRecordService.updateTaskFinally(taskId);
//                continue;
//            }
//
//            log.error(StrUtil.format("调用前主线程: 【{}】 ,线程ID: 【{}】", Thread.currentThread().getName(), Thread.currentThread().getId()));
//
//            entity.setReportPeriodMonth(reportPeriodMonth.getReportPeriodMonth());
//            entity.setReportPeriodId(reportPeriodMonth.getId());
//            firstMileCostAllocationService.asyncPushAllocatedCost(entity.getId(), taskId,detail.getId(), entity, firstMileDeliveryEntity, firstMileDeliveryDetailEntityList);
//
//            log.error(StrUtil.format("调用后主线程: 【{}】 ,线程ID: 【{}】", Thread.currentThread().getName(), Thread.currentThread().getId()));
//        }
//    }

}
