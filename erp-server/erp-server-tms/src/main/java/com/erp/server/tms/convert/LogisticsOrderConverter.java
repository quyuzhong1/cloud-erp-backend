package com.erp.server.tms.convert;

import com.common.business.mapper.BooleanMapperWork;
import com.common.business.mapper.NumberMapperWork;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsProductVO;
import com.sdk.tms.disifang.model.order.request.DeclareProductInfo;
import com.sdk.tms.disifang.model.order.request.DeclareProductInfo;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.sdk.tms.disifang.model.order.request.OrderRequest;
import com.sdk.tms.weishi.dto.request.WeiShiCreateOrderRequest;
import com.sdk.tms.weishi.dto.response.WeiShiGetTrackNumber;
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
@Mapper(uses = {BooleanMapperWork.class, TypeConversionWorker.class,NumberMapperWork.class}, builder = @Builder(disableBuilder = true))
public interface LogisticsOrderConverter {

    LogisticsOrderConverter INSTANCE = Mappers.getMapper(LogisticsOrderConverter.class);

    @Mappings({
            @Mapping(target = "refNo", source = "deliveryNo"),
            @Mapping(target = "businessType", constant = "BDS"),
            //费用模式转换
            @Mapping(target = "dutyType", source = "logisticsChannelEntity.taxModel", qualifiedByName = "taxModelToDSF"),
            //TODO 渠道产品代码
            @Mapping(target = "logisticsServiceInfo.logisticsProductCode", source = "logisticsChannelEntity.code"),
            //收货人
            @Mapping(target = "recipientInfo.first_name", source = "receiverInfoVO.name"),
            @Mapping(target = "recipientInfo.phone", source = "receiverInfoVO.telNumber"),
            @Mapping(target = "recipientInfo.country", source = "receiverInfoVO.country"),
            @Mapping(target = "recipientInfo.state", source = "receiverInfoVO.province"),
            @Mapping(target = "recipientInfo.city", source = "receiverInfoVO.city"),
            @Mapping(target = "recipientInfo.district", source = "receiverInfoVO.district"),
            @Mapping(target = "recipientInfo.street", source = "receiverInfoVO.addressFirst"),
//            @Mapping(target = "recipientInfo.house_number", source = "receiverInfoVO.addressFirst"),
            //发货人
            @Mapping(target = "sender.first_name", source = "senderInfo.name"),
            @Mapping(target = "sender.phone", source = "senderInfo.telNumber"),
            @Mapping(target = "sender.country", source = "senderInfo.country"),
            @Mapping(target = "sender.state", source = "senderInfo.provinceName"),
            @Mapping(target = "sender.city", source = "senderInfo.cityName"),
            @Mapping(target = "sender.district", source = "senderInfo.districtName"),
            @Mapping(target = "sender.street", source = "senderInfo.addressFirst"),
    })
    OrderRequest orderRequestToDsf(LogisticsOrderVO logisticsOrderVO);

    /**
     * 产品申报信息
     *
     * @param logisticsProductVO
     * @return
     */
    @Mappings({
            @Mapping(target = "declare_product_code", source = "declareModel"),
            @Mapping(target = "declare_product_name_cn", source = "declareChineseName"),
            @Mapping(target = "declare_product_name_en", source = "declareEnglishName"),
            @Mapping(target = "declare_product_code_qty", source = "quantity"),
            //出口国/起始国/发件人国家_申报单价（按对应币别的法定单位，最多4位小数点）
            @Mapping(target = "declare_unit_price_export", source = "declareCurrency"),
            //USD
            @Mapping(target = "currency_export", source = "declareCurrencySymbol"),
            @Mapping(target = "declare_unit_price_import", source = "destCurrency"),
            @Mapping(target = "currency_import", source = "destCurrencySymbol")
    })
    DeclareProductInfo dsfProductMapping(LogisticsProductVO logisticsProductVO);

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
            @Mapping(target = "senderInfo.state",source = "senderInfo.provinceName"),
            @Mapping(target = "senderInfo.city",source = "senderInfo.cityName"),
            @Mapping(target = "senderInfo.zipCode",source = "senderInfo.zipCode"),
            @Mapping(target = "senderInfo.houseNumber",source = "senderInfo.companyName"),
            @Mapping(target = "senderInfo.address",source = "senderInfo.addressFirst"),
            @Mapping(target = "parcelInfo.hasBattery",source = "parceInfoVO.hasBattery",qualifiedByName = "boolToInteger"),
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

    @Mapping(target = "goodsNameCh", source = "declareChineseName")
    @Mapping(target = "goodsNameEn", source = "declareEnglishName")
    @Mapping(target = "price", source = "price")
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
            @Mapping(target = "shippingMethod" ,source = "channelCode"),
            @Mapping(target = "countryCode" ,source = "receiverInfoVO.country"),
            @Mapping(target = "referenceNo" ,source = "deliveryNo"),
            @Mapping(target = "orderWeight" ,source = "parceInfoVO.totalWeight" ,qualifiedByName = "divideByOneThousandWithThreeDecimal"),
            @Mapping(target = "orderPieces" ,source = "parceInfoVO.totalQuantity"),
            @Mapping(target = "insuranceValue" ,source = "parceInfoVO.insuranceValue"),
            @Mapping(target = "consignee.consigneeCompany",source = "receiverInfoVO.companyName"),
            @Mapping(target = "consignee.consigneeProvince",source = "receiverInfoVO.province"),
            @Mapping(target = "consignee.consigneeCity",source = "receiverInfoVO.city"),
            @Mapping(target = "consignee.consigneeStreet",source = "receiverInfoVO.addressFirst"),
            @Mapping(target = "consignee.consigneeStreet2",source = "receiverInfoVO.addressSecond"),
            @Mapping(target = "consignee.consigneePostcode",source = "receiverInfoVO.zipCode"),
            @Mapping(target = "consignee.consigneeName",source = "receiverInfoVO.name"),
            @Mapping(target = "consignee.consigneeTelephone",source = "receiverInfoVO.telNumber"),
            @Mapping(target = "consignee.consigneeMobile",source = "receiverInfoVO.telNumber"),
            @Mapping(target = "consignee.consigneeEmail",source = "receiverInfoVO.email"),
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
            @Mapping(target = "invoiceUnitcharge", source = "price"),
            @Mapping(target = "invoiceCurrencycode", source = "declareCurrency"),
            @Mapping(target = "hsCode", source = "customsCode"),
            @Mapping(target = "sku", source = "skuId")
    })
    WeiShiCreateOrderRequest.ItemArr orderRequestByWeiShi(LogisticsProductVO logisticsProductVO);

    @Mappings({
            @Mapping(target = "deliveryNo", source = "orderNumber"),
            @Mapping(target = "transportNo", source = "wayBillNumber"),
            @Mapping(target = "trackNo", source = "trackingNumber")
    })
    LogisticsOrderResponseVO trackInfoConvertByWeiShi(WeiShiGetTrackNumber data);
    List<LogisticsOrderResponseVO> trackInfoConvertByWeiShi(List<WeiShiGetTrackNumber> data);
}
