package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname biSysModuleEntity
 * @Description TODO
 * @Date 2022-12-12 16:56
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bi_sys_module")
public class BiSysModuleEntity implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 模块名
     */
    private String name;
    /**
     * 父级id
     */
    private String pid;

    /**
     * 是否已添加 1 已添加 0 未添加
     */
    private Integer isAdd;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 更新人id
     */
    @TableField(value = "update_user_id",fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
