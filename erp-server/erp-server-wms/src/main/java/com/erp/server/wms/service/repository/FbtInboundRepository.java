package com.erp.server.wms.service.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.wms.entity.OverseasInventoryEntity;
import com.erp.model.wms.enums.ShipmentSourceTypeEnum;
import com.erp.server.wms.mapper.FbaShipmentDetailMapper;
import com.erp.server.wms.mapper.FbaShipmentMapper;
import com.erp.server.wms.mapper.FbaShipmentReceiveMapper;
import com.erp.server.wms.mapper.OverseasInventoryMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class FbtInboundRepository {

    @Resource
    private FbaShipmentMapper fbaShipmentMapper;
    @Resource
    private FbaShipmentDetailMapper fbaShipmentDetailMapper;
    @Resource
    private FbaShipmentReceiveMapper fbaShipmentReceiveMapper;
    @Resource
    private OverseasInventoryMapper overseasInventoryMapper;

    public FbaShipmentEntity findShipmentByInboundOrderId(String inboundOrderId) {
        return fbaShipmentMapper.selectOne(Wrappers.<FbaShipmentEntity>lambdaQuery()
                .eq(FbaShipmentEntity::getFbaShipmentId, inboundOrderId)
                .eq(FbaShipmentEntity::getSourceType, ShipmentSourceTypeEnum.FBT.getCode())
                .last("limit 1"));
    }

    public void saveShipment(FbaShipmentEntity entity) {
        fbaShipmentMapper.insert(entity);
    }

    public void updateShipment(FbaShipmentEntity entity) {
        fbaShipmentMapper.updateById(entity);
    }

    public List<FbaShipmentDetailEntity> listShipmentDetails(String mainId) {
        return fbaShipmentDetailMapper.selectList(Wrappers.<FbaShipmentDetailEntity>lambdaQuery()
                .eq(FbaShipmentDetailEntity::getMainId, mainId));
    }

    public void saveShipmentDetail(FbaShipmentDetailEntity entity) {
        fbaShipmentDetailMapper.insert(entity);
    }

    public void updateShipmentDetail(FbaShipmentDetailEntity entity) {
        fbaShipmentDetailMapper.updateById(entity);
    }

    public boolean existsInventoryRecord(String recordId) {
        Integer count = fbaShipmentReceiveMapper.selectCount(Wrappers.<FbaShipmentReceiveEntity>lambdaQuery()
                .eq(FbaShipmentReceiveEntity::getUniqueMd5, recordId));
        return count != null && count > 0;
    }

    public void saveInventoryRecord(FbaShipmentReceiveEntity entity) {
        fbaShipmentReceiveMapper.insert(entity);
    }

    public OverseasInventoryEntity findOverseasInventory(String warehouseCode, String platformSku) {
        return findOverseasInventory(warehouseCode, platformSku, null);
    }

    public OverseasInventoryEntity findOverseasInventory(String warehouseCode, String platformSku, String overseasProviderId) {
        LambdaQueryWrapper<OverseasInventoryEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(OverseasInventoryEntity::getWarehouseCode, warehouseCode)
                .eq(OverseasInventoryEntity::getPlatformSku, platformSku);
        if (overseasProviderId == null) {
            wrapper.and(w -> w.isNull(OverseasInventoryEntity::getOverseasProviderId)
                    .or()
                    .eq(OverseasInventoryEntity::getOverseasProviderId, ""));
        } else {
            wrapper.eq(OverseasInventoryEntity::getOverseasProviderId, overseasProviderId);
        }
        wrapper.last("limit 1");
        return overseasInventoryMapper.selectOne(wrapper);
    }

    public void saveOverseasInventory(OverseasInventoryEntity entity) {
        overseasInventoryMapper.insert(entity);
    }

    public void updateOverseasInventory(OverseasInventoryEntity entity) {
        overseasInventoryMapper.updateById(entity);
    }
}
