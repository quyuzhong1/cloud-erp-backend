package com.erp.server.wms.convert;

import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.erp.server.wms.service.FbaDeliveryService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 海外发货计划实体映射工具
 * @Author Luo_WG
 * @Date 2023/10/31 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface OverseasDeliveryPlanConverter {
    OverseasDeliveryPlanConverter INSTANCE = Mappers.getMapper(OverseasDeliveryPlanConverter.class);

    @Mappings({
            @Mapping(target = "toWarehouseId", constant = ""),
            @Mapping(target = "toWarehouseName", constant = ""),
            @Mapping(target = "fromWarehouseId", constant = ""),
            @Mapping(target = "fromWarehouseName", constant = ""),
            @Mapping(target = "handleUserId", constant = ""),
            @Mapping(target = "detailList", ignore = true),
            @Mapping(target = "handleTime", constant = ""),
            @Mapping(target = "handleUserName", constant = ""),
            @Mapping(target = "requisitionWarehouseName", constant = ""),
            @Mapping(target = "status", constant = "")
    })
    RequisitionApplicationDTO.AddDTO DeliveryPlanGRA(OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO dto);



    @Mappings({
            @Mapping(target = "approveQty", ignore = true),
            @Mapping(target = "pickingQty", ignore = true)
    })
    RequisitionApplicationDetailDTO.AddDTO DeliveryPlanDetailGRA(OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO dto);

    @Mappings({
            @Mapping(target = "destWarehouseId", source = "toWarehouseId"),
            @Mapping(target = "destWarehouseName", source = "toWarehouseName")
    })
    FbaDeliveryDTO.AddDTO generateDeliverFDD(OverseasDeliveryPlanDTO.GenerateDeliverViewDTO dto);

    FbaDeliveryDetailDTO.AddDTO generateDeliverDetailFDD(OverseasDeliveryPlanDTO.GenerateDeliverViewDTO dto);
}
