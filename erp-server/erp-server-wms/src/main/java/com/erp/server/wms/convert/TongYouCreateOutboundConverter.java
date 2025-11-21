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
            @Mapping(target = "ck_nums", source = "warehouseCode"),
            @Mapping(target = "deliver_no", source = "referenceNo"),
            @Mapping(target = "chqd", source = ""),
            @Mapping(target = "country", source = ""),
            @Mapping(target = "city", source = ""),
            @Mapping(target = "zip", source = "receiverInfo.zip"),
            @Mapping(target = "address", source = "receiverInfo.address"),
            @Mapping(target = "address2", source = "receiverInfo.address2"),
            @Mapping(target = "contact", source = "receiverInfo.contact"),
            @Mapping(target = "mobile", source = "receiverInfo.mobile"),
            @Mapping(target = "phone", source = ""),
            @Mapping(target = "email", source = ""),
            @Mapping(target = "house_number", source = ""),
            @Mapping(target = "address_type", source = ""),
            @Mapping(target = "qmfw", source = ""),
            @Mapping(target = "platform", source = ""),
            @Mapping(target = "api_type", source = ""),
            @Mapping(target = "waybill", source = ""),
            @Mapping(target = "pda_url", source = ""),
            @Mapping(target = "beizhu", source = ""),
    })
    TongYouCreateOutboundReq InboundToThird(ThirdWarehouseCreateOutboundReq req, ThirdWarehouseCreateOutboundReq.ReceiverInfo receiverInfo);



    @Mappings({
            @Mapping(target = "sku", source = "productSku"),
            @Mapping(target = "nums", source = "quantity"),
    })
    TongYouCreateOutboundReq.AddDetailDTO InboundDetailToThird(ThirdWarehouseCreateOutboundReq.Item req);
    List<TongYouCreateOutboundReq.AddDetailDTO> InboundDetailToThird(List<ThirdWarehouseCreateOutboundReq.Item> req);
}
