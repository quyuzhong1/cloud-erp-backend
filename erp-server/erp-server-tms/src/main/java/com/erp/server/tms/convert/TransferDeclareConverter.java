package com.erp.server.tms.convert;

import com.erp.model.oms.dto.SplitSkuDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface TransferDeclareConverter {
    TransferDeclareConverter INSTANCE = Mappers.getMapper(TransferDeclareConverter.class);

    @Mappings({
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "qty", source = "qty"),
            @Mapping(target = "productTitleEn", source = "declareEnglishName"),
            @Mapping(target = "purposeDeclaredValue", source = "destDeclarePrice"),
    })
    TransferLogisticsCreateOrderReq.ProductDetail transferDeclareProductConvert(SplitSkuDTO transferDeclareProductDTO);
    List<TransferLogisticsCreateOrderReq.ProductDetail> transferDeclareProductConvert(List<SplitSkuDTO> transferDeclareProductDTOList);
}
