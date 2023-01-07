package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 业务编码表
 * @date 2022/11/21 11:24
 */
@Data
@TableName("sys_code")
public class SysCodeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 类目
     */
    @TableField("category")
    private String category;

    /**
     * 顺序码
     */
    @TableField("num")
    private Integer num;

    /**
     * 编码类型 (枚举SysNoEnum，1:sku,2:spu)
     */
    @TableField("type")
    private Integer type;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField("create_user_name")
    private String createUserName;

    /**
     * 更新人id
     */
    @TableField("update_user_id")
    private String updateUserId;

    /**
     * 更新人
     */
    @TableField("update_user_name")
    private String updateUserName;
}
