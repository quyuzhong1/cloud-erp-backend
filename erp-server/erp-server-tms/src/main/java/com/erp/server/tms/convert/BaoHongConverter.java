package com.erp.server.tms.convert;

import com.common.business.mapper.BigDecimalMapperWork;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateInboundReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateProductReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsOrderDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import com.erp.tms.aliexpress.model.order.request.Address;
import com.sdk.tms.baohong.api.asn.ReceivingInfo;
import com.sdk.tms.baohong.api.asn.ReceivingItemsType;
import com.sdk.tms.baohong.api.order.CreateOrderInfo;
import com.sdk.tms.baohong.api.order.OrderDataArr;
import com.sdk.tms.baohong.api.order.ProductDeatil;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.api.product.DataRow;
import com.sdk.tms.baohong.api.product.ProductRow;
import com.sdk.tms.baohong.api.product.RecordItemRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author lrp
 * @ClassName LogisticsLabelConverter
 * @description: 物流标签转换类
 */
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class, BigDecimalMapperWork.class})
public interface BaoHongConverter {

    BaoHongConverter INSTANCE = Mappers.getMapper(BaoHongConverter.class);

    @Mappings({
            @Mapping(target = "name", source = "smNameCn"),
            @Mapping(target = "code", source = "smCode"),
            @Mapping(target = "logisticsPlatform", constant = "BaoHong"),
    })
    TransferLogisticsChannelEntity transferLogisticsChannelConvert(SmRow data);
    List<TransferLogisticsChannelEntity> transferLogisticsChannelConvert(List<SmRow> data);

    @Mappings({
            @Mapping(target = "declarePlatform", constant = "BaoHong"),
            @Mapping(target = "skuNo", source = "productSku"),
            @Mapping(target = "goodId", source = "goodsId"),
            @Mapping(target = "customBarcode", source = "productBarcode"),
            @Mapping(target = "productName", source = "productTitle"),
            @Mapping(target = "productNameCn", source = "productTitle"),
            @Mapping(target = "productNameEn", source = "productTitleEn"),
            @Mapping(target = "status", expression = "java(com.common.core.constant.EnumMessage.getByCode(com.sdk.tms.baohong.enums.BaoHongEnum.ProductStatusEnum.class,data.getProductStatus()).getProductRegistrationStatusEnum().getCode())"),
            @Mapping(target = "declareNameCn", source = "hsGoodsName"),
            @Mapping(target = "customsCode", source = "hsCode"),
            @Mapping(target = "grossWeight", expression = "java(new BigDecimal(data.getProductWeight()).multiply(java.math.BigDecimal.valueOf(1000)))"),
            @Mapping(target = "declarePrice", source = "productDeclaredValue"),
            @Mapping(target = "currency", source = "currencyCode"),
    })
    ProductRegistrationEntity productRegistrationConvert(DataRow data);
    List<ProductRegistrationEntity> productRegistrationConvert(List<DataRow> data);


    @Mappings({
            @Mapping(target = "trackingNumber", source = "trackingNumber"),
            @Mapping(target = "oabCountry", source = "country"),
            @Mapping(target = "smCode", source = "shippingCode"),
            @Mapping(target = "oabName", source = "name"),
            @Mapping(target = "referenceNo", source = "referenceNo"),
            @Mapping(target = "deliveryAddress", source = "deliveryAddress"),
            @Mapping(target = "oabStreetAddress1", source = "streetAddress"),
            @Mapping(target = "oabState", source = "state"),
            @Mapping(target = "oabCity", source = "city"),
            @Mapping(target = "oabPostcode", source = "postcode"),
            @Mapping(target = "oabPhone", source = "phone"),
            @Mapping(target = "orderStatus", source = "orderStatus"),
            @Mapping(target = "iossNo", source = "iossNo"),
            @Mapping(target = "serialNo", source = "serialNo"),
            @Mapping(target = "grossWeight", source = "grossWeight",qualifiedByName = "bigDecimalToStr"),
            @Mapping(target = "buyInsurance", source = "buyInsurance"),
            @Mapping(target = "orderProduct", source = "productDetailList"),
    })
    CreateOrderInfo createOrderConvert(TransferLogisticsCreateOrderReq createOrderReq);

    @Mappings({
            @Mapping(target = "productSku", source = "skuNo"),
            @Mapping(target = "opQuantity", source = "qty"),
            @Mapping(target = "productTitleEn", source = "productTitleEn"),
            @Mapping(target = "purposeDeclaredValue", source = "purposeDeclaredValue"),
    })
    ProductDeatil productConvert(TransferLogisticsCreateOrderReq.ProductDetail productDetailList);
    List<ProductDeatil> productConvert(List<TransferLogisticsCreateOrderReq.ProductDetail> productDetailList);

    @Mappings({
            @Mapping(target = "referenceNo", source = "referenceNo"),
            @Mapping(target = "orderCode", source = "orderCode"),
            @Mapping(target = "trackingNumber", source = "trackingNumber"),
            @Mapping(target = "orderStatusEnum", expression = "java(BaoHongConverter.orderStatusConvert(data.getOrderStatus()))"),
    })
    TransferLogisticsOrderDTO createOrderInfoConvert(OrderDataArr data);

