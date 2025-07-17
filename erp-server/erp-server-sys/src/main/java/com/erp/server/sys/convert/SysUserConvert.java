package com.erp.server.sys.convert;

import com.common.business.dto.FindUserDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.entity.SysUserInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
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
@Mapper
@Component
public interface SysUserConvert {
    SysUserConvert INSTANCE = Mappers.getMapper(SysUserConvert.class);

    @Mapping(target = "isSuper", source = "isSuper")
    SysUserInfoEntity copyDTOtoSysUser(SysUserInfoDTO sysUserInfoDTO);

    @Mappings({
            @Mapping(target = "userId", source = "uid"),
            @Mapping(target = "userName", source = "userName"),
            @Mapping(target = "departmentId", source = "deptId"),
            @Mapping(target = "departmentName", source = "deptName")
    })
    List<FindUserDTO> sysUserDeptToFindUser(List<SysUserDeptDTO> sourceDataList);
}
