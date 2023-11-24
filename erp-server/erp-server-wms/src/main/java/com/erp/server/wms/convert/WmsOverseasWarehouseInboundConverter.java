package com.erp.server.wms.convert;

import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundReceivedEntity;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * 海外仓入库api相关
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface WmsOverseasWarehouseInboundConverter {

    WmsOverseasWarehouseInboundConverter INSTANCE = Mappers.getMapper(WmsOverseasWarehouseInboundConverter.class);

    @Mappings({
            @Mapping(target = "receiveUser", source = "receiveUser"),
            @Mapping(target = "receiveQty", source = "receiveQty"),
            @Mapping(target = "receiveTime", source = "receiveTime"),
    })
    OverseasWarehouseInboundDTO.ReceiveRecordView receivedEntityToView(OverseasWarehouseInboundReceivedEntity entity);

}
