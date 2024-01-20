package com.erp.server.srm.convert;

import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper()
public interface DeliveryOrderConverter {
    DeliveryOrderConverter INSTANCE = Mappers.getMapper(DeliveryOrderConverter.class);

    @Mapping(target = "detailId", source = "id")
    @Mapping(target = "purchaseQty", source = "orderQty")
    DeliveryOrderDetailDTO.ViewDTO detailViewConvert(DeliveryOrderDetailEntity detail);
    List<DeliveryOrderDetailDTO.ViewDTO> detailViewConvert(List<DeliveryOrderDetailEntity> details);

    @Mapping(target = "detailList", source = "detailList")
    @Mapping(target = "receiptStatusName", expression = "java(com.common.core.constant.EnumMessage.getNameByCode(com.erp.model.srm.enums.DeliveryOrderEnum.ReceiptStatusEnum.class,entity.getReceiptStatus()))")
    DeliveryOrderDTO.ViewDTO viewConvert(DeliveryOrderEntity entity,List<DeliveryOrderDetailEntity> detailList);
}