    static TransferLogisticsStatusEnum orderStatusConvert(String orderStatus){
        if(orderStatus.equals("11") || orderStatus.equals("9")){
            return TransferLogisticsStatusEnum.OUTSTOCK;
        }
        if(orderStatus.equals("4")){
            return TransferLogisticsStatusEnum.SUBMITTED;
        }
        if(orderStatus.equals("3")){
            return TransferLogisticsStatusEnum.UNUSUAL;
        }
        if(orderStatus.equals("2")){
            return TransferLogisticsStatusEnum.CONFIRMED;
        }
        if(orderStatus.equals("1")){
            return TransferLogisticsStatusEnum.DRAFT;
        }
        if(orderStatus.equals("0")){
            return TransferLogisticsStatusEnum.DELETED;
        }
        return null;
    }

    @Mappings({
            @Mapping(target = "refCode", source = "referenceCode"),
            @Mapping(target = "isDelivery", source = "isDelivery",qualifiedByName = "boolToInteger"),
            @Mapping(target = "packNo", source = "packQty"),
            @Mapping(target = "roughWeight", source = "grossWeight",qualifiedByName = "bigDecimalToStr"),
            @Mapping(target = "receivingStatus", source = "receivingStatus"),
            @Mapping(target = "receivingItems", source = "receiveItemList"),
    })
    ReceivingInfo createReceiveOrderConvert(TransferLogisticsCreateInboundReq createInboundReq);

    @Mappings({
            @Mapping(target = "orderCode", source = "orderCode"),
            @Mapping(target = "groossWeight", source = "grossWeight",qualifiedByName = "bigDecimalToStr"),
    })
    ReceivingItemsType  createReceiveOrderConvert(TransferLogisticsCreateInboundReq.ReceiveItem item);
    List<ReceivingItemsType>  createReceiveOrderConvert(List<TransferLogisticsCreateInboundReq.ReceiveItem> items);

    @Mappings({
            @Mapping(target = "cnName", source = "smNameCn"),
            @Mapping(target = "code", source = "smCode"),
            @Mapping(target = "logisticsPlatform", constant = "BaoHong"),
    })
    LogisticsSaleChannelEntity channelConvert(SmRow data);
    List<LogisticsSaleChannelEntity> channelConvert(List<SmRow> data);

    @Mappings({
            @Mapping(target = "declaredValue", source = "declaredValue",qualifiedByName = "bigDecimalToFloat"),
            @Mapping(target = "weight", source = "weight",qualifiedByName = "bigDecimalToFloat"),
            @Mapping(target = "length", source = "length",qualifiedByName = "bigDecimalToFloat"),
            @Mapping(target = "width", source = "width",qualifiedByName = "bigDecimalToFloat"),
            @Mapping(target = "height", source = "height",qualifiedByName = "bigDecimalToFloat"),
            @Mapping(target = "hasBattery", source = "hasBattery",qualifiedByName = "boolToInteger"),
            @Mapping(target = "firstQauntity", source = "firstQauntity",qualifiedByName = "bigDecimalToFloat"),
            @Mapping(target = "batteryType", expression = "java(com.sdk.tms.baohong.enums.BaoHongEnum.BatteryEnum.getCodeByErp(createProductReq.getProductProperty()))"),
            @Mapping(target = "secondQauntity", source = "secondQauntity",qualifiedByName = "bigDecimalToFloat")
    })
    RecordItemRequest createProductConvert(TransferLogisticsCreateProductReq createProductReq);


    @Mappings({
            @Mapping(target = "declarePlatform", constant = "BaoHong"),
            @Mapping(target = "skuNo", source = "productSku"),
            @Mapping(target = "customBarcode", source = "productBarcode"),
            @Mapping(target = "productName", source = "productTitle"),
            @Mapping(target = "productNameCn", source = "productTitle"),
            @Mapping(target = "productNameEn", source = "productTitleEn"),
            @Mapping(target = "status", expression = "java(com.common.core.constant.EnumMessage.getByCode(com.sdk.tms.baohong.enums.BaoHongEnum.ProductStatusEnum.class,data.getProductStatus()).getProductRegistrationStatusEnum().getCode())"),
            @Mapping(target = "declareNameCn", source = "hsGoodsName"),
            @Mapping(target = "customsCode", source = "hsCode"),
            @Mapping(target = "grossWeight", expression = "java(new BigDecimal(data.getProductWeight()).multiply(java.math.BigDecimal.valueOf(1000)))"),
            @Mapping(target = "declarePrice", source = "productDeclaredValue"),
            @Mapping(target = "isParts", source = "isAccessories",qualifiedByName="strToBooleanByNum"),
            @Mapping(target = "isInvoice", source = "hasInvoice",qualifiedByName = "strToBooleanByNum"),
            @Mapping(target = "currency", source = "currencyCode"),
            @Mapping(target = "declareElement", source = "modelSerial"),
            @Mapping(target = "failureReason", source = "rejectReason"),
    })
    ProductRegistrationEntity productInfoConvert(ProductRow data);
}
