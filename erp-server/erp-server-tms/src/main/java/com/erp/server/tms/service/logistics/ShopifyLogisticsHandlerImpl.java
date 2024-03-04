package com.erp.server.tms.service.logistics;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.oms.shopify.api.rest.model.ShopifyFulfillmentServicesItem;
import com.sdk.oms.shopify.api.rest.model.ShopifyFulfillmentServicesRoot;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import com.sdk.tms.express.model.order.request.OrderRequest;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.model.base.BaseResponse;
import com.sdk.tms.shopee.model.logistics.response.LogisticsChannel;
import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jim
 * @ClassName ShopifyLogisticsHandlerImpl
 * @description: 亚马逊物流接口开发
 * @date 2023年12月25日
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.SHOPIFY)
public class ShopifyLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private ShopifyRestClientService shopifyRestClientService;

    /**
     * 渠道查询
     */
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        List<LogisticsSaleChannelEntity> entityList = new ArrayList<>();
        entityList.add(new LogisticsSaleChannelEntity().setCode("4PX").setPlatformChannelId("4PX").setCnName("4PX").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("AGS").setPlatformChannelId("AGS").setCnName("AGS").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Amazon Logistics UK").setPlatformChannelId("Amazon Logistics UK").setCnName("Amazon Logistics UK").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Amazon Logistics US").setPlatformChannelId("Amazon Logistics US").setCnName("Amazon Logistics US").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("An Post").setPlatformChannelId("An Post").setCnName("An Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Anjun Logistics").setPlatformChannelId("Anjun Logistics").setCnName("Anjun Logistics").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("APC").setPlatformChannelId("APC").setCnName("APC").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Asendia USA").setPlatformChannelId("Asendia USA").setCnName("Asendia USA").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Australia Post").setPlatformChannelId("Australia Post").setCnName("Australia Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Bonshaw").setPlatformChannelId("Bonshaw").setCnName("Bonshaw").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("BPost").setPlatformChannelId("BPost").setCnName("BPost").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("BPost International").setPlatformChannelId("BPost International").setCnName("BPost International").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Canada Post").setPlatformChannelId("Canada Post").setCnName("Canada Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Canpar").setPlatformChannelId("Canpar").setCnName("Canpar").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("CDL Last Mile").setPlatformChannelId("CDL Last Mile").setCnName("CDL Last Mile").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("China Post").setPlatformChannelId("China Post").setCnName("China Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Chronopost").setPlatformChannelId("Chronopost").setCnName("Chronopost").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Chukou1").setPlatformChannelId("Chukou1").setCnName("Chukou1").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Colissimo").setPlatformChannelId("Colissimo").setCnName("Colissimo").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Comingle").setPlatformChannelId("Comingle").setCnName("Comingle").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Coordinadora").setPlatformChannelId("Coordinadora").setCnName("Coordinadora").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Correios").setPlatformChannelId("Correios").setCnName("Correios").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Correos").setPlatformChannelId("Correos").setCnName("Correos").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("CTT").setPlatformChannelId("CTT").setCnName("CTT").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("CTT Express").setPlatformChannelId("CTT Express").setCnName("CTT Express").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Cyprus Post").setPlatformChannelId("Cyprus Post").setCnName("Cyprus Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Delnext").setPlatformChannelId("Delnext").setCnName("Delnext").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Deutsche Post").setPlatformChannelId("Deutsche Post").setCnName("Deutsche Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("DHL eCommerce").setPlatformChannelId("DHL eCommerce").setCnName("DHL eCommerce").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("DHL eCommerce Asia").setPlatformChannelId("DHL eCommerce Asia").setCnName("DHL eCommerce Asia").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("DHL Express").setPlatformChannelId("DHL Express").setCnName("DHL Express").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("DoorDash").setPlatformChannelId("DoorDash").setCnName("DoorDash").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("DPD").setPlatformChannelId("DPD").setCnName("DPD").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("DPD Local").setPlatformChannelId("DPD Local").setCnName("DPD Local").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("DPD UK").setPlatformChannelId("DPD UK").setCnName("DPD UK").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("DTD Express").setPlatformChannelId("DTD Express").setCnName("DTD Express").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("DX").setPlatformChannelId("DX").setCnName("DX").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Eagle").setPlatformChannelId("Eagle").setCnName("Eagle").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Estes").setPlatformChannelId("Estes").setCnName("Estes").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Evri").setPlatformChannelId("Evri").setCnName("Evri").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("FedEx").setPlatformChannelId("FedEx").setCnName("FedEx").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("First Global Logistics").setPlatformChannelId("First Global Logistics").setCnName("First Global Logistics").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("First Line").setPlatformChannelId("First Line").setCnName("First Line").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("FSC").setPlatformChannelId("FSC").setCnName("FSC").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Fulfilla").setPlatformChannelId("Fulfilla").setCnName("Fulfilla").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("GLS").setPlatformChannelId("GLS").setCnName("GLS").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Guangdong Weisuyi Information Technology (WSE)").setPlatformChannelId("Guangdong Weisuyi Information Technology (WSE)").setCnName("Guangdong Weisuyi Information Technology (WSE)").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Heppner Internationale Spedition GmbH & Co.").setPlatformChannelId("Heppner Internationale Spedition GmbH & Co.").setCnName("Heppner Internationale Spedition GmbH & Co.").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Iceland Post").setPlatformChannelId("Iceland Post").setCnName("Iceland Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("IDEX").setPlatformChannelId("IDEX").setCnName("IDEX").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Israel Post").setPlatformChannelId("Israel Post").setCnName("Israel Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Japan Post (EN)").setPlatformChannelId("Japan Post (EN)").setCnName("Japan Post (EN)").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Japan Post (JA)").setPlatformChannelId("Japan Post (JA)").setCnName("Japan Post (JA)").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("La Poste").setPlatformChannelId("La Poste").setCnName("La Poste").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Lasership").setPlatformChannelId("Lasership").setCnName("Lasership").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Latvia Post").setPlatformChannelId("Latvia Post").setCnName("Latvia Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Lietuvos Paštas").setPlatformChannelId("Lietuvos Paštas").setCnName("Lietuvos Paštas").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Logisters").setPlatformChannelId("Logisters").setCnName("Logisters").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Lone Star Overnight").setPlatformChannelId("Lone Star Overnight").setCnName("Lone Star Overnight").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("M3 Logistics").setPlatformChannelId("M3 Logistics").setCnName("M3 Logistics").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Meteor Space").setPlatformChannelId("Meteor Space").setCnName("Meteor Space").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Mondial Relay").setPlatformChannelId("Mondial Relay").setCnName("Mondial Relay").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("New Zealand Post").setPlatformChannelId("New Zealand Post").setCnName("New Zealand Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("NinjaVan").setPlatformChannelId("NinjaVan").setCnName("NinjaVan").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("North Russia Supply Chain (Shenzhen) Co.").setPlatformChannelId("North Russia Supply Chain (Shenzhen) Co.").setCnName("North Russia Supply Chain (Shenzhen) Co.").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("OnTrac").setPlatformChannelId("OnTrac").setCnName("OnTrac").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Packeta").setPlatformChannelId("Packeta").setCnName("Packeta").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Pago Logistics").setPlatformChannelId("Pago Logistics").setCnName("Pago Logistics").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Ping An Da Tengfei Express").setPlatformChannelId("Ping An Da Tengfei Express").setCnName("Ping An Da Tengfei Express").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Pitney Bowes").setPlatformChannelId("Pitney Bowes").setCnName("Pitney Bowes").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Portal PostNord").setPlatformChannelId("Portal PostNord").setCnName("Portal PostNord").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Poste Italiane").setPlatformChannelId("Poste Italiane").setCnName("Poste Italiane").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("PostNL").setPlatformChannelId("PostNL").setCnName("PostNL").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("PostNord DK").setPlatformChannelId("PostNord DK").setCnName("PostNord DK").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("PostNord NO").setPlatformChannelId("PostNord NO").setCnName("PostNord NO").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("PostNord SE").setPlatformChannelId("PostNord SE").setCnName("PostNord SE").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Purolator").setPlatformChannelId("Purolator").setCnName("Purolator").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Qxpress").setPlatformChannelId("Qxpress").setCnName("Qxpress").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Qyun Express").setPlatformChannelId("Qyun Express").setCnName("Qyun Express").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Royal Mail").setPlatformChannelId("Royal Mail").setCnName("Royal Mail").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Royal Shipments").setPlatformChannelId("Royal Shipments").setCnName("Royal Shipments").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Sagawa (EN)").setPlatformChannelId("Sagawa (EN)").setCnName("Sagawa (EN)").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Sagawa (JA)").setPlatformChannelId("Sagawa (JA)").setCnName("Sagawa (JA)").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Sendle").setPlatformChannelId("Sendle").setCnName("Sendle").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("SF Express").setPlatformChannelId("SF Express").setCnName("SF Express").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("SFC Fulfillment").setPlatformChannelId("SFC Fulfillment").setCnName("SFC Fulfillment").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("SHREE NANDAN COURIER").setPlatformChannelId("SHREE NANDAN COURIER").setCnName("SHREE NANDAN COURIER").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Singapore Post").setPlatformChannelId("Singapore Post").setCnName("Singapore Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Southwest Air Cargo").setPlatformChannelId("Southwest Air Cargo").setCnName("Southwest Air Cargo").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("StarTrack").setPlatformChannelId("StarTrack").setCnName("StarTrack").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Step Forward Freight").setPlatformChannelId("Step Forward Freight").setCnName("Step Forward Freight").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Swiss Post").setPlatformChannelId("Swiss Post").setCnName("Swiss Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("TForce Final Mile").setPlatformChannelId("TForce Final Mile").setCnName("TForce Final Mile").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Tinghao").setPlatformChannelId("Tinghao").setCnName("Tinghao").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("TNT").setPlatformChannelId("TNT").setCnName("TNT").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Toll IPEC").setPlatformChannelId("Toll IPEC").setCnName("Toll IPEC").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("United Delivery Service").setPlatformChannelId("United Delivery Service").setCnName("United Delivery Service").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("UPS").setPlatformChannelId("UPS").setCnName("UPS").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("USPS").setPlatformChannelId("USPS").setCnName("USPS").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Venipak").setPlatformChannelId("Venipak").setCnName("Venipak").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("We Post").setPlatformChannelId("We Post").setCnName("We Post").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Whistl").setPlatformChannelId("Whistl").setCnName("Whistl").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Wizmo").setPlatformChannelId("Wizmo").setCnName("Wizmo").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("WMYC").setPlatformChannelId("WMYC").setCnName("WMYC").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Xpedigo").setPlatformChannelId("Xpedigo").setCnName("Xpedigo").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("XPO Logistics").setPlatformChannelId("XPO Logistics").setCnName("XPO Logistics").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Yamato (EN)").setPlatformChannelId("Yamato (EN)").setCnName("Yamato (EN)").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("Yamato (JA)").setPlatformChannelId("Yamato (JA)").setCnName("Yamato (JA)").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("YiFan Express").setPlatformChannelId("YiFan Express").setCnName("YiFan Express").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("YunExpress").setPlatformChannelId("YunExpress").setCnName("YunExpress").setLogisticsPlatform(LogisticsPlatformEnum.SHOPIFY.getCode()));
        return success(entityList);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.SHOPIFY;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
