package com.cloud.erp.admin.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloud.erp.admin.modules.sys.entity.SysPostUserEntity;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.erp.common.dto.BaseSearchDTO;
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
    List<SysUserVO> findPostUser(@Param("params") BaseSearchDTO dto);
}
