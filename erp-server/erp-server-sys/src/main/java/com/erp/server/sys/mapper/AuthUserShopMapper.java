package com.erp.server.sys.mapper;

import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.entity.AuthUserShopEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 用户-店铺权限 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-02-27
 */
@Mapper
public interface AuthUserShopMapper extends BaseMapper<AuthUserShopEntity> {
    /**
     * 根据用户获取店铺权限
     * @param userId
     * @return
     */
    List<SysUserDTO.ShopDTO> getShopUserList(@Param("userId") String userId);
}
