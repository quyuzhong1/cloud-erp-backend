package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @Classname SysPostUserEntity

 * @Date 2022-07-12 9:55
 * @Created by yl
 */
@Data
@TableName("sys_post_user")





public class SysPostUserEntity {

    @TableId(value = "id",type = IdType.ASSIGN_ID )
    private String id;


    private String postId;

    private String userId;


    @TableField(fill= FieldFill.INSERT)
    private Date createTime;

    @TableField(fill= FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
