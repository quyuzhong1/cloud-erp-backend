package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Classname SysDepartmentUserEntity
 * @Description TODO
 * @Date 2022-07-13 18:51
 * @Created by yl
 */
@TableName("sys_department_user")
@NoArgsConstructor
@Data
public class SysDepartmentUserEntity {

    @TableId(value = "id",type = IdType.ASSIGN_ID )
    private String id;


    private String departmentId;

    private String userId;

    private Integer leadState;

    @TableField(fill= FieldFill.INSERT)
    private Date createTime;

    @TableField(fill= FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
