package com.erp.server.wms.convert.impl;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.dto.FbaDeliveryDetailDTO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.FbaShipmentDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.server.wms.convert.FbaShipmentConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FbaShipmentConverterImpl implements FbaShipmentConverter {
    @Override
    public FbaShipmentDTO.ViewDTO fbaShipmentToViewDTO(FbaShipmentEntity shipmentEntity) {
        if (shipmentEntity == null) {
            return null;
        }
        FbaShipmentDTO.ViewDTO viewDTO = new FbaShipmentDTO.ViewDTO();
        return viewDTO;
    }

    @Override
    public FbaShipmentDetailDTO.ViewDTO fbaShipmentDetailToViewDTO(FbaShipmentDetailEntity detailEntity) {
        if (detailEntity == null) {
            return null;
        }
        FbaShipmentDetailDTO.ViewDTO viewDTO = new FbaShipmentDetailDTO.ViewDTO();
        return viewDTO;
    }

    @Override
    public FbaShipmentDTO.ReceiveRecordView fbaShipmentReceiveEntityToView(FbaShipmentReceiveEntity entities) {
        if (entities == null) {
            return null;
        }
        FbaShipmentDTO.ReceiveRecordView viewDTO = new FbaShipmentDTO.ReceiveRecordView();
        return viewDTO;
    }

    @Override
    public FbaShipmentDTO.ShipmentStatusRecordView fbaShipmentStatusEntityToView(FbaShipmentStatusEntity entities) {
        if (entities == null) {
            return null;
        }
        FbaShipmentDTO.ShipmentStatusRecordView viewDTO = new FbaShipmentDTO.ShipmentStatusRecordView();
        return viewDTO;
    }

    @Override
    public FbaDeliveryDTO.AddDTO fbaGenerateDeliverViewToDeliveryAdd(FbaShipmentDTO.GenerateDeliverView view, List<WarehouseEntity> warehouseEntities, List<BaseIdDTO.CodeDTO> accountingCompanyList) {
        if (view == null) {
            return null;
        }

        FbaDeliveryDTO.AddDTO viewDTO = new FbaDeliveryDTO.AddDTO();
        viewDTO.setSourceType(SourceTypeEnum.FBA_SHIPMENT.getCode());
        viewDTO.setDemandType(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode());

        //设置仓库名称
        String destWarehouseName = warehouseEntities.stream().filter(req -> req.getId().equals(view.getDestWarehouseId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        viewDTO.setDestWarehouseName(destWarehouseName);
        WarehouseEntity warehouseEntity = warehouseEntities.stream().filter(req -> req.getId().equals(view.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseEntity());
        viewDTO.setDestWarehouseName(warehouseEntity.getName());

        //设置库存组织
        String orgName = accountingCompanyList.stream().filter(d -> d.getId().equals(warehouseEntity.getOrgId())).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        viewDTO.setInventoryOrgId(warehouseEntity.getOrgId());
        viewDTO.setInventoryOrgName(orgName);
        return viewDTO;
    }

    @Override
    public FbaDeliveryDetailDTO.AddDTO fbaGenerateDeliverViewToDeliveryDetailAdd(FbaShipmentDTO.GenerateDeliverView view) {
        return null;
    }
}
