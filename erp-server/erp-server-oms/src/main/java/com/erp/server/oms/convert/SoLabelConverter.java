package com.erp.server.oms.convert;

import com.erp.model.oms.dto.SoLabelDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 发货通知单转换类
 * @Author zdy
 * @Date 2025/02/24 18:55
 **/
@Mapper
@Component
public interface SoLabelConverter {
    SoLabelConverter INSTANCE = Mappers.getMapper(SoLabelConverter.class);

    @Mapping(target = "hasLabel", ignore = true)
    @Mapping(target = "soId", source = "sourceId")
    @Mapping(target = "soCode", source = "sourceCode")
    @Mapping(target = "logisticsLabelUrl", ignore = true)
    SoLabelDTO.PrintLabelDTO entityToPrintDTO(SoDeliveryNoticeEntity entity);
    List<SoLabelDTO.PrintLabelDTO> entityToPrintDTO(List<SoDeliveryNoticeEntity> soDeliveryNoticeEntities);
}
