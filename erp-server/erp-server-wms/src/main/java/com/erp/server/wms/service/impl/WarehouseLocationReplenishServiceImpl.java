package com.erp.server.wms.service.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReplenishBillStatusEnum;
import com.erp.model.wms.enums.ReplenishTypeEnum;
import com.erp.server.wms.mapper.WarehouseLocationReplenishMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.boot.autoconfigure.properties.SagaAsyncThreadPoolProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 仓位库存预警服务类
 * @date 2024-06-24
 * @author tanmujin
 */
@Service
public class WarehouseLocationReplenishServiceImpl extends SuperServiceImpl<WarehouseLocationReplenishMapper, WarehouseLocationReplenishEntity> implements WarehouseLocationReplenishService {

    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private WarehouseLocationSafetyInventoryService safetyInventoryService;
    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;

    @Override
    public PagingVO<WarehouseLocationReplenishDTO.ViewDTO> paging(PagingDTO<WarehouseLocationReplenishDTO.SearchParamDTO> pagingDTO) {
        Page<Object> page = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<WarehouseLocationReplenishEntity> pageResult =  this.baseMapper.paging(page, pagingDTO.getParams());
        List<WarehouseLocationReplenishDTO.ViewDTO> viewList = fillViewList(pageResult.getRecords());
        return new PagingVO<>(viewList, (int)pageResult.getTotal(), (int)pageResult.getPages(), (int)pageResult.getCurrent());
    }

