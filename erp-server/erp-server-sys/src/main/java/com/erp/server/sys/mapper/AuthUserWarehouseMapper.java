package com.erp.server.sys.mapper;

import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.AuthUserWarehouseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 用户-仓库权限 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-02-27
 */
@Mapper
public interface AuthUserWarehouseMapper extends BaseMapper<AuthUserWarehouseEntity> {

    /**
     * 根据用户获取仓库权限
     * @param userId
     * @return
     */
    List<SysUserDTO.WarehouseDTO> getWarehouseUserList(@Param("userId") String userId);
}
