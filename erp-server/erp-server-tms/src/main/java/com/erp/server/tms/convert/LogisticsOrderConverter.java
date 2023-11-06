package com.erp.server.tms.convert;

import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.sdk.tms.disifang.model.order.request.OrderRequest;
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
@Mapper
public interface LogisticsOrderConverter {

    LogisticsOrderConverter INSTANCE = Mappers.getMapper(LogisticsOrderConverter.class);

    @Mappings({
            @Mapping(target = "reserveTime", source = "skuNo"),
            @Mapping(target = "pickupInfo.name", source = "logisticsOrderVO.receiverInfoVO.name"),
            @Mapping(target = "pickupInfo.phone", source = "logisticsOrderVO.receiverInfoVO.telNumber"),
            @Mapping(target = "pickupInfo.country", source = "logisticsOrderVO.receiverInfoVO.country"),
            @Mapping(target = "pickupInfo.province", source = "logisticsOrderVO.receiverInfoVO.province"),
            @Mapping(target = "pickupInfo.city", source = "logisticsOrderVO.receiverInfoVO.city"),
            @Mapping(target = "pickupInfo.district", source = "logisticsOrderVO.receiverInfoVO.district"),
//            @Mapping(target = "pickupInfo.street", source = "addressEntity.name"),
            @Mapping(target = "pickupInfo.detailAddress", source = "logisticsOrderVO.receiverInfoVO.addressFirst"),
            @Mapping(target = "pickupInfo.zipCode", source = "logisticsOrderVO.receiverInfoVO.zipCode")
    })
    OrderRequest orderRequestToDsf(LogisticsOrderVO logisticsOrderVO);

}
