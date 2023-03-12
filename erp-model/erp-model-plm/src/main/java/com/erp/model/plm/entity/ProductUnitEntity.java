package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName product_unit
 */
@TableName(value ="product_unit")
@Data
public class ProductUnitEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 单位名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name")
    private String createUserName;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id")
    private String updateUserId;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name")
    private String updateUserName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}