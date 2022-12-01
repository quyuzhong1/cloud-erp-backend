package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 模板角色成员列表DTO
 * @date 2022/11/16 11:11
 */
@Data
@NoArgsConstructor
public class TemplateRoleShowDTO implements Serializable {


    /**
     * 模板id
     */
    private String templateId;

    /**
     * 角色id
     */
    private String id;

    /**
     * 角色名
     */
    private String name;

    /**
     * 成员表id
     */
    private String membersId;
    /**
     * 成员名称
     */
    private String memberName;

    /**
     * 角色成员中间表id
     */
    private String roleRefMembersId;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private Date createTime;

}
