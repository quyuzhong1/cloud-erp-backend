package com.erp.model.sys.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@NoArgsConstructor
public class SysRoleMenuDataScopeDTO {
    //菜单id集合
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
