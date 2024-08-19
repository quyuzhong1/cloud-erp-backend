package com.erp.server.tms.convert;

import com.common.business.mapper.BigDecimalMapperWork;
import com.common.business.mapper.BooleanMapperWork;
import com.common.business.mapper.NumberMapperWork;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.erp.model.tms.vo.request.ParceInfoVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.tms.aliexpress.model.order.request.Address;
import com.erp.tms.aliexpress.model.order.request.DeclareProduct;
import com.erp.tms.batong.model.order.request.CargoVolume;
import com.erp.tms.batong.model.order.request.Consignee;
import com.erp.tms.batong.model.order.request.Invoice;
import com.erp.tms.batong.model.order.request.Shipper;
import com.sdk.tms.disifang.model.order.request.DeclareProductInfo;
import com.sdk.tms.disifang.model.order.request.OrderRequest;
import com.sdk.tms.express.model.order.request.CargoDetail;
import com.sdk.tms.express.model.order.request.ContactInfo;
import com.sdk.tms.express.model.order.request.CustomsInfo;
import com.sdk.tms.tongyou.dto.request.TongYouCreateOrderRequest;
import com.sdk.tms.tongyou.dto.response.TongYouOrderInfo;
import com.sdk.tms.ubi.model.order.request.OrderItem;
import com.sdk.tms.ubi.model.order.request.UbiOrder;
import com.sdk.tms.ubi.model.order.response.TrackBase;
import com.sdk.tms.weishi.dto.request.WeiShiCreateOrderRequest;
import com.sdk.tms.weishi.dto.response.WeiShiGetTrackNumber;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.response.YanWenQueryOrder;
import com.sdk.tms.yuntu.dto.request.YunTuCreateOrderRequest;
import com.sdk.tms.yuntu.dto.response.YunTuTrackingNumber;
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
@Mapper(uses = {BooleanMapperWork.class, TypeConversionWorker.class,NumberMapperWork.class, BigDecimalMapperWork.class}, builder = @Builder(disableBuilder = true))
public interface LogisticsOrderConverter {

    LogisticsOrderConverter INSTANCE = Mappers.getMapper(LogisticsOrderConverter.class);

    @Mappings({
            @Mapping(target = "cargoType", constant = "3"),
            @Mapping(target = "refNo", source = "deliveryNo"),
            @Mapping(target = "iossNo", source = "iossCode"),
            @Mapping(target = "vatNo", ignore = true),
            @Mapping(target = "businessType", constant = "BDS"),
            //费用模式转换
            @Mapping(target = "dutyType", source = "logisticsChannelEntity.taxModel", qualifiedByName = "taxModelToDSF"),
            //TODO 渠道产品代码
            @Mapping(target = "logisticsServiceInfo.logisticsProductCode", source = "logisticsSaleChannel.code"),
            @Mapping(target = "logisticsServiceInfo.customsService", constant = "N"),
            @Mapping(target = "logisticsServiceInfo.signatureService", source = "logisticsChannelEntity.isApiSign", qualifiedByName = "booleanToYOrN"),
            //收货人
            @Mapping(target = "recipientInfo.first_name", source = "receiverInfoVO.contact"),
            @Mapping(target = "recipientInfo.phone", source = "receiverInfoVO.telNumber"),
            @Mapping(target = "recipientInfo.email", source = "receiverInfoVO.email"),
            @Mapping(target = "recipientInfo.country", source = "receiverInfoVO.country"),
            @Mapping(target = "recipientInfo.state", source = "receiverInfoVO.province"),
            @Mapping(target = "recipientInfo.city", source = "receiverInfoVO.city"),
            @Mapping(target = "recipientInfo.district", source = "receiverInfoVO.district"),
            @Mapping(target = "recipientInfo.street", source = "receiverInfoVO.addressFirst"),
            @Mapping(target = "recipientInfo.post_code", source = "receiverInfoVO.zipCode"),
            @Mapping(target = "recipientInfo.house_number", source = "receiverInfoVO.streetAddress"),
            //发货人
            @Mapping(target = "sender.first_name", source = "senderInfo.name"),
            @Mapping(target = "sender.phone", source = "senderInfo.telNumber"),
            @Mapping(target = "sender.email", source = "senderInfo.email"),
            @Mapping(target = "sender.country", source = "senderInfo.country"),
            @Mapping(target = "sender.state", source = "senderInfo.provinceName"),
            @Mapping(target = "sender.city", source = "senderInfo.cityName"),
            @Mapping(target = "sender.district", source = "senderInfo.districtName"),
            @Mapping(target = "sender.street", source = "senderInfo.addressFirst"),
            @Mapping(target = "sender.post_code", source = "senderInfo.zipCode"),
            @Mapping(target = "sender.company", source = "senderInfo.companyName"),
//            @Mapping(target = "sender.house_number", source = "senderInfo.streetAddress"),
            @Mapping(target = "returnInfo.isReturnOnDomestic", constant = "U"),
//            @Mapping(target = "returnInfo.domesticReturnAddr.first_name", source = "senderInfo.name"),
//            @Mapping(target = "returnInfo.domesticReturnAddr.phone", source = "senderInfo.telNumber"),
//            @Mapping(target = "returnInfo.domesticReturnAddr.email", source = "senderInfo.email"),
//            @Mapping(target = "returnInfo.domesticReturnAddr.country", source = "senderInfo.country"),
//            @Mapping(target = "returnInfo.domesticReturnAddr.state", source = "senderInfo.provinceName"),
//            @Mapping(target = "returnInfo.domesticReturnAddr.city", source = "senderInfo.cityName"),
//            @Mapping(target = "returnInfo.domesticReturnAddr.district", source = "senderInfo.districtName"),
//            @Mapping(target = "returnInfo.domesticReturnAddr.street", source = "senderInfo.addressFirst"),
//            @Mapping(target = "returnInfo.domesticReturnAddr.post_code", source = "senderInfo.zipCode"),
            @Mapping(target = "returnInfo.isReturnOnOversea", constant = "U"),
//            @Mapping(target = "returnInfo.overseaReturnAddr.first_name", source = "senderInfo.name"),
//            @Mapping(target = "returnInfo.overseaReturnAddr.phone", source = "senderInfo.telNumber"),
//            @Mapping(target = "returnInfo.overseaReturnAddr.email", source = "senderInfo.email"),
//            @Mapping(target = "returnInfo.overseaReturnAddr.country", source = "senderInfo.country"),
//            @Mapping(target = "returnInfo.overseaReturnAddr.state", source = "senderInfo.provinceName"),
//            @Mapping(target = "returnInfo.overseaReturnAddr.city", source = "senderInfo.cityName"),
//            @Mapping(target = "returnInfo.overseaReturnAddr.district", source = "senderInfo.districtName"),
//            @Mapping(target = "returnInfo.overseaReturnAddr.street", source = "senderInfo.addressFirst"),
//            @Mapping(target = "returnInfo.overseaReturnAddr.post_code", source = "senderInfo.zipCode"),
            ////保险信息封装 暂时不做 默认为N
            @Mapping(target = "is_insure", constant = "N"),
            @Mapping(target = "deliverTypeInfo.deliver_type", constant = "1"),
            @Mapping(target = "deliverToRecipientInfo.deliver_type", constant = "HOME_DELIVERY")
    })
    OrderRequest orderRequestToDsf(LogisticsOrderVO logisticsOrderVO);

