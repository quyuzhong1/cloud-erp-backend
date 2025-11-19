package com.erp.server.wms.convert;

import com.erp.model.wms.dto.third.ThirdWarehouseCreateInboundReq;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.tongyou.dto.request.TongYouCreateInboundReq;
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
public interface TongYouCreateInboundConverter {
    TongYouCreateInboundConverter INSTANCE = Mappers.getMapper(TongYouCreateInboundConverter.class);

    @Mappings({
            @Mapping(target = "waybill", source = "receivingCode"),
            @Mapping(target = "mdck", source = "warehouseCode"),
            @Mapping(target = "beizhu", source = "remark"),
            @Mapping(target = "country", source = "countryName"),
    })
    TongYouCreateInboundReq.AddDTO InboundToThird(ThirdWarehouseCreateInboundReq req);



    @Mappings({
            @Mapping(target = "ck_sku", source = "productSku"),
            @Mapping(target = "nums", source = "quantity"),
            @Mapping(target = "zxh", source = "boxNo"),
            @Mapping(target = "weight", source = "packageWeight"),
            @Mapping(target = "cc", source = "boxLength"),
            @Mapping(target = "kk", source = "boxWidth"),
            @Mapping(target = "gg", source = "boxHeight"),
    })
    TongYouCreateInboundReq.AddDetailDTO InboundDetailToThird(ThirdWarehouseCreateInboundReq.Item req);
    List<TongYouCreateInboundReq.AddDetailDTO> InboundDetailToThird(List<ThirdWarehouseCreateInboundReq.Item> req);
}
