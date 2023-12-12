package com.erp.server.tms.convert;

import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsInterceptOrderVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * @author lrp
 * @ClassName LogisticsLabelConverter
 * @description: 物流标签转换类
 */
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class})
public interface LogisticsOperationOrderConverter {

    LogisticsOperationOrderConverter INSTANCE = Mappers.getMapper(LogisticsOperationOrderConverter.class);


    @Mappings({
            @Mapping(target = "transportNo", source = "transportNo"),
            @Mapping(target = "trackNo", source = "trackNo"),
            @Mapping(target = "deliveryNo", source = "deliveryNo")
    })
    CancelResponseVO cancelOrderCovert(LogisticsCancelOrderVO data);

    @Mappings({
            @Mapping(target = "transportNo", source = "transportNo"),
            @Mapping(target = "trackNo", source = "trackNo"),
            @Mapping(target = "deliveryNo", source = "deliveryNo")
    })
    InterceptResponseVO interceptOrderCovert(LogisticsInterceptOrderVO data);
}
