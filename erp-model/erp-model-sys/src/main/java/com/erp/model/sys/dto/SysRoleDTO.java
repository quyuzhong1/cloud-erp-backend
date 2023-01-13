package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/9 12:20
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SysRoleDTO {

    /**
     * 角色id
     */
    private String id;
    /**
     * 角色名
     */
    private String roleName;
}
