package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.modules.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.SysRoleUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ${comments}
 * 
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-08 11:23:00
 */
@Mapper
public interface SysRoleUserMapper extends BaseMapper<SysRoleUserEntity> {

    List<SysUserDTO> findRoleUser(@Param("params") BaseSearchDTO dto);
}
