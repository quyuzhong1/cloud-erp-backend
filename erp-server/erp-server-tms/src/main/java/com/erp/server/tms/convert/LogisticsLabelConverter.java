package com.erp.server.tms.convert;

import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.sdk.tms.disifang.model.chanel.response.ChanelInfo;
import com.sdk.tms.weishi.dto.response.WeiShiChannel;
import com.sdk.tms.yanwen.dto.response.YanWenChannel;
import com.sdk.tms.yuntu.dto.response.YunTuChannel;
import com.sdk.tms.yuntu.dto.response.YunTuPrintLabel;
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
@Mapper(uses = {TypeConversionWorker.class,BooleanMapperWork.class})
public interface LogisticsLabelConverter {

    LogisticsLabelConverter INSTANCE = Mappers.getMapper(LogisticsLabelConverter.class);

    @Mappings({
            @Mapping(target = "deliveryNoList", source = "orderNumberList"),
            @Mapping(target = "base64", source = "base64")
    })
    LogisticsPrintLabelResponse labelConvertByYuTu(YunTuPrintLabel yunTuPrintLabel);
    List<LogisticsPrintLabelResponse> labelConvertByYuTu(List<YunTuPrintLabel> list);

}
