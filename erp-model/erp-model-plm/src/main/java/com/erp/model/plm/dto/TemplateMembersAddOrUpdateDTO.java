package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 成员新增DTO
 * @date 2022/11/16 11:25
 */
@Data
@NoArgsConstructor
public class TemplateMembersAddOrUpdateDTO implements Serializable {


    /**
     * 角色id
     */
    @NotBlank(message = "角色id不能为空")
    private String id;

    /**
     * 角色名
     */
    private String name;

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;

    /**
     * 角色成员中间表id
     */
    @NotNull(message = "角色成员中间表id不能为空")
    private String roleRefMembersId;

    /**
     *  成员集合
     */
    @Valid
    private List<TemplateMembersDTO> membersList;

}
