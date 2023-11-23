package com.erp.server.wms.convert;


import com.common.business.dto.PlatformWarehouseDTO;
import com.erp.model.wms.dto.RequisitionApplicationDetailDTO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
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
}
