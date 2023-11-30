package com.erp.server.wms.convert;


import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;
import com.erp.model.wms.dto.TransferInfoDetailDTO;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * 要货申请
 */
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface RequisitionApplicationConverter {

    RequisitionApplicationConverter INSTANCE = Mappers.getMapper(RequisitionApplicationConverter.class);

    RequisitionApplicationDetailDTO.ViewDTO radEntityToRadDto(RequisitionApplicationDetailEntity entity);

    @Mappings({
            @Mapping(target = "qty", source = "approveQty"),
            @Mapping(target = "outWarehouseId", source = "fromWarehouseId"),
            @Mapping(target = "outWarehouseLocation", source = "fromWarehouseLocation"),
            @Mapping(target = "inWarehouseId", source = "toWarehouseId"),
            @Mapping(target = "inWarehouseLocation", source = "toWarehouseLocation"),
            @Mapping(target = "sourceDetailId", constant = "")
    })
    TransferInfoDetailDTO.AddDTO radHandleListToTransferInfoDetail(RequisitionApplicationDTO.HandleListDTO handleListDTO);

    @Mappings({
            @Mapping(target = "qty", source = "pickingQty"),
            @Mapping(target = "outWarehouseId", source = "pickingWarehouseId"),
            @Mapping(target = "inWarehouseId", source = "requisitionWarehouseId"),
            @Mapping(target = "inWarehouseLocation", source = "requisitionWarehouseLocation"),
            @Mapping(target = "sourceDetailId", constant = "")
    })
    TransferInfoDetailDTO.AddDTO radFinishListToTransferInfoDetail(RequisitionApplicationDTO.FinishListDTO finishListDTO);

}
