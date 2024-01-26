package com.erp.server.tms.convert;

import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.tms.aliexpress.model.order.request.Address;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.api.product.DataRow;
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
public interface BaoHongConverter {

    BaoHongConverter INSTANCE = Mappers.getMapper(BaoHongConverter.class);

    @Mappings({
            @Mapping(target = "name", source = "smNameCn"),
            @Mapping(target = "code", source = "smCode"),
            @Mapping(target = "logisticsPlatform", constant = "BaoHong"),
    })
    TransferLogisticsChannelEntity transferLogisticsChannelConvert(SmRow data);
    List<TransferLogisticsChannelEntity> transferLogisticsChannelConvert(List<SmRow> data);

    @Mappings({
            @Mapping(target = "skuNo", source = "productSku"),
            @Mapping(target = "status", expression = "java(com.common.core.constant.EnumMessage.getByCode(com.sdk.tms.baohong.enums.BaoHongEnum.ProductStatusEnum.class,data.getProductStatus()).getProductRegistrationStatusEnum().getCode())"),
            @Mapping(target = "declarePlatform", constant = "BaoHong"),
    })
    ProductRegistrationEntity productRegistrationConvert(DataRow data);
    List<ProductRegistrationEntity> productRegistrationConvert(List<DataRow> data);
}
