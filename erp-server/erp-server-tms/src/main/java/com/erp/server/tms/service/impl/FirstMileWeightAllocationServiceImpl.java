package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.CfgSettingDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.rpc.plm.feign.ProductPackFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.server.tms.mapper.FirstMileWeightAllocationMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.common.core.enums.ApiError;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_FM_ESTIMATED_BILL;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_FM_WEIGHT_ALLOCATION;

/**
 * <p>
 * 头程重量分摊 服务实现类
 * </p>
 *
 * @author tmj
 * @since 2024-08-20
 */
@Slf4j
@Service
public class FirstMileWeightAllocationServiceImpl extends SuperServiceImpl<FirstMileWeightAllocationMapper, FirstMileWeightAllocationEntity> implements FirstMileWeightAllocationService {

    @Resource
    private PackingTaskFeign packingTaskFeign;
    @Resource
    private ProductDetailFeign productDetailFeign;
    @Resource
    private ProductPackFeign productPackFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private FirstMileCostAllocationService costAllocationService;
    @Resource
    private TmsFirstMileReconciliationDetailService reconciliationDetailService;
    @Resource
    private WmsWarehouseFeign warehouseFeign;
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private TmsCostDetailService tmsCostDetailService;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Resource
    private OverseaWarehouseInboundFeign overseaWarehouseInboundFeign;
    @Resource
    private FirstMileDeliveryDetailFeign firstMileDeliveryDetailFeign;
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private PackingTaskDetailFeign packingTaskDetailFeign;
    @Resource
    private WmsCartonFeign wmsCartonFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    @Override
    public PagingVO<FirstMileWeightAllocationDTO.ViewDTO> paging(PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto) {
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<FirstMileWeightAllocationDTO.ViewDTO> pageData = baseMapper.paging(query, dto.getParams());
        //会有性能问题，待优化
        List<String> logisticsBillIds = pageData.getRecords().stream().map(item -> item.getLogisticsBillId()).distinct().collect(Collectors.toList());
        for (String logisticsBillId : logisticsBillIds) {
            updateCostAllocationStatus(logisticsBillId);
        }
        pageData = baseMapper.paging(query, dto.getParams());
        fillData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillData(List<FirstMileWeightAllocationDTO.ViewDTO> records) {
        if(records.isEmpty()){
            return;
        }
        //仓库
        List<String> warehouseIds = records.stream().map(item -> item.getFromWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseList = warehouseFeign.listByIds(warehouseIds);
        Map<String, WarehouseDTO.ListDTO> warehouseMap = warehouseList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2));
        //费用分摊
        List<String> logisticsBillIds = records.stream().map(item -> item.getLogisticsBillId()).distinct().collect(Collectors.toList());
        List<FirstMileCostAllocationDTO.LastedAllocMonthDTO> lastedAllocationMonthList =  costAllocationService.listLastedAllocationMonth(logisticsBillIds);
        for (FirstMileWeightAllocationDTO.ViewDTO item : records) {
            if(StringUtils.isBlank(item.getCostAllocationStatus())){
                item.setCostAllocationStatus(CostAllocationStatusEnum.NOT.getCode());
            }
            item.setCostAllocationStatusName(CostAllocationStatusEnum.getName(item.getCostAllocationStatus()));
            item.setAllocationTypeName(WeightAllocationTypeEnum.getName(item.getAllocationType()));
            item.setFeeRuleName(ShippingFeeRuleEnum.getName(item.getFeeRule()));
            if(warehouseMap.containsKey(item.getFromWarehouseId())){
                item.setFromWarehouseName(warehouseMap.get(item.getFromWarehouseId()).getName());
            }
            item.setBoxSizeStr(item.getBoxLength() + "*" + item.getBoxWidth() + "*" + item.getBoxHeight() + " " + item.getBoxSizeUnit());
            item.setCreateTimeStr(item.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            if(StringUtils.isNotBlank(item.getCalculateMonth())){
                item.setLatestCostAllocationMonth(LocalDate.parse(item.getCalculateMonth() + "-01"));
                item.setLatestCostAllocationMonthStr(item.getCalculateMonth());
            }
            /*FirstMileCostAllocationDTO.LastedAllocMonthDTO lastedAllocMonthDTO = lastedAllocationMonthList.stream().filter(v -> v.getLogisticsBillId().equals(item.getLogisticsBillId())).findFirst().orElse(null);
            if(lastedAllocMonthDTO != null){
                item.setCalculatePeriodId(lastedAllocMonthDTO.getReportPeriodId());
                item.setLatestCostAllocationMonth(lastedAllocMonthDTO.getLatestMonth());
                if(lastedAllocMonthDTO.getLatestMonth() != null){
                    item.setLatestCostAllocationMonthStr(lastedAllocMonthDTO.getLatestMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")));
                }
                if(StringUtils.isNotBlank(lastedAllocMonthDTO.getStatus()) && StringUtils.isNotBlank(lastedAllocMonthDTO.getBillSourceType()) && lastedAllocMonthDTO.getEndPeriodTransitCost() != null){
                    if(lastedAllocMonthDTO.getStatus().equals("confirm") && lastedAllocMonthDTO.getBillSourceType().equals(ReconciliationBillTypeEnum.ACTUAL.getCode()) && lastedAllocMonthDTO.getEndPeriodTransitCost().compareTo(BigDecimal.ZERO) == 0){
                        item.setCostAllocationStatus(CostAllocationStatusEnum.ALREADY.getCode());
                    }else if(lastedAllocMonthDTO.getStatus().equals("confirm") && lastedAllocMonthDTO.getEndPeriodTransitCost().compareTo(BigDecimal.ZERO) > 0){
                        item.setCostAllocationStatus(CostAllocationStatusEnum.PART.getCode());
                    }else{
                        item.setCostAllocationStatus(CostAllocationStatusEnum.NOT.getCode());
                    }
                }
            }else {
                item.setCostAllocationStatus(CostAllocationStatusEnum.NOT.getCode());
            }*/
        }
    }

    @Override
    public void exportExcel(FirstMileWeightAllocationDTO.ExportParamDTO dto) {
        /*List<FirstMileWeightAllocationDTO.ViewDTO> list = baseMapper.listByParam(dto);
        fillData(list);
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        StringBuilder builder = new StringBuilder();
        builder.append("头程重量分摊导出").append(date);
        try {
            new ExcelPrintUtils().patchExport(list, response, builder.toString(), "excel/firstMileWeightAllocationExport.xlsx");
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }*/
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        StringBuilder builder = new StringBuilder();
        builder.append("头程重量分摊导出").append(date);
        downloadTaskFeign.saveDownloadTask(builder.toString(), EXPORT_TMS_FM_WEIGHT_ALLOCATION.getCode(), dto);
    }

    @Override
    public List<FirstMileWeightAllocationDTO.TabDTO> tabList() {
        List<FirstMileWeightAllocationDTO.TabDTO> list = new ArrayList<>();
        Integer wait = this.lambdaQuery().in(FirstMileWeightAllocationEntity::getCostAllocationStatus, Arrays.asList("not", "part")).count();
        Integer already = this.lambdaQuery().in(FirstMileWeightAllocationEntity::getCostAllocationStatus, Collections.singletonList("already")).count();
        list.add(new FirstMileWeightAllocationDTO.TabDTO("wait", "待分摊", wait));
        list.add(new FirstMileWeightAllocationDTO.TabDTO("already", "已分摊", already));
        return list;
    }

    /**
     * 更新费用分摊状态
     */
    private void updateCostAllocationStatus(String logisticsBillId){
        //更新费用分摊状态
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        List<FirstMileWeightAllocationDTO.CostAllocationDTO> costAllocationList = baseMapper.listCostAllocation(logisticsBillId);
        if(costAllocationList.isEmpty()){
            this.lambdaUpdate()
                    .set(FirstMileWeightAllocationEntity::getCostAllocationStatus, CostAllocationStatusEnum.NOT.getCode())
                    .eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId)
                    .update();
            return;
        }
        costAllocationList.sort(Comparator.comparing(FirstMileWeightAllocationDTO.CostAllocationDTO::getReportPeriod).reversed());
        FirstMileWeightAllocationDTO.CostAllocationDTO costAllocationDTO = costAllocationList.get(0);
        if(costAllocationDTO.getCostAllocationStatus().equals("confirm")
                && costAllocationDTO.getBillSourceType() != null && costAllocationDTO.getBillSourceType().equals("actual")
                && costAllocationDTO.getEndPeriodTransitCost() != null && costAllocationDTO.getEndPeriodTransitCost().compareTo(BigDecimal.ZERO) == 0){
            this.lambdaUpdate().set(FirstMileWeightAllocationEntity::getCostAllocationStatus, CostAllocationStatusEnum.ALREADY.getCode())
                    .set(FirstMileWeightAllocationEntity::getCalculateMonth, costAllocationDTO.getReportPeriod().format(formatter))
                    .eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId)
                    .update();
            return;
        }
        this.lambdaUpdate()
                .set(FirstMileWeightAllocationEntity::getCostAllocationStatus, CostAllocationStatusEnum.PART.getCode())
                .set(FirstMileWeightAllocationEntity::getCalculateMonth, costAllocationDTO.getReportPeriod().format(formatter))
                .eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO weightReCompute(String logisticsBillId) {
        updateCostAllocationStatus(logisticsBillId);
        List<FirstMileWeightAllocationEntity> list = this.lambdaQuery().eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId).list();
        boolean allMatch = list.stream().allMatch(item -> item.getCostAllocationStatus().equals(CostAllocationStatusEnum.NOT.getCode()));
        if(!allMatch){
            return BatchResultDTO.fail(logisticsBillId, logisticsBillId, "只能对未分摊的数据进行重量重算");
        }