    /**
     * 填充字段
     * @date: 2024-06-24
     * @author: tanmujin
     */
    private List<WarehouseLocationReplenishDTO.ViewDTO> fillViewList(List<WarehouseLocationReplenishEntity> entityList) {
        if(entityList.isEmpty()){
            return Collections.emptyList();
        }

        List<WarehouseLocationReplenishDTO.ViewDTO> dtoList = new ArrayList<>(entityList.size());

        //仓库信息
        List<String> warehouseIds = entityList.stream().map(item -> item.getWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.getBaseMapper().selectBatchIds(warehouseIds);
        Map<String, String> warehouseIdNameMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        //库区信息、仓位信息
        List<WarehouseLocationEntity> list = warehouseLocationService.getBaseMapper().selectList(new QueryWrapper<WarehouseLocationEntity>()
                .in("warehouse_id", warehouseIds)
                .eq("is_deleted", false)
        );

        for (WarehouseLocationReplenishEntity entity : entityList) {
            WarehouseLocationReplenishDTO.ViewDTO dto = new WarehouseLocationReplenishDTO.ViewDTO();
            BeanMapper.copy(entity, dto);

            //仓库名称
            dto.setWarehouseName(warehouseIdNameMap.get(entity.getWarehouseId()));

            //取货库区名称
            WarehouseLocationEntity fromAreaEntity = list.stream()
                    .filter(item -> item.getType().equals("area") && item.getCode().equals(entity.getFromWarehouseArea()))
                    .findFirst().orElse(new WarehouseLocationEntity());
            dto.setFromWarehouseAreaName(fromAreaEntity.getName());

            //取货仓位名称
            WarehouseLocationEntity fromLocationEntity = list.stream()
                    .filter(item -> item.getType().equals("location") && item.getCode().equals(entity.getFromWarehouseLocation()))
                    .findFirst().orElse(new WarehouseLocationEntity());
            dto.setFromWarehouseLocationName(fromLocationEntity.getName());

            //补货库区名称
            WarehouseLocationEntity toAreaEntity = list.stream()
                    .filter(item -> item.getType().equals("area") && item.getCode().equals(entity.getToWarehouseArea()))
                    .findFirst().orElse(new WarehouseLocationEntity());
            dto.setToWarehouseAreaName(toAreaEntity.getName());

            //补货仓位名称
            WarehouseLocationEntity toLocationEntity = list.stream()
                    .filter(item -> item.getType().equals("location") && item.getCode().equals(entity.getToWarehouseLocation()))
                    .findFirst().orElse(new WarehouseLocationEntity());
            dto.setToWarehouseLocationName(toLocationEntity.getName());

            //处理状态
            dto.setStatusName(ReplenishBillStatusEnum.getNameByCode(dto.getStatus()));

            dtoList.add(dto);
        }

        return dtoList;
    }

    @Override
    public BatchResultDTO cancelHandle(String id) {
        WarehouseLocationReplenishEntity entity = new WarehouseLocationReplenishEntity();
        entity.setId(id);
        entity.setStatus(ReplenishBillStatusEnum.NO_NEED_HANDLE.getCode());
        this.baseMapper.updateById(entity);

        WarehouseLocationReplenishEntity replenishEntity = this.baseMapper.selectById(id);
        String code = replenishEntity.getSkuNo() + " : " + replenishEntity.getToWarehouseLocation();
        return BatchResultDTO.success(id, code, OperationTypeEnum.UPDATE);
    }

    @Override
    public Boolean exportExcel(WarehouseLocationReplenishDTO.ExportParamDTO dto, HttpServletResponse response) {
        WarehouseLocationReplenishDTO.SearchParamDTO searchParamDto = new WarehouseLocationReplenishDTO.SearchParamDTO();
        BeanMapper.copy(dto, searchParamDto);
        List<WarehouseLocationReplenishEntity> entityList = this.baseMapper.listByParam(searchParamDto);
        List<WarehouseLocationReplenishDTO.ViewDTO> viewList = fillViewList(entityList);
        List<String> ids = viewList.stream().map(item -> item.getId()).distinct().collect(Collectors.toList());
        try {
            String fileName = "仓位补货" + DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            String excelPath = "excel/warehouseLocationReplenishExport.xlsx";
            new ExcelPrintUtils().patchExport(viewList, response, fileName, excelPath);

            //更新状态：处理中
            WarehouseLocationReplenishEntity updateEntity = new WarehouseLocationReplenishEntity();
            updateEntity.setStatus(ReplenishBillStatusEnum.HANDLE_ING.getCode());
            this.baseMapper.update(updateEntity, new QueryWrapper<WarehouseLocationReplenishEntity>().in("id", ids));
        } catch (IOException e) {
            log.error("导出仓位补货清单失败：{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public BatchResultDTO handle(WarehouseLocationReplenishDTO.HandleDTO handleDTO) {
        WarehouseLocationReplenishEntity entity = this.baseMapper.selectById(handleDTO.getId());

        WarehouseLocationReplenishEntity updateEntity = new WarehouseLocationReplenishEntity();
        BeanMapper.copy(handleDTO, updateEntity);
        updateEntity.setId(entity.getId());
        updateEntity.setStatus(ReplenishBillStatusEnum.HANDLED.getCode());
        this.baseMapper.updateById(updateEntity);

        return BatchResultDTO.success(updateEntity.getId(), updateEntity.getSkuNo() + " : " + handleDTO.getFromWarehouseLocation(), OperationTypeEnum.ADD);
    }

    @Override
    public BatchResultDTO add(WarehouseLocationReplenishDTO.AddDTO dto) {
        WarehouseLocationReplenishEntity entity = new WarehouseLocationReplenishEntity();
        entity.setSkuId(dto.getSkuId());
        entity.setSkuNo(dto.getSkuNo());
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(dto.getSourceCode());
        entity.setSourceType(dto.getSourceType().getCode());
        entity.setWarehouseId(dto.getWarehouseId());
        entity.setStatus(ReplenishBillStatusEnum.WAIT_HANDLE.getCode());

        //发货缺货补货
        if(dto.getSourceType().equals(ReplenishTypeEnum.DELIVER_STOCK_OUT)){
            //推荐取货库区和取货仓位
            Pair<WarehouseLocationEntity, InventoryEntity> pair = getFromAreaAndLocation(dto);
            entity.setFromWarehouseArea(pair.getKey().getCode());
            entity.setFromWarehouseLocation(pair.getValue().getWarehouseLocation());

            //推荐补货库区
            List<WarehouseLocationEntity> pickAreaList = warehouseLocationService.list(new QueryWrapper<WarehouseLocationEntity>()
                    .eq("warehouse_id", dto.getWarehouseId())
                    .eq("type", "area")
                    .eq("area_type", "pickingArea")
                    .eq("is_deleted", false)
            );
            List<String> pickAreaIds = pickAreaList.stream().map(item -> item.getId()).collect(Collectors.toList());

            //推荐补货仓位
            List<WarehouseLocationEntity> pickLocationList = warehouseLocationService.list(new QueryWrapper<WarehouseLocationEntity>()
                    .eq("warehouse_id", dto.getWarehouseId())
                    .eq("type", "location")
                    .in("parent_id", pickAreaIds)
                    .eq("is_deleted", false)
            );
            List<String> pickLocationCodeList = pickLocationList.stream().map(item -> item.getCode()).collect(Collectors.toList());
            List<InventoryEntity> pickInventoryList = inventoryService.list(new QueryWrapper<InventoryEntity>()
                    .eq("warehouse_id", dto.getWarehouseId())
                    .eq("sku_id", dto.getSkuId())
                    .in("warehouse_location", pickLocationCodeList)
                    .eq("dict_inventory_status", "usable")
                    .orderByAsc("qty")
            );
            InventoryEntity minQtyInventoryEntity = pickInventoryList.get(0);
            entity.setToWarehouseLocation(minQtyInventoryEntity.getWarehouseLocation());

            //推荐补货库区
            WarehouseLocationEntity locationEntity = pickLocationList.stream().filter(item -> item.getCode().equals(minQtyInventoryEntity.getWarehouseLocation())).findAny().get();
            WarehouseLocationEntity pickAreaEntity = pickAreaList.stream().filter(item -> item.getId().equals(locationEntity.getParentId())).findAny().get();
            entity.setToWarehouseArea(pickAreaEntity.getCode());

            Integer suggestQty = 0;
            WarehouseLocationSafetyInventoryEntity safetyInventoryEntity = safetyInventoryService.getOne(new QueryWrapper<WarehouseLocationSafetyInventoryEntity>()
                    .eq("sku_id", dto.getSkuId())
                    .eq("warehouse_id", dto.getWarehouseId())
                    .eq("warehouse_location", minQtyInventoryEntity.getWarehouseLocation())
            );
            //有最大补货量时：等于最大补货量+缺货数量-仓位可用库存
            if(safetyInventoryEntity.getMaxQty() != 0){
                suggestQty = safetyInventoryEntity.getMaxQty() + dto.getQty() - minQtyInventoryEntity.getQty();
            }
            //无最大补货量有安全库存时：等于安全库存+缺货数量-仓位可用库存
            if(safetyInventoryEntity.getMaxQty() == 0 && safetyInventoryEntity.getSafetyQty() != 0){
                suggestQty = safetyInventoryEntity.getSafetyQty() + dto.getQty() - minQtyInventoryEntity.getQty();
            }
            //无最大补货量无安全库存时：等于缺货数量
            if(safetyInventoryEntity.getMaxQty() == 0 && safetyInventoryEntity.getSafetyQty() == 0){
                suggestQty = dto.getQty();
            }
            entity.setSuggestQty(suggestQty);
        }

        //安全库存补货
        if(dto.getSourceType().equals(ReplenishTypeEnum.SAFETY_INVENTORY)){
            Pair<WarehouseLocationEntity, InventoryEntity> pair = getFromAreaAndLocation(dto);
            entity.setFromWarehouseArea(pair.getKey().getCode());
            entity.setFromWarehouseLocation(pair.getValue().getWarehouseLocation());

            entity.setToWarehouseArea(dto.getWarehouseArea());
            entity.setToWarehouseLocation(dto.getWarehouseLocation());

            Integer suggestQty = 0;
            WarehouseLocationSafetyInventoryEntity safetyInventoryEntity = safetyInventoryService.getOne(new QueryWrapper<WarehouseLocationSafetyInventoryEntity>()
                    .eq("sku_id", dto.getSkuId())
                    .eq("warehouse_id", dto.getWarehouseId())
                    .eq("warehouse_location", dto.getWarehouseLocation())
                    .eq("is_deleted", false)
            );
            InventoryEntity inventoryEntity = inventoryService.getOne(new QueryWrapper<InventoryEntity>()
                    .eq("warehouse_id", dto.getWarehouseId())
                    .eq("sku_id", dto.getSkuId())
                    .eq("warehouse_location", dto.getWarehouseLocation())
                    .eq("dict_inventory_status", "usable")
                    .eq("is_deleted", false)
            );
            //有设置补货上限量时：等于补货上限量-当前可用库存
            if(safetyInventoryEntity.getMaxQty() != 0){
                suggestQty = safetyInventoryEntity.getMaxQty() - inventoryEntity.getQty();
            }
            //没有设置补货上限量时：等于安全库存-当前可用库存
            if(safetyInventoryEntity.getMaxQty() == 0 && safetyInventoryEntity.getSafetyQty() != 0){
                suggestQty = safetyInventoryEntity.getSafetyQty() - inventoryEntity.getQty();
            }
            //没有设置补货触发量时，不参与补货计算
            if(safetyInventoryEntity.getMaxQty() == 0 && safetyInventoryEntity.getSafetyQty() == 0){

            }
            entity.setSuggestQty(suggestQty);
        }

        this.baseMapper.insert(entity);
        return BatchResultDTO.success(entity.getId(), dto.getSkuNo(), OperationTypeEnum.ADD);
    }

    @Override
    public BatchResultDTO finish(WarehouseLocationReplenishDTO.HandleDTO dto) {
        WarehouseLocationReplenishEntity updateEntity = new WarehouseLocationReplenishEntity();
        BeanMapper.copy(dto, updateEntity);
        updateEntity.setStatus(ReplenishBillStatusEnum.HANDLED.getCode());
        this.baseMapper.updateById(updateEntity);

        //生成仓位移动，并自动审核通过
        WarehouseLocationReplenishEntity fullEntity = this.baseMapper.selectById(dto.getId());
        WarehouseLocationMoveDetailDTO.AddDTO moveDetail = new WarehouseLocationMoveDetailDTO.AddDTO();
        moveDetail.setMainId(fullEntity.getId());
        moveDetail.setSkuId(fullEntity.getSkuId());
        moveDetail.setSkuNo(fullEntity.getSkuNo());
        moveDetail.setWarehouseId(fullEntity.getWarehouseId());
        moveDetail.setQty(fullEntity.getQty());
        moveDetail.setOutWarehouseLocation(fullEntity.getFromWarehouseLocation());
        moveDetail.setInWarehouseLocation(fullEntity.getToWarehouseLocation());
        moveDetail.setRemark("仓位补货自动生成");

        WarehouseLocationMoveDTO.AddDTO addDTO = new WarehouseLocationMoveDTO.AddDTO();
        addDTO.setWarehouseId(fullEntity.getWarehouseId());
        addDTO.setPcShow(Boolean.TRUE);
        addDTO.setDetailList(Collections.singletonList(moveDetail));
        warehouseLocationMoveService.addAndApprove(addDTO);

        return BatchResultDTO.success(fullEntity.getId(), fullEntity.getSourceCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public List<WarehouseLocationReplenishDTO.TabDTO> listTabInfo() {
        List<WarehouseLocationReplenishDTO.TabDTO> list = this.baseMapper.listTabInfo();
        if(list.size() == 4){
            return list;
        }
        List<String> tabFlagList = list.stream().map(item -> item.getTabFlag()).collect(Collectors.toList());
        if(! tabFlagList.contains(ReplenishBillStatusEnum.WAIT_HANDLE.getCode())){
            list.add(new WarehouseLocationReplenishDTO.TabDTO(ReplenishBillStatusEnum.WAIT_HANDLE.getCode(), 0));
        }
        if(! tabFlagList.contains(ReplenishBillStatusEnum.HANDLE_ING.getCode())){
            list.add(new WarehouseLocationReplenishDTO.TabDTO(ReplenishBillStatusEnum.HANDLE_ING.getCode(), 0));
        }
        if(! tabFlagList.contains(ReplenishBillStatusEnum.HANDLED.getCode())){
            list.add(new WarehouseLocationReplenishDTO.TabDTO(ReplenishBillStatusEnum.HANDLED.getCode(), 0));
        }
        if(! tabFlagList.contains(ReplenishBillStatusEnum.NO_NEED_HANDLE.getCode())){
            list.add(new WarehouseLocationReplenishDTO.TabDTO(ReplenishBillStatusEnum.NO_NEED_HANDLE.getCode(), 0));
        }
        return list;
    }

    /**
     * 获取推荐取货库区，推荐取货仓位
     * @param dto
     * @return key：推荐取货库区，value：推荐取货仓位
     * @date: 2024-06-27
     * @author: tanmujin
     */
    private Pair<WarehouseLocationEntity, InventoryEntity> getFromAreaAndLocation(WarehouseLocationReplenishDTO.AddDTO dto){
        //查找仓库下的备货区
        List<WarehouseLocationEntity> stockAreaList = warehouseLocationService.list(new QueryWrapper<WarehouseLocationEntity>()
                .eq("warehouse_id", dto.getWarehouseId())
                .eq("type", "area")
                .eq("area_type", "stockingArea")
                .eq("is_deleted", false)
        );
        List<String> stockAreaId = stockAreaList.stream().map(item -> item.getId()).distinct().collect(Collectors.toList());
        if(stockAreaId.isEmpty()){
            log.error(String.format("没有找到库区：%s", dto));
            return new Pair<>(new WarehouseLocationEntity(), new InventoryEntity());
        }

        //查找备货区下的所有仓位
        List<WarehouseLocationEntity> stockLocationList = warehouseLocationService.list(new QueryWrapper<WarehouseLocationEntity>()
                .eq("warehouse_id", dto.getWarehouseId())
                .eq("type", "location")
                .in("parent_id", stockAreaId)
                .eq("is_deleted", false)
        );
        List<String> stockLocationCodeList = stockLocationList.stream().map(item -> item.getCode()).distinct().collect(Collectors.toList());
        if(stockLocationCodeList.isEmpty()){
            log.error(String.format("没有找到仓位：%s", dto));
            return new Pair<>(new WarehouseLocationEntity(), new InventoryEntity());
        }
        //查找仓库下，备货区，sku的仓位库存
        List<InventoryEntity> stockInventoryList = inventoryService.list(new QueryWrapper<InventoryEntity>()
                .eq("warehouse_id", dto.getWarehouseId())
                .eq("sku_id", dto.getSkuId())
                .in("warehouse_location", stockLocationCodeList)
                .eq("dict_inventory_status", "usable")
                .orderByDesc("qty")
        );
        //取可用库存最多的一个
        if(stockInventoryList.isEmpty()){
            log.error(String.format("没有找到可用库存：%s", dto));
            return new Pair<>(new WarehouseLocationEntity(), new InventoryEntity());
        }
        InventoryEntity maxQtyInventoryEntity = stockInventoryList.get(0);

        //根据最大库存仓位找库区
        WarehouseLocationEntity locationEntity = stockLocationList.stream().filter(item -> StringUtils.equals(item.getCode(), maxQtyInventoryEntity.getWarehouseLocation())).findFirst().get();
        WarehouseLocationEntity areaEntity = stockAreaList.stream().filter(item -> StringUtils.equals(item.getId(), locationEntity.getParentId())).findFirst().get();

        return new Pair<>(areaEntity, maxQtyInventoryEntity);
    }

    @Override
    public List<WarehouseLocationReplenishDTO.LocationQtyDTO> listLocationQty(List<WarehouseLocationReplenishDTO.LocationQtyDTO> paramlist) {
        if(CollectionUtils.isEmpty(paramlist)){
            return Collections.emptyList();
        }
        List<WarehouseLocationReplenishDTO.LocationQtyDTO> resultList = new ArrayList<>();
        for (WarehouseLocationReplenishDTO.LocationQtyDTO param : paramlist) {
            List<WarehouseLocationEntity> locationList = warehouseLocationService.listLocation(param.getWarehouseId(), param.getWarehouseArea());
            List<String> locationCodeList = locationList.stream().map(item -> item.getCode()).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(locationCodeList)){
                WarehouseLocationReplenishDTO.LocationQtyDTO locationQtyDTO = new WarehouseLocationReplenishDTO.LocationQtyDTO();
                locationQtyDTO.setWarehouseId(param.getWarehouseId());
                locationQtyDTO.setWarehouseArea(param.getWarehouseArea());
                locationQtyDTO.setSkuId(param.getSkuId());
                locationQtyDTO.setLocationQtyList(Collections.emptyList());
                resultList.add(locationQtyDTO);
                continue;
            }
            List<InventoryEntity> inventoryList = inventoryService.getBaseMapper().selectList(new QueryWrapper<InventoryEntity>()
                    .eq("warehouse_id", param.getWarehouseId())
                    .eq("sku_id", param.getSkuId())
                    .eq("dict_inventory_status", "usable")
                    .in("warehouse_location", locationCodeList));
            List<WarehouseLocationReplenishDTO.LocationQtyDetailDTO> detailList = new ArrayList<>();
            WarehouseLocationReplenishDTO.LocationQtyDTO locationQtyDTO = new WarehouseLocationReplenishDTO.LocationQtyDTO();
            locationQtyDTO.setWarehouseId(param.getWarehouseId());
            locationQtyDTO.setWarehouseArea(param.getWarehouseArea());
            locationQtyDTO.setSkuId(param.getSkuId());
            inventoryList.forEach(item -> detailList.add(new WarehouseLocationReplenishDTO.LocationQtyDetailDTO(item.getWarehouseLocation(), item.getQty())));
            locationQtyDTO.setLocationQtyList(detailList);
            resultList.add(locationQtyDTO);
        }

        return resultList;
    }
}
