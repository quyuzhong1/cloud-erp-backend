package com.erp.server.wms.convert;

import com.erp.model.wms.dto.inventory.InOutStockCoreDTO;
import com.erp.model.wms.dto.inventory.InventoryBaseInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * 出入库实体映射工具
 **/
@Mapper
@Component
public interface InOutStockCoreConverter {

    InOutStockCoreConverter INSTANCE = Mappers.getMapper(InOutStockCoreConverter.class);

    InOutStockCoreDTO copyInOutStockCoreDTO(InOutStockCoreDTO zeroInventoryParam);

    InOutStockCoreDTO baseToInOutStockCoreDTO(InventoryBaseInfoDTO param);
}
