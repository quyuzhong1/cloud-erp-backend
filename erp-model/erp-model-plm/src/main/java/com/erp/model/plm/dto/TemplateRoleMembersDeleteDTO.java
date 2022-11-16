package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/16 11:31
 */
@Data
@NoArgsConstructor
public class TemplateRoleMembersDeleteDTO implements Serializable {

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;

    /**
     * 角色成员中间表id
     */
    @NotBlank(message = "角色成员中间表id不能为空")
    private String roleRefMembersId;
}
