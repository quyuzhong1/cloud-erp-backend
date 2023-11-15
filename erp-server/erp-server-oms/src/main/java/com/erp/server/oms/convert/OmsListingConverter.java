package com.erp.server.oms.convert;

import com.common.business.dto.PlatformProductDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 映射工具类
 * </p>
 *
 * @author Jim
 * @since 2023-11-15
 */
@Mapper
@Component
public interface OmsListingConverter {
    OmsListingConverter INSTANCE = Mappers.getMapper(OmsListingConverter.class);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "createUserId", ignore = true),
            @Mapping(target = "createUserName", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUserId", ignore = true),
            @Mapping(target = "updateUserName", ignore = true),
            @Mapping(target = "isDeleted", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "matchResult", ignore = true),
    })
    ListingInfoEntity listingDtoToEntity(PlatformProductDTO platformProductDTO);

}
