package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.tms.dto.CfgSettingDTO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.model.tms.enums.CostAllocationStatusEnum;
import com.erp.model.tms.enums.ShippingFeeRuleEnum;
import com.erp.model.tms.enums.WeightAllocationEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.rpc.plm.feign.ProductPackFeign;
import com.erp.rpc.wms.feign.PackingTaskFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.tms.mapper.FirstMileWeightAllocationMapper;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.FirstMileCostAllocationService;
import com.erp.server.tms.service.FirstMileWeightAllocationService;
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
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.enums.ApiError;

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
    private WmsWarehouseFeign warehouseFeign;

    @Override
    public BatchResultDTO add(FirstMileWeightAllocationDTO.AddDTO addDTO) {
        Integer count = this.lambdaQuery().eq(FirstMileWeightAllocationEntity::getLogisticsBillId, addDTO.getLogisticsBillId()).count();
        if(count > 0){
            log.error("已生成头程重量分摊，不能再次生成：{}", addDTO);
            return BatchResultDTO.fail(addDTO.getLogisticsBillId(), addDTO.getBusinessCode(), "已生成头程重量分摊，不能再次生成");
        }
        List<FirstMileWeightAllocationEntity> entityList = handleData(addDTO);
        boolean save = super.saveBatch(entityList);
        if(!save) {
            log.error("保存头程重量分摊失败：{}", addDTO);
            BatchResultDTO.fail(addDTO.getLogisticsBillId(), addDTO.getBusinessCode(), "保存头程重量分摊失败");
        }
        return BatchResultDTO.success(addDTO.getLogisticsBillId(), addDTO.getBusinessCode());
    }

    /**
     * 处理新增数据
     */
    private List<FirstMileWeightAllocationEntity> handleData(FirstMileWeightAllocationDTO.AddDTO dto) {
        List<FirstMileWeightAllocationEntity> list = new ArrayList<>(dto.getPackingDTOList().size());
        //装箱信息
        List<String> taskIds = dto.getPackingDTOList().stream().map(item -> item.getTaskId()).distinct().collect(Collectors.toList());
        List<WmsCartonSpecDTO.WmsCartonSpecView> cartonSpecViewList = packingTaskFeign.listCartonSpecByTaskIds(taskIds);
        Map<String, WmsCartonSpecDTO.WmsCartonSpecView> cartonSepcViewMap = cartonSpecViewList.stream().collect(Collectors.toMap(item -> item.getTaskId(), item2 -> item2));
        //sku明细
        List<String> skuIds = dto.getPackingDTOList().stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailList = productDetailFeign.listByIds(skuIds);
        Map<String, ProductDetailEntity> productDetailMap = productDetailList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2));
        //sku包装信息
        List<ProductPackEntity> productPackList = productPackFeign.listBySkuIds(skuIds);
        Map<String, ProductPackEntity> productPackMap = productPackList.stream().collect(Collectors.toMap(item -> item.getSkuId(), item2 -> item2));
        //系统配置
        CfgSettingDTO.ViewDTO cfgSettingView = cfgSettingService.view();
        String weightFirstAllocation = cfgSettingView.getAllocationSettingDTO().getWeightFirstAllocation();
        for (WmsCartonDetailDTO.ListPackingDetailDTO packingDTO : dto.getPackingDTOList()) {
            FirstMileWeightAllocationEntity entity = new FirstMileWeightAllocationEntity();
            entity.setLogisticsBillId(dto.getLogisticsBillId());
            entity.setSourceId(dto.getSourceId());
            entity.setSourceCode(dto.getSourceCode());
            entity.setBusinessCode(dto.getBusinessCode());
            entity.setTransportNo(dto.getTransportNo());
            entity.setSupplierId(dto.getSupplierId());
            entity.setSupplierName(dto.getSupplierName());
            entity.setShopId(dto.getShopId());
            entity.setShopName(dto.getShopName());
            entity.setToCountry(dto.getToCountry());
            entity.setFromWarehouseId(dto.getFromWarehouseId());
            if(WeightAllocationEnum.OUTSTOCK_CHARGED_WEIGHT.getCode().equals(weightFirstAllocation)){
                if(ShippingFeeRuleEnum.BILLING_WEIGHT.getCode().equals(dto.getFeeRule())){
                    entity.setAllocationType("按照{出库计费重}分摊");
                }
                if(ShippingFeeRuleEnum.NET_WEIGHT.getCode().equals(dto.getFeeRule())){
                    entity.setAllocationType("按照{出库实重}分摊");
                }
                if(ShippingFeeRuleEnum.VOLUME_WEIGHT.getCode().equals(dto.getFeeRule())){
                    entity.setAllocationType("按照{出库体积重}分摊");
                }
            }
            if(WeightAllocationEnum.SINGLE_PRODUCT_WEIGHT.getCode().equals(weightFirstAllocation)){
                entity.setAllocationType("按单产品重量分摊");
            }
            entity.setFeeRule(dto.getFeeRule());
//            entity.setCalculatePeriodId();  //联表查询
//            entity.setCalculateMonth(); //联表查询
//            entity.setAllocationStatus();   //联表查询
            entity.setBoxId(packingDTO.getId());
            entity.setBoxNo(packingDTO.getBoxNo());
            entity.setBoxLength(packingDTO.getLength());
            entity.setBoxWidth(packingDTO.getWidth());
            entity.setBoxHeight(packingDTO.getHeight());
            entity.setBoxSizeUnit(packingDTO.getSizeUnit());
            if(cartonSepcViewMap.containsKey(packingDTO.getTaskId())){
                WmsCartonSpecDTO.WmsCartonSpecView cartonSpecView = cartonSepcViewMap.get(packingDTO.getTaskId());
                WmsCartonSpecDTO.ViewDTO specView = cartonSpecView.getWmsCartonList().stream().filter(item -> StringUtils.compare(String.valueOf(item.getBoxNo()), packingDTO.getBoxNo()) == 0).findFirst().orElseGet(null);
                if(specView != null){
                    entity.setOutStockWeight(specView.getPackageWeight());
                    entity.setWeightUnit(specView.getWeightUnit());
                    WmsCartonDetailDTO.ViewDTO cartonDetailView = specView.getDetailList().stream().filter(item -> item.getSkuId().equals(packingDTO.getSkuId())).findFirst().orElseGet(null);
                    entity.setDeliveryQty(cartonDetailView != null ? cartonDetailView.getDeliveryQty() : 0);
                }
            }
            entity.setSkuId(packingDTO.getSkuId());
            entity.setSkuNo(packingDTO.getSkuNo());
//            entity.setPlatformSkuId();
//            entity.setPlatformSkuNo();
            if(productDetailMap.containsKey(packingDTO.getSkuId())){
                ProductDetailEntity productDetail = productDetailMap.get(packingDTO.getSkuId());
                entity.setProductName(productDetail.getName());
            }
            if(productPackMap.containsKey(packingDTO.getSkuId())){
                ProductPackEntity productPack = productPackMap.get(packingDTO.getSkuId());
                BigDecimal productWeight = BigDecimal.ZERO;
                if(productPack.getGrossWeight() != null){
                    productWeight = productPack.getGrossWeight();
                } else if (productPack.getNetWeight() != null) {
                    productWeight = productPack.getNetWeight();
                }
                entity.setProductWeight(productWeight.divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP));  //单位换算为KG
            }
            entity.setVolumeWeight(packingDTO.getLength().multiply(packingDTO.getWidth()).multiply(packingDTO.getHeight()).divide(BigDecimal.valueOf(dto.getVolumeSetting()), RoundingMode.HALF_UP));
            entity.setChargedWeight(entity.getOutStockWeight().max(entity.getVolumeWeight()));
            BigDecimal allocationWeight = computeAllocationWeight();
            entity.setAllocationWeight(allocationWeight);
            entity.setWeightUnit(packingDTO.getWeightUnit());
            list.add(entity);
        }
        return list;
    }

    /**
     * 计算分摊重量
     */
    private BigDecimal computeAllocationWeight() {
        return null;
    }

    @Override
    public PagingVO<FirstMileWeightAllocationDTO.ViewDTO> paging(PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto) {
        Page<?> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<FirstMileWeightAllocationDTO.ViewDTO> pageData = baseMapper.paging(query, dto.getParams());
        fillData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillData(List<FirstMileWeightAllocationDTO.ViewDTO> records) {
        List<String> warehouseIds = records.stream().map(item -> item.getFromWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseList = warehouseFeign.listByIds(warehouseIds);
        Map<String, WarehouseDTO.ListDTO> warehouseMap = warehouseList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2));
        for (FirstMileWeightAllocationDTO.ViewDTO item : records) {
            item.setAllocationStatusName(CostAllocationStatusEnum.getName(item.getAllocationStatus()));
            if(warehouseMap.containsKey(item.getFromWarehouseId())){
                item.setFromWarehouseName(warehouseMap.get(item.getFromWarehouseId()).getName());
            }
        }
    }

    @Override
    public void exportExcel(FirstMileWeightAllocationDTO.ExportParamDTO dto, HttpServletResponse response) {
        List<FirstMileWeightAllocationDTO.ViewDTO> list;
        if(! dto.getIds().isEmpty()){
            list = baseMapper.listByParamIds(dto.getIds());
        }else {
            list = baseMapper.listByParam(dto);
        }
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
        list.add(getTabCount(CostAllocationStatusEnum.ALREADY));
        list.add(getTabCount(CostAllocationStatusEnum.PART));
        list.add(getTabCount(CostAllocationStatusEnum.NOT));
        return list;
    }

    private FirstMileWeightAllocationDTO.TabDTO getTabCount(CostAllocationStatusEnum statusEnum) {
        int count = baseMapper.tabCount(statusEnum.getCode());
        return new FirstMileWeightAllocationDTO.TabDTO(statusEnum.getCode(), statusEnum.getName(), count);
    }

    @Override
    public BaseResultDTO.AddDTO pushCostAllocation(FirstMileWeightAllocationDTO.PushCostAllocationDTO dto) {
        FirstMileCostAllocationDTO.AddDTO costDto = new FirstMileCostAllocationDTO.AddDTO();
        //todo 收集参数
        return costAllocationService.add(costDto);
    }

    @Override
    public BatchResultDTO weightReCompute(String logisticsBillId) {

        return null;
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
}
