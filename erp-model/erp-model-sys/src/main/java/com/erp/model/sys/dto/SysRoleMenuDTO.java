package com.erp.model.sys.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

/**
 * @Classname SysRoleMenuDTO
 * @Description TODO
 * @Date 2022-10-14 15:54
 * @Created by yl
 */
@Data
public class SysRoleMenuDTO  implements Serializable {


    /**
     * 角色id
     */
    @NotNull(message = "角色Id不能为空")
    private String roleId;

    /**
     * 菜单id
     */
    @NotNull(message = "菜单Id不能为空")
    private String menuId;


    /**
     * 数据权限不能为空
     * 1-自己
     * 2-部门
     * 3-全部
     */
    @NotNull(message = "数据权限不能为空")
    @StateEnumValue(intValues = {1,2,3}, message = "数据权限有误")
    private Integer dataScope;
}
