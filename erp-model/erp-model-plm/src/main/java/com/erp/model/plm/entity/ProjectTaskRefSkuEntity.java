package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
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
public class ProjectTaskRefSkuEntity extends BaseEntity implements Serializable {

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
     * 是否已完成任务
     * 0 没有 1 已完成
     */
    @TableField(value = "is_finish_task")
    private Integer isFinishTask;
}
