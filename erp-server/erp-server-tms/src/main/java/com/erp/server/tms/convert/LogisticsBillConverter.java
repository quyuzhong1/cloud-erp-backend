package com.erp.server.tms.convert;

import com.common.business.mapper.BigDecimalToIntMapperWork;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.erp.model.tms.vo.request.ParceInfoVO;
import com.erp.model.tms.vo.request.ReceiverInfoVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 *物流单转化
 *@author yl
 *@date 2023-11-23
 */
@Mapper(uses = {BigDecimalToIntMapperWork.class},builder = @Builder(disableBuilder = true))
public interface LogisticsBillConverter {

    LogisticsBillConverter INSTANCE = Mappers.getMapper(LogisticsBillConverter.class);

    @Mappings({
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "actId", source = "customerId"),
            @Mapping(target = "contact", source = "receiverName"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "telNumber", source = "receiverTelNumber"),
            @Mapping(target = "country", source = "country"),
            @Mapping(target = "province", source = "provinceName"),
            @Mapping(target = "city", source = "cityName"),
            @Mapping(target = "district", source = "districtName"),
            @Mapping(target = "streetAddress", source = "fullAddress"),
            @Mapping(target = "addressFirst", source = "firstAddress"),
            @Mapping(target = "addressSecond", source = "secondAddress"),
            @Mapping(target = "zipCode", source = "postCode"),
            @Mapping(target = "receiverTaxNo", source = "receiverTaxNo")
    })
    ReceiverInfoVO convertReceiver(LogisticsBillDTO.ReceiverDTO  receiver);

    @Mappings({
            @Mapping(target = "totalWeight", source = "weight" , qualifiedByName="bigDecimalToInt"),
            @Mapping(target = "length", source = "length" , qualifiedByName="bigDecimalToInt"),
            @Mapping(target = "width", source = "width", qualifiedByName="bigDecimalToInt"),
            @Mapping(target = "height", source = "height", qualifiedByName="bigDecimalToInt"),
            @Mapping(target = "currency", source = "currency"),
    })
    ParceInfoVO convertParceInfo(LogisticsBillDTO.PackageDTO packageDTO);


    @Mappings({
            @Mapping(target = "childOrderId", source = "childOrderId" ),
//            @Mapping(target = "price", source = "price" ),
            @Mapping(target = "quantity", source = "quantity"),
            @Mapping(target = "isElectric", source = "isElectric"),
            @Mapping(target = "weight", source = "weight"),
            @Mapping(target = "skuId", source = "skuId"),
            @Mapping(target = "skuNo", source = "skuNo"),
            @Mapping(target = "productProperty", source = "productProperty"),
            @Mapping(target = "productPropertyId", source = "productPropertyId"),
            @Mapping(target = "declareModel", source = "declareModel"),
            @Mapping(target = "declareChineseName", source = "declareChineseName"),
            @Mapping(target = "declareEnglishName", source = "declareEnglishName"),
            @Mapping(target = "declarePrice", source = "declarePrice"),
            @Mapping(target = "customsCode", source = "customsCode"),
            @Mapping(target = "declareUnit", source = "declareUnit"),
            @Mapping(target = "declareElement", source = "declareElement"),
            @Mapping(target = "englishMaterial", source = "englishMaterial"),
            @Mapping(target = "englishUsage", source = "englishUsage"),
            @Mapping(target = "declareCurrency", source = "declareCurrency"),
            @Mapping(target = "declareCurrencySymbol", source = "declareCurrencySymbol"),
            @Mapping(target = "destDeclarePrice", source = "destDeclarePrice"),
            @Mapping(target = "destCurrency", source = "destCurrency"),
            @Mapping(target = "destCurrencySymbol", source = "destCurrencySymbol"),
            @Mapping(target = "exemption", source = "exemption"),
            @Mapping(target = "sourceCargo", source = "sourceCargo"),
            @Mapping(target = "sourceCountry", source = "sourceCountry"),
            @Mapping(target = "combinationDeclareType", source = "combinationDeclareType"),
    })
    LogisticsProductVO convertLogisticsProduct( LogisticsProductDTO.ProductDTO sku);
    List<LogisticsProductVO> convertLogisticsProduct( List<LogisticsProductDTO.ProductDTO> skuList);
}
