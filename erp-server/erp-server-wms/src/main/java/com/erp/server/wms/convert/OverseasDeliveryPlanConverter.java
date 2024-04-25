package com.erp.server.wms.convert;

import com.erp.model.wms.dto.*;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

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
            @Mapping(target = "handleUserName", constant = ""),
            @Mapping(target = "requisitionWarehouseName", constant = ""),
    })
    RequisitionApplicationDTO.AddDTO DeliveryPlanGRA(OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO dto);



    @Mappings({
            @Mapping(target = "approveQty", ignore = true),
            @Mapping(target = "pickingQty", ignore = true)
    })
    RequisitionApplicationDetailDTO.AddDTO DeliveryPlanDetailGRA(OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO dto);

    @Mappings({
            @Mapping(target = "destWarehouseId", source = "toWarehouseId"),
            @Mapping(target = "destWarehouseName", source = "toWarehouseName"),
            @Mapping(target = "countryId", source = "country")
    })
    FirstMileDeliveryDTO.AddDTO generateDeliverFDD(OverseasDeliveryPlanDTO.GenerateDeliverViewDTO dto);

    @Mappings({
            @Mapping(target = "platformSkuNo", source = "platformSku")
    })
    FirstMileDeliveryDetailDTO.AddDTO generateDeliverDetailFDD(OverseasDeliveryPlanDTO.GenerateDeliverViewDTO dto);
}
