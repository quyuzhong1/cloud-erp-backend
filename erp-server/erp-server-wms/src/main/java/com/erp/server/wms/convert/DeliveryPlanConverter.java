package com.erp.server.wms.convert;

import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.WmsDeliveryPlanDetailEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 海外发货计划实体映射工具
 * @Author Luo_WG
 * @Date 2023/10/31 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface DeliveryPlanConverter {
    DeliveryPlanConverter INSTANCE = Mappers.getMapper(DeliveryPlanConverter.class);

    @Mappings({
            @Mapping(target = "toWarehouseName", constant = ""),
            @Mapping(target = "fromWarehouseId", constant = ""),
            @Mapping(target = "fromWarehouseName", constant = ""),
            @Mapping(target = "handleUserId", constant = ""),
            @Mapping(target = "detailList", ignore = true),
            @Mapping(target = "handleUserName", constant = ""),
            @Mapping(target = "requisitionWarehouseName", constant = ""),
    })
    RequisitionApplicationDTO.AddDTO DeliveryPlanGRA(WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO dto);



    @Mappings({
            @Mapping(target = "approveQty", ignore = true),
            @Mapping(target = "platformFnSku", source = "fnSku"),
            @Mapping(target = "pickingQty", ignore = true)
    })
    RequisitionApplicationDetailDTO.AddDTO DeliveryPlanDetailGRA(WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO dto);

    @Mappings({
            @Mapping(target = "destWarehouseId", source = "toWarehouseId"),
            @Mapping(target = "destWarehouseName", source = "toWarehouseName"),
            @Mapping(target = "countryId", source = "country")
    })
    FirstMileDeliveryDTO.AddDTO generateDeliverFDD(WmsDeliveryPlanDTO.GenerateDeliverViewDTO dto);

    @Mappings({
            @Mapping(target = "platformSkuNo", source = "platformSku")
    })
    FirstMileDeliveryDetailDTO.AddDTO generateDeliverDetailFDD(WmsDeliveryPlanDTO.GenerateDeliverViewDTO dto);

    @Mappings({
            @Mapping(target = "platformSpu", source = "asin"),
            @Mapping(target = "platformFnSku", source = "fnSku"),
    })
    WmsDeliveryPlanDetailEntity generateDeliveryDetailAdd(WmsDeliveryPlanDetailDTO.AddDTO dto);
    List<WmsDeliveryPlanDetailEntity> generateDeliveryDetailAdd(List<WmsDeliveryPlanDetailDTO.AddDTO> list);

    @Mappings({
            @Mapping(target = "platformSpu", source = "asin"),
            @Mapping(target = "platformFnSku", source = "fnSku"),
    })
    WmsDeliveryPlanDetailEntity generateDeliveryDetailUpdate(WmsDeliveryPlanDetailDTO.UpdateDTO dto);
    List<WmsDeliveryPlanDetailEntity> generateDeliveryDetailUpdate(List<WmsDeliveryPlanDetailDTO.UpdateDTO> list);
}
