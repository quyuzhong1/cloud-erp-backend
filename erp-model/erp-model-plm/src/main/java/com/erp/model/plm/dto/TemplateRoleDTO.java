package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 角色DTO
 * @date 2022/11/15 11:07
 */
@Data
@NoArgsConstructor
public class TemplateRoleDTO implements Serializable {

    /**
     * 角色id
     */
    private String id;

    /**
     * 角色名
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50,message = "角色名最长50字符")
    private String name;

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;


}
