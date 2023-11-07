package com.erp.server.tms.convert;

import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
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
@Mapper
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
}
