package com.erp.model.sys.dto;

import com.common.business.dto.UserRequestPermissionsDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 数据权限解析所需的聚合上下文
 *
 * <p>由 {@code DataPermissionFeign#getDataPermissionContext(uid)} 一次返回，
 * 替代 {@code DataPermissionAspect} 原本散开的 5 次 Feign 调用：</p>
 * <ol>
 *   <li>{@code SysUserFeign.getRequestPermissionsList(uid)}</li>
 *   <li>{@code SysUserFeign.getRoleIdList(uid)}</li>
 *   <li>{@code SysUserFeign.getDepUserList(uid)}</li>
 *   <li>{@code AuthDataFeign.getShopUserList(uid)}</li>
 *   <li>{@code AuthDataFeign.getWarehouseUserList(uid)}</li>
 * </ol>
 *
 * <p>聚合后业务侧可以一次拿全，并在本地 {@code FeignDataPermissionContextResolver} 做 60s TTL 缓存，
 * 避免每个 {@code @DataPermission} 注解的请求都打爆 sys。</p>
 *
 * <p><b>不可变约定</b>：本 DTO 一旦构造完应当视为不可变（5 个 List 字段都 unmodifiable），
 * 业务侧不得修改其中元素，否则会污染 cache。</p>
 *
 * @author cloud-erp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPermissionContextDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户权限码集合（含 dataScope，用于按 menuCode 匹配数据范围）
     */
    private List<UserRequestPermissionsDTO> permissionsList;

    /**
     * 用户角色 id 列表（用于超管旁路：roleId="1" 直接放行 ALL 数据范围）
     */
    private List<String> roleIdList;

    /**
     * 用户所在部门下的所有用户 id（DATA_SCOPE_DEPT 时用作 IN 条件）
     */
    private List<String> depUserList;

    /**
     * 用户店铺权限
     */
    private List<SysUserDTO.ShopDTO> shopUserList;

    /**
     * 用户仓库权限
     */
    private List<SysUserDTO.WarehouseDTO> warehouseUserList;

    /**
     * 空上下文（用于 cache miss + Feign 失败时的安全降级；最保守：等价于无任何数据权限）
     */
    public static DataPermissionContextDTO empty() {
        return DataPermissionContextDTO.builder()
                .permissionsList(Collections.emptyList())
                .roleIdList(Collections.emptyList())
                .depUserList(Collections.emptyList())
                .shopUserList(Collections.emptyList())
                .warehouseUserList(Collections.emptyList())
                .build();
    }
}
