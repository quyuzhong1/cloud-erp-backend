package com.erp.server.tms.convert;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.LogisticsRegisterVO;
import com.erp.tms.aliexpress.model.channel.response.ChannelResponse;
import com.erp.tms.aliexpress.model.query.response.ServiceResult;
import com.erp.tms.batong.model.label.base.BaseData;
import com.sdk.oms.shopify.api.rest.model.ShopifyFulfillmentServicesItem;
import com.sdk.oms.walmart.dto.walmart.WalmartCarriersDTO;
import com.sdk.tms.disifang.model.chanel.response.ChanelInfo;
import com.sdk.tms.shopee.model.logistics.response.LogisticsChannel;
import com.sdk.tms.tongyou.dto.response.TongYouChannel;
import com.sdk.tms.track123.model.request.RegisterRequest;
import com.sdk.tms.ubi.model.catalog.response.ServiceCataLog;
import com.sdk.tms.weishi.dto.response.WeiShiChannel;
import com.sdk.tms.yanwen.dto.response.YanWenChannel;
import com.sdk.tms.yuntu.dto.response.YunTuChannel;
import com.sdk.wangdian.sdk.api.setting.dto.LogisticsQueryResponse;
import com.sdk.wms.antu.dto.response.AntuLogisticsProductsResp;
import com.sdk.wms.goodcang.dto.response.GoodCangLogisticsProductsResp;
import com.sdk.wms.iml.dto.response.ImlInventoryLogisticsProductsResp;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsOrderConverter
 * @description: 物流订单转换类
 * @date 2023年11月06日
 * @version: 1.0
 */
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class})
public interface LogisticsChannelConverter {

    LogisticsChannelConverter INSTANCE = Mappers.getMapper(LogisticsChannelConverter.class);



