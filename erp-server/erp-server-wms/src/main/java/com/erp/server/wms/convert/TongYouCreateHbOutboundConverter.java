package com.erp.server.wms.convert;

import com.erp.model.wms.dto.third.ThirdWarehouseCreateFbaOutboundReq;
import com.erp.server.wms.convert.tool.TypeConversionWorker;
import com.sdk.wms.tongyou.dto.request.TongYouCreateHbOutboundReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(uses = TypeConversionWorker.class)
@Component
public interface TongYouCreateHbOutboundConverter {

    TongYouCreateHbOutboundConverter INSTANCE = Mappers.getMapper(TongYouCreateHbOutboundConverter.class);

    @Mapping(target = "token", ignore = true)
    @Mapping(target = "ck_nums", source = "mappingWarehouseCode")
    @Mapping(target = "deliver_no", source = "referenceNo")
    @Mapping(target = "chqd", source = "channelCode")
    @Mapping(target = "country", source = "countryName")
    @Mapping(target = "city", source = "province")
    @Mapping(target = "district", source = "city")
    @Mapping(target = "zip", source = "postCode")
    @Mapping(target = "address", source = "address1")
    @Mapping(target = "address2", constant = "")
    @Mapping(target = "contact", source = "receiverName")
    @Mapping(target = "mobile", source = "telNumber")
    @Mapping(target = "phone", source = "telNumber")
    @Mapping(target = "file1", source = "productLabelFileUrl")
    @Mapping(target = "file2", source = "outerBoxLabelFileUrl")
    @Mapping(target = "file3", source = "orderAttachmentFileUrl")
    @Mapping(target = "beizhu", source = "remark")
    @Mapping(target = "is_hb", ignore = true)
    @Mapping(target = "is_hz", ignore = true)
    @Mapping(target = "deliver_products", source = "items")
    TongYouCreateHbOutboundReq toHbOutboundReq(ThirdWarehouseCreateFbaOutboundReq req);

    @Mapping(target = "nums", source = "deliveryQty")
    @Mapping(target = "sku", source = "warehousePlatformSku")
    @Mapping(target = "sku_news", source = "relabelSku")
    TongYouCreateHbOutboundReq.DeliverProductDTO toHbProduct(ThirdWarehouseCreateFbaOutboundReq.Item item);

    List<TongYouCreateHbOutboundReq.DeliverProductDTO> toHbProductList(List<ThirdWarehouseCreateFbaOutboundReq.Item> items);
}
