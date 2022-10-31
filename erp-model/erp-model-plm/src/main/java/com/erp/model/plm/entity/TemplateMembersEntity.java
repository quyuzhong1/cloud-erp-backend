package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName template_members
 */
@Data
@TableName(value ="template_members")
public class TemplateMembersEntity implements Serializable {
    /**
     * 
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 
     */
    private String memberId;

    /**
     * 
     */
    private String memberName;

    /**
     * 
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 
     */
    private String templateId;

    /**
     * 
     */
    private String createUserId;

    /**
     * 
     */
    private String createUserName;

    /**
     * 
     */
    private Short isCharge;

    private static final long serialVersionUID = 1L;


}