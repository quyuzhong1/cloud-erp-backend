package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/18 14:44
 */
@Data
@TableName(value ="project_sys_task_ref_sku")
public class ProjectTaskSysRefSkuEntity {

    /**
     * 表id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 系统任务id
     */
    @TableField("sys_task_id")
    private String sysTaskId;

    /**
     * skuid
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人
     */
    @TableField("create_user_name")
    private String createUserName;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private String createUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

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
