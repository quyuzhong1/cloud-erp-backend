package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname SysProductFieldEntity
 * @Description TODO
 * @Date 2022-09-15 11:56
 * @Created by yl
 */
@Data
@TableName("sys_product_field")
public class SysProductFieldEntity implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("scope")
    private Integer scope;

    @TableField("name")
    private String name;

    @TableField("if_required")
    private Integer ifRequired;


    //字段类型 1：单选框 2 文本框 3：多选框 4 ： 日期  5：成员
    @TableField("type")
    private Integer type;


    //1 启用  0 未启用
    @TableField("state")
    private Integer state;


    @TableField("content")
    private String content;


    @TableField("create_user_id")
    private String createUserId;

    @TableField("create_user_name")
    private String createUserName;

    @TableField(value="create_time",fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value="update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;



}
