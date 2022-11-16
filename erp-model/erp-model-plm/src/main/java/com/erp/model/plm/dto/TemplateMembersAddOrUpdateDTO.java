package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 成员新增DTO
 * @date 2022/11/16 11:25
 */
@Data
@NoArgsConstructor
public class TemplateMembersAddOrUpdateDTO extends TemplateMembersDTO {

    /**
     * 角色成员中间表id
     */
    @NotBlank(message = "角色成员中间表id不能为空")
    private String roleRefMembersId;

    /**
     *  成员集合
     */
    @Valid
    private List<TemplateMembersDTO> membersList;

}
