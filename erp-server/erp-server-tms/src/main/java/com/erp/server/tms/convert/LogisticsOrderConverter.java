package com.erp.server.tms.convert;

import com.common.business.mapper.BooleanMapper;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.sdk.tms.disifang.model.order.request.OrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * @author zdy
 * @ClassName LogisticsOrderConverter
 * @description: 物流订单转换类
 * @date 2023年11月06日
 * @version: 1.0
 */
@Mapper(uses = BooleanMapper.class , builder = @Builder(disableBuilder = true))
public interface LogisticsOrderConverter {

    LogisticsOrderConverter INSTANCE = Mappers.getMapper(LogisticsOrderConverter.class);

    OrderRequest orderRequestToDsf(LogisticsOrderVO logisticsOrderVO);

    @Mappings({
            @Mapping(target = "channelId" ,source = "channelId"),
            @Mapping(target = "orderSource" ,source = "channelId"),
            @Mapping(target = "orderNumber" ,source = "deliveryNo"),
            @Mapping(target = "receiverInfo.name",source = "receiverInfoVO.name"),
            @Mapping(target = "receiverInfo.country",source = "receiverInfoVO.country"),
            @Mapping(target = "receiverInfo.address",source = "receiverInfoVO.addressFirst"),
            @Mapping(target = "receiverInfo.phone",source = "receiverInfoVO.telNumber"),
            @Mapping(target = "receiverInfo.state",source = "receiverInfoVO.province"),
            @Mapping(target = "receiverInfo.city",source = "receiverInfoVO.city"),
            @Mapping(target = "receiverInfo.zipCode",source = "receiverInfoVO.zipCode"),
            @Mapping(target = "receiverInfo.company",source = "receiverInfoVO.companyName"),
            @Mapping(target = "senderInfo.name",source = "senderInfo.name"),
            @Mapping(target = "senderInfo.phone",source = "senderInfo.telNumber"),
            @Mapping(target = "senderInfo.company",source = "senderInfo.companyName"),
            @Mapping(target = "senderInfo.email",source = "senderInfo.email"),
            @Mapping(target = "senderInfo.country",source = "senderInfo.country"),
            @Mapping(target = "senderInfo.state",source = "senderInfo.province"),
            @Mapping(target = "senderInfo.city",source = "senderInfo.city"),
            @Mapping(target = "senderInfo.zipCode",source = "senderInfo.zipCode"),
            @Mapping(target = "senderInfo.houseNumber",source = "senderInfo.companyName"),
            @Mapping(target = "senderInfo.address",source = "senderInfo.addressFirst"),
            @Mapping(target = "parcelInfo.hasBattery",source = "parceInfoVO.hasBattery"),
            @Mapping(target = "parcelInfo.currency",source = "parceInfoVO.currency"),
            @Mapping(target = "parcelInfo.totalPrice",source = "parceInfoVO.totalPrice"),
            @Mapping(target = "parcelInfo.totalQuantity",source = "parceInfoVO.totalQuantity"),
            @Mapping(target = "parcelInfo.totalWeight",source = "parceInfoVO.totalWeight"),
            @Mapping(target = "parcelInfo.height",source = "parceInfoVO.height"),
            @Mapping(target = "parcelInfo.width",source = "parceInfoVO.width"),
            @Mapping(target = "parcelInfo.length",source = "parceInfoVO.length"),
            @Mapping(target = "parcelInfo.ioss",source = "parceInfoVO.ioss"),
            @Mapping(target = "parcelInfo.productList",source = "logisticsProductVOList")
    })
    YanWenCreateWayBillRequest orderRequestByYanWen(LogisticsOrderVO logisticsOrderVO);

    @Mapping(target = "goodsNameCh",source = "declareChineseName")
    @Mapping(target = "goodsNameEn",source = "declareEnglishName")
    @Mapping(target = "price",source = "price")
    @Mapping(target = "quantity",source = "quantity")
    @Mapping(target = "weight",source = "weight")
    @Mapping(target = "hscode",source = "customsCode")
    @Mapping(target = "url",source = "url")
    @Mapping(target = "material",source = "englishMaterial")
    YanWenCreateWayBillRequest.ParcelInfo.Product yanWenProductMapping(LogisticsProductVO logisticsProductVO);
}
