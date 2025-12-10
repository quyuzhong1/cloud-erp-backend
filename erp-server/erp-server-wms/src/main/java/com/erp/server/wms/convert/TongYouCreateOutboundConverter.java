package com.erp.server.wms.convert;

import com.erp.model.wms.dto.third.ThirdWarehouseCreateOutboundReq;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.tongyou.dto.request.TongYouCreateOutboundReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 海外发货计划实体映射工具
 * @Author Luo_WG
 * @Date 2023/10/31 18:55
 **/
@Mapper(uses = TypeConversionWorker.class)
@Component
public interface TongYouCreateOutboundConverter {
    TongYouCreateOutboundConverter INSTANCE = Mappers.getMapper(TongYouCreateOutboundConverter.class);

    @Mappings({
            @Mapping(target = "ck_nums", source = "req.warehouseCode"),
            @Mapping(target = "deliver_no", source = "req.referenceNo"),
            @Mapping(target = "chqd", source = "req.shippingMethod"),
            @Mapping(target = "country", source = "receiverInfo.countryName"),
            @Mapping(target = "city", source = "receiverInfo.province"),
            @Mapping(target = "district", source = "receiverInfo.city"),
            @Mapping(target = "address", source = "receiverInfo.address1"),
            @Mapping(target = "address2", source = "receiverInfo.address2"),
            @Mapping(target = "contact", source = "receiverInfo.name"),
            @Mapping(target = "mobile", source = "receiverInfo.phone"),
            @Mapping(target = "phone", source = "receiverInfo.buyerNumber"),
            @Mapping(target = "email", source = "receiverInfo.email"),
            @Mapping(target = "house_number", constant = ""),
            @Mapping(target = "address_type", constant = ""),
            @Mapping(target = "qmfw", source = "req.isApiSignName"),
            @Mapping(target = "platform", constant = ""),
            @Mapping(target = "api_type", constant = ""),
            @Mapping(target = "waybill", constant = ""),
            @Mapping(target = "pda_url", source = "req.labelUrl"),
            @Mapping(target = "beizhu", constant = ""),
    })
    TongYouCreateOutboundReq outboundToThird(ThirdWarehouseCreateOutboundReq req, ThirdWarehouseCreateOutboundReq.ReceiverInfo receiverInfo);



    @Mappings({
            @Mapping(target = "sku", source = "thirdBarcode"),
            @Mapping(target = "nums", source = "quantity"),
    })
    TongYouCreateOutboundReq.AddDetailDTO outboundDetailToThird(ThirdWarehouseCreateOutboundReq.Item req);
    List<TongYouCreateOutboundReq.AddDetailDTO> outboundDetailToThird(List<ThirdWarehouseCreateOutboundReq.Item> req);
}
