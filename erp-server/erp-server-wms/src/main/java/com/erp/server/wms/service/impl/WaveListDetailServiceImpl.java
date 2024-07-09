package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WaveListDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.PickingListsEntity;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.model.wms.enums.PickingStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
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
    public WaveListDetailDTO.ViewDTO view(String waveId) {
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
        Map<String, String> deliveryCode2IdMap = pickingList.stream().collect(Collectors.toMap(item1 -> item1.getSourceCode(), item2 -> item2.getId()));
        List<PickingDetailEntity> pickingDetailList = pickingDetailService.list(new QueryWrapper<PickingDetailEntity>().in("main_id", deliveryCode2IdMap.values()));

        List<String> soIds = waveDetailList.stream().map(WaveListDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soDetailTotalList = soB2cFeign.listDetailByMainIds(soIds);
        String warehouseId = soDetailTotalList.get(0).getWarehouseId();
        String warehouseName = soDetailTotalList.get(0).getWarehouseName();

        List<WarehouseLocationDTO.MappingDTO> locationMappingList = warehouseLocationService.listArea2LocationMapping(warehouseId);
        Map<String, WarehouseLocationDTO.MappingDTO> locationMap = locationMappingList.stream().collect(Collectors.toMap(item -> item.getLocationCode(), item2 -> item2));

        List<WaveListDetailDTO.DeliveryInfoDTO> rowList = new ArrayList<>(waveDetailList.size());
        //发货单列表
        for (WaveListDetailEntity deliveryLevel : waveDetailList) {
            List<SoB2cDetailEntity> soDetailList = soDetailTotalList.stream().filter(item -> item.getMainId().equals(deliveryLevel.getSoId())).collect(Collectors.toList());
            List<PickingDetailEntity> pickingDetailGroup = pickingDetailList.stream().filter(item -> item.getMainId().equals(deliveryCode2IdMap.get(deliveryLevel.getDeliveryCode()))).collect(Collectors.toList());
            //发货单下sku列表
            for (SoB2cDetailEntity skuLevel : soDetailList) {
                List<PickingDetailEntity> groupBySkuPickingDetail = pickingDetailGroup.stream().filter(item -> item.getSkuId().equals(skuLevel.getSkuId())).collect(Collectors.toList());
                //sku下仓位列表
                for (PickingDetailEntity locationLevel : groupBySkuPickingDetail) {
                    WarehouseLocationDTO.MappingDTO mappingDTO = locationMap.get(locationLevel.getWarehouseLocation());
                    WaveListDetailDTO.DeliveryInfoDTO rowDTO = new WaveListDetailDTO.DeliveryInfoDTO();
                    BeanMapper.copy(deliveryLevel, rowDTO);  //拷贝基本信息：篮筐号，销售订单编号，发货单号，拣货状态，物流渠道
                    rowDTO.setSkuId(skuLevel.getSkuId());
                    rowDTO.setSkuNo(skuLevel.getSkuNo());
                    rowDTO.setSalesQty(skuLevel.getQty());
                    rowDTO.setPickedSumQty(0);
                    rowDTO.setPickingStatusName(PickingStatusEnum.getName(deliveryLevel.getPickingStatus()));

                    rowDTO.setWarehouseLocation(locationLevel.getWarehouseLocation());
                    rowDTO.setWarehouseLocationName(mappingDTO.getLocationName());
                    rowDTO.setWarehouseArea(mappingDTO.getAreaCode());
                    rowDTO.setWarehouseAreaName(mappingDTO.getAreaName());
                    rowDTO.setShouldPickQty(locationLevel.getQty());
                    rowDTO.setPickedQty(locationLevel.getPickedQty());
                    rowDTO.setIsOutStock(locationLevel.getIsOutStock());
                    rowList.add(rowDTO);
                }
            }
        }
        viewDTO.setDeliveryInfoList(rowList);
        viewDTO.setWarehouseId(warehouseId);
        viewDTO.setWarehouseName(warehouseName);

        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> moveOut(WaveListDetailDTO.MoveOutDTO moveOutDTO) {
        LoginUser user = UserContext.getNonLoginUser();
        WaveListDetailEntity entity = baseMapper.selectOne(new QueryWrapper<WaveListDetailEntity>()
                .eq("main_id", moveOutDTO.getWaveId())
                .eq("delivery_id", moveOutDTO.getDeliveryId())
        );
        if(! entity.getPickingStatus().equals(PickingStatusEnum.NOT_START.getCode())){
            return ApiResult.error("只能针对未开始的订单移出波次");
        }
        baseMapper.deleteById(entity.getId());
        List<WaveListDetailEntity> detailList = baseMapper.selectList(new QueryWrapper<WaveListDetailEntity>().eq("main_id", moveOutDTO.getWaveId()));
        if(detailList.isEmpty()){
            waveListService.getBaseMapper().deleteById(moveOutDTO.getWaveId());
        }
        deliveryService.updateStatus(Collections.singletonList(moveOutDTO.getDeliveryId()) , SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode());
        operateLogService.addModuleOperateLog(String.format("移除波次中的发货单【%s】", entity.getDeliveryCode()), ModuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode(), entity.getMainId(), "编辑操作", user.getUid(), user.getRealName());
        return ApiResult.success();
    }

    @Override
    public List<WaveListDetailEntity> listByMainIds(List<String> waveIds) {
        return list(Wrappers.<WaveListDetailEntity>lambdaQuery().in(WaveListDetailEntity::getMainId, waveIds));
    }
}
