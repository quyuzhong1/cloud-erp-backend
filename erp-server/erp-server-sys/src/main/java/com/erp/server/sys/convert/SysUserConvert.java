package com.erp.server.sys.convert;

import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.entity.SysUserInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * @author zdy
 * @ClassName SysUserConvert
 * @description: TODO
 * @date 2024年01月09日
 * @version: 1.0
 */
@Mapper
@Component
public interface SysUserConvert {
    SysUserConvert INSTANCE = Mappers.getMapper(SysUserConvert.class);

    @Mapping(target = "isSuper", source = "super")
    SysUserInfoEntity copyDTOtoSysUser(SysUserInfoDTO sysUserInfoDTO);
}