    /**
     * 产品申报信息
     *
     * @param logisticsProductVO
     * @return
     */
    @Mappings({
            @Mapping(target = "material", source = "englishMaterial"),
            @Mapping(target = "declare_product_code", source = "skuNo"),
            @Mapping(target = "hscode_import", source = "customsCode"),
            @Mapping(target = "declare_product_name_cn", source = "declareChineseName"),
            @Mapping(target = "declare_product_name_en", source = "declareEnglishName"),
            @Mapping(target = "declare_product_code_qty", source = "quantity"),
            //出口国/起始国/发件人国家_申报单价（按对应币别的法定单位，最多4位小数点）
            @Mapping(target = "declare_unit_price_export", source = "declarePrice"),
            //USD
            @Mapping(target = "currency_export", source = "declareCurrency"),
            @Mapping(target = "declare_unit_price_import", source = "destDeclarePrice"),
            @Mapping(target = "currency_import", source = "destCurrency")
    })
    DeclareProductInfo dsfProductMapping(LogisticsProductVO logisticsProductVO);

    @Mappings({
            @Mapping(target = "channelId" ,source = "logisticsSaleChannel.platformChannelId"),
            @Mapping(target = "orderSource" ,source = "orderSource"),
            @Mapping(target = "orderNumber" ,source = "deliveryNo"),
            @Mapping(target = "receiverInfo.name",source = "receiverInfoVO.contact"),
            @Mapping(target = "receiverInfo.country",source = "receiverInfoVO.country"),
            @Mapping(target = "receiverInfo.address",expression = "java(receiverInfoVO.getAddressFirst()+\" \"+receiverInfoVO.getAddressSecond()+\" \"+receiverInfoVO.getStreetAddress())"),
            @Mapping(target = "receiverInfo.phone",source = "receiverInfoVO.telNumber"),
            @Mapping(target = "receiverInfo.state",source = "receiverInfoVO.province"),
            @Mapping(target = "receiverInfo.city",source = "receiverInfoVO.city"),
            @Mapping(target = "receiverInfo.zipCode",source = "receiverInfoVO.zipCode"),
            @Mapping(target = "receiverInfo.company",source = "receiverInfoVO.companyName"),
            @Mapping(target = "receiverInfo.taxNumber",source = "receiverInfoVO.receiverTaxNo"),
            @Mapping(target = "senderInfo.name",source = "senderInfo.name"),
            @Mapping(target = "senderInfo.phone",source = "senderInfo.telNumber"),
            @Mapping(target = "senderInfo.company",source = "senderInfo.companyName"),
            @Mapping(target = "senderInfo.email",source = "senderInfo.email"),
            @Mapping(target = "senderInfo.country",source = "senderInfo.country"),
            @Mapping(target = "senderInfo.state",source = "senderInfo.provinceName"),
            @Mapping(target = "senderInfo.city",source = "senderInfo.cityName"),
            @Mapping(target = "senderInfo.zipCode",source = "senderInfo.zipCode"),
            @Mapping(target = "senderInfo.houseNumber",source = "senderInfo.companyName"),
            @Mapping(target = "senderInfo.address",source = "senderInfo.addressFirst"),
            @Mapping(target = "senderInfo.taxNumber",source = "senderInfo.taxNumber"),
            @Mapping(target = "parcelInfo.hasBattery",source = "parceInfoVO.hasBattery",qualifiedByName = "boolToInteger"),
            @Mapping(target = "parcelInfo.currency",constant = "USD"),
            @Mapping(target = "parcelInfo.totalPrice",source = "parceInfoVO.totalPrice"),
            @Mapping(target = "parcelInfo.totalQuantity",source = "parceInfoVO.totalQuantity"),
            @Mapping(target = "parcelInfo.totalWeight",source = "parceInfoVO.totalWeight"),
            @Mapping(target = "parcelInfo.height",source = "parceInfoVO.height"),
            @Mapping(target = "parcelInfo.width",source = "parceInfoVO.width"),
            @Mapping(target = "parcelInfo.length",source = "parceInfoVO.length"),
            @Mapping(target = "parcelInfo.ioss",source = "iossCode"),
            @Mapping(target = "parcelInfo.productList",source = "logisticsProductVOList")
    })
    YanWenCreateWayBillRequest orderRequestByYanWen(LogisticsOrderVO logisticsOrderVO);

