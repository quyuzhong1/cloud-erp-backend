package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 项目成员表
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("project_members")
public class ProjectMembersEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 成员id
     */
    @TableField("member_id")
    private String memberId;

    /**
     * 成员名
     */
    @TableField("member_name")
    private String memberName;

    /**
     * 项目id
     */
    @TableField("project_id")
    private String projectId;

    /**
     * 是否是负责人 0 不是 1 是
     */
    @TableField("is_charge")
    private Integer isCharge;


    /**
     * 产品id
     */
    @TableField("product_id")
    private String productId;




    /**
     * 产品id
     */
    @TableField("create_user_id")
    private String createUserId;

    @TableField("create_user_name")
    private String createUserName;


    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
