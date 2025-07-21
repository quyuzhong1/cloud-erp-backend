package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.CfgSettingDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.model.tms.dto.excel.FirstMileWeightChangeExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.plm.feign.ProductPackFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.server.tms.listener.FirstMileWeightChangeExcelListener;
import com.erp.server.tms.mapper.FirstMileWeightAllocationMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private ProductPackFeign productPackFeign;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private FirstMileCostAllocationService costAllocationService;
    @Resource
    private WmsWarehouseFeign warehouseFeign;
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
    private WmsCartonFeign wmsCartonFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private FirstMileChangeRecordService firstMileChangeRecordService;
    @Autowired
    private FirstMileSkuCostAllocationService firstMileSkuCostAllocationService;

    @Override
    public PagingVO<FirstMileWeightAllocationDTO.ViewDTO> paging(PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
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
        DecimalFormat decimalFormat = new DecimalFormat("0.0000");
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
            item.setOutStockWeightStr(decimalFormat.format(item.getOutStockWeight()));
            item.setChargedWeightStr(decimalFormat.format(item.getChargedWeight()));
            item.setProductWeightStr(decimalFormat.format(item.getProductWeight()));
            item.setAllocationWeightStr(decimalFormat.format(item.getAllocationWeight()));
        }
    }

    @Override
    public void exportExcel(FirstMileWeightAllocationDTO.ExportParamDTO dto) {
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        downloadTaskFeign.saveExportTask("头程重量分摊导出" + date, EXPORT_TMS_FM_WEIGHT_ALLOCATION.getCode(), dto);
    }

    @Override
    public List<FirstMileWeightAllocationDTO.TabDTO> tabList(FirstMileWeightAllocationDTO.PagingParamDTO pagingParamDTO) {
        List<FirstMileWeightAllocationDTO.TabDTO> list = new ArrayList<>();
        Integer wait = baseMapper.countTabNum(pagingParamDTO.getPermissionSql(),Arrays.asList("not", "part"));
        Integer already = baseMapper.countTabNum(pagingParamDTO.getPermissionSql(),Collections.singletonList("already"));
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
        //日期倒排
        costAllocationList.sort(Comparator.comparing(FirstMileWeightAllocationDTO.CostAllocationDTO::getReportPeriod).reversed());
        FirstMileWeightAllocationDTO.CostAllocationDTO costAllocationDTO = costAllocationList.get(0);
        LocalDate reportPeriod = costAllocationDTO.getReportPeriod();
        if(Objects.nonNull(reportPeriod)){
            String calculateMonth = reportPeriod.format(formatter);

            //获取日期最新的数据集合
            List<FirstMileWeightAllocationDTO.CostAllocationDTO> list = costAllocationList.stream().filter(item -> item.getReportPeriod().equals(reportPeriod)).collect(Collectors.toList());
            //按sku维度分组
            Map<String, List<FirstMileWeightAllocationDTO.CostAllocationDTO>> groupedBySku = list.stream().collect(Collectors.groupingBy(FirstMileWeightAllocationDTO.CostAllocationDTO::getSkuId));

            // 核对主判断条件
            //最新费用分摊的核算状态first_mile_cost_allocation表的status值是confirm
            boolean isConfirm = "confirm".equals(costAllocationDTO.getCostAllocationStatus());
            //最新费用分摊的费用来源是实际账单first_mile_sku_cost_allocation表的bill_source_type值：actual
            boolean isActualSource = "actual".equals(Optional.ofNullable(costAllocationDTO.getBillSourceType()).orElse(""));

            if(isConfirm && isActualSource){
                //sku维度 最新费用分摊的费用的期末在途费用为0 first_mile_sku_cost_allocation_detail表的end_period_transit_cost：0
                groupedBySku.forEach((skuId, items) -> {
                    boolean allMatch = items.stream()
                            .allMatch(item -> BigDecimal.ZERO.compareTo(item.getEndPeriodTransitCost()) == 0);

                    LambdaUpdateChainWrapper<FirstMileWeightAllocationEntity> updateWrapper = this.lambdaUpdate()
                            .set(FirstMileWeightAllocationEntity::getCalculateMonth, calculateMonth)
                            .eq(FirstMileWeightAllocationEntity::getSkuId, skuId)
                            .eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId);

                    if (allMatch) {
                        updateWrapper.set(FirstMileWeightAllocationEntity::getCostAllocationStatus, CostAllocationStatusEnum.ALREADY.getCode());
                    } else {
                        updateWrapper.set(FirstMileWeightAllocationEntity::getCostAllocationStatus, CostAllocationStatusEnum.PART.getCode());
                    }
                    updateWrapper.update();
                });
            }else {
                //sku维度 费用重量分摊存在则更新为部分分摊
                 this.lambdaUpdate().set(FirstMileWeightAllocationEntity::getCostAllocationStatus, CostAllocationStatusEnum.PART.getCode())
                        .set(FirstMileWeightAllocationEntity::getCalculateMonth, calculateMonth)
                        .in(FirstMileWeightAllocationEntity::getSkuId, groupedBySku.keySet())
                        .eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId)
                        .update();
            }
        }
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
        if (Objects.isNull(logisticsBillInfo)) {
            return BatchResultDTO.fail(logisticsBillId, logisticsBillId, "物流单信息不存在");
        }
        //物流渠道
        LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(logisticsBillInfo.getChannelId());
        if (Objects.isNull(logisticsChannelEntity)) {
            return BatchResultDTO.fail(logisticsBillId, logisticsBillId, "物流渠道信息不存在");
        }
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
        //装箱内容物详情
        List<String> fbaShipmentCodes = firstMileDeliveryDetailList.stream().map(FirstMileDeliveryDetailEntity::getFbaShipmentCode).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());

        List<WmsCartonDTO.DetailDTO> cartonDetailList = wmsCartonFeign.listByPackingTaskId(packingTaskEntity.getId(),fbaShipmentCodes);
        //系统配置
        CfgSettingDTO.ViewDTO cfgSettingView = cfgSettingService.view();
        String cfgWeightAllocationType = cfgSettingView.getAllocationSettingDTO().getWeightFirstAllocation();
        List<String> skuIds = firstMileDeliveryDetailList.stream().map(FirstMileDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //遍历出combo类型的skuid
        List<BomChildrenSkuDTO> comboSkuList = bomSkuList.stream().filter(b -> b.getType().equals(BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
        Map<String, List<BomChildrenSkuDTO>> comboSkuMap = comboSkuList.stream().collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuId));
        List<String> comboSkuIds = comboSkuList.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        //遍历出single类型的skuid
        List<String> singleSkuIds = skuIds.stream().filter(e ->!comboSkuIds.contains(e)).collect(Collectors.toList());
        //产品包装信息
        List<ProductPackEntity> productPackList = productPackFeign.listBySkuIds(singleSkuIds);

        List<FirstMileWeightAllocationEntity> entityList = this.lambdaQuery().eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId).list();
        List<FirstMileWeightAllocationEntity> updateList = new ArrayList<>(entityList.size());
        for (FirstMileWeightAllocationEntity oldEntity : entityList) {
            FirstMileWeightAllocationEntity entity = new FirstMileWeightAllocationEntity();
            WmsCartonDTO.DetailDTO detailDTO = cartonDetailList.stream().filter(e -> e.getBoxId().equals(oldEntity.getBoxId())).findFirst().orElseThrow(() -> new ServiceException("物流单【{}】的装箱内容【{}】不存在", logisticsBillEntity.getTransportNo(), oldEntity.getBoxNo()));
            entity.setId(oldEntity.getId());
            entity.setBoxId(oldEntity.getBoxId());
            entity.setBoxNo(oldEntity.getBoxNo());
            entity.setDeliveryQty(oldEntity.getDeliveryQty());
            entity.setFeeRule(logisticsChannelEntity.getFeeRule());
            //2024-09-14 jack 重算时出库重量字段值从装箱内容物详情信息中获取
            entity.setOutStockWeight(getOutStockWeight(detailDTO.getPackageWeight(), oldEntity.getSourceId(),oldEntity.getBusinessCode(),oldEntity.getBoxId()));
            BigDecimal boxLength = getBoxLength(detailDTO.getBoxLength(),oldEntity.getSourceId(),oldEntity.getBusinessCode(),oldEntity.getBoxId());
            entity.setBoxLength(boxLength);
            BigDecimal boxWidth = getBoxWidth(detailDTO.getBoxWidth(),oldEntity.getSourceId(),oldEntity.getBusinessCode(),oldEntity.getBoxId());
            entity.setBoxWidth(boxWidth);
            BigDecimal boxHeight = getBoxHeight(detailDTO.getBoxHeight(),oldEntity.getSourceId(),oldEntity.getBusinessCode(),oldEntity.getBoxId());
            entity.setBoxHeight(boxHeight);
            BigDecimal boxSize = boxLength.multiply(boxWidth).multiply(boxHeight);
            BigDecimal volumeSetting = BigDecimal.valueOf(logisticsChannelEntity.getVolumeSetting());
            if(volumeSetting.compareTo(BigDecimal.ZERO) == 0){
                throw new ServiceException("物流渠道【{}】的材积设置不能为0", logisticsChannelEntity.getName());
            }
            BigDecimal volumeWeight = boxSize.divide(volumeSetting, 4, RoundingMode.HALF_UP);
            entity.setVolumeWeight(volumeWeight);
            entity.setChargedWeight(entity.getOutStockWeight().max(volumeWeight));
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
            //SKU体积重【体积重计算方式 长cm*宽cm*高cm/渠道的材积设置------------注意尺寸单位是cm】
            BigDecimal skuVolumeWeight = getProductVolumeWeight(oldEntity.getSkuId(), oldEntity.getSkuNo(), singleSkuIds, productPackList, comboSkuIds, comboSkuMap, volumeSetting);
            //重量分摊-单产品重量-取值优化-按照计费规则取值
            entity.setProductWeight(getProductWeight(oldEntity, entity, singleSkuIds, productPackList, comboSkuIds, comboSkuMap, skuVolumeWeight));
            updateList.add(entity);
        }
        computeAllocationWeight(updateList, cfgWeightAllocationType, logisticsChannelEntity);
        boolean updated = this.updateBatchById(updateList);

        return updated ? BatchResultDTO.success(logisticsBillId, logisticsBillId) : BatchResultDTO.fail(logisticsBillId, logisticsBillId, OperationTypeEnum.UPDATE);
    }

    private BigDecimal getBoxHeight(BigDecimal boxHeight, String deliveryId, String businessCode, String boxId) {
        //先获取修改记录配置
        FirstMileChangeRecordEntity changeRecord = firstMileChangeRecordService.getOutStockWeightByParams(FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode(),deliveryId,businessCode,FirstMileChangeRecordCategoryFieldEnum.BOX_HEIGHT.getCode(),boxId);
        if (Objects.nonNull(changeRecord)){
            return new BigDecimal(changeRecord.getNewValue());
        }
        return boxHeight;
    }

    private BigDecimal getBoxWidth(BigDecimal boxWidth, String deliveryId, String businessCode, String boxId) {
        //先获取修改记录配置
        FirstMileChangeRecordEntity changeRecord = firstMileChangeRecordService.getOutStockWeightByParams(FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode(),deliveryId,businessCode,FirstMileChangeRecordCategoryFieldEnum.BOX_WIDTH.getCode(),boxId);
        if (Objects.nonNull(changeRecord)){
            return new BigDecimal(changeRecord.getNewValue());
        }
        return boxWidth;
    }

    private BigDecimal getBoxLength(BigDecimal boxLength, String deliveryId, String businessCode, String boxId) {
        //先获取修改记录配置
        FirstMileChangeRecordEntity changeRecord = firstMileChangeRecordService.getOutStockWeightByParams(FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode(),deliveryId,businessCode,FirstMileChangeRecordCategoryFieldEnum.BOX_LENGTH.getCode(),boxId);
        if (Objects.nonNull(changeRecord)){
            return new BigDecimal(changeRecord.getNewValue());
        }
        return boxLength;
    }

    private BigDecimal getOutStockWeight(BigDecimal outStockWeight, String deliveryId, String businessCode, String boxId) {
        //先获取修改记录配置
        FirstMileChangeRecordEntity changeRecord = firstMileChangeRecordService.getOutStockWeightByParams(FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode(),deliveryId,businessCode,FirstMileChangeRecordCategoryFieldEnum.CHARGED_WEIGHT.getCode(),boxId);
        if (Objects.nonNull(changeRecord)){
            return new BigDecimal(changeRecord.getNewValue());
        }
        return outStockWeight;
    }

    private BigDecimal getProductWeight(FirstMileWeightAllocationEntity oldEntity, FirstMileWeightAllocationEntity entity, List<String> singleSkuIds, List<ProductPackEntity> productPackList, List<String> comboSkuIds, Map<String, List<BomChildrenSkuDTO>> comboSkuMap, BigDecimal skuVolumeWeight) {
        //先根据调整记录检查是否存在修改的单产品重量-当前单配置查询
        FirstMileChangeRecordEntity changeRecord = firstMileChangeRecordService.getProductWeightByParams(FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode(),oldEntity.getSourceId(),oldEntity.getBusinessCode(),oldEntity.getSkuId(),FirstMileChangeRecordCategoryFieldEnum.PRODUCT_WEIGHT.getCode(),oldEntity.getId(),oldEntity.getBoxId(),oldEntity.getPlatformSkuNo());
        if (Objects.nonNull(changeRecord)){
            //将字符串转成BigDecimal
            if(StringUtils.isNotBlank(changeRecord.getNewValue())){
                return new BigDecimal(changeRecord.getNewValue());
            }
        }
        if(ShippingFeeRuleEnum.BILLING_WEIGHT.getCode().equals(entity.getFeeRule())){
           //取值 单产品的毛重，净重，体积重 取值最大【体积重计算方式 长cm*宽cm*高cm/渠道的材积设置------------注意尺寸单位是cm】
            //毛重
            BigDecimal grossWeight = getProductGrossWeight(oldEntity.getSkuId(), oldEntity.getSkuNo(), singleSkuIds, productPackList, comboSkuIds, comboSkuMap);
            //净重
            BigDecimal netWeight = getProductNetWeight(oldEntity.getSkuId(), oldEntity.getSkuNo(), singleSkuIds, productPackList, comboSkuIds, comboSkuMap);
            return grossWeight.max(netWeight).max(skuVolumeWeight);
        }else if (ShippingFeeRuleEnum.NET_WEIGHT.getCode().equals(entity.getFeeRule())){
            //取值实重 优先取值毛重，没有取值净重
            return getNetWeightByRule(oldEntity.getSkuId(), oldEntity.getSkuNo(), singleSkuIds, productPackList, entity, comboSkuIds, comboSkuMap);
        }else if(ShippingFeeRuleEnum.VOLUME_WEIGHT.getCode().equals(entity.getFeeRule())){
            //取值体积重【体积重计算方式 长cm*宽cm*高cm/渠道的材积设置------------注意尺寸单位是cm】
            return skuVolumeWeight;
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getProductVolumeWeight(String skuId, String skuNo, List<String> singleSkuIds, List<ProductPackEntity> productPackList, List<String> comboSkuIds, Map<String, List<BomChildrenSkuDTO>> comboSkuMap, BigDecimal volumeSetting) {
        BigDecimal productSize = BigDecimal.ZERO;
        if(singleSkuIds.contains(skuId)){
            ProductPackEntity productPackEntity = productPackList.stream().filter(item -> item.getSkuId().equals(skuId)).findFirst().orElseThrow(() -> new ServiceException("【{}】没有找到产品包装信息",skuNo));
            productSize = productPackEntity.getProductLength().multiply(productPackEntity.getProductWidth()).multiply(productPackEntity.getProductHeight());

        }else if(comboSkuIds.contains(skuId)){
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS = comboSkuMap.get(skuId);
            for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOS) {
                productSize = productSize.add(bomChildrenSkuDTO.getLength().multiply(bomChildrenSkuDTO.getWidth()).multiply(bomChildrenSkuDTO.getHeight()).multiply(BigDecimal.valueOf(bomChildrenSkuDTO.getQuantity())));
            }
        }
        return productSize.divide(new BigDecimal(1000)).divide(volumeSetting, 4, RoundingMode.HALF_UP);
    }

    /**
     * 计算实重
     * @param skuId
     * @param skuNo
     * @param singleSkuIds
     * @param productPackList
     * @param entity
     * @param comboSkuIds
     * @param comboSkuMap
     */
    private BigDecimal getNetWeightByRule(String skuId, String skuNo, List<String> singleSkuIds, List<ProductPackEntity> productPackList, FirstMileWeightAllocationEntity entity, List<String> comboSkuIds, Map<String, List<BomChildrenSkuDTO>> comboSkuMap) {
        if(singleSkuIds.contains(skuId)){
            ProductPackEntity productPackEntity = productPackList.stream().filter(item -> item.getSkuId().equals(skuId)).findFirst().orElseThrow(() -> new ServiceException("【{}】没有找到产品包装信息",skuNo));
            BigDecimal netWeight1 = Objects.nonNull(productPackEntity.getNetWeight()) ? productPackEntity.getNetWeight() : BigDecimal.ZERO;
            BigDecimal grossWeight1 = Objects.nonNull(productPackEntity.getGrossWeight()) ? productPackEntity.getGrossWeight() : BigDecimal.ZERO;
            BigDecimal productWeight = grossWeight1.compareTo(BigDecimal.ZERO) != 0 ? grossWeight1 : netWeight1;
            return productWeight.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
        }else if(comboSkuIds.contains(skuId)){
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS = comboSkuMap.get(skuId);
            BigDecimal sum = BigDecimal.ZERO;
            for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOS) {
                BigDecimal grossWeight = bomChildrenSkuDTO.getGrossWeight().compareTo(BigDecimal.ZERO)<=0 ?  bomChildrenSkuDTO.getNetWeight() : bomChildrenSkuDTO.getGrossWeight();
                BigDecimal quantity = null == bomChildrenSkuDTO.getQuantity() || bomChildrenSkuDTO.getQuantity() <0 ? BigDecimal.ZERO : new BigDecimal(bomChildrenSkuDTO.getQuantity());
                sum = sum.add(quantity.multiply(grossWeight).divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP));
            }
            return sum;
        }
        return BigDecimal.ZERO;
    }

    /**
     * 计算产品净重
     * @param skuId
     * @param skuNo
     * @param singleSkuIds
     * @param productPackList
     * @param comboSkuIds
     * @param comboSkuMap
     * @return
     */
    private static BigDecimal getProductNetWeight(String skuId,String skuNo, List<String> singleSkuIds, List<ProductPackEntity> productPackList, List<String> comboSkuIds, Map<String, List<BomChildrenSkuDTO>> comboSkuMap) {
        BigDecimal netWeight = BigDecimal.ZERO;
        if(singleSkuIds.contains(skuId)){
            ProductPackEntity productPackEntity = productPackList.stream().filter(item -> item.getSkuId().equals(skuId)).findFirst().orElseThrow(() -> new ServiceException("【{}】没有找到产品包装信息",skuNo));
            BigDecimal netWeight1 = Objects.nonNull(productPackEntity.getNetWeight()) ? productPackEntity.getNetWeight() : BigDecimal.ZERO;
            netWeight = netWeight1.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
        }else if(comboSkuIds.contains(skuId)){
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS = comboSkuMap.get(skuId);
            for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOS) {
                BigDecimal netWeight1 = bomChildrenSkuDTO.getNetWeight();
                BigDecimal quantity = null == bomChildrenSkuDTO.getQuantity() || bomChildrenSkuDTO.getQuantity() <0 ? BigDecimal.ZERO : new BigDecimal(bomChildrenSkuDTO.getQuantity());
                netWeight = netWeight.add(quantity.multiply(netWeight1).divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP));
            }
        }
        return netWeight;
    }

    /**
     * 计算产品毛重
     * @param skuId
     * @param skuNo
     * @param singleSkuIds
     * @param productPackList
     * @param comboSkuIds
     * @param comboSkuMap
     * @return
     */
    private static BigDecimal getProductGrossWeight(String skuId, String skuNo, List<String> singleSkuIds, List<ProductPackEntity> productPackList, List<String> comboSkuIds, Map<String, List<BomChildrenSkuDTO>> comboSkuMap) {
        BigDecimal grossWeight = BigDecimal.ZERO;
        if(singleSkuIds.contains(skuId)){
            ProductPackEntity productPackEntity = productPackList.stream().filter(item -> item.getSkuId().equals(skuId)).findFirst().orElseThrow(() -> new ServiceException("【{}】没有找到产品包装信息",skuNo));
            BigDecimal grossWeight1 = Objects.nonNull(productPackEntity.getGrossWeight()) ? productPackEntity.getGrossWeight() : BigDecimal.ZERO;
            grossWeight = grossWeight1.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);;
        }else if(comboSkuIds.contains(skuId)){
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS = comboSkuMap.get(skuId);
            for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOS) {
                BigDecimal grossWeight1 = bomChildrenSkuDTO.getGrossWeight().compareTo(BigDecimal.ZERO)<=0 ?  bomChildrenSkuDTO.getNetWeight() : bomChildrenSkuDTO.getGrossWeight();
                BigDecimal quantity = null == bomChildrenSkuDTO.getQuantity() || bomChildrenSkuDTO.getQuantity() <0 ? BigDecimal.ZERO : new BigDecimal(bomChildrenSkuDTO.getQuantity());
                grossWeight = grossWeight.add(quantity.multiply(grossWeight1).divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP));
            }
        }
        return grossWeight;
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
                	if(skuWeightSum.compareTo(BigDecimal.ZERO) == 0 || boxWeightSum.compareTo(BigDecimal.ZERO) == 0) {
                		entity.setAllocationWeight(BigDecimal.ZERO);
                	}else {
                		BigDecimal allocationWeight = skuWeightSum.multiply(weightByAllocationType).divide(boxWeightSum, 2, RoundingMode.DOWN);
                        entity.setAllocationWeight(allocationWeight);
                	}
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
    public BatchResultDTO add(String logisticsBillId) throws InterruptedException {
        //物流单
        LogisticsBillEntity logisticsBillEntity = logisticsBillService.getById(logisticsBillId);

        Integer count = this.lambdaQuery().eq(FirstMileWeightAllocationEntity::getLogisticsBillId, logisticsBillId).count();
        if(count > 0){
            return BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getOutstockCode(), "已下推重量分摊，不能再次下推");
        }

        FirstMileWeightAllocationDTO.LogisticsBillInfoDTO logisticsBillInfo = baseMapper.getLogisticsBillInfo(logisticsBillId);
        //物流渠道
        LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(logisticsBillInfo.getChannelId());
        if(logisticsChannelEntity == null){
            return BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getOutstockCode(), "没有找到物流渠道");
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
            return BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getOutstockCode(), "没有找到装箱任务");
        }
        //装箱内容物详情
        List<String> fbaShipmentCodes = firstMileDeliveryDetailList.stream().map(FirstMileDeliveryDetailEntity::getFbaShipmentCode).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());

        List<WmsCartonDTO.DetailDTO> cartonDetailList = wmsCartonFeign.listByPackingTaskId(packingTaskEntity.getId(),fbaShipmentCodes);
        //系统配置
        CfgSettingDTO.ViewDTO cfgSettingView = cfgSettingService.view();
        String cfgWeightAllocationType = cfgSettingView.getAllocationSettingDTO().getWeightFirstAllocation();
        List<String> skuIds = firstMileDeliveryDetailList.stream().map(FirstMileDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //遍历出combo类型的skuid
        List<BomChildrenSkuDTO> comboSkuList = bomSkuList.stream().filter(b -> b.getType().equals(BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
        Map<String, List<BomChildrenSkuDTO>> comboSkuMap = comboSkuList.stream().collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuId));
        List<String> comboSkuIds = comboSkuList.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        //遍历出single类型的skuid
        List<String> singleSkuIds = skuIds.stream().filter(e ->!comboSkuIds.contains(e)).collect(Collectors.toList());
        //产品包装信息
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
            entity.setOutStockWeight(getOutStockWeight(cartonDetail.getPackageWeight(), entity.getSourceId(), entity.getBusinessCode(), entity.getBoxId()));
            entity.setWeightUnit(cartonDetail.getWeightUnit());
            entity.setBoxLength(getBoxLength(cartonDetail.getBoxLength(), entity.getSourceId(), entity.getBusinessCode(), entity.getBoxId()));
            entity.setBoxWidth(getBoxWidth(cartonDetail.getBoxWidth(), entity.getSourceId(), entity.getBusinessCode(), entity.getBoxId()));
            entity.setBoxHeight(getBoxHeight(cartonDetail.getBoxHeight(), entity.getSourceId(), entity.getBusinessCode(), entity.getBoxId()));
            entity.setBoxSizeUnit(cartonDetail.getSizeUnit());
            Optional<FirstMileDeliveryDetailEntity> deliveryDetailOptional = firstMileDeliveryDetailList.stream().filter(item -> item.getSkuId().equals(cartonDetail.getSkuId()) && (item.getFnSku().equals(cartonDetail.getFnSku()) || item.getPlatformSkuNo().equals(cartonDetail.getFnSku()) )).findFirst();
            if(deliveryDetailOptional.isPresent()){
                FirstMileDeliveryDetailEntity deliveryDetail = deliveryDetailOptional.get();
                entity.setProductName(deliveryDetail.getProductName());
                entity.setPlatformSkuNo(deliveryDetail.getPlatformSkuNo());
            }
            BigDecimal boxSize = entity.getBoxLength().multiply(entity.getBoxWidth()).multiply(entity.getBoxHeight());
            BigDecimal volumeSetting = BigDecimal.valueOf(weightAllocationDTO.getVolumeSetting());
            if(volumeSetting.compareTo(BigDecimal.ZERO) == 0){
                String format = String.format("物流渠道【%s】的材积设置不能为0", logisticsChannelEntity.getName());
                return BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getOutstockCode(), format);
            }
            BigDecimal volumeWeight = boxSize.divide(volumeSetting, 4, RoundingMode.HALF_UP);
            entity.setVolumeWeight(volumeWeight);
            //SKU体积重【体积重计算方式 长cm*宽cm*高cm/渠道的材积设置------------注意尺寸单位是cm】
            BigDecimal skuVolumeWeight = getProductVolumeWeight(cartonDetail.getSkuId(), cartonDetail.getSkuNo(), singleSkuIds, productPackList, comboSkuIds, comboSkuMap, volumeSetting);
            //重量分摊-单产品重量-取值优化-按照计费规则取值
            entity.setProductWeight(getProductWeight(cartonDetail, entity, singleSkuIds, productPackList, comboSkuIds, comboSkuMap, skuVolumeWeight));
            //出库计费重
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
        //保存
        boolean success = this.saveBatch(saveList);
        if(success){
            updateCostAllocationStatus(logisticsBillId);
        }
        return success ? BatchResultDTO.success(logisticsBillId, logisticsBillEntity.getOutstockCode()) : BatchResultDTO.fail(logisticsBillId, logisticsBillEntity.getOutstockCode(), "保存失败");
    }

    private BigDecimal getProductWeight(WmsCartonDTO.DetailDTO cartonDetail, FirstMileWeightAllocationEntity entity, List<String> singleSkuIds, List<ProductPackEntity> productPackList, List<String> comboSkuIds, Map<String, List<BomChildrenSkuDTO>> comboSkuMap, BigDecimal skuVolumeWeight) {
        //先根据调整记录检查是否存在修改的单产品重量-当前单配置查询
        FirstMileChangeRecordEntity changeRecord = firstMileChangeRecordService.getProductWeightByParams(FirstMileChangeRecordSourceTypeEnum.FIRSTMILEWEIGHT.getCode(),entity.getSourceId(),entity.getBusinessCode(),cartonDetail.getSkuId(),FirstMileChangeRecordCategoryFieldEnum.PRODUCT_WEIGHT.getCode(),entity.getId(),cartonDetail.getBoxId(), entity.getPlatformSkuNo());
        if (Objects.nonNull(changeRecord)){
            //将字符串转成BigDecimal
            if(StringUtils.isNotBlank(changeRecord.getNewValue())){
                return new BigDecimal(changeRecord.getNewValue());
            }
        }
        if(ShippingFeeRuleEnum.BILLING_WEIGHT.getCode().equals(entity.getFeeRule())){
            //取值 单产品的毛重，净重，体积重 取值最大【体积重计算方式 长cm*宽cm*高cm/渠道的材积设置------------注意尺寸单位是cm】
            //毛重
            BigDecimal grossWeight = getProductGrossWeight(cartonDetail.getSkuId(), cartonDetail.getSkuNo(), singleSkuIds, productPackList, comboSkuIds, comboSkuMap);
            //净重
            BigDecimal netWeight = getProductNetWeight(cartonDetail.getSkuId(), cartonDetail.getSkuNo(), singleSkuIds, productPackList, comboSkuIds, comboSkuMap);
            return grossWeight.max(netWeight).max(skuVolumeWeight);
        }else if (ShippingFeeRuleEnum.NET_WEIGHT.getCode().equals(entity.getFeeRule())){
            //取值实重 优先取值毛重，没有取值净重
            return getNetWeightByRule(cartonDetail.getSkuId(), cartonDetail.getSkuNo(), singleSkuIds, productPackList, entity, comboSkuIds, comboSkuMap);
        }else if(ShippingFeeRuleEnum.VOLUME_WEIGHT.getCode().equals(entity.getFeeRule())){
            //取值体积重【体积重计算方式 长cm*宽cm*高cm/渠道的材积设置------------注意尺寸单位是cm】
            return skuVolumeWeight;
        }
        return BigDecimal.ZERO;
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
        if (!CharSequenceUtil.isBlank(sourceId)) {
            this.lambdaUpdate()
                    .set(FirstMileWeightAllocationEntity::getCalculateMonth, "")
                    .eq(FirstMileWeightAllocationEntity::getSourceId, sourceId)
                    .update();
        }
    }

    @Override
    public List<FirstMileWeightAllocationDTO.ViewProductWeightDTO> viewProductWeight(FirstMileWeightAllocationDTO.ViewProductWeightParamDTO dto) {
        List<FirstMileWeightAllocationDTO.ViewProductWeightDTO> list = baseMapper.viewProductWeight(dto);
        //校验分摊数据-仅可操作未分摊数据
        for (FirstMileWeightAllocationDTO.ViewProductWeightDTO productWeightDTO : list) {
            if(!productWeightDTO.getCostAllocationStatus().equals(CostAllocationStatusEnum.NOT.getCode())){
                throw new ServiceException("仅可调整未分摊数据");
            }
        }
        //是修改出库尺寸时，查询装箱信息
        if (Objects.equals(dto.getChangeType(), FirstMileWeightChangeTypeEnum.CHANGE_OUTSTOCK_SIZE.getCode())){
            List<String> boxIds = list.stream().map(FirstMileWeightAllocationDTO.ViewProductWeightDTO::getBoxId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<WmsCartonDTO.CartonSkuDTO> cartonDetailList = wmsCartonFeign.listSkuByBoxIds(boxIds);
            Map<String, String> cartonSkuMap = cartonDetailList.stream().collect(Collectors.toMap(WmsCartonDTO.CartonSkuDTO::getBoxId, WmsCartonDTO.CartonSkuDTO::getSku));
            for (FirstMileWeightAllocationDTO.ViewProductWeightDTO productWeightDTO : list) {
                String sku = cartonSkuMap.get(productWeightDTO.getBoxId());
                productWeightDTO.setSku(sku);
            }
        }
        return list;
    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        FirstMileWeightChangeExcelListener excelListenerUtil = new FirstMileWeightChangeExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), FirstMileWeightChangeExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            //数据验证
            List<FirstMileWeightChangeExcelDTO> dataList = excelListenerUtil.getDataList();
            //错误的
            List<FirstMileWeightChangeExcelDTO> errorList = excelListenerUtil.getErrorList();
            //处理验证成功数据
            handleImportSuccessList(dataList, errorList);
            if (!errorList.isEmpty()) {
                StringBuffer sb = new StringBuffer();
                String excelPath = "excel/firstMileWeightChangeExportError.xlsx";
                String name = "头程重量分摊调整错误.xlsx";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    throw new ServiceException(ApiError.ERROR_95125);
                }
                return Boolean.FALSE;
            }
        } catch (SocketTimeoutException e) {
            log.error("导入超时错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_IMPORT_TIMEOUT);
        } catch (IOException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        return Boolean.TRUE;

    }

    @Override
    public List<FirstMileWeightAllocationEntity> listBySourceCodeList(List<String> businessCodeList, List<String> sourceCodeList, List<String> transportNoList) {
        if (CollUtil.isEmpty(businessCodeList) && CollUtil.isEmpty(sourceCodeList) && CollUtil.isEmpty(transportNoList)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().or().in(CollUtil.isNotEmpty(businessCodeList),FirstMileWeightAllocationEntity::getBusinessCode,businessCodeList)
                .or().in(CollUtil.isNotEmpty(sourceCodeList),FirstMileWeightAllocationEntity::getSourceCode,sourceCodeList)
                .or().in(CollUtil.isNotEmpty(transportNoList),FirstMileWeightAllocationEntity::getTransportNo,transportNoList).list();
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/firstMileWeightChangeTemplate.xlsx";
        String excelName = "头程重量分摊调整导入.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.DEFAULT);
        }
    }

    private void handleImportSuccessList(List<FirstMileWeightChangeExcelDTO> dataList, List<FirstMileWeightChangeExcelDTO> errorList) {
    }


}