    @Mapping(target = "goodsNameCh", source = "declareChineseName")
    @Mapping(target = "goodsNameEn", source = "declareEnglishName")
    @Mapping(target = "price", source = "destDeclarePrice")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "weight", source = "weight")
    @Mapping(target = "hscode", source = "customsCode")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "material", source = "englishMaterial")
    YanWenCreateWayBillRequest.ParcelInfo.Product yanWenProductMapping(LogisticsProductVO logisticsProductVO);


    @Mapping(target = "transportNo",source = "waybillNumber")
    @Mapping(target = "deliveryNo",source = "orderNumber")
    @Mapping(target = "trackNo",source = "waybillNumber")
    LogisticsOrderResponseVO orderQueryByYanWen(YanWenQueryOrder yanWenQueryOrder);
    List<LogisticsOrderResponseVO> orderQueryByYanWen(List<YanWenQueryOrder> list);

    @Mappings({
            @Mapping(target = "shippingMethod" ,source = "logisticsSaleChannel.code"),
            @Mapping(target = "countryCode" ,source = "receiverInfoVO.country"),
            @Mapping(target = "referenceNo" ,source = "platformCode"),
            @Mapping(target = "orderWeight" ,source = "parceInfoVO.totalWeight" ,qualifiedByName = "divideByOneThousandWithThreeDecimal"),
            @Mapping(target = "orderPieces" ,constant = "1"),
            @Mapping(target = "insuranceValue" ,source = "parceInfoVO.insuranceValue"),
            @Mapping(target = "consignee.consigneeCompany",source = "receiverInfoVO.companyName"),
            @Mapping(target = "consignee.consigneeProvince",source = "receiverInfoVO.province"),
            @Mapping(target = "consignee.consigneeCity",source = "receiverInfoVO.city"),
            @Mapping(target = "consignee.consigneeStreet",source = "receiverInfoVO.addressFirst"),
            @Mapping(target = "consignee.consigneeStreet2",source = "receiverInfoVO.addressSecond"),
            @Mapping(target = "consignee.consigneePostcode",source = "receiverInfoVO.zipCode"),
            @Mapping(target = "consignee.consigneeName",source = "receiverInfoVO.contact"),
            @Mapping(target = "consignee.consigneeTelephone",source = "receiverInfoVO.telNumber"),
            @Mapping(target = "consignee.consigneeMobile",source = "receiverInfoVO.telNumber"),
            @Mapping(target = "consignee.consigneeEmail",source = "receiverInfoVO.email"),
            @Mapping(target = "consignee.consigneeTaxno",source = "receiverInfoVO.receiverTaxNo"),
            @Mapping(target = "shipper.shipperCompany",source = "senderInfo.companyName"),
            @Mapping(target = "shipper.shipperCountrycode",source = "senderInfo.country"),
            @Mapping(target = "shipper.shipperProvince",source = "senderInfo.provinceName"),
            @Mapping(target = "shipper.shipperCity",source = "senderInfo.cityName"),
            @Mapping(target = "shipper.shipperStreet",source = "senderInfo.addressFirst"),
            @Mapping(target = "shipper.shipperPostcode",source = "senderInfo.zipCode"),
            @Mapping(target = "shipper.shipperAreacode",source = "senderInfo.districtName"),
            @Mapping(target = "shipper.shipperName",source = "senderInfo.name"),
            @Mapping(target = "shipper.shipperTelephone",source = "senderInfo.telNumber"),
            @Mapping(target = "shipper.shipperMobile",source = "senderInfo.telNumber"),
            @Mapping(target = "shipper.shipperEmail",source = "senderInfo.email"),
            @Mapping(target = "shipper.orderNote",source = "remark"),
            @Mapping(target = "itemArr",source = "logisticsProductVOList")
    })
    WeiShiCreateOrderRequest orderRequestByWeiShi(LogisticsOrderVO logisticsOrderVO);
    @Mappings({
            @Mapping(target = "invoiceEnname", source = "declareEnglishName"),
            @Mapping(target = "invoiceCnname", source = "declareChineseName"),
            @Mapping(target = "invoiceWeight", source = "weight",qualifiedByName = "divideByOneThousandWithThreeDecimal"),
            @Mapping(target = "invoiceQuantity", source = "quantity"),
            @Mapping(target = "unitCode", source = "declareUnit"),
            @Mapping(target = "invoiceUnitcharge", source = "destDeclarePrice"),
            @Mapping(target = "invoiceCurrencycode", source = "destCurrency"),
            @Mapping(target = "hsCode", source = "customsCode"),
            @Mapping(target = "sku", source = "skuNo")
    })
    WeiShiCreateOrderRequest.ItemArr orderRequestByWeiShi(LogisticsProductVO logisticsProductVO);

    @Mappings({
            @Mapping(target = "referenceNo", source = "deliveryNo"),
            //发货网点
//            @Mapping(target = "facility", source = "facility"),
            @Mapping(target = "serviceCode", source = "logisticsSaleChannel.code"),
            //物流服务类型  Priority 优先  Express-Post 特快专线
            @Mapping(target = "serviceOption", source = "logisticsSaleChannel.shipmentMethod"),
            @Mapping(target = "incoterm", source = "logisticsChannelEntity.taxModel"),
            @Mapping(target = "weight", source = "parceInfoVO.totalWeight"),
            //重量单位
            @Mapping(target = "weightUnit", constant = "g"),

            @Mapping(target = "length", source = "parceInfoVO.length"),
            @Mapping(target = "width", source = "parceInfoVO.width"),
            @Mapping(target = "height", source = "parceInfoVO.height"),
            @Mapping(target = "dimensionUnit", constant = "CM"),
            @Mapping(target = "platform", source = "orderSource"),
            //收货人
            @Mapping(target = "recipientName", source = "receiverInfoVO.contact"),
            @Mapping(target = "phone", source = "receiverInfoVO.telNumber"),
            @Mapping(target = "email", source = "receiverInfoVO.email"),
            @Mapping(target = "postcode", source = "receiverInfoVO.zipCode"),
            @Mapping(target = "country", source = "receiverInfoVO.country"),
            @Mapping(target = "state", source = "receiverInfoVO.province"),
            @Mapping(target = "city", source = "receiverInfoVO.city"),
//            @Mapping(target = "recipientInfo.district", source = "receiverInfoVO.district"),
            @Mapping(target = "addressLine1", source = "receiverInfoVO.addressFirst"),
            @Mapping(target = "addressLine2", source = "receiverInfoVO.addressSecond"),
            @Mapping(target = "recipientTaxId", source = "receiverInfoVO.receiverTaxNo"),
//            @Mapping(target = "recipientInfo.house_number", source = "receiverInfoVO.addressFirst"),
            //发货人
            @Mapping(target = "shipperName", source = "senderInfo.name"),
            @Mapping(target = "shipperPhone", source = "senderInfo.telNumber"),
            @Mapping(target = "shipperEmail", source = "senderInfo.email"),
            @Mapping(target = "shipperCountry", source = "senderInfo.country"),
            @Mapping(target = "shipperState", source = "senderInfo.provinceName"),
            @Mapping(target = "shipperCity", source = "senderInfo.cityName"),
//            @Mapping(target = "sender.district", source = "senderInfo.districtName"),
            @Mapping(target = "shipperAddressLine1", source = "senderInfo.addressFirst"),
            @Mapping(target = "shipperAddressLine2", source = "senderInfo.addressSecond")
    })
    UbiOrder orderRequestByUBI(LogisticsOrderVO logisticsOrderVO);


    @Mapping(target = "itemNo",source = "id")
    @Mapping(target = "sku",source = "skuId")
    @Mapping(target = "description",source = "declareEnglishName")
    @Mapping(target = "nativeDescription",source = "declareChineseName")
    @Mapping(target = "hsCode",source = "customsCode")
    @Mapping(target = "originCountry",source = "sourceCountry")
    @Mapping(target = "itemCount",source = "quantity")
    @Mapping(target = "unitValue",source = "destDeclarePrice")
    //TODO Item重量，转换成KG
    @Mapping(target = "weight",source = "weight",qualifiedByName = "gTokg")
    OrderItem orderItemRequestByUBI(LogisticsProductVO logisticsProductVO);
    List<OrderItem> orderItemsRequestByUBI(List<LogisticsProductVO> logisticsProductVOS);

    @Mapping(target = "transportNo",source = "orderId")
    @Mapping(target = "deliveryNo",source = "referenceNo")
    @Mapping(target = "trackNo",source = "trackingNo")
    LogisticsOrderResponseVO ordersQueryByUBI( TrackBase trackBase);
    List<LogisticsOrderResponseVO> ordersQueryByUBI( List<TrackBase> trackNumber);



    @Mappings({
            @Mapping(target = "deliveryNo", source = "orderNumber"),
            @Mapping(target = "transportNo", source = "wayBillNumber"),
            @Mapping(target = "trackNo", source = "trackingNumber")
    })
    LogisticsOrderResponseVO trackInfoConvertByWeiShi(WeiShiGetTrackNumber data);
    List<LogisticsOrderResponseVO> trackInfoConvertByWeiShi(List<WeiShiGetTrackNumber> data);


    @Mappings({
            @Mapping(target = "taxNumber" ,source = "receiverInfoVO.receiverTaxNo"),
            @Mapping(target = "customerOrderNumber" ,source = "deliveryNo"),
            @Mapping(target = "shippingMethodCode" ,source = "logisticsSaleChannel.code"),
            @Mapping(target = "length" ,source = "parceInfoVO.length"),
            @Mapping(target = "width" ,source = "parceInfoVO.width"),
            @Mapping(target = "height" ,source = "parceInfoVO.height"),
            @Mapping(target = "weight" ,source = "parceInfoVO.totalWeight" ,qualifiedByName = "divideByOneThousandWithThreeDecimal"),
            @Mapping(target = "packageCount" ,constant = "1"),
            @Mapping(target = "sourceCode" ,source = "orderSource"),
            @Mapping(target = "returnOption" ,source = "returnOption"),
            @Mapping(target = "insuranceOption" ,constant = "0"),
            @Mapping(target = "iossCode" ,source = "iossCode"),
//            @Mapping(target = "coverage" ,source = "parceInfoVO.insuranceValue"),
            @Mapping(target = "receiver.countryCode",source = "receiverInfoVO.country"),
            @Mapping(target = "receiver.firstName",source = "receiverInfoVO.contact"),
//            @Mapping(target = "receiver.lastName",source = "receiverInfoVO.name"),
            @Mapping(target = "receiver.company",source = "receiverInfoVO.companyName"),
            @Mapping(target = "receiver.street",source = "receiverInfoVO.addressFirst"),
//            @Mapping(target = "receiver.streetAddress1",source = "receiverInfoVO.addressFirst"),
            @Mapping(target = "receiver.streetAddress2",source = "receiverInfoVO.addressSecond"),
            @Mapping(target = "receiver.city",source = "receiverInfoVO.city"),
            @Mapping(target = "receiver.state",source = "receiverInfoVO.province"),
            @Mapping(target = "receiver.zip",source = "receiverInfoVO.zipCode"),
            @Mapping(target = "receiver.phone",source = "receiverInfoVO.telNumber"),
            @Mapping(target = "receiver.email",source = "receiverInfoVO.email"),
            @Mapping(target = "receiver.mobileNumber",source = "receiverInfoVO.telNumber"),
//            @Mapping(target = "receiver.houseNumber",source = "receiverInfoVO.streetAddress"),
            @Mapping(target = "sender.countryCode",source = "senderInfo.country"),
            @Mapping(target = "sender.lastName",source = "senderInfo.name"),
            @Mapping(target = "sender.company",source = "senderInfo.companyName"),
            @Mapping(target = "sender.street",source = "senderInfo.addressFirst"),
            @Mapping(target = "sender.state",source = "senderInfo.provinceName"),
            @Mapping(target = "sender.city",source = "senderInfo.cityName"),
            @Mapping(target = "sender.zip",source = "senderInfo.zipCode"),
            @Mapping(target = "sender.phone",source = "senderInfo.telNumber"),
            @Mapping(target = "parcels",source = "logisticsProductVOList")
    })
    YunTuCreateOrderRequest orderRequestByYunTu(LogisticsOrderVO logisticsOrderVO);

    @Mappings({
            @Mapping(target = "EName" ,source = "declareEnglishName"),
            @Mapping(target = "CName" ,source = "declareChineseName"),
            @Mapping(target = "hsCode" ,source = "customsCode"),
            @Mapping(target = "quantity" ,source = "quantity"),
            @Mapping(target = "unitPrice" ,source = "destDeclarePrice"),
            @Mapping(target = "unitWeight" ,source = "weight",qualifiedByName = "divideByOneThousandWithThreeDecimal"),
            @Mapping(target = "remark" ,source = "remark"),
            @Mapping(target = "productUrl" ,ignore = true),
            @Mapping(target = "sku" ,source = "skuNo"),
            @Mapping(target = "invoiceRemark" ,source = "distributionInfo"),
            @Mapping(target = "currencyCode" ,source = "destCurrency"),
            @Mapping(target = "invoicePart" ,source = "englishMaterial"),
            @Mapping(target = "invoiceUsage" ,source = "englishUsage")
    })
    YunTuCreateOrderRequest.Parcels orderRequestByYunTu(LogisticsProductVO productVO);

    @Mappings({
            @Mapping(target = "transportNo" ,source = "wayBillNumber"),
            @Mapping(target = "trackNo" ,source = "trackingNumber"),
            @Mapping(target = "deliveryNo" ,source = "customerOrderNumber"),
            @Mapping(target = "code" ,constant = "200"),
            @Mapping(target = "message" ,constant = "调用成功")
    })
    LogisticsOrderResponseVO orderQueryByYunTu(YunTuTrackingNumber data);
    List<LogisticsOrderResponseVO> orderQueryByYunTu(List<YunTuTrackingNumber> data);


    @Mappings({
            @Mapping(target = "contactType" ,constant = "1"),
            @Mapping(target = "company",source = "senderInfo.companyName"),
            @Mapping(target = "country",source = "senderInfo.country"),
            @Mapping(target = "province",source = "senderInfo.provinceName"),
            @Mapping(target = "city",source = "senderInfo.cityName"),
            @Mapping(target = "county",source = "senderInfo.districtName"),
            @Mapping(target = "address",source = "senderInfo.addressFirst"),
            @Mapping(target = "postCode",source = "senderInfo.zipCode"),
            @Mapping(target = "contact",source = "senderInfo.name"),
            @Mapping(target = "tel",source = "senderInfo.telNumber"),
            @Mapping(target = "mobile",source = "senderInfo.telNumber"),
            @Mapping(target = "email",source = "senderInfo.email")
//            @Mapping(target = "taxNo",source = "senderInfo.taxNumber")
    })
    ContactInfo orderRequestSendUserByExpress(LogisticsOrderVO logisticsOrderVO);
    @Mappings({
            @Mapping(target = "contactType" ,constant = "2"),
            @Mapping(target = "country" ,source = "receiverInfoVO.country"),
            @Mapping(target = "company",source = "receiverInfoVO.companyName"),
            @Mapping(target = "province",source = "receiverInfoVO.province"),
            @Mapping(target = "city",source = "receiverInfoVO.city"),
            @Mapping(target = "county",source = "senderInfo.districtName"),
            @Mapping(target = "address",source = "receiverInfoVO.addressFirst"),
            @Mapping(target = "email",source = "receiverInfoVO.email"),
            @Mapping(target = "postCode",source = "receiverInfoVO.zipCode"),
            @Mapping(target = "contact",source = "receiverInfoVO.contact"),
            @Mapping(target = "tel",source = "receiverInfoVO.telNumber"),
            @Mapping(target = "taxNo",source = "receiverInfoVO.receiverTaxNo"),
            @Mapping(target = "mobile",source = "receiverInfoVO.telNumber")
    })
    ContactInfo orderRequestReceiverUserByExpress(LogisticsOrderVO logisticsOrderVO);

    @Mappings({
            @Mapping(target = "name" ,source = "declareChineseName"),
            @Mapping(target = "count" ,source = "quantity"),
            @Mapping(target = "unit",source = "declareUnit"),
            @Mapping(target = "weight",source = "weight", qualifiedByName = "gTokg"),
            @Mapping(target = "amount",source = "destDeclarePrice"),
            @Mapping(target = "currency",source = "destCurrency"),
            @Mapping(target = "sourceArea",source = "sourceCountry"),
            @Mapping(target = "hsCode",source = "customsCode"),
            @Mapping(target = "goodsCode",source = "customsCode"),
            @Mapping(target = "specifications",source = "declareModel")
    })
    CargoDetail orderRequestCargoDetailByExpress(LogisticsProductVO logisticsProductVO);
    List<CargoDetail> orderRequestCargoDetailByExpress(List<LogisticsProductVO> logisticsProductVOList);

    @Mappings({
            @Mapping(target = "declaredValue" ,source = "totalPrice"),
            @Mapping(target = "declaredValueCurrency" ,source = "currency"),
            @Mapping(target = "taxPayMethod",constant = "1"),
//            @Mapping(target = "tax",source = "insuranceValue")
    })
    CustomsInfo orderRequestCustomsInfoByExpress(ParceInfoVO parceInfoVO);

    @Mappings({
            @Mapping(target = "BNo", source = "deliveryNo"),
            @Mapping(target = "charged", source = "parceInfoVO.hasBattery", qualifiedByName = "boolToInteger"),
            @Mapping(target = "insuranceValue", source = "parceInfoVO.insuranceValue"),
            @Mapping(target = "insureValue", source = "parceInfoVO.insureValue"),
            @Mapping(target = "itemType", constant = "2"),
            @Mapping(target = "pracelType", constant = "1"),
            @Mapping(target = "cod", constant = "0"),
            @Mapping(target = "logisticsId", source = "logisticsSaleChannel.code"),
            @Mapping(target = "note", source = "remark"),
            @Mapping(target = "material", source = "material"),
            @Mapping(target = "orderNo", source = "deliveryNo"),
            @Mapping(target = "passportNumber", source = "passportNumber"),
            @Mapping(target = "source", constant = "BCDC77BDC117750AC882462407E47D92"),
            @Mapping(target = "taxId", source = "receiverInfoVO.receiverTaxNo"),
            @Mapping(target = "iossVatId", source = "iossCode"),
            @Mapping(target = "isTaxed", constant = "0"),
            @Mapping(target = "transportCost", source = "transportCost"),
            @Mapping(target = "weight", source = "parceInfoVO.totalWeight", qualifiedByName = "divideByOneThousandWithThreeDecimal"),
            @Mapping(target = "recipient.actId", source = "receiverInfoVO.actId"),
            @Mapping(target = "recipient.address", source = "receiverInfoVO.addressFirst"),
            @Mapping(target = "recipient.address2", source = "receiverInfoVO.addressSecond"),
            @Mapping(target = "recipient.address3", constant = ""),
            @Mapping(target = "recipient.CName", source = "receiverInfoVO.companyName"),
            @Mapping(target = "recipient.city", source = "receiverInfoVO.city"),
            @Mapping(target = "recipient.contactPerson", source = "receiverInfoVO.contact"),
            @Mapping(target = "recipient.email", source = "receiverInfoVO.email"),
            @Mapping(target = "recipient.countryCode", source = "receiverInfoVO.country"),
            @Mapping(target = "recipient.mobileNo", source = "receiverInfoVO.telNumber"),
            @Mapping(target = "recipient.province", source = "receiverInfoVO.province"),
            @Mapping(target = "recipient.telNo", source = "receiverInfoVO.telNumber"),
            @Mapping(target = "recipient.zip", source = "receiverInfoVO.zipCode"),
            @Mapping(target = "sender.actId", source = "senderInfo.actId"),
            @Mapping(target = "sender.address1", source = "senderInfo.addressFirst"),
            @Mapping(target = "sender.address2", source = "senderInfo.addressSecond"),
            @Mapping(target = "sender.CName", source = "senderInfo.companyName"),
            @Mapping(target = "sender.city", source = "senderInfo.cityName"),
            @Mapping(target = "sender.country", source = "senderInfo.country"),
            @Mapping(target = "sender.emai", source = "senderInfo.email"),
            @Mapping(target = "sender.mobile", source = "senderInfo.telNumber"),
            @Mapping(target = "sender.name", source = "senderInfo.name"),
            @Mapping(target = "sender.postcode", source = "senderInfo.zipCode"),
            @Mapping(target = "sender.province", source = "senderInfo.provinceName"),
            @Mapping(target = "sender.tel", source = "senderInfo.telNumber"),
            @Mapping(target = "declareInfos", source = "logisticsProductVOList"),
            @Mapping(target = "codAmount", ignore = true),
            @Mapping(target = "codCurrency", ignore = true)
    })
    TongYouCreateOrderRequest orderRequestByTongYou(LogisticsOrderVO logisticsOrderVO);
    @Mappings({
            @Mapping(target = "currency" ,source = "destCurrency"),
            @Mapping(target = "des" ,constant = ""),
            @Mapping(target = "hs" ,source = "customsCode"),
            @Mapping(target = "nameCN" ,source = "declareChineseName"),
            @Mapping(target = "nameEN" ,source = "declareEnglishName"),
            @Mapping(target = "price" ,source = "destDeclarePrice"),
            @Mapping(target = "qty" ,source = "quantity"),
            @Mapping(target = "sku" ,source = "skuNo"),
            @Mapping(target = "weight" ,source = "weight",qualifiedByName = "divideByOneThousandWithThreeDecimal"),
            @Mapping(target = "url" ,source = "url")
    })
    TongYouCreateOrderRequest.DeclareInfo orderRequestByTongYou(LogisticsProductVO logisticsOrderVO);

    @Mappings({
            @Mapping(target = "transportNo" ,source = "transportNo"),
            @Mapping(target = "trackNo" ,source = "trackNo"),
            @Mapping(target = "deliveryNo" ,source = "orderNo"),
            @Mapping(target = "logisticsChannelNo" ,source = "logisticsId"),
            @Mapping(target = "countryCode" ,source = "countryCode")
    })
    LogisticsOrderResponseVO orderQueryByTongYou(TongYouOrderInfo tongYouOrderInfo);


    @Mappings({
            @Mapping(target = "aneroid_markup" ,source = "isLiquid"),
            @Mapping(target = "category_cn_desc" ,source = "declareChineseName"),
            @Mapping(target = "category_en_desc" ,source = "declareEnglishName"),
            @Mapping(target = "contains_battery" ,source = "isElectric"),
            @Mapping(target = "hs_code" ,source = "customsCode"),
            @Mapping(target = "only_battery" ,source = "onlyBattery"),
            @Mapping(target = "product_declare_amount" ,source = "destDeclarePrice"),
            @Mapping(target = "product_id" ,source = "skuId", qualifiedByName = "strToLong"),
            @Mapping(target = "product_num" ,source = "quantity"),
            @Mapping(target = "product_weight" ,source = "grossWeight", qualifiedByName = "gTokg"),
            @Mapping(target = "child_order_id" ,source = "childOrderId"),
            @Mapping(target = "sc_item_code" ,source = "scItemCode"),
            @Mapping(target = "sc_item_id" ,source = "scItemId"),
            @Mapping(target = "sc_item_name" ,source = "scItemName"),
            @Mapping(target = "sku_code" ,source = "skuNo"),
            @Mapping(target = "sku_value" ,source = "skuName")
    })
    DeclareProduct orderRequestProductByAliExpress(LogisticsProductVO logisticsProductVO);
    List<DeclareProduct> orderRequestProductByAliExpress(List<LogisticsProductVO> logisticsProductVOList);

    @Mappings({
            @Mapping(target = "country",source = "senderInfo.country"),
            @Mapping(target = "memberType",constant = "sender"),
            @Mapping(target = "province",source = "senderInfo.provinceName"),
            @Mapping(target = "city",source = "senderInfo.cityName"),
            @Mapping(target = "county",source = "senderInfo.districtName"),
            @Mapping(target = "streetAddress",source = "senderInfo.addressFirst"),
            @Mapping(target = "postCode",source = "senderInfo.zipCode"),
            @Mapping(target = "name",source = "senderInfo.name"),
            @Mapping(target = "phone",source = "senderInfo.telNumber"),
            @Mapping(target = "addressId",source = "senderInfo.id"),
            @Mapping(target = "email",source = "senderInfo.email")
    })
    Address orderRequestSendUserByAliExpress(LogisticsOrderVO logisticsOrderVO);

    @Mappings({
            @Mapping(target = "country",source = "pickUpInfo.country"),
            @Mapping(target = "memberType",constant = "pickup"),
            @Mapping(target = "province",source = "pickUpInfo.provinceName"),
            @Mapping(target = "city",source = "pickUpInfo.cityName"),
            @Mapping(target = "county",source = "pickUpInfo.districtName"),
            @Mapping(target = "streetAddress",source = "pickUpInfo.addressFirst"),
            @Mapping(target = "postCode",source = "pickUpInfo.zipCode"),
            @Mapping(target = "name",source = "pickUpInfo.name"),
            @Mapping(target = "phone",source = "pickUpInfo.telNumber"),
            @Mapping(target = "addressId",source = "pickUpInfo.id"),
            @Mapping(target = "email",source = "pickUpInfo.email")
    })
    Address orderRequestPickUpUserByAliExpress(LogisticsOrderVO logisticsOrderVO);

    @Mappings({
            @Mapping(target = "country" ,source = "receiverInfoVO.country"),
            @Mapping(target = "memberType",constant = "receiver"),
            @Mapping(target = "province",source = "receiverInfoVO.province"),
            @Mapping(target = "city",source = "receiverInfoVO.city"),
            @Mapping(target = "county",source = "receiverInfoVO.district"),
            @Mapping(target = "streetAddress",source = "receiverInfoVO.addressFirst"),
            @Mapping(target = "email",source = "receiverInfoVO.email"),
            @Mapping(target = "postCode",source = "receiverInfoVO.zipCode"),
            @Mapping(target = "name",source = "receiverInfoVO.contact"),
            @Mapping(target = "phone",source = "receiverInfoVO.telNumber")
    })
    Address orderRequestReceiverUserByAliExpress(LogisticsOrderVO logisticsOrderVO);
    @Mappings({
            @Mapping(target = "country",source = "returnInfo.country"),
            @Mapping(target = "memberType",constant = "refund"),
            @Mapping(target = "province",source = "returnInfo.provinceName"),
            @Mapping(target = "city",source = "returnInfo.cityName"),
            @Mapping(target = "county",source = "returnInfo.districtName"),
            @Mapping(target = "streetAddress",source = "returnInfo.addressFirst"),
            @Mapping(target = "postCode",source = "returnInfo.zipCode"),
            @Mapping(target = "name",source = "returnInfo.name"),
            @Mapping(target = "phone",source = "returnInfo.telNumber"),
            @Mapping(target = "addressId",source = "returnInfo.id"),
            @Mapping(target = "email",source = "returnInfo.email")
    })
    Address orderRequestRefundUserByAliExpress(LogisticsOrderVO logisticsOrderVO);

    @Mappings({
            @Mapping(target = "shipperName",source = "senderInfo.contact"),
            @Mapping(target = "shipperCompany",constant = "senderInfo.companyName"),
            @Mapping(target = "shipperCountryCode",source = "senderInfo.country"),
            @Mapping(target = "shipperProvince",source = "senderInfo.provinceName"),
            @Mapping(target = "shipperCity",source = "senderInfo.cityName"),
            @Mapping(target = "shipperDistrict",source = "senderInfo.districtName"),
            @Mapping(target = "shipperStreet",source = "senderInfo.addressFirst"),
            @Mapping(target = "shipperPostCode",source = "senderInfo.zipCode"),
            @Mapping(target = "shipperTelephone",source = "senderInfo.telNumber"),
            @Mapping(target = "shipperMobile",source = "senderInfo.telNumber"),
            @Mapping(target = "shipperEmail",source = "senderInfo.email")
    })
    Shipper orderShippingByBaTong(LogisticsOrderVO logisticsOrderVO);

    @Mappings({
            @Mapping(target = "consigneeName",source = "receiverInfoVO.contact"),
            @Mapping(target = "consigneeCompany",constant = "receiverInfoVO.companyName"),
            @Mapping(target = "consigneeCountryCode",source = "receiverInfoVO.country"),
            @Mapping(target = "consigneeProvince",source = "receiverInfoVO.province"),
            @Mapping(target = "consigneeCity",source = "receiverInfoVO.city"),
            @Mapping(target = "consigneeDistrict",source = "receiverInfoVO.district"),
            @Mapping(target = "consigneeStreet",source = "receiverInfoVO.streetAddress"),
            @Mapping(target = "consigneePostCode",source = "receiverInfoVO.zipCode"),
            @Mapping(target = "consigneeTelephone",source = "receiverInfoVO.telNumber"),
            @Mapping(target = "consigneeMobile",source = "receiverInfoVO.telNumber"),
            @Mapping(target = "consigneeEmail",source = "receiverInfoVO.email"),
            @Mapping(target = "consigneeTariff",source = "receiverInfoVO.receiverTaxNo")
    })
    Consignee orderConsigneeByBaTong(LogisticsOrderVO logisticsOrder);


    @Mappings({
            @Mapping(target = "skuNo",source = "skuNo"),
            @Mapping(target = "invoiceEnName",source = "declareEnglishName"),
            @Mapping(target = "invoiceCnName",source = "declareChineseName"),
            @Mapping(target = "invoiceQuantity",source = "quantity"),
            @Mapping(target = "unitCode", constant = "PCE"),
            @Mapping(target = "invoiceUnitCharge",source = "destDeclarePrice",qualifiedByName = "bigDecimalToStr"),
            @Mapping(target = "hsCode",source = "customsCode"),
            @Mapping(target = "invoiceMaterial",source = "englishMaterial"),
            @Mapping(target = "invoiceNote",source = "distributionInfo"),
            @Mapping(target = "invoiceUse",source = "englishUsage"),
    })
    Invoice orderInvoiceByBaTong(LogisticsProductVO logisticsProductVO);
    List<Invoice> orderInvoiceByBaTong(List<LogisticsProductVO> logisticsProductVOList);

    @Mappings({
            @Mapping(target = "inVolumeLength",source = "length", qualifiedByName="intToStr"),
            @Mapping(target = "inVolumeWidth",source = "width",qualifiedByName="intToStr"),
            @Mapping(target = "inVolumeHeight",source = "height",qualifiedByName="intToStr"),
            @Mapping(target = "inVolumeGrossWeight",source = "totalWeight",qualifiedByName = "gTokgStr"),

    })
    CargoVolume orderCargoVolumeByBaTong(ParceInfoVO parceInfoVO);
}