        FirstMileWeightAllocationDTO.LogisticsBillInfoDTO logisticsBillInfo = baseMapper.getLogisticsBillInfo(logisticsBillId);
        //物流渠道
        LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(logisticsBillInfo.getChannelId());
        //物流单
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(logisticsBillId);
        //发货单
        String deliveryId = logisticsBillEntity.getOutstockId();
        List<FirstMileDeliveryEntity> firstMileDeliveryList = wmsFirstMileDeliveryFeign.listByIds(Collections.singletonList(deliveryId));
        FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryList.get(0);
        //发货单明细
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailList = firstMileDeliveryDetailFeign.listByMainId(Collections.singletonList(firstMileDeliveryEntity.getId()));
        //装箱任务
        PackingTaskEntity packingTaskEntity;
        if(firstMileDeliveryEntity.getSourceType().equals(SourceTypeEnum.REQUISITION_APPLICATION.getCode())){
            packingTaskEntity = packingTaskFeign.getBySourceId(firstMileDeliveryEntity.getSourceId());
            if(packingTaskEntity == null){
                packingTaskEntity = packingTaskFeign.getBySourceId(firstMileDeliveryEntity.getId());
            }
        }else {
            packingTaskEntity = packingTaskFeign.getBySourceId(firstMileDeliveryEntity.getId());
        }
        if(packingTaskEntity == null){
            return BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getTransportNo(), "没有找到装箱任务");
        }
        //系统配置
        CfgSettingDTO.ViewDTO cfgSettingView = cfgSettingService.view();
        String cfgWeightAllocationType = cfgSettingView.getAllocationSettingDTO().getWeightFirstAllocation();
        //产品包装信息
        List<String> skuIds = firstMileDeliveryDetailList.stream().map(FirstMileDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<String> childrenSkuIds = bomSkuList.stream().map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        childrenSkuIds.addAll(skuIds);
        List<ProductPackEntity> productPackList = productPackFeign.listBySkuIds(childrenSkuIds);

        List<FirstMileWeightAllocationEntity> entityList = this.lambdaQuery().eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId).list();
        List<FirstMileWeightAllocationEntity> updateList = new ArrayList<>(entityList.size());
        for (FirstMileWeightAllocationEntity oldEntity : entityList) {
            FirstMileWeightAllocationEntity entity = new FirstMileWeightAllocationEntity();
            entity.setId(oldEntity.getId());
            entity.setBoxId(oldEntity.getBoxId());
            entity.setBoxNo(oldEntity.getBoxNo());
            entity.setDeliveryQty(oldEntity.getDeliveryQty());
            entity.setFeeRule(logisticsChannelEntity.getFeeRule());
            entity.setOutStockWeight(oldEntity.getOutStockWeight());
            BigDecimal boxSize = oldEntity.getBoxLength().multiply(oldEntity.getBoxWidth()).multiply(oldEntity.getBoxHeight());
            BigDecimal volumeSetting = BigDecimal.valueOf(logisticsChannelEntity.getVolumeSetting());
            if(volumeSetting.compareTo(BigDecimal.ZERO) == 0){
                throw new ServiceException("物流渠道【{}】的材积设置不能为0", logisticsChannelEntity.getName());
            }
            BigDecimal volumeWeight = boxSize.divide(volumeSetting, 4, RoundingMode.HALF_UP);
            entity.setVolumeWeight(volumeWeight);
            entity.setChargedWeight(oldEntity.getOutStockWeight().max(volumeWeight));
            if(cfgWeightAllocationType.equals("outstockChargedWeight")){
                if(ShippingFeeRuleEnum.BILLING_WEIGHT.getCode().equals(logisticsChannelEntity.getFeeRule())){
                    entity.setAllocationType(WeightAllocationTypeEnum.BILLING_WEIGHT.getCode());
                }
                if(ShippingFeeRuleEnum.NET_WEIGHT.getCode().equals(logisticsChannelEntity.getFeeRule())){
                    entity.setAllocationType(WeightAllocationTypeEnum.NET_WEIGHT.getCode());
                }
                if(ShippingFeeRuleEnum.VOLUME_WEIGHT.getCode().equals(logisticsChannelEntity.getFeeRule())){
                    entity.setAllocationType(WeightAllocationTypeEnum.VOLUME_WEIGHT.getCode());
                }
            }
            if(cfgWeightAllocationType.equals("productWeight")){
                entity.setAllocationType(WeightAllocationTypeEnum.PRODUCT_WEIGHT.getCode());
            }
            List<BomChildrenSkuDTO> bomChildrenList = bomSkuList.stream().filter(item -> item.getParentSkuId().equals(oldEntity.getSkuId())).collect(Collectors.toList());
            if(!bomChildrenList.isEmpty()){
                //bom
                BigDecimal parentSkuWeight = getParentSkuWeight(bomChildrenList, productPackList);
                entity.setProductWeight(parentSkuWeight);
            }else {
                //单sku
                Optional<ProductPackEntity> productPackOptional = productPackList.stream().filter(item -> item.getSkuId().equals(oldEntity.getSkuId())).findFirst();
                ProductPackEntity productPackEntity = productPackOptional.get();
                productPackEntity.handleData();
                BigDecimal productWeight = productPackEntity.getGrossWeight().compareTo(BigDecimal.ZERO) != 0 ? productPackEntity.getGrossWeight() : productPackEntity.getNetWeight();
                entity.setProductWeight(productWeight.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP));
            }
            updateList.add(entity);
        }
        computeAllocationWeight(updateList, cfgWeightAllocationType, logisticsChannelEntity);
        boolean updated = this.updateBatchById(updateList);

        return updated ? BatchResultDTO.success(logisticsBillId, logisticsBillId) : BatchResultDTO.fail(logisticsBillId, logisticsBillId, OperationTypeEnum.UPDATE);
    }

    /**
     * 计算分摊重量
     */
    private void computeAllocationWeight(List<FirstMileWeightAllocationEntity> entityList, String cfgWeightAllocationType, LogisticsChannelEntity logisticsChannelEntity) {
        Map<String, List<FirstMileWeightAllocationEntity>> boxGroupMap = entityList.stream().collect(Collectors.groupingBy(FirstMileWeightAllocationEntity::getBoxId));
        for (Map.Entry<String, List<FirstMileWeightAllocationEntity>> entry : boxGroupMap.entrySet()) {
            //统计每个箱子中所有sku的总重量
            BigDecimal boxWeightSum = BigDecimal.ZERO;
            List<FirstMileWeightAllocationEntity> boxEntityList = entry.getValue();
            for (FirstMileWeightAllocationEntity dto : boxEntityList) {
                BigDecimal skuWeightSum = dto.getProductWeight().multiply(BigDecimal.valueOf(dto.getDeliveryQty()));
                boxWeightSum = boxWeightSum.add(skuWeightSum);
            }
            //计算分摊重量
            BigDecimal allocationWeightSum = BigDecimal.ZERO;
            BigDecimal weightByAllocationType = getFeeRuleWeight(logisticsChannelEntity.getFeeRule(), boxEntityList.get(0));
            for (FirstMileWeightAllocationEntity entity : boxEntityList) {
                BigDecimal skuWeightSum = entity.getProductWeight().multiply(BigDecimal.valueOf(entity.getDeliveryQty()));
                if(cfgWeightAllocationType.equals("outstockChargedWeight")){
                    BigDecimal allocationWeight = skuWeightSum.multiply(weightByAllocationType).divide(boxWeightSum, 2, RoundingMode.DOWN);
                    entity.setAllocationWeight(allocationWeight);
                }
                if(cfgWeightAllocationType.equals("productWeight")){
                    entity.setAllocationWeight(entity.getProductWeight().multiply(BigDecimal.valueOf(entity.getDeliveryQty())));
                }
                allocationWeightSum = allocationWeightSum.add(entity.getAllocationWeight());
            }
            if(allocationWeightSum.compareTo(weightByAllocationType) < 0){
                boxEntityList.sort(Comparator.comparing(FirstMileWeightAllocationEntity::getDeliveryQty).reversed());
                FirstMileWeightAllocationEntity entity = boxEntityList.get(0);
                BigDecimal subtract = weightByAllocationType.subtract(allocationWeightSum);
                entity.setAllocationWeight(entity.getAllocationWeight().add(subtract));
            }
        }
    }

    @Override
    public BatchResultDTO deleteByLogisticsBillId(String logisticsBillId) {
        int count = costAllocationService.count(new LambdaQueryWrapper<FirstMileCostAllocationEntity>().eq(FirstMileCostAllocationEntity::getLogisticsBillId, logisticsBillId));
        if(count > 0){
            return BatchResultDTO.fail(logisticsBillId, logisticsBillId, "已下推费用分摊，不允许删除");
        }
        boolean remove = this.lambdaUpdate().eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId).remove();
        return remove ? BatchResultDTO.success(logisticsBillId, logisticsBillId) : BatchResultDTO.fail(logisticsBillId, logisticsBillId, OperationTypeEnum.DELETE);
    }

    @Override
    public List<FirstMileWeightAllocationEntity> listByLogisticsBillIds(List<String> logisticsBillIds) {
        if (logisticsBillIds.isEmpty()){
            return this.lambdaQuery().in(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillIds).list();
        }
        return Collections.emptyList();
    }

    @Override
    public List<FirstMileWeightAllocationEntity> listBySourceIds(List<String> sourceIds, List<String> statusList) {
//        if (CollectionUtils.isEmpty(sourceIds) && CollectionUtils.isEmpty(statusList)){
//            return Collections.emptyList();
//        }
        return baseMapper.listBySourceIds(sourceIds,statusList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO add(String logisticsBillId) {
        //物流单
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(logisticsBillId);

        Integer count = this.lambdaQuery().eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId).count();
        if(count > 0){
            return BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getTransportNo(), "已下推重量分摊，不能再次下推");
        }

        FirstMileWeightAllocationDTO.LogisticsBillInfoDTO logisticsBillInfo = baseMapper.getLogisticsBillInfo(logisticsBillId);
        //物流渠道
        LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(logisticsBillInfo.getChannelId());
        if(logisticsChannelEntity == null){
            return BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getTransportNo(), "没有找到物流渠道");
        }
        //发货单
        String deliveryId = logisticsBillEntity.getOutstockId();
        List<FirstMileDeliveryEntity> firstMileDeliveryList = wmsFirstMileDeliveryFeign.listByIds(Collections.singletonList(deliveryId));
        FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryList.get(0);
        //发货单明细
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailList = firstMileDeliveryDetailFeign.listByMainId(Collections.singletonList(firstMileDeliveryEntity.getId()));
        //装箱任务
        PackingTaskEntity packingTaskEntity;
        if(firstMileDeliveryEntity.getSourceType().equals(SourceTypeEnum.REQUISITION_APPLICATION.getCode())){
            packingTaskEntity = packingTaskFeign.getBySourceId(firstMileDeliveryEntity.getSourceId());
            if(packingTaskEntity == null){
                packingTaskEntity = packingTaskFeign.getBySourceId(firstMileDeliveryEntity.getId());
            }
        }else {
            packingTaskEntity = packingTaskFeign.getBySourceId(firstMileDeliveryEntity.getId());
        }
        if(packingTaskEntity == null){
            return BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getTransportNo(), "没有找到装箱任务");
        }
        //装箱内容物详情
        List<WmsCartonDTO.DetailDTO> cartonDetailList = wmsCartonFeign.listByPackingTaskId(packingTaskEntity.getId());
        //系统配置
        CfgSettingDTO.ViewDTO cfgSettingView = cfgSettingService.view();
        String cfgWeightAllocationType = cfgSettingView.getAllocationSettingDTO().getWeightFirstAllocation();
        //产品包装信息
        List<String> skuIds = firstMileDeliveryDetailList.stream().map(FirstMileDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //遍历出combo类型的skuid
        List<BomChildrenSkuDTO> comboSkuList = bomSkuList.stream().filter(b -> b.getType().equals(BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
        Map<String, List<BomChildrenSkuDTO>> comboSkuMap = comboSkuList.stream().collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuId));
        List<String> comboSkuIds = comboSkuList.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        //遍历出single类型的skuid
        List<String> singleSkuIds = skuIds.stream().filter(e ->!comboSkuIds.contains(e)).collect(Collectors.toList());
        List<ProductPackEntity> productPackList = productPackFeign.listBySkuIds(singleSkuIds);
        //物流商
        LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(logisticsBillEntity.getLogisticsSupplierId());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(Collections.singletonList(firstMileDeliveryEntity.getCountryId()));
        FirstMileWeightAllocationDTO.AddDTO weightAllocationDTO = new FirstMileWeightAllocationDTO.AddDTO();
        weightAllocationDTO.setLogisticsBillId(logisticsBillId);
        weightAllocationDTO.setSourceId(logisticsBillEntity.getOutstockId());
        weightAllocationDTO.setSourceCode(logisticsBillEntity.getOutstockCode());
        weightAllocationDTO.setShopId(logisticsBillEntity.getShopId());
        weightAllocationDTO.setShopName(logisticsBillEntity.getShopName());
        weightAllocationDTO.setSupplierId(logisticsBillEntity.getLogisticsSupplierId());
        weightAllocationDTO.setSupplierName(logisticsSupplierEntity.getSupplierName());
        if(firstMileDeliveryEntity.getDemandType().equals(FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode())){
            //备货第三方仓：取海外仓入库单号
            List<OverseasWarehouseInboundEntity> warehouseInboundEntity = overseaWarehouseInboundFeign.listBySourceIds(Collections.singletonList(deliveryId));
            if(!warehouseInboundEntity.isEmpty()){
                weightAllocationDTO.setBusinessCode(warehouseInboundEntity.get(0).getCode());
            }
        }
        if(firstMileDeliveryEntity.getDemandType().equals(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode())){
            //备货FBA仓：取FBA货件单号
            String fbaShipmentCode = firstMileDeliveryDetailList.get(0).getFbaShipmentCode();
            weightAllocationDTO.setBusinessCode(fbaShipmentCode);
        }
        weightAllocationDTO.setTransportNo(logisticsBillEntity.getTransportNo());
        weightAllocationDTO.setFeeRule(logisticsChannelEntity.getFeeRule());
        weightAllocationDTO.setVolumeSetting(logisticsChannelEntity.getVolumeSetting());
        weightAllocationDTO.setToCountry(firstMileDeliveryEntity.getCountryId());
        if(!countryList.isEmpty()){
            weightAllocationDTO.setToCountryName(countryList.get(0).getNameCn());
        }
        weightAllocationDTO.setFromWarehouseId(firstMileDeliveryEntity.getDeliveryWarehouseId());
        List<FirstMileWeightAllocationEntity> saveList = new ArrayList<>();
        for (WmsCartonDTO.DetailDTO cartonDetail : cartonDetailList) {
            FirstMileWeightAllocationEntity entity = new FirstMileWeightAllocationEntity();
            BeanMapper.copy(weightAllocationDTO, entity);
            entity.setSkuId(cartonDetail.getSkuId());
            entity.setSkuNo(cartonDetail.getSkuNo());
            entity.setDeliveryQty(cartonDetail.getPackQty());
            entity.setBoxId(cartonDetail.getBoxId());
            entity.setBoxNo(cartonDetail.getBoxNo());
            entity.setOutStockWeight(cartonDetail.getPackageWeight());
            entity.setWeightUnit(cartonDetail.getWeightUnit());
            entity.setBoxLength(cartonDetail.getBoxLength());
            entity.setBoxWidth(cartonDetail.getBoxWidth());
            entity.setBoxHeight(cartonDetail.getBoxHeight());
            entity.setBoxSizeUnit(cartonDetail.getSizeUnit());
            Optional<FirstMileDeliveryDetailEntity> deliveryDetailOptional = firstMileDeliveryDetailList.stream().filter(item -> item.getSkuId().equals(cartonDetail.getSkuId())).findFirst();
            if(deliveryDetailOptional.isPresent()){
                FirstMileDeliveryDetailEntity deliveryDetail = deliveryDetailOptional.get();
                entity.setProductName(deliveryDetail.getProductName());
                entity.setPlatformSkuNo(deliveryDetail.getPlatformSkuNo());
            }
            //2024-09-13 jack and 凤玲 头程费用分摊页面--单产品重量取值逻辑优化
            //单产品重量：取值来源表为产品管理-毛重/净重：优先取值毛重，其次取值净重
            //1.单SKU时：直接取值毛重和净重
            //2.组合SKU时：销售套装取值 优先毛重*用例【取值版本为最新版本，后续改为发货单版本】
            if(singleSkuIds.contains(cartonDetail.getSkuId())){
                ProductPackEntity productPackEntity = productPackList.stream().filter(item -> item.getSkuId().equals(cartonDetail.getSkuId())).findFirst().get();
                productPackEntity.handleData();
                BigDecimal productWeight = productPackEntity.getGrossWeight().compareTo(BigDecimal.ZERO) != 0 ? productPackEntity.getGrossWeight() : productPackEntity.getNetWeight();
                entity.setProductWeight(productWeight.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP));
            }else if(comboSkuIds.contains(cartonDetail.getSkuId())){
                List<BomChildrenSkuDTO> bomChildrenSkuDTOS = comboSkuMap.get(cartonDetail.getSkuId());
                BigDecimal sum = BigDecimal.ZERO;
                for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOS) {
                    BigDecimal grossWeight = bomChildrenSkuDTO.getGrossWeight().compareTo(BigDecimal.ZERO)<=0 ?  bomChildrenSkuDTO.getNetWeight() : bomChildrenSkuDTO.getGrossWeight();
                    BigDecimal quantity = null == bomChildrenSkuDTO.getQuantity() || bomChildrenSkuDTO.getQuantity() <0 ? BigDecimal.ZERO : new BigDecimal(bomChildrenSkuDTO.getQuantity());
                    sum = sum.add(quantity.multiply(grossWeight).divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP));
                }
                entity.setProductWeight(sum);
            }
            BigDecimal boxSize = cartonDetail.getBoxLength().multiply(cartonDetail.getBoxWidth()).multiply(cartonDetail.getBoxHeight());
            BigDecimal volumeSetting = BigDecimal.valueOf(weightAllocationDTO.getVolumeSetting());
            if(volumeSetting.compareTo(BigDecimal.ZERO) == 0){
                String format = String.format("物流渠道【%s】的材积设置不能为0", logisticsChannelEntity.getName());
                return BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getTransportNo(), format);
            }
            BigDecimal volumeWeight = boxSize.divide(volumeSetting, 4, RoundingMode.HALF_UP);
            entity.setVolumeWeight(volumeWeight);
            entity.setChargedWeight(cartonDetail.getPackageWeight().max(volumeWeight));
            if(cfgWeightAllocationType.equals("outstockChargedWeight")){
                if(ShippingFeeRuleEnum.BILLING_WEIGHT.getCode().equals(weightAllocationDTO.getFeeRule())){
                    entity.setAllocationType(WeightAllocationTypeEnum.BILLING_WEIGHT.getCode());
                }
                if(ShippingFeeRuleEnum.NET_WEIGHT.getCode().equals(weightAllocationDTO.getFeeRule())){
                    entity.setAllocationType(WeightAllocationTypeEnum.NET_WEIGHT.getCode());
                }
                if(ShippingFeeRuleEnum.VOLUME_WEIGHT.getCode().equals(weightAllocationDTO.getFeeRule())){
                    entity.setAllocationType(WeightAllocationTypeEnum.VOLUME_WEIGHT.getCode());
                }
            }
            if(cfgWeightAllocationType.equals("productWeight")){
                entity.setAllocationType(WeightAllocationTypeEnum.PRODUCT_WEIGHT.getCode());
            }
            saveList.add(entity);
        }
        computeAllocationWeight(saveList, cfgWeightAllocationType, logisticsChannelEntity);
        saveList.sort(Comparator.comparing(FirstMileWeightAllocationEntity::getBoxNo).reversed());
        //保存
        boolean success = this.saveBatch(saveList);
        if(success){
            updateCostAllocationStatus(logisticsBillId);
        }
        return success ? BatchResultDTO.success(logisticsBillId, logisticsBillEntity.getTransportNo()) : BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getTransportNo(), "保存失败");
    }


    /**
     * 按照计费规则获取重量
     */
    private BigDecimal getFeeRuleWeight(String feeRule, FirstMileWeightAllocationEntity entity) {
        if(StringUtils.equals(feeRule, ShippingFeeRuleEnum.BILLING_WEIGHT.getCode())){
            //计费重
            return entity.getChargedWeight();
        }
        if(StringUtils.equals(feeRule, ShippingFeeRuleEnum.NET_WEIGHT.getCode())){
            //出库实重
            return entity.getOutStockWeight();
        }
        if(StringUtils.equals(feeRule, ShippingFeeRuleEnum.VOLUME_WEIGHT.getCode())){
            //体积重
            return entity.getVolumeWeight();
        }
        throw new ServiceException("匹配计费规则失败:{}", feeRule);
    }

    /**
     * 计算bom的重量
     *
     * @param bomChildrenList 子sku集合
     * @param productPackList
     * @return 子sku的总重量
     */
    private BigDecimal getParentSkuWeight(List<BomChildrenSkuDTO> bomChildrenList, List<ProductPackEntity> productPackList) {
        BigDecimal parentSkuWeight = BigDecimal.ZERO;
        for (BomChildrenSkuDTO childrenSkuDTO : bomChildrenList) {
            Optional<ProductPackEntity> productPackOptional = productPackList.stream().filter(item -> item.getSkuId().equals(childrenSkuDTO.getSkuId())).findFirst();
            if(productPackOptional.isPresent()){
                ProductPackEntity productPackEntity = productPackOptional.get();
                productPackEntity.handleData();
                BigDecimal productWeight = productPackEntity.getGrossWeight().compareTo(BigDecimal.ZERO) != 0 ? productPackEntity.getGrossWeight() : productPackEntity.getNetWeight();
                BigDecimal productWeightSum = productWeight.multiply(BigDecimal.valueOf(childrenSkuDTO.getQuantity())).divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
                parentSkuWeight = parentSkuWeight.add(productWeightSum);
            }
        }
        return parentSkuWeight;
    }


    @Override
    public void updateCalculateMonthBySourceId(String sourceId) {
        if (!StrUtil.isBlank(sourceId)) {
            this.lambdaUpdate()
                    .set(FirstMileWeightAllocationEntity::getCalculateMonth, "")
                    .eq(FirstMileWeightAllocationEntity::getSourceId, sourceId)
                    .update();
        }
    }


}
