package com.erp.server.tms.convert;

import com.erp.model.oms.dto.TransferDeclareProductDTO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.entity.TransferDeclareProductEntity;
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
            @Mapping(target = "purposeDeclaredValue", source = "toDeclarePrice"),
    })
    TransferLogisticsCreateOrderReq.ProductDetail declareProductEntityToCreateOrderReq(TransferDeclareProductEntity declareProductEntity);
    List<TransferLogisticsCreateOrderReq.ProductDetail> declareProductEntityToCreateOrderReq(List<TransferDeclareProductEntity> declareProductEntity);

    @Mappings({
            @Mapping(target = "price", source = "destDeclarePrice"),
            @Mapping(target = "declarePrice", source = "declarePrice"),
            @Mapping(target = "quantity", source = "qty"),
            @Mapping(target = "grossWeight", source = "grossWeight"),
            @Mapping(target = "weight", source = "weight"),
            @Mapping(target = "declareCurrency", source = "currency"),
            @Mapping(target = "amount", ignore = true),
            @Mapping(target = "childOrderId", ignore = true),
            @Mapping(target = "destCurrencySymbol", source = "currencySymbol")
    })
    LogisticsProductDTO.ProductDTO omsProductToTmsProduct(TransferDeclareProductDTO transferDeclareProductDTO);

    @Mappings({
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "qty", source = "qty"),
            @Mapping(target = "productTitleEn", source = "declareEnglishName"),
            @Mapping(target = "purposeDeclaredValue", source = "destDeclarePrice"),
    })
    TransferLogisticsCreateOrderReq.ProductDetail transferDeclareProductConvert(TransferDeclareProductDTO transferDeclareProductDTO);
    List<TransferLogisticsCreateOrderReq.ProductDetail> transferDeclareProductConvert(List<TransferDeclareProductDTO> transferDeclareProductDTOList);
}
