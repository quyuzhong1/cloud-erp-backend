package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname ProjectRoleDTO
 * @Description TODO
 * @Date 2022-09-26 16:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class RoleDTO implements Serializable {

    @NotBlank(message = "角色名不能为空")
    private String name;

    @NotBlank(message = "产品id不能为空")
    private String productId;

    @NotBlank(message = "项目id不能为空")
    private String projectId;
}
