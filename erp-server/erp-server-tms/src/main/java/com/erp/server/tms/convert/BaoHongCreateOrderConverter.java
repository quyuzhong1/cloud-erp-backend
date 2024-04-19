package com.erp.server.tms.convert;

import com.common.business.mapper.BigDecimalMapperWork;
import com.common.business.mapper.BooleanMapperWork;
import com.common.business.mapper.NumberMapperWork;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.sdk.tms.baohong.api.order.CreateOrderInfo;
import com.sdk.tms.baohong.api.order.ProductDeatil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 保宏下单
 */
@Mapper(uses = {BooleanMapperWork.class, NumberMapperWork.class, BigDecimalMapperWork.class})
public interface BaoHongCreateOrderConverter {
    BaoHongCreateOrderConverter INSTANCE = Mappers.getMapper(BaoHongCreateOrderConverter.class);

    @Mappings({
            @Mapping(target = "channel", constant = "2"),
            @Mapping(target = "oabCountry", source = "logisticsOrderVO.receiverInfoVO.country"),
            @Mapping(target = "smCode", source = "logisticsOrderVO.logisticsChannelEntity.code"),
            @Mapping(target = "referenceNo", source = "logisticsOrderVO.deliveryNo"),
            @Mapping(target = "oabName", source = "logisticsOrderVO.receiverInfoVO.contact"),
            @Mapping(target = "oabCompany", source = "logisticsOrderVO.receiverInfoVO.companyName"),
            @Mapping(target = "oabState", source = "logisticsOrderVO.receiverInfoVO.district"),
            @Mapping(target = "oabCity", source = "logisticsOrderVO.receiverInfoVO.city"),
            @Mapping(target = "oabPostcode", source = "logisticsOrderVO.receiverInfoVO.zipCode"),
            @Mapping(target = "oabStreetAddress1", source = "logisticsOrderVO.receiverInfoVO.addressFirst"),
            @Mapping(target = "oabStreetAddress2", source = "logisticsOrderVO.receiverInfoVO.addressSecond"),
            @Mapping(target = "oabPhone", source = "logisticsOrderVO.receiverInfoVO.telNumber"),
            @Mapping(target = "oabEmail", source = "logisticsOrderVO.receiverInfoVO.email"),
            @Mapping(target = "deliveryAddress", source = "logisticsOrderVO.receiverInfoVO.addressFirst"),
            @Mapping(target = "orderStatus", constant = "2"),
            @Mapping(target = "iossNo", source = "logisticsOrderVO.iossCode"),
            @Mapping(target = "buyInsurance", source = "logisticsOrderVO.logisticsChannelEntity.isApiInsurance", qualifiedByName = "boolToInteger"),
            @Mapping(target = "tradeMode", constant = "1210"),
            @Mapping(target = "grossWeight", source = "logisticsOrderVO.parceInfoVO.totalWeight", qualifiedByName = "intToStr")
    })
    CreateOrderInfo LogisticsOrderVOToCreateOrderInfo(LogisticsOrderVO logisticsOrderVO);

    @Mappings({
            @Mapping(target = "productSku", source = "logisticsProductVO.skuNo"),
            @Mapping(target = "productTitleEn", source = "logisticsProductVO.declareEnglishName"),
            @Mapping(target = "opQuantity", source = "logisticsProductVO.quantity"),
            @Mapping(target = "purposeDeclaredValue", source = "logisticsProductVO.destDeclarePrice", qualifiedByName = "bigDecimalToStr"),
    })
    ProductDeatil LogisticsProductVOToProductDeatil(LogisticsProductVO logisticsProductVO);
    List<ProductDeatil> LogisticsProductVOToProductDeatil(List<LogisticsProductVO> logisticsProductVO);
}
