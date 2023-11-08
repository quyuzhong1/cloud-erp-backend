package com.erp.server.tms.convert;

import com.common.business.mapper.BooleanMapper;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.sdk.tms.disifang.model.order.request.DeclareProductInfo;
import com.sdk.tms.disifang.model.order.request.DeclareProductInfo;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.sdk.tms.disifang.model.order.request.OrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.response.YanWenQueryOrder;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsOrderConverter
 * @description: 物流订单转换类
 * @date 2023年11月06日
 * @version: 1.0
 */
@Mapper(uses = {
        BooleanMapper.class,
        TypeConversionWorker.class}, builder = @Builder(disableBuilder = true))
public interface LogisticsOrderConverter {

    LogisticsOrderConverter INSTANCE = Mappers.getMapper(LogisticsOrderConverter.class);

    @Mappings({
            @Mapping(target = "refNo", source = "deliveryNo"),
            @Mapping(target = "businessType", constant = "BDS"),
            //费用模式转换
            @Mapping(target = "dutyType", source = "logisticsChannelEntity.taxModel", qualifiedByName = "taxModelToDSF"),
            //TODO 渠道产品代码
            @Mapping(target = "logisticsServiceInfo.logisticsProductCode", source = "skuNo"),
            //收货人
            @Mapping(target = "recipientInfo.first_name", source = "receiverInfoVO.name"),
            @Mapping(target = "recipientInfo.phone", source = "receiverInfoVO.telNumber"),
            @Mapping(target = "recipientInfo.country", source = "receiverInfoVO.country"),
            @Mapping(target = "recipientInfo.province", source = "receiverInfoVO.province"),
            @Mapping(target = "recipientInfo.city", source = "receiverInfoVO.city"),
            @Mapping(target = "recipientInfo.district", source = "receiverInfoVO.district"),
//            @Mapping(target = "recipientInfo.street", source = "addressEntity.name"),
            @Mapping(target = "recipientInfo.street", source = "receiverInfoVO.addressFirst"),
            //发货人
            @Mapping(target = "sender.first_name", source = "senderInfo.name"),
            @Mapping(target = "sender.phone", source = "senderInfo.telNumber"),
            @Mapping(target = "sender.country", source = "senderInfo.country"),
            @Mapping(target = "sender.province", source = "senderInfo.province"),
            @Mapping(target = "sender.city", source = "senderInfo.city"),
            @Mapping(target = "sender.district", source = "senderInfo.district"),
//            @Mapping(target = "recipientInfo.street", source = "addressEntity.name"),
            @Mapping(target = "sender.street", source = "senderInfo.addressFirst"),
    })
    OrderCollectRequest orderRequestToDsf(LogisticsOrderVO logisticsOrderVO);

    @Mappings({
            @Mapping(target = "channelId", source = "channelId"),
            @Mapping(target = "orderSource", source = "channelId"),
            @Mapping(target = "orderNumber", source = "deliveryNo"),
            @Mapping(target = "receiverInfo.name", source = "receiverInfoVO.name"),
            @Mapping(target = "receiverInfo.country", source = "receiverInfoVO.country"),
            @Mapping(target = "receiverInfo.address", source = "receiverInfoVO.addressFirst"),
            @Mapping(target = "receiverInfo.phone", source = "receiverInfoVO.telNumber"),
            @Mapping(target = "receiverInfo.state", source = "receiverInfoVO.province"),
            @Mapping(target = "receiverInfo.city", source = "receiverInfoVO.city"),
            @Mapping(target = "receiverInfo.zipCode", source = "receiverInfoVO.zipCode"),
            @Mapping(target = "receiverInfo.company", source = "receiverInfoVO.companyName"),
            @Mapping(target = "senderInfo.name", source = "senderInfo.name"),
            @Mapping(target = "senderInfo.phone", source = "senderInfo.telNumber"),
            @Mapping(target = "senderInfo.company", source = "senderInfo.companyName"),
            @Mapping(target = "senderInfo.email", source = "senderInfo.email"),
            @Mapping(target = "senderInfo.country", source = "senderInfo.country"),
            @Mapping(target = "senderInfo.state", source = "senderInfo.province"),
            @Mapping(target = "senderInfo.city", source = "senderInfo.city"),
            @Mapping(target = "senderInfo.zipCode", source = "senderInfo.zipCode"),
            @Mapping(target = "senderInfo.houseNumber", source = "senderInfo.companyName"),
            @Mapping(target = "senderInfo.address", source = "senderInfo.addressFirst"),
            @Mapping(target = "parcelInfo.hasBattery", source = "parceInfoVO.hasBattery"),
            @Mapping(target = "parcelInfo.currency", source = "parceInfoVO.currency"),
            @Mapping(target = "parcelInfo.totalPrice", source = "parceInfoVO.totalPrice"),
            @Mapping(target = "parcelInfo.totalQuantity", source = "parceInfoVO.totalQuantity"),
            @Mapping(target = "parcelInfo.totalWeight", source = "parceInfoVO.totalWeight"),
            @Mapping(target = "parcelInfo.height", source = "parceInfoVO.height"),
            @Mapping(target = "parcelInfo.width", source = "parceInfoVO.width"),
            @Mapping(target = "parcelInfo.length", source = "parceInfoVO.length"),
            @Mapping(target = "parcelInfo.ioss", source = "parceInfoVO.ioss"),
            @Mapping(target = "parcelInfo.productList", source = "logisticsProductVOList")
    })
    YanWenCreateWayBillRequest orderRequestByYanWen(LogisticsOrderVO logisticsOrderVO);

    @Mapping(target = "goodsNameCh", source = "declareChineseName")
    @Mapping(target = "goodsNameEn", source = "declareEnglishName")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "weight", source = "weight")
    @Mapping(target = "hscode", source = "customsCode")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "material", source = "englishMaterial")
    YanWenCreateWayBillRequest.ParcelInfo.Product yanWenProductMapping(LogisticsProductVO logisticsProductVO);


    @Mapping(target = "orderNo",source = "waybillNumber")
    @Mapping(target = "deliveryNo",source = "orderNumber")
    @Mapping(target = "trackNo",source = "waybillNumber")
    LogisticsOrderResponseVO orderQueryByYanWen(YanWenQueryOrder yanWenQueryOrder);
    List<LogisticsOrderResponseVO> orderQueryByYanWen(List<YanWenQueryOrder> list);
}
