package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 *  ProjectRoleDTO
 * @Description TODO
 * @Date 2022-09-26 16:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProjectRoleDTO implements Serializable {


    /**
     * 角色名
     */
    @NotBlank(message = "角色名不能为空")
    private String name;

    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;


    /**
     * 项目id
     */
    private String projectId;
}
