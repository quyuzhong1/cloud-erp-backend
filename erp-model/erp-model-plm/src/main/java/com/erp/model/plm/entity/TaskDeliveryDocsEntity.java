package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname 交付的文档
 * @Description TODO
 * @Date 2022-09-22 9:38
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("task_delivery_docs")
public class TaskDeliveryDocsEntity implements Serializable {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 产品id
     */
    @TableField("product_id")
    private String productId;


    /**
     * 文档名表id
     */
    @TableField("docs_name_id")
    private String docsNameId;

    /**
     * 文档名
     */
    @TableField("docs_name")
    private String docsName;

    /**
     * 任务Id
     */
    @TableField(value = "task_id")
    private String taskId;


    /**
     * 是否系统任务
     */
    @TableField(value = "is_sys")
    private Integer isSys;


    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
