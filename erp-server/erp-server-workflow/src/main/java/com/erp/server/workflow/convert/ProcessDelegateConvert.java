package com.erp.server.workflow.convert;

import com.common.business.dto.FindUserDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 委托审批转换器
 * @author will
 * @date 2025/5/30 15:09
 */
@Mapper
@Component
public interface ProcessDelegateConvert {
    ProcessDelegateConvert INSTANCE = Mappers.getMapper(ProcessDelegateConvert.class);
    @Mappings({
            @Mapping(target = "userId", source = "uid"),
            @Mapping(target = "userName", source = "userName"),
            @Mapping(target = "departmentId", source = "deptId"),
            @Mapping(target = "departmentName", source = "deptName")
    })
    FindUserDTO sysUserDeptToFindUser(SysUserDeptDTO dto);
    List<FindUserDTO> sysUserDeptToFindUser(List<SysUserDeptDTO> sourceDataList);
}
