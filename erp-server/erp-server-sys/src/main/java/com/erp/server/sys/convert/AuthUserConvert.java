package com.erp.server.sys.convert;

import com.common.business.mapper.NumberMapperWork;
import com.erp.model.oms.entity.ShopSysUserAuthEntity;
import com.erp.model.sys.entity.AuthUserShopEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zdy
 * @ClassName SysUserConvert
 * @description: TODO
 * @date 2024年01月09日
 * @version: 1.0
 */
@Mapper(uses = NumberMapperWork.class)
@Component
public interface AuthUserConvert {
    AuthUserConvert INSTANCE = Mappers.getMapper(AuthUserConvert.class);

    @Mapping(target = "dataScope", source = "authType", qualifiedByName = "typeToDataScope")
    AuthUserShopEntity OmsShopAuthToSysShopAuth(ShopSysUserAuthEntity entity);
    List<AuthUserShopEntity> OmsShopAuthToSysShopAuth(List<ShopSysUserAuthEntity> list);
}
