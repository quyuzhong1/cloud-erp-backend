package com.erp.server.wms.service.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.date.DateUtil;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReplenishBillStatusEnum;
import com.erp.model.wms.enums.ReplenishTypeEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.server.wms.mapper.WarehouseLocationReplenishMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.boot.autoconfigure.properties.SagaAsyncThreadPoolProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
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
    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

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
            dto.setUpdateTime(entity.getHandleTime());
            dto.setUpdateUserName(entity.getHandleUserName());
            //补货类型（来源类型）
            dto.setSourceTypeName(ReplenishTypeEnum.getName(entity.getSourceType()));
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
        WarehouseLocationReplenishEntity selectById = baseMapper.selectById(id);
        if(selectById.getStatus().equals(ReplenishBillStatusEnum.HANDLED.getCode())){
            return BatchResultDTO.fail(id, id, "补货单已处理完成，操作失败");
        }

        WarehouseLocationReplenishEntity entity = new WarehouseLocationReplenishEntity();
        entity.setId(id);
        entity.setStatus(ReplenishBillStatusEnum.NO_NEED_HANDLE.getCode());
        this.baseMapper.updateById(entity);

        //没有待处理的补货单之后，将发货单状态变为待处理，并清除异常原因
        WarehouseLocationReplenishEntity replenish = this.baseMapper.selectById(id);
        String sourceId = replenish.getSourceId();
        if(StringUtils.isNotBlank(sourceId)){
            List<WarehouseLocationReplenishEntity> commonSourceList = this.baseMapper.selectList(new QueryWrapper<WarehouseLocationReplenishEntity>().eq("source_id", sourceId));
            boolean allMatch = commonSourceList.stream().allMatch(item -> {
                String status = item.getStatus();
                return StringUtils.equals(ReplenishBillStatusEnum.HANDLED.getCode(), status) || StringUtils.equals(ReplenishBillStatusEnum.NO_NEED_HANDLE.getCode(), status);
            });
            if(allMatch){
                soB2cDeliveryService.update(new UpdateWrapper<SoB2cDeliveryEntity>()
                        .set("status", SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode())
                        .set("abnormal_cause", "")
                        .eq("id", sourceId));
            }
        }

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
        List<String> waitHandleIds = viewList.stream()
                .filter(item -> item.getStatus().equals(ReplenishBillStatusEnum.WAIT_HANDLE.getCode()))
                .map(WarehouseLocationReplenishDTO.ViewDTO::getId)
                .collect(Collectors.toList());
        try {
            String fileName = "仓位补货" + DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            String excelPath = "excel/warehouseLocationReplenishExport.xlsx";
            new ExcelPrintUtils().patchExport(viewList, response, fileName, excelPath);

            //只有勾选导出的才更新状态：处理中
            boolean isExportById = dto.getAdvanceQueryDTOList().stream().anyMatch(item -> item.getField().equals("id"));
            if(isExportById && !waitHandleIds.isEmpty()){
                this.update(new UpdateWrapper<WarehouseLocationReplenishEntity>()
                        .set("status", ReplenishBillStatusEnum.HANDLE_ING.getCode())
                        .in("id", waitHandleIds));
            }
        } catch (IOException e) {
            log.error("导出仓位补货清单失败：{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
                    .eq("disabled", false)
            );
            List<String> pickAreaIds = pickAreaList.stream().map(item -> item.getId()).collect(Collectors.toList());
            if(pickAreaIds.isEmpty()){
                throw new ServiceException(ApiError.ERROR_NOT_FOUND_WAREHOUSE_AREA);
            }

            //推荐补货仓位
            List<WarehouseLocationEntity> pickLocationList = warehouseLocationService.list(new QueryWrapper<WarehouseLocationEntity>()
                    .eq("warehouse_id", dto.getWarehouseId())
                    .eq("type", "location")
                    .in("parent_id", pickAreaIds)
                    .eq("is_deleted", false)
            );
            List<String> pickLocationCodeList = pickLocationList.stream().map(item -> item.getCode()).collect(Collectors.toList());
            if(pickLocationCodeList.isEmpty()){
                throw new ServiceException(ApiError.ERROR_NOT_FOUND_WAREHOUSE_LOCATION);
            }
            List<InventoryEntity> pickInventoryList = inventoryService.list(new QueryWrapper<InventoryEntity>()
                    .eq("warehouse_id", dto.getWarehouseId())
                    .eq("sku_id", dto.getSkuId())
                    .in("warehouse_location", pickLocationCodeList)
                    .eq("dict_inventory_status", "usable")
                    .orderByAsc("qty")
            );

            if(pickLocationList.isEmpty()){
                WarehouseLocationReplenishEntity replenishItem = new WarehouseLocationReplenishEntity();
                BeanMapper.copy(entity, replenishItem);
                replenishItem.setSuggestQty(dto.getQty());
                this.save(replenishItem);
                return BatchResultDTO.success(entity.getId(), dto.getSkuNo(), OperationTypeEnum.ADD);
            }

            //所有的仓位都补货
            List<WarehouseLocationReplenishEntity> replenishList = new ArrayList<>();
            for (InventoryEntity inventoryEntity : pickInventoryList) {
                WarehouseLocationReplenishEntity replenishItem = new WarehouseLocationReplenishEntity();
                BeanMapper.copy(entity, replenishItem);
                replenishItem.setToWarehouseLocation(inventoryEntity.getWarehouseLocation());

                //推荐补货库区
                WarehouseLocationEntity locationEntity = pickLocationList.stream().filter(item -> item.getCode().equals(inventoryEntity.getWarehouseLocation())).findAny().get();
                WarehouseLocationEntity pickAreaEntity = pickAreaList.stream().filter(item -> item.getId().equals(locationEntity.getParentId())).findAny().get();
                replenishItem.setToWarehouseArea(pickAreaEntity.getCode());

                Integer suggestQty = 0;
                WarehouseLocationSafetyInventoryEntity safetyInventoryEntity = safetyInventoryService.getOne(new QueryWrapper<WarehouseLocationSafetyInventoryEntity>()
                        .eq("sku_id", dto.getSkuId())
                        .eq("warehouse_id", dto.getWarehouseId())
                        .eq("warehouse_location", inventoryEntity.getWarehouseLocation())
                );
                if(safetyInventoryEntity != null){
                    //有最大补货量时：等于最大补货量+缺货数量-仓位可用库存
                    if(safetyInventoryEntity.getMaxQty() != 0){
                        suggestQty = safetyInventoryEntity.getMaxQty() + dto.getQty() - inventoryEntity.getQty();
                    }
                    //无最大补货量有安全库存时：等于安全库存+缺货数量-仓位可用库存
                    if(safetyInventoryEntity.getMaxQty() == 0 && safetyInventoryEntity.getSafetyQty() != 0){
                        suggestQty = safetyInventoryEntity.getSafetyQty() + dto.getQty() - inventoryEntity.getQty();
                    }
                    //无最大补货量无安全库存时：等于缺货数量
                    if(safetyInventoryEntity.getMaxQty() == 0 && safetyInventoryEntity.getSafetyQty() == 0){
                        suggestQty = dto.getQty();
                    }
                }else {
                    suggestQty = dto.getQty();
                }
                replenishItem.setSuggestQty(suggestQty);
                replenishList.add(replenishItem);
            }
            this.saveBatch(replenishList);
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
            );
            if(safetyInventoryEntity == null){
                return BatchResultDTO.fail(dto.getSkuId(), dto.getSkuNo(), "没有找到仓位安全库存");
            }
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
            this.save(entity);
        }

        return BatchResultDTO.success(entity.getId(), dto.getSkuNo(), OperationTypeEnum.ADD);
    }

    @Override
    public BatchResultDTO finish(WarehouseLocationReplenishDTO.HandleDTO dto) {
        LoginUser loginUser = UserContext.getNonLoginUser();
        WarehouseLocationReplenishEntity updateEntity = new WarehouseLocationReplenishEntity();
        BeanMapper.copy(dto, updateEntity);
        updateEntity.setStatus(ReplenishBillStatusEnum.HANDLED.getCode());
        updateEntity.setHandleUserId(loginUser.getUid());
        updateEntity.setHandleUserName(loginUser.getUserName());
        updateEntity.setHandleTime(LocalDateTime.now());
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
        try{
            warehouseLocationMoveService.addAndApprove(addDTO);
        }catch (Exception e){
            e.printStackTrace();
            return BatchResultDTO.fail(fullEntity.getId(), fullEntity.getSourceCode(), OperationTypeEnum.UPDATE);
        }

        //修改发货单状态，清除异常
        if(StringUtils.isNotBlank(fullEntity.getSourceId())){
            List<WarehouseLocationReplenishEntity> commonSourceList = this.baseMapper.selectList(new QueryWrapper<WarehouseLocationReplenishEntity>().eq("source_id", fullEntity.getSourceId()));
            boolean allMatch = commonSourceList.stream().allMatch(item -> {
                String status = item.getStatus();
                return StringUtils.equals(ReplenishBillStatusEnum.HANDLED.getCode(), status) || StringUtils.equals(ReplenishBillStatusEnum.NO_NEED_HANDLE.getCode(), status);
            });
            if(allMatch){
                soB2cDeliveryService.update(new UpdateWrapper<SoB2cDeliveryEntity>()
                        .set("status", SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode())
                        .set("abnormal_cause", "")
                        .eq("id", fullEntity.getSourceId()));
            }
        }

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

    @Override
    public List<BatchResultDTO> verifyReplenishQty(List<WarehouseLocationReplenishDTO.HandleDTO> dtoList) {
        List<String> ids = dtoList.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<WarehouseLocationReplenishEntity> list = listByIds(ids);
        Map<String, String> warehouseIdMap = list.stream().collect(Collectors.toMap(BaseEntity::getId, WarehouseLocationReplenishEntity::getWarehouseId));
        Map<String, String> skuIdMap = list.stream().collect(Collectors.toMap(BaseEntity::getId, WarehouseLocationReplenishEntity::getSkuId));
        Map<String, String> skuNoMap = list.stream().collect(Collectors.toMap(BaseEntity::getId, WarehouseLocationReplenishEntity::getSkuNo));

        for (WarehouseLocationReplenishDTO.HandleDTO handleDTO : dtoList) {
            String warehouseId = warehouseIdMap.get(handleDTO.getId());
            String skuId = skuIdMap.get(handleDTO.getId());
            handleDTO.setWarehouseId(warehouseId);
            handleDTO.setSkuId(skuId);
        }

        List<BatchResultDTO> verifyList = new ArrayList<>(dtoList.size());
        Map<String, List<WarehouseLocationReplenishDTO.HandleDTO>> collect = dtoList.stream().collect(Collectors.groupingBy(item -> item.getWarehouseId() + "#" + item.getFromWarehouseLocation() + "#" + item.getSkuId()));
        int i = 0;
        for (Map.Entry<String, List<WarehouseLocationReplenishDTO.HandleDTO>> entry : collect.entrySet()) {
            String[] keySplit = entry.getKey().split("#");
            String warehouseId = keySplit[0];
            String warehouseLocation = keySplit[1];
            String skuId = keySplit[2];
            List<WarehouseLocationReplenishDTO.HandleDTO> handleList = entry.getValue();
            int sum = handleList.stream().mapToInt(WarehouseLocationReplenishDTO.HandleDTO::getQty).sum();
            InventoryEntity inventory = inventoryService.getOne(new QueryWrapper<InventoryEntity>()
                    .eq("warehouse_id", warehouseId)
                    .eq("warehouse_location", warehouseLocation)
                    .eq("sku_id", skuId)
                    .eq("dict_inventory_status", "usable"));
            if(sum > inventory.getQty()){
                String format = String.format("仓位可用库存不足，可用数量：%s，总取货数量：%s", inventory.getQty(), sum);
                verifyList.add(BatchResultDTO.fail(String.valueOf(i), warehouseId + ":" + warehouseLocation + ":" + skuId, format));
            }else {
                verifyList.add(BatchResultDTO.success(String.valueOf(i), warehouseId + ":" + warehouseLocation + ":" + skuId, "成功"));
            }
            i++;
        }
        if(! verifyList.stream().allMatch(BatchResultDTO::getSuccess)){
            return verifyList;
        }

        List<BatchResultDTO> verifyResultList = new ArrayList<>(dtoList.size());
        for (WarehouseLocationReplenishDTO.HandleDTO handleDTO : dtoList) {
            String warehouseId = warehouseIdMap.get(handleDTO.getId());
            String skuId = skuIdMap.get(handleDTO.getId());
            String skuNo = skuNoMap.get(handleDTO.getId());
            InventoryEntity inventory = inventoryService.getOne(new QueryWrapper<InventoryEntity>()
                    .eq("warehouse_id", warehouseId)
                    .eq("warehouse_location", handleDTO.getFromWarehouseLocation())
                    .eq("sku_id", skuId)
                    .eq("dict_inventory_status", "usable"));
            if(handleDTO.getQty() > inventory.getQty()){
                String format = String.format("仓位可用库存不足，可用数量：%s，取货数量：%s", inventory.getQty(), handleDTO.getQty());
                verifyResultList.add(BatchResultDTO.fail(handleDTO.getId(), handleDTO.getFromWarehouseLocation() + ":" + skuNo, format));
            }else {
                verifyResultList.add(BatchResultDTO.success(handleDTO.getId(), handleDTO.getFromWarehouseLocation() + ":" + skuNo, "成功"));
            }

            handleDTO.setWarehouseId(warehouseId);
            handleDTO.setSkuId(skuId);
        }

        return verifyResultList;
    }
}
