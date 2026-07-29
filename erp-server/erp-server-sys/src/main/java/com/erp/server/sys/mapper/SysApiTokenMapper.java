package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.sys.entity.SysApiTokenEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 个人访问令牌 Mapper 接口
 * </p>
 */
@Mapper
public interface SysApiTokenMapper extends BaseMapper<SysApiTokenEntity> {
}
