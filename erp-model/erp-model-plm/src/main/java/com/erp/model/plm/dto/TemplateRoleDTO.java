package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
    private String name;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 更新人id
     */
    private String updateUserId;

    /**
     * 更新人
     */
    private String updateUserName;

    /**
     * 标示id
     */
    private String templateId;

    /**
     * 辅助字段：成员表id（界面显示）
     */
    private String membersId;
    /**
     * 辅助字段：成员名称（界面显示）
     */
    private String memberName;

    /**
     * 辅助字段：角色成员中间表id
     */
    private String roleRefMembersId;

    /**
     *  辅助字段：成员表
     */
    private List<TemplateMembersDTO> membersList;
}
