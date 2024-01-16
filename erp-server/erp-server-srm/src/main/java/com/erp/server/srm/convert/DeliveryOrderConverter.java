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
    DeliveryOrderDetailDTO.ViewDTO detailViewConvert(DeliveryOrderDetailEntity detail);
    List<DeliveryOrderDetailDTO.ViewDTO> detailViewConvert(List<DeliveryOrderDetailEntity> details);

    @Mapping(target = "detailList", source = "detailList")
    DeliveryOrderDTO.ViewDTO viewConvert(DeliveryOrderEntity entity,List<DeliveryOrderDetailEntity> detailList);
}
