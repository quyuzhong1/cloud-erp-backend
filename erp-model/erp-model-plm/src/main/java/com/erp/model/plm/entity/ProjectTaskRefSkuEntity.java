package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/18 14:25
 */
@Data
@TableName(value ="project_task_ref_sku")
public class ProjectTaskRefSkuEntity implements Serializable {

    /**
     * 表id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务id
     */
    @TableField("task_id")
    private String taskId;

    /**
     * skuid
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * 产品id
     */
    @TableField("product_id")
    private String productId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

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
    private LocalDateTime updateTime;

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


    /**
     * 是否已完成任务
     * 0 没有 1 已完成
     */
    @TableField(value = "is_finish_task")
    private Integer isFinishTask;
}
