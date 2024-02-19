package com.erp.server.dmp.convert;

import com.erp.model.dmp.lingxing.ShopEntity;
import com.sdk.third.lingxing.dto.ShopInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 领星店铺映射工具类
 * </p>
 *
 * @author Jim
 * @since 2024-02-18
 */
@Mapper
@Component
public interface DmpShopInfoConverter {

    DmpShopInfoConverter INSTANCE = Mappers.getMapper(DmpShopInfoConverter.class);
    @Mappings({
    })
    ShopEntity dtoToEntity(ShopInfoDTO dto);

    @Mappings({
    })
    List<ShopEntity> dtoListToEntityList(List<ShopInfoDTO> dtoList);


}
