package com.erp.server.sys.mapper;

import com.erp.model.sys.dto.SysFieldPermissionDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字段权限管理 Mapper（只读）
 *
 * <p>专门服务于"角色管理 - 字段权限"tab。写操作直接走 {@link SysRoleMenuMapper}
 * 走逻辑删除复用既有模式，本接口只负责聚合查询。</p>
 *
 * @author cloud-erp
 */
@Mapper
public interface SysFieldPermissionMapper {

    /**
     * 列出所有 type=5 字段权限菜单 + 当前角色是否拥有 + 该 menu_code 覆盖的技术字段
     */
    List<SysFieldPermissionDTO.ListVO> listFieldPermissionForRole(@Param("roleId") String roleId,
                                                                  @Param("system") String system);

    /**
     * 拉取所有 type=5 字段权限菜单的 menu_id（用于 save 时圈定本维度全集）
     */
    List<String> findFieldPermissionMenuIds();
}
