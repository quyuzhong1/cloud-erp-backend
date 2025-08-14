package com.erp.server.tms.convert;

import com.common.business.mapper.BooleanMapperWork;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsInterceptOrderVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.tms.aliexpress.model.order.request.Address;
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
public interface LogisticsAddressConverter {

    LogisticsAddressConverter INSTANCE = Mappers.getMapper(LogisticsAddressConverter.class);



    @Mappings({
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "companyName", constant = "深圳市唯迹科技有限公司"),
            @Mapping(target = "contact", source = "name"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "telNumber", source = "phone"),
            @Mapping(target = "country", source = "country"),
            @Mapping(target = "countryName", source = "country"),
            @Mapping(target = "provinceName", source = "province"),
            @Mapping(target = "cityName", source = "city"),
            @Mapping(target = "districtName", source = "county"),
            @Mapping(target = "addressFirst", source = "streetAddress"),
            @Mapping(target = "zipCode", source = "postCode"),
            @Mapping(target = "street", source = "street"),
            @Mapping(target = "fax", source = "fax"),
            @Mapping(target = "addressId", source = "addressId"),
            @Mapping(target = "isDefault", source = "isDefault"),
            @Mapping(target = "language", source = "language"),
            @Mapping(target = "isBySync", constant = "true"),
            @Mapping(target = "tradeManageId", source = "tradeManageId"),
            @Mapping(target = "addressSecond", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "shopId", ignore = true),
            @Mapping(target = "type", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    LogisticsAddressEntity sellerAddressToLogisticsAddress(Address sender);
    List<LogisticsAddressEntity> sellerAddressToLogisticsAddress(List<Address> senders);
}