    @Mappings({
            @Mapping(target = "platformChannelId", source = "id"),
            @Mapping(target = "code", source = "id"),
            @Mapping(target = "cnName", source = "nameCh"),
            @Mapping(target = "enName", source = "nameEn"),
            @Mapping(target = "logisticsPlatform", constant = "YanWen"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByYanWen(YanWenChannel yanWenChannelList);
    List<LogisticsSaleChannelEntity> channelConvertByYanWenList(List<YanWenChannel> yanWenChannelList);


    /**
     * 巴通所有渠道
      * @param
     * @return
     */
    @Mappings({
            @Mapping(target = "code", source = "code"),
            @Mapping(target = "cnName", source = "cnName"),
            @Mapping(target = "enName", source = "enName"),
            @Mapping(target = "logisticsPlatform", constant = "BaTong"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByBaTong(BaseData baTong);
    List<LogisticsSaleChannelEntity> channelConvertByBaTong(List<BaseData> baTongChannelList);

    @Mappings({
            @Mapping(target = "code", source = "logistics_product_code"),
            @Mapping(target = "cnName", source = "logistics_product_name_cn"),
            @Mapping(target = "enName", source = "logistics_product_name_en"),
            @Mapping(target = "isTrack", source = "order_track",qualifiedByName = "yOrNToBoolean"),
            @Mapping(target = "logisticsPlatform", constant = "DSF"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByDSF(ChanelInfo chanelInfo);
    List<LogisticsSaleChannelEntity> channelConvertByDSF(List<ChanelInfo> chanelInfos);

    @Mappings({
            @Mapping(target = "cnName", source = "cnName"),
            @Mapping(target = "enName", source = "enName"),
            @Mapping(target = "code", source = "code"),
            @Mapping(target = "isTrack", source = "trackStatus", qualifiedByName = "yOrNToBoolean"),
            @Mapping(target = "aging", source = "aging"),
            @Mapping(target = "logisticsPlatform", constant = "WeiShi"),
            @Mapping(target = "id", ignore = true),
    })
    LogisticsSaleChannelEntity channelConvertByWeiShi(WeiShiChannel data);
    List<LogisticsSaleChannelEntity> channelConvertByWeiShi(List<WeiShiChannel> data);

    @Mappings({
            @Mapping(target = "code", source = "serviceCode"),
            @Mapping(target = "cnName", source = "serviceName"),
            @Mapping(target = "enName", source = "nativeName"),
            @Mapping(target = "supplierName", source = "serviceProvider"),
            @Mapping(target = "supplierCode", source = "serviceProviderCode"),
            @Mapping(target = "logisticsPlatform", constant = "UBI"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByUBI(ServiceCataLog serviceCataLog);
    List<LogisticsSaleChannelEntity> channelConvertByUBI(List<ServiceCataLog> serviceCataLogList);

    @Mappings({
            @Mapping(target = "code", source = "code"),
            @Mapping(target = "cnName", source = "CName"),
            @Mapping(target = "enName", source = "EName"),
            @Mapping(target = "isTrack", source = "hasTrackingNumber"),
//            @Mapping(target = "aging", source = "displayName"),
            @Mapping(target = "logisticsPlatform", constant = "YunTu"),
            @Mapping(target = "id", ignore = true),
    })
    LogisticsSaleChannelEntity channelConvertByYunTu(YunTuChannel data);
    List<LogisticsSaleChannelEntity> channelConvertByYunTu(List<YunTuChannel> data);

    @Mappings({
            @Mapping(target = "code", source = "code"),
            @Mapping(target = "cnName", source = "cnName"),
            @Mapping(target = "enName", source = "enName"),
            @Mapping(target = "channelStatus", source = "status"),
            @Mapping(target = "logisticsPlatform", constant = "TongYou"),
            @Mapping(target = "id", ignore = true),
    })
    LogisticsSaleChannelEntity channelConvertByTongYou(TongYouChannel data);
    List<LogisticsSaleChannelEntity> channelConvertByTongYou(List<TongYouChannel> data);

    @Mappings({
            @Mapping(target = "platformChannelId", source = "logisticsChannelId"),
            @Mapping(target = "code", source = "logisticsChannelId"),
            @Mapping(target = "cnName", source = "logisticsChannelName"),
            @Mapping(target = "enName", source = "logisticsChannelName"),
            @Mapping(target = "channelStatus", source = "enabled",qualifiedByName = "booleanToStatus"),
            @Mapping(target = "logisticsPlatform", constant = "Shopee"),
            @Mapping(target = "id", ignore = true),
    })
    LogisticsSaleChannelEntity channelConvertByShopee(LogisticsChannel logisticsChannel);
    List<LogisticsSaleChannelEntity> channelConvertByShopee(List<LogisticsChannel> logisticsChannels);

    @Mappings({
            @Mapping(target = "name", source = "cnName"),
            @Mapping(target = "code", source = "code"),
            @Mapping(target = "effectiveTime", source = "aging"),
            @Mapping(target = "syncSourceId",source = "id"),
            @Mapping(target = "disabled",constant = "true"),
            @Mapping(target = "id", ignore = true),
    })
    LogisticsChannelEntity channelConvertBySaleChannel(LogisticsSaleChannelEntity channel);
    List<LogisticsChannelEntity> channelConvertBySaleChannel(List<LogisticsSaleChannelEntity> saleChannelList);

    @Mappings({
            @Mapping(target = "code", source = "id"),
            @Mapping(target = "value", source = "name"),
            @Mapping(target = "disabled", source = "disabled"),

    })
    BaseDropDownDTO.DisabledDTO convertByChannelDown(LogisticsChannelEntity logisticsChannel);
    List<BaseDropDownDTO.DisabledDTO> convertByChannelDown(List<LogisticsChannelEntity> list);


    @Mappings({
            @Mapping(target = "code", source = "serviceName"),
            @Mapping(target = "cnName", source = "displayName"),
            @Mapping(target = "enName", source = "displayName"),
            @Mapping(target = "supplierName", source = "logisticsCompany"),
            @Mapping(target = "isTrack", constant = "true"),
            @Mapping(target = "aging", source = ".", qualifiedByName = "convertAging"),
            @Mapping(target = "logisticsPlatform", constant = "AliExpress"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByAliExpress(ChannelResponse chanelInfo);
    List<LogisticsSaleChannelEntity> channelConvertByAliExpress(List<ChannelResponse> chanelInfos);

    @Mappings({
            @Mapping(target = "trackNo", source = "trackNo"),
            @Mapping(target = "courierCode", source = "courierCode"),
            @Mapping(target = "extendFieldMap.phoneSuffix", source = "phoneSuffix", qualifiedByName = "getPhoneSuffix4")
    })
    RegisterRequest registerTrackNoByTrack123(LogisticsRegisterVO logisticsRegisterVO);
    List<RegisterRequest> registerTrackNoByTrack123(List<LogisticsRegisterVO> logisticsRegisterVOS);

    @Mappings({
            @Mapping(target = "code", source = "code"),
            @Mapping(target = "cnName", source = "name"),
            @Mapping(target = "enName", source = "nameEn"),
            @Mapping(target = "supplierName", source = "spCode"),
            @Mapping(target = "isTrack", constant = "true"),
            @Mapping(target = "logisticsPlatform", constant = "goodcang"),
            @Mapping(target = "overseasWarehouseId", source = "erpWarehouseId"),
            @Mapping(target = "platformWarehouseCode", source = "warehouseCode"),
            @Mapping(target = "carrierType", source = "distributorType"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByGoodCang(GoodCangLogisticsProductsResp data);
    List<LogisticsSaleChannelEntity> channelConvertByGoodCang(List<GoodCangLogisticsProductsResp> data);

    @Mappings({
            @Mapping(target = "code", source = "code"),
            @Mapping(target = "cnName", source = "name"),
            @Mapping(target = "enName", source = "nameEn"),
            @Mapping(target = "isTrack", constant = "true"),
            @Mapping(target = "logisticsPlatform", constant = "iml"),
            @Mapping(target = "overseasWarehouseId", source = "erpWarehouseId"),
            @Mapping(target = "platformWarehouseCode", source = "warehouseCode"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByIml(ImlInventoryLogisticsProductsResp data);
    List<LogisticsSaleChannelEntity> channelConvertByIml(List<ImlInventoryLogisticsProductsResp> data);


    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "disabled", source = "disabled"),

    })
    LogisticsSupplierDTO.ListChildTreeDTO convertTree(LogisticsChannelEntity entity);
     List<LogisticsSupplierDTO.ListChildTreeDTO> convertTree(List<LogisticsChannelEntity> channelList);


    @Mappings({
            @Mapping(target = "platformChannelId", source = "id"),
            @Mapping(target = "code", source = "id"),
            @Mapping(target = "cnName", expression = "java(cn.hutool.core.util.StrUtil.format(\"{}【{}】\",logisticsChannel.getName(),logisticsChannel.getId()))"),
            @Mapping(target = "enName", source = "name"),
            // 渠道状态0正常1.暂停2.已关闭（默认0）
            @Mapping(target = "channelStatus", constant = "0"),
            @Mapping(target = "logisticsPlatform", constant = "Shopify"),
            @Mapping(target = "id", ignore = true),
    })
    LogisticsSaleChannelEntity channelConvertByShopify(ShopifyFulfillmentServicesItem logisticsChannel);



    List<LogisticsSaleChannelEntity> channelConvertByShopify(List<ShopifyFulfillmentServicesItem> fulfillmentServices);

    @Mappings({
            @Mapping(target = "platformChannelId", source = "carrierId"),
            @Mapping(target = "code", source = "carrierId"),
            @Mapping(target = "cnName", source = "carrierName"),
            @Mapping(target = "enName", source = "carrierName"),
            // 渠道状态0正常1.暂停2.已关闭（默认0）
            @Mapping(target = "channelStatus", constant = "0"),
            @Mapping(target = "logisticsPlatform", constant = "Walmart"),
            @Mapping(target = "id", ignore = true),
    })
    LogisticsSaleChannelEntity channelConvertByWalmart(WalmartCarriersDTO.Carrier logisticsChannel);

    List<LogisticsSaleChannelEntity> channelConvertByWalmart(List<WalmartCarriersDTO.Carrier> carrierList);
    @Mappings({
            @Mapping(target = "platformChannelId", source = "logisticsServiceId"),
            @Mapping(target = "code", source = "logisticsServiceId"),
            @Mapping(target = "cnName", source = "warehouseName"),
//            @Mapping(target = "enName", source = "displayName"),
            @Mapping(target = "supplierName", source = "logisticsServiceName"),
            @Mapping(target = "isTrack", constant = "true"),
            @Mapping(target = "aging", source = "logisticsTimeliness"),
            @Mapping(target = "logisticsPlatform", constant = "AliExpress"),
            @Mapping(target = "channelStatus", constant = "0"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity serviceConvertByAliExpress(ServiceResult serviceResult);
    List<LogisticsSaleChannelEntity> serviceConvertByAliExpress(List<ServiceResult> serviceResults);

    /**
     * 查询数据转换
     * @param item
     * @return
     */
    @Mappings({
            @Mapping(target = "isPrintPlatform", ignore = true),
            @Mapping(target = "effectiveTimeStr", ignore = true),
            @Mapping(target = "logisticsPlatform", ignore = true),
            @Mapping(target = "logisticsSupplierId", ignore = true),
            @Mapping(target = "logisticsSupplierName", ignore = true),
            @Mapping(target = "logisticsSupplierShortName", ignore = true),
            @Mapping(target = "shippingTemplateName", ignore = true),
            @Mapping(target = "supplierId", ignore = true)
    })
    LogisticsChannelDTO.BaseDTO convertToChannelDTO(@Param("item") LogisticsChannelEntity item);
    @Mappings({
            @Mapping(target = "code", source = "code"),
            @Mapping(target = "cnName", source = "name"),
            @Mapping(target = "enName", source = "nameEn"),
            @Mapping(target = "isTrack", constant = "true"),
            @Mapping(target = "logisticsPlatform", source = "logisticsPlatform"),
            @Mapping(target = "overseasWarehouseId", source = "erpWarehouseId"),
            @Mapping(target = "platformWarehouseCode", source = "warehouseCode"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByAntu(AntuLogisticsProductsResp data);
    List<LogisticsSaleChannelEntity> channelConvertByAntu(List<AntuLogisticsProductsResp> data);
    @Mappings({
            @Mapping(target = "code", source = "logisticsNo"),
            @Mapping(target = "cnName", source = "logisticsName"),
            @Mapping(target = "enName", source = "logisticsName"),
            @Mapping(target = "isTrack", constant = "false"),
            @Mapping(target = "logisticsPlatform", expression = "java(com.common.business.enums.LogisticsPlatformEnum.WDT.getCode())"),
            @Mapping(target = "overseasWarehouseId", ignore = true),
            @Mapping(target = "platformWarehouseCode", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "aging", ignore = true),
            @Mapping(target = "carrierType", ignore = true),
            @Mapping(target = "channelStatus", source = "disabled",qualifiedByName = "booleanToStatus"),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "destinationCountry", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "originCountry", ignore = true),
            @Mapping(target = "platformChannelId", source = "logisticsId"),
            @Mapping(target = "servicePlatform", constant = "tms"),
            @Mapping(target = "shipmentMethod", ignore = true),
            @Mapping(target = "sourceData", ignore = true),
            @Mapping(target = "supplierCode", ignore = true),
            @Mapping(target = "supplierName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByWdt(LogisticsQueryResponse.Details detail);
    List<LogisticsSaleChannelEntity> channelConvertByWdt(List<LogisticsQueryResponse.Details> detailList);
}
