package com.erp.server.tms.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.CfgSettingDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.rpc.plm.feign.BomSkuFeign;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.rpc.plm.feign.ProductPackFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.server.tms.mapper.FirstMileWeightAllocationMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.enums.ApiError;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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
    private BomSkuFeign bomSkuFeign;



    @Override
    public PagingVO<FirstMileWeightAllocationDTO.ViewDTO> paging(PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto) {
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<FirstMileWeightAllocationDTO.ViewDTO> pageData = baseMapper.paging(query, dto.getParams());
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
        //国家
        List<String> countryCodeList = records.stream().map(item -> item.getToCountry()).distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryCodeList);
        Map<String, String> countryNameMap = countryList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2.getNameCn()));
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
            item.setToCountryName(countryNameMap.get(item.getToCountry()));
            item.setBoxSizeStr(item.getBoxLength() + "*" + item.getBoxWidth() + "*" + item.getBoxHeight() + " " + item.getBoxSizeUnit());
            item.setCreateTimeStr(timeFormatter.format(item.getCreateTime()));
            FirstMileCostAllocationDTO.LastedAllocMonthDTO lastedAllocMonthDTO = lastedAllocationMonthList.stream().filter(v -> v.getLogisticsBillId().equals(item.getLogisticsBillId())).findFirst().orElse(null);
            if(lastedAllocMonthDTO != null){
                item.setCalculatePeriodId(lastedAllocMonthDTO.getReportPeriodId());
                item.setLatestCostAllocationMonth(lastedAllocMonthDTO.getLatestMonth());
                if(lastedAllocMonthDTO.getLatestMonth() != null){
                    item.setLatestCostAllocationMonthStr(monthFormatter.format(lastedAllocMonthDTO.getLatestMonth()));
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
            }
        }
    }

    @Override
    public void exportExcel(FirstMileWeightAllocationDTO.ExportParamDTO dto, HttpServletResponse response) {
        List<FirstMileWeightAllocationDTO.ViewDTO> list = baseMapper.listByParam(dto);
        fillData(list);
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        StringBuilder builder = new StringBuilder();
        builder.append("头程重量分摊导出").append(date);
        try {
            new ExcelPrintUtils().patchExport(list, response, builder.toString(), "excel/firstMileWeightAllocationExport.xlsx");
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
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

    @Override
    public BatchResultDTO weightReCompute(String logisticsBillId) {
        return BatchResultDTO.success(logisticsBillId, logisticsBillId, OperationTypeEnum.UPDATE);
    }

    @Override
    public BatchResultDTO deleteById(String id) {
        FirstMileWeightAllocationEntity entity = baseMapper.selectById(id);
        int count = costAllocationService.count(new LambdaQueryWrapper<FirstMileCostAllocationEntity>().eq(FirstMileCostAllocationEntity::getLogisticsBillId, entity.getLogisticsBillId()));
        if(count > 0){
            return BatchResultDTO.fail(id, entity.getBusinessCode(), "已下推费用分摊，不允许删除");
        }
        int remove = baseMapper.deleteById(id);
        return remove > 0 ? BatchResultDTO.success(id, entity.getBusinessCode()) : BatchResultDTO.fail(id, entity.getBusinessCode(), OperationTypeEnum.DELETE);
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
        if (CollectionUtils.isEmpty(sourceIds) && CollectionUtils.isEmpty(statusList)){
            return Collections.emptyList();
        }
        return baseMapper.listBySourceIds(sourceIds,statusList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO add(String logisticsBillId) {
        Integer count = this.lambdaQuery().eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId).count();
        if(count > 0){
            throw new ServiceException("已下推重量分摊，不能再次下推");
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
        PackingTaskEntity packingTaskEntity = packingTaskFeign.getBySourceId(firstMileDeliveryEntity.getSourceId());
        if(packingTaskEntity == null){
            throw new ServiceException("没有找到装箱任务");
        }
        //装箱内容物详情
        List<WmsCartonDTO.DetailDTO> cartonDetailList = wmsCartonFeign.listByPackingTaskId(packingTaskEntity.getId());
        //系统配置
        CfgSettingDTO.ViewDTO cfgSettingView = cfgSettingService.view();
        String cfgWeightAllocationType = cfgSettingView.getAllocationSettingDTO().getWeightFirstAllocation();
        //产品包装信息
        List<String> skuIds = firstMileDeliveryDetailList.stream().map(FirstMileDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomSkuList = bomSkuFeign.listBomChildBySkuIds(skuIds);
        List<String> childrenSkuIds = bomSkuList.stream().map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        childrenSkuIds.addAll(skuIds);
        List<ProductPackEntity> productPackList = productPackFeign.listBySkuIds(childrenSkuIds);
        //物流商
        LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(logisticsBillEntity.getLogisticsSupplierId());

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
            weightAllocationDTO.setBusinessCode(warehouseInboundEntity.get(0).getCode());
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
            List<BomChildrenSkuDTO> bomChildrenList = bomSkuList.stream().filter(item -> item.getParentSkuId().equals(cartonDetail.getSkuId())).collect(Collectors.toList());
            if(!bomChildrenList.isEmpty()){
                //bom
                BigDecimal parentSkuWeight = getParentSkuWeight(bomChildrenList, productPackList);
                entity.setProductWeight(parentSkuWeight);
            }else {
                //单sku
                Optional<ProductPackEntity> productPackOptional = productPackList.stream().filter(item -> item.getSkuId().equals(cartonDetail.getSkuId())).findFirst();
                ProductPackEntity productPackEntity = productPackOptional.get();
                productPackEntity.handleData();
                BigDecimal productWeight = productPackEntity.getGrossWeight().compareTo(BigDecimal.ZERO) != 0 ? productPackEntity.getGrossWeight() : productPackEntity.getNetWeight();
                entity.setProductWeight(productWeight.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP));
            }

            BigDecimal boxSize = cartonDetail.getBoxLength().multiply(cartonDetail.getBoxWidth()).multiply(cartonDetail.getBoxHeight());
            BigDecimal volumeSetting = BigDecimal.valueOf(weightAllocationDTO.getVolumeSetting());
            if(volumeSetting.compareTo(BigDecimal.ZERO) == 0){
                throw new ServiceException("物流渠道【{}】的材积设置不能为0", logisticsChannelEntity.getName());
            }
            BigDecimal volumeWeight = boxSize.divide(volumeSetting, 4, RoundingMode.HALF_UP);
            entity.setVolumeWeight(volumeWeight);
            entity.setChargedWeight(cartonDetail.getPackageWeight().max(volumeWeight));
            if(WeightAllocationEnum.OUTSTOCK_CHARGED_WEIGHT.getCode().equals(cfgWeightAllocationType)){
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
            if(WeightAllocationEnum.SINGLE_PRODUCT_WEIGHT.getCode().equals(cfgWeightAllocationType)){
                entity.setAllocationType(WeightAllocationTypeEnum.PRODUCT_WEIGHT.getCode());
            }
            saveList.add(entity);
        }

        Map<String, List<FirstMileWeightAllocationEntity>> boxGroupMap = saveList.stream().collect(Collectors.groupingBy(FirstMileWeightAllocationEntity::getBoxId));
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
            for (FirstMileWeightAllocationEntity dto : boxEntityList) {
                BigDecimal skuWeightSum = dto.getProductWeight().multiply(BigDecimal.valueOf(dto.getDeliveryQty()));
                BigDecimal divide = skuWeightSum.divide(boxWeightSum, 4, RoundingMode.HALF_UP);
                if(WeightAllocationEnum.OUTSTOCK_CHARGED_WEIGHT.getCode().equals(cfgWeightAllocationType)){
                    BigDecimal weightByAllocationType = getFeeRuleWeight(weightAllocationDTO, dto);
                    BigDecimal allocationWeight = divide.multiply(weightByAllocationType);
                    dto.setAllocationWeight(allocationWeight);
                }
                if(WeightAllocationEnum.SINGLE_PRODUCT_WEIGHT.getCode().equals(cfgWeightAllocationType)){
                    dto.setAllocationWeight(dto.getProductWeight().multiply(BigDecimal.valueOf(dto.getDeliveryQty())));
                }
                allocationWeightSum = allocationWeightSum.add(dto.getAllocationWeight());
                //如果是箱子中最后一个产品
                if(boxEntityList.indexOf(dto) != (boxEntityList.size() - 1)){

                }
            }
        }
        saveList.sort(Comparator.comparing(FirstMileWeightAllocationEntity::getBoxNo).reversed());
        //保存
        boolean success = this.saveBatch(saveList);
        return success ? BatchResultDTO.success(logisticsBillId, logisticsBillId) : BatchResultDTO.fail(logisticsBillId, logisticsBillId, OperationTypeEnum.ADD);
    }

    /**
     * 按照计费规则获取重量
     */
    private BigDecimal getFeeRuleWeight(FirstMileWeightAllocationDTO.AddDTO weightAllocationDTO, FirstMileWeightAllocationEntity entity) {
        if(StringUtils.equals(weightAllocationDTO.getFeeRule(), ShippingFeeRuleEnum.BILLING_WEIGHT.getCode())){
            //计费重
            return entity.getChargedWeight();
        }
        if(StringUtils.equals(weightAllocationDTO.getFeeRule(), ShippingFeeRuleEnum.NET_WEIGHT.getCode())){
            //出库实重
            return entity.getOutStockWeight();
        }
        if(StringUtils.equals(weightAllocationDTO.getFeeRule(), ShippingFeeRuleEnum.VOLUME_WEIGHT.getCode())){
            //体积重
            return entity.getVolumeWeight();
        }
        throw new ServiceException("匹配计费规则失败:{}", weightAllocationDTO.getFeeRule());
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
}
