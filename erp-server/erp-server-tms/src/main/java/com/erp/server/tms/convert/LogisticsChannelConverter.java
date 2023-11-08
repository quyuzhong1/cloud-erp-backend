package com.erp.server.tms.convert;

import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.sdk.tms.disifang.model.chanel.response.ChanelInfo;
import com.sdk.tms.disifang.model.order.request.OrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.response.YanWenChannel;
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
@Mapper(uses = TypeConversionWorker.class)
public interface LogisticsChannelConverter {

    LogisticsChannelConverter INSTANCE = Mappers.getMapper(LogisticsChannelConverter.class);

    @Mappings({
            @Mapping(target = "platformChannelId", source = "id"),
            @Mapping(target = "cnName", source = "nameCh"),
            @Mapping(target = "enName", source = "nameEn"),
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
            @Mapping(target = "transport_mode", source = "shipmentMethod"),
            @Mapping(target = "id", ignore = true)
    })
    LogisticsSaleChannelEntity channelConvertByDSF(ChanelInfo chanelInfo);
    List<LogisticsSaleChannelEntity> channelConvertByDSFList(List<ChanelInfo> chanelInfos);
}
