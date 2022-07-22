package com.cloud.erp.admin.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname SysPostEntity
 * @Description TODO
 * @Date 2022-07-12 9:55
 * @Created by yl
 */
@Data
@TableName("sys_post")
public class SysPostEntity  implements Serializable {

    @TableId(value = "id",type = IdType.ASSIGN_ID )
    private String id;


    private String postName;

    private String postRemark;


    @TableField(fill= FieldFill.INSERT)
    private Date createTime;

    @TableField(fill= FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
