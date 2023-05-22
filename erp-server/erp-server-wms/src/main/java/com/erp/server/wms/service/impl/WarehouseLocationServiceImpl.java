package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationStatusEnum;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.server.wms.mapper.WarehouseLocationMapper;
import com.erp.server.wms.service.WarehouseLocationService;
import com.common.business.service.SuperServiceImpl;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 仓库仓位表 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
@Service
public class WarehouseLocationServiceImpl extends SuperServiceImpl<WarehouseLocationMapper, WarehouseLocationEntity> implements WarehouseLocationService {

    @Override
    public List<WarehouseLocationDTO.LocationListDTO> select(String warehouseId) {
        // 根据仓库查询仓位
        List<WarehouseLocationEntity> warehouseLocationList =  lambdaQuery().eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
                .eq(WarehouseLocationEntity::getType, WarehouseLocationTypeEnum.LOCATION.getCode()).list();
        if(CollUtil.isEmpty(warehouseLocationList)) {
            return Lists.newArrayList();
        }
        List<WarehouseLocationDTO.LocationListDTO> dataList = Lists.newArrayListWithExpectedSize(warehouseLocationList.size());
        warehouseLocationList.stream().forEach(warehouseLocation->{
            WarehouseLocationDTO.LocationListDTO data = new WarehouseLocationDTO.LocationListDTO();
            data.setId(warehouseLocation.getId());
            data.setCode(warehouseLocation.getCode());
            data.setName(warehouseLocation.getName());
            data.setStatus(warehouseLocation.getStatus());
            WarehouseLocationStatusEnum warehouseLocationStatus = WarehouseLocationStatusEnum.of(data.getStatus());
            data.setStatusName(WarehouseLocationStatusEnum.getName(data.getStatus()));
            data.setCanCheck(Boolean.TRUE);
            if(Objects.equals(warehouseLocation.getDisabled(), Boolean.TRUE) || Objects.equals(warehouseLocationStatus, WarehouseLocationStatusEnum.STOP)) {
                data.setCanCheck(Boolean.FALSE);
            }
            dataList.add(data);
        });
        return dataList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void quoteLocation(List<String> ids) {
        if (CollUtil.isNotEmpty(ids)) {
            this.lambdaUpdate().in(WarehouseLocationEntity::getId, ids).
                    set(WarehouseLocationEntity::getOccupyStatus, Boolean.TRUE).update();
        }
    }

    @Override
    public WarehouseLocationDTO.LocationDetailDTO findById(String id) {
        WarehouseLocationEntity warehouseLocation = super.getById(id);
        Optional.ofNullable(warehouseLocation).orElseThrow(()->new ServiceException("仓位信息不存在"));

        WarehouseLocationTypeEnum warehouseLocationType = WarehouseLocationTypeEnum.of(warehouseLocation.getType());

        WarehouseLocationDTO.LocationDetailDTO detailDTO = new WarehouseLocationDTO.LocationDetailDTO();
        detailDTO.setType(warehouseLocation.getType());
        detailDTO.setTypeName(WarehouseLocationTypeEnum.getName(warehouseLocation.getType()));
        detailDTO.setCode(warehouseLocation.getCode());
        detailDTO.setName(warehouseLocation.getName());
        detailDTO.setWarehouseId(warehouseLocation.getWarehouseId());
        detailDTO.setDisabled(warehouseLocation.getDisabled());
        detailDTO.setRemark(warehouseLocation.getRemark());
        detailDTO.setStatus(warehouseLocation.getStatus());
        if(Objects.equals(warehouseLocationType, WarehouseLocationTypeEnum.LOCATION)) {
            detailDTO.setStatusName(WarehouseLocationStatusEnum.getName(warehouseLocation.getStatus()));

            // 查询分区信息
            WarehouseLocationEntity warehouseArea = super.getById(warehouseLocation.getParentId());
            if(Objects.nonNull(warehouseArea)) {
                detailDTO.setAreaId(warehouseArea.getId());
                detailDTO.setAreaType(warehouseArea.getCode());
                detailDTO.setAreaName(warehouseArea.getName());
            }
        }
        return detailDTO;
    }


}
