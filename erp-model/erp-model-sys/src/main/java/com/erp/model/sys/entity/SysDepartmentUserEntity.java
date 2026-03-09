package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Classname SysDepartmentUserEntity

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

    /**
     * 用户删除状态 false:正常 true：已删除
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Boolean isDeleted;
}
