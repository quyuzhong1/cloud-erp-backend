package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.wms.dto.CfgRulePickingStagingDTO;
import com.erp.model.wms.entity.CfgRulePickingStagingEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.PickingBillTypeEnum;
import com.erp.server.wms.mapper.CfgRulePickingStagingMapper;
import com.erp.server.wms.service.CfgRulePickingStagingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 拣货暂存规则 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
@Service
public class CfgRulePickingStagingServiceImpl extends SuperServiceImpl<CfgRulePickingStagingMapper, CfgRulePickingStagingEntity> implements CfgRulePickingStagingService {

    @Lazy
    @Resource
    private WarehouseService warehouseService;
    @Lazy
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Override
    public CfgRulePickingStagingEntity getByWarehouseId(String warehouseId, String billType) {
        if (CharSequenceUtil.isBlank(warehouseId) || CharSequenceUtil.isBlank(billType)) {
            return null;
        }
        return this.lambdaQuery().eq(CfgRulePickingStagingEntity::getWarehouseId,warehouseId)
                .eq(CfgRulePickingStagingEntity::getBillType, billType).last(" limit 1 ").one();
    }

    @Override
    public List<CfgRulePickingStagingDTO.StagingDTO> viewStaging() {
        List<CfgRulePickingStagingDTO.StagingDTO> list = baseMapper.viewStaging();
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<String> warehouseIds = list.stream().map(CfgRulePickingStagingDTO.StagingDTO::getWarehouseId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(warehouseIds);
        Map<String, String> warehouseMap = warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));
        List<String> warehouseLocationIdList = list.stream()
                .flatMap(obj -> Stream.of(obj.getB2bWarehouseLocationId(), obj.getFbaWarehouseLocationId(), obj.getThirdWarehouseLocationId()).filter(CharSequenceUtil::isNotBlank))
                .filter(value -> value != null && !value.isEmpty())
                .collect(Collectors.toList());
        List<WarehouseLocationEntity> locationEntityList = warehouseLocationService.listByIds(warehouseLocationIdList);
        Map<String, String> locationMap = locationEntityList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getId, WarehouseLocationEntity::getName));
        for (CfgRulePickingStagingDTO.StagingDTO obj : list) {
            obj.setWarehouseName(warehouseMap.getOrDefault(obj.getWarehouseId(), ""));
            obj.setB2bWarehouseLocationName(locationMap.getOrDefault(obj.getB2bWarehouseLocationId(), ""));
            obj.setFbaWarehouseLocationName(locationMap.getOrDefault(obj.getFbaWarehouseLocationId(), ""));
            obj.setThirdWarehouseLocationName(locationMap.getOrDefault(obj.getThirdWarehouseLocationId(), ""));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO saveStaging(CfgRulePickingStagingDTO.StagingDTO dto, String warehouseName, Map<String, WarehouseLocationEntity> locationMap) {
        WarehouseLocationEntity b2bWarehouseLocation = locationMap.getOrDefault(dto.getB2bWarehouseLocationId(), null);
        if (b2bWarehouseLocation == null) {
            throw new RuntimeException("B2B暂存库位不存在");
        }
        WarehouseLocationEntity fbaWarehouseLocation = locationMap.getOrDefault(dto.getFbaWarehouseLocationId(), null);
        if (fbaWarehouseLocation == null) {
            throw new RuntimeException("FBA暂存库位不存在");
        }
        WarehouseLocationEntity thirdWarehouseLocation = locationMap.getOrDefault(dto.getThirdWarehouseLocationId(), null);
        if (thirdWarehouseLocation == null) {
            throw new RuntimeException("第三方暂存库位不存在");
        }
        List<CfgRulePickingStagingEntity> addList = new ArrayList<>(3);
        CfgRulePickingStagingEntity b2bStagingEntity = new CfgRulePickingStagingEntity();
        b2bStagingEntity.setBillType(PickingBillTypeEnum.B2B.getCode());
        b2bStagingEntity.setWarehouseAreaId(b2bWarehouseLocation.getParentId());
        b2bStagingEntity.setWarehouseLocationId(b2bWarehouseLocation.getId());
        b2bStagingEntity.setWarehouseLocation(b2bWarehouseLocation.getName());
        b2bStagingEntity.setWarehouseId(dto.getWarehouseId());
        addList.add(b2bStagingEntity);

        CfgRulePickingStagingEntity fbaStagingEntity = new CfgRulePickingStagingEntity();
        fbaStagingEntity.setBillType(PickingBillTypeEnum.FBA.getCode());
        fbaStagingEntity.setWarehouseAreaId(fbaWarehouseLocation.getParentId());
        fbaStagingEntity.setWarehouseLocationId(fbaWarehouseLocation.getId());
        fbaStagingEntity.setWarehouseLocation(fbaWarehouseLocation.getName());
        fbaStagingEntity.setWarehouseId(dto.getWarehouseId());
        addList.add(fbaStagingEntity);

        CfgRulePickingStagingEntity thirdStagingEntity = new CfgRulePickingStagingEntity();
        thirdStagingEntity.setBillType(PickingBillTypeEnum.THIRD.getCode());
        thirdStagingEntity.setWarehouseAreaId(thirdWarehouseLocation.getParentId());
        thirdStagingEntity.setWarehouseLocationId(thirdWarehouseLocation.getId());
        thirdStagingEntity.setWarehouseLocation(thirdWarehouseLocation.getName());
        thirdStagingEntity.setWarehouseId(dto.getWarehouseId());
        addList.add(thirdStagingEntity);
        this.saveBatch(addList);
        return BatchResultDTO.success(dto.getWarehouseId(), warehouseName, "保存成功");
    }

    @Override
    public List<CfgRulePickingStagingEntity> listByWarehouseIds(List<String> warehouseIds) {
        if (CollUtil.isEmpty(warehouseIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(CfgRulePickingStagingEntity::getWarehouseId, warehouseIds).list();
    }

    @Override
    public void removeOtherWarehouse(List<String> warehouseIds) {
        if (CollUtil.isEmpty(warehouseIds)){
            List<CfgRulePickingStagingEntity> list = this.lambdaQuery().list();
            if (CollUtil.isNotEmpty(list)){
                this.removeByIds(list.stream().map(CfgRulePickingStagingEntity::getId).collect(Collectors.toList()));
            }
        }else {
            this.lambdaUpdate().notIn(CfgRulePickingStagingEntity::getWarehouseId, warehouseIds).remove();
        }
    }
}
