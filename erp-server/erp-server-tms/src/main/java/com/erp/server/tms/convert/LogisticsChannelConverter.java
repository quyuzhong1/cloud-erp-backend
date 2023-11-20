package com.erp.server.tms.convert;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.tms.aliexpress.model.channel.response.ChannelResponse;
import com.sdk.tms.disifang.model.chanel.response.ChanelInfo;
import com.sdk.tms.shopee.model.logistics.response.LogisticsChannel;
import com.sdk.tms.tongyou.dto.response.TongYouChannel;
import com.sdk.tms.ubi.model.catalog.response.ServiceCataLog;
import com.sdk.tms.weishi.dto.response.WeiShiChannel;
import com.sdk.tms.yanwen.dto.response.YanWenChannel;
import com.sdk.tms.yuntu.dto.response.YunTuChannel;
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
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class})
public interface LogisticsChannelConverter {

    LogisticsChannelConverter INSTANCE = Mappers.getMapper(LogisticsChannelConverter.class);

    @Mappings({
            @Mapping(target = "platformChannelId", source = "id"),
            @Mapping(target = "cnName", source = "nameCh"),
            @Mapping(target = "enName", source = "nameEn"),
            @Mapping(target = "logisticsPlatform", constant = "YanWen"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByYanWen(YanWenChannel yanWenChannelList);
    List<LogisticsSaleChannelEntity> channelConvertByYanWenList(List<YanWenChannel> yanWenChannelList);

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
            @Mapping(target = "logisticsPlatform", constant = "ALI_EXPRESS"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByAliExpress(ChannelResponse chanelInfo);
    List<LogisticsSaleChannelEntity> channelConvertByAliExpress(List<ChannelResponse> chanelInfos);

}
