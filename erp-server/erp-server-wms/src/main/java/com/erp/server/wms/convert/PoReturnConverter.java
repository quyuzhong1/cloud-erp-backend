package com.erp.server.wms.convert;

import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
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
 * 采购退货单
 * @Author zdy
 * @Date 2024/09/24 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface PoReturnConverter {
    PoReturnConverter INSTANCE = Mappers.getMapper(PoReturnConverter.class);

    @Mapping(target = "qty", ignore = true)
    @Mapping(target = "amount", ignore = true)
    PurchasePriceDTO.PriceDTO priceToViewDTO(PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO viewDTO);
}
