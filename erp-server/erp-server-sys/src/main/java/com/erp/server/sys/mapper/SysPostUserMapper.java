package com.erp.server.sys.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.common.business.dto.base.BaseSearchDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.SysPostUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Classname SysPostUserMapper
 * @Description TODO
 * @Date 2022-07-12 17:11
 * @Created by yl
 */
@Mapper
public interface SysPostUserMapper extends BaseMapper<SysPostUserEntity> {
    List<SysUserDTO> findPostUser(@Param("params") BaseSearchDTO dto);
}
