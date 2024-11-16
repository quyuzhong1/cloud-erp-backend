package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WaveListDetailDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.PickingStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.wms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.WaveListDetailMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WaveListDetailServiceImpl extends SuperServiceImpl<WaveListDetailMapper, WaveListDetailEntity> implements WaveListDetailService {

    @Resource
    private WaveListService waveListService;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private PickingListsService pickingListsService;
    @Resource
    private PickingDetailService pickingDetailService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    @Lazy
    private SoB2cDeliveryService deliveryService;
    @Resource
    private PickingCartTypeService pickingCartTypeService;
    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Override
    public List<WaveListDetailEntity> listByMainId(String mainId) {
        return list(Wrappers.<WaveListDetailEntity>lambdaQuery().eq(WaveListDetailEntity::getMainId, mainId));
    }

    @Override
    public Map<String, String> getOrderBasketNoMap(List<String> soIds) {
        List<WaveListDetailEntity> waveListDetailEntityList =list(Wrappers.<WaveListDetailEntity>lambdaQuery().in(WaveListDetailEntity::getSoId, soIds));
        return waveListDetailEntityList.stream().collect(Collectors.toMap(WaveListDetailEntity::getSoId, WaveListDetailEntity::getBasketNo, (k1, k2)->k1));
    }

    @Override
    public WaveListDetailDTO.ViewDTO view(String waveId) throws ServiceException {
        WaveListEntity waveEntity = waveListService.getById(waveId);
        WaveListDetailDTO.ViewDTO viewDTO = new WaveListDetailDTO.ViewDTO();
        BeanMapper.copy(waveEntity, viewDTO);
        List<WaveListDetailEntity> waveDetailList = list(Wrappers.<WaveListDetailEntity>lambdaQuery().eq(WaveListDetailEntity::getMainId, waveId));

        List<String> deliveryCodes = waveDetailList.stream().map(WaveListDetailEntity::getDeliveryCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliveryCodes)) {
            throw new ServiceException("未找到发货单信息");
        }
        List<PickingListsEntity> pickingList = pickingListsService.list(new QueryWrapper<PickingListsEntity>().in("source_code", deliveryCodes));
        if(pickingList.isEmpty()){
            throw new ServiceException("没有找到拣货单");
        }
        List<String> pickingIds = pickingList.stream().map(PickingListsEntity::getId).distinct().collect(Collectors.toList());
        List<PickingDetailEntity> pickingDetailList = pickingDetailService.list(new QueryWrapper<PickingDetailEntity>().in("main_id", pickingIds));

        List<String> soIds = waveDetailList.stream().map(WaveListDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soDetailTotalList = soB2cFeign.listDetailByMainIds(soIds);
        String warehouseId = soDetailTotalList.get(0).getWarehouseId();
        String warehouseName = soDetailTotalList.get(0).getWarehouseName();
        viewDTO.setWarehouseId(warehouseId);
        viewDTO.setWarehouseName(warehouseName);
        viewDTO.setStatusName(WaveStatusEnum.getNameByCode(waveEntity.getStatus()));
        PickingCartTypeEntity cartTypeEntity = pickingCartTypeService.getById(waveEntity.getPickingCartType());
        if(cartTypeEntity != null){
            viewDTO.setPickingCartTypeName(cartTypeEntity.getName());
        }

        List<WarehouseLocationDTO.MappingDTO> locationMappingList = warehouseLocationService.listArea2LocationMapping(warehouseId);
        Map<String, WarehouseLocationDTO.MappingDTO> locationMap = locationMappingList.stream().collect(Collectors.toMap(item -> item.getLocationCode(), item2 -> item2, (o1, o2) -> o1));

        List<WaveListDetailDTO.DeliveryInfoDTO> rowList = new ArrayList<>(waveDetailList.size());
        //发货单列表
        for (WaveListDetailEntity deliveryLevel : waveDetailList) {
            List<String> pickIds = pickingList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceCode(), deliveryLevel.getDeliveryCode())).map(PickingListsEntity::getId).distinct().collect(Collectors.toList());
            List<PickingDetailEntity> pickingDetailGroup = pickingDetailList.stream().filter(item -> pickIds.contains(item.getMainId())).collect(Collectors.toList());
            Map<String, List<PickingDetailEntity>> collect = pickingDetailGroup.stream().collect(Collectors.groupingBy(item -> item.getSkuId() + "#" + item.getSkuNo()));
            //sku列表
            for (Map.Entry<String, List<PickingDetailEntity>> skuEntry : collect.entrySet()) {
                String key = skuEntry.getKey();
                String[] split = key.split("#");
                List<PickingDetailEntity> locationList = skuEntry.getValue();

                WaveListDetailDTO.DeliveryInfoDTO rowDTO = new WaveListDetailDTO.DeliveryInfoDTO();
                BeanMapper.copy(deliveryLevel, rowDTO);  //拷贝基本信息：篮筐号，销售订单编号，发货单号，拣货状态，物流渠道
                rowDTO.setSkuId(split[0]);
                rowDTO.setSkuNo(split[1]);
                int salesQty = locationList.stream().mapToInt(PickingDetailEntity::getQty).sum();
                rowDTO.setSalesQty(salesQty);
                int pickedQty = locationList.stream().mapToInt(PickingDetailEntity::getPickedQty).sum();
                rowDTO.setPickedSumQty(pickedQty);
                rowDTO.setPickingStatusName(PickingStatusEnum.getName(deliveryLevel.getPickingStatus()));
                //仓位列表
                List<WaveListDetailDTO.LocationInfoDTO> locationInfoDTOS = new ArrayList<>(locationList.size());
                for (PickingDetailEntity detail : locationList) {
                    WaveListDetailDTO.LocationInfoDTO dto = new WaveListDetailDTO.LocationInfoDTO();
                    WarehouseLocationDTO.MappingDTO mappingDTO = locationMap.get(detail.getWarehouseLocation());
                    dto.setWarehouseLocation(detail.getWarehouseLocation());
                    dto.setWarehouseLocationName(mappingDTO.getLocationName());
                    dto.setWarehouseArea(mappingDTO.getAreaCode());
                    dto.setWarehouseAreaName(mappingDTO.getAreaName());
                    dto.setShouldPickQty(detail.getQty());
                    dto.setPickedQty(detail.getPickedQty());
                    dto.setIsOutStock(detail.getIsOutStock());
                    locationInfoDTOS.add(dto);
                }
                rowDTO.setLocationInfoList(locationInfoDTOS);
                rowList.add(rowDTO);
            }
        }
        viewDTO.setDeliveryInfoList(rowList);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> moveOut(WaveListDetailDTO.MoveOutDTO moveOutDTO) {
        LoginUser user = UserContext.getNonLoginUser();

        WaveListDetailEntity entity = baseMapper.selectOne(new QueryWrapper<WaveListDetailEntity>().eq("id", moveOutDTO.getWaveId()).eq("delivery_id", moveOutDTO.getDeliveryId()));
        if(! entity.getPickingStatus().equals(PickingStatusEnum.NOT_START.getCode())){
            return ApiResult.error("只能针对未开始的订单移出波次");
        }
        String waveId = entity.getMainId();
        String deliveryId = entity.getDeliveryId();

        //发货单移出波次
        baseMapper.deleteById(entity.getId());
        //删除波次
        List<WaveListDetailEntity> detailList = baseMapper.selectList(new QueryWrapper<WaveListDetailEntity>().eq("main_id", waveId));
        if(detailList.isEmpty()){
            waveListService.getBaseMapper().deleteById(waveId);
        }
        //释放冻结库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_B2C_DELIVERY, Collections.singletonList(deliveryId));
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
        //修改发货单状态：待处理
        deliveryService.update(new UpdateWrapper<SoB2cDeliveryEntity>().set("status", SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode()).eq("id", deliveryId).notIn("status", SoB2cDeliveryStatusEnum.SHIPPED.getCode(), SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode()));
        //删除拣货单
        PickingListsEntity pickingListEntity = pickingListsService.getOne(new LambdaQueryWrapper<PickingListsEntity>().eq(PickingListsEntity::getSourceId, deliveryId));
        pickingListsService.remove(new LambdaQueryWrapper<PickingListsEntity>().eq(PickingListsEntity::getId, pickingListEntity.getId()));
        //删除拣货单明细
        pickingDetailService.remove(new LambdaQueryWrapper<PickingDetailEntity>().eq(PickingDetailEntity::getMainId, pickingListEntity.getId()));
        //记录日志
        operateLogService.addModuleOperateLog(String.format("移除波次中的发货单【%s】", entity.getDeliveryCode()), ModuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode(), waveId, "编辑操作", user.getUid(), user.getUserName());
        return ApiResult.success();
    }

    @Override
    public List<WaveListDetailEntity> listByMainIds(List<String> waveIds) {
        return list(Wrappers.<WaveListDetailEntity>lambdaQuery().in(WaveListDetailEntity::getMainId, waveIds));
    }

    /**
     * 移出波次
     *
     * @param deliveryId  发货单ID
     * @param isIntercept
     */
    @Override
    public ApiResult<?> moveOut(String deliveryId, Boolean isIntercept) {
        LoginUser user = UserContext.getNonLoginUser();
        List<WaveListDetailEntity> entityList = baseMapper.selectList(new QueryWrapper<WaveListDetailEntity>()
                .eq("delivery_id", deliveryId));
        List<String> ids = entityList.stream().map(BaseEntity::getId).distinct().collect(Collectors.toList());
        List<String> mainIds = entityList.stream().map(WaveListDetailEntity::getMainId).distinct().collect(Collectors.toList());
        if(mainIds.size() > 1){
            return ApiResult.error("该发货单关联了多个波次，请检查");
        }
        if (CollectionUtils.isEmpty(mainIds)) {
            return ApiResult.success();
        }
        //删除该明细
        baseMapper.deleteBatchIds(ids);
        //如果波次下明细为空，删除波次
        List<WaveListDetailEntity> detailList = this.list(new QueryWrapper<WaveListDetailEntity>().in("main_id", mainIds));
        if(detailList.isEmpty()){
            waveListService.getBaseMapper().delete(new QueryWrapper<WaveListEntity>().in("id", mainIds));
        }
        WaveListDetailEntity entity = entityList.stream().findFirst().orElse(new WaveListDetailEntity());
        if(isIntercept){
            operateLogService.addModuleOperateLog(String.format("移除发货单--订单发起拦截，自动取消发货单【%s】拣货波次", entity.getDeliveryCode()), ModuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode(), entity.getMainId(), "编辑操作", user.getUid(), user.getRealName());
        }else{
            operateLogService.addModuleOperateLog(String.format("移除波次中的发货单【%s】", entity.getDeliveryCode()), ModuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode(), entity.getMainId(), "编辑操作", user.getUid(), user.getRealName());
        }
        return ApiResult.success();
    }

    @Override
    public List<WaveListDetailEntity> listCancelByDeliveryIds(List<String> deliveryIds) {
        return baseMapper.listByWaveListCode(deliveryIds,WaveStatusEnum.PICK_ING.getCode(), WaveStatusEnum.HANG_UP.getCode());
    }

    @Override
    public void deleteByMainId(String mainId) {
        lambdaUpdate().eq(WaveListDetailEntity::getMainId,mainId).remove();
    }

    /**
     * 根据发货单ID获取拣货单明细
     */
    @Override
    public List<PickingDetailEntity> getPickingDetail(List<String> deliveryIds){
        List<PickingListsEntity> pickingList = pickingListsService.list(new LambdaQueryWrapper<PickingListsEntity>().in(PickingListsEntity::getSourceId, deliveryIds));
        if(pickingList.isEmpty()){
            return Collections.emptyList();
        }
        List<String> pickingIds = pickingList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        return pickingDetailService.list(new LambdaQueryWrapper<PickingDetailEntity>().in(PickingDetailEntity::getMainId, pickingIds));
    }
}
