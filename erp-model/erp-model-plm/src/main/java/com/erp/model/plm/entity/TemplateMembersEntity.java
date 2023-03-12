package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName template_members 模板成员表
 */
@Data
@TableName(value ="template_members")
public class TemplateMembersEntity implements Serializable {
    /**
     * id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 成员id
     */
    private String memberId;

    /**
     * 成员名称
     */
    private String memberName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id",fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value ="create_user_name",fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 更新人id
     */
    @TableField(value = "update_user_id",fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 更新人
     */
    @TableField(value ="update_user_name",fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 是否是负责人 0 不是 1 是
     */
    private Short isCharge;

    private static final long serialVersionUID = 1L;


}