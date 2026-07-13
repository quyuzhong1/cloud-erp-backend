package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.sys.entity.SysApiTokenWhitelistEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * API Token 接口白名单 Mapper 接口
 * </p>
 */
@Mapper
public interface SysApiTokenWhitelistMapper extends BaseMapper<SysApiTokenWhitelistEntity> {
}
