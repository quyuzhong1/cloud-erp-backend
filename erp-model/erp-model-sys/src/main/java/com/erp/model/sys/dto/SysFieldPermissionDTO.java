package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 字段权限请求/响应实体
 *
 * <p>"角色管理 - 字段权限"tab 专用：
 * <ul>
 *   <li>列表：列出所有 {@code sys_menu.type=5} 的字段权限码菜单 + 当前角色是否拥有</li>
 *   <li>保存：按角色全量替换其拥有的字段权限菜单</li>
 * </ul>
 *
 * @author cloud-erp
 */
@Data
@NoArgsConstructor
public class SysFieldPermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 列表查询入参
     */
    @Data
    @NoArgsConstructor
    public static class ListSearchDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "角色id不能为空")
        private String roleId;

        /**
         * 所属系统过滤（可选；对应 sys_menu.system）
         */
        private String system;
    }

    /**
     * 列表响应行：一行 = 一个字段权限菜单（type=5）
     */
    @Data
    @NoArgsConstructor
    public static class ListVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String menuId;

        /**
         * 同 cfg_mask_field.permission_code
         */
        private String menuCode;

        /**
         * 字段显示名（如"采购金额"）
         */
        private String menuName;

        /**
         * 父菜单名（如"OMS"），方便前端按业务模块分组展示
         */
        private String parentMenuName;

        /**
         * 所属系统
         */
        private String system;

        /**
         * 当前角色是否拥有该字段权限（即明文可见）
         */
        private Boolean visible;

        /**
         * 该 menu_code 当前覆盖的技术字段列表（class_path#field_name），
         * 由 cfg_mask_field 反查；运营 tooltip 用，可为空
         */
        private List<String> boundFields;
    }

    /**
     * 保存入参：全量替换该角色"字段权限"维度的关联
     */
    @Data
    @NoArgsConstructor
    public static class SaveDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "角色id不能为空")
        private String roleId;

        /**
         * 当前角色应拥有的字段权限菜单 id 列表（全量覆盖该角色 type=5 维度的关联）。
         * <p>传空列表表示清空该角色所有字段权限。</p>
         */
        @NotNull(message = "visibleMenuIds 不能为空")
        private List<String> visibleMenuIds;
    }
}
