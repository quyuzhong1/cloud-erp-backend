package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname CopyRoleDTO
 * @Description TODO
 * @Date 2022-11-17 12:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CopyRoleDTO implements Serializable {

    @NotBlank(message = "角色id不能为空")
    private String roleId;
}
