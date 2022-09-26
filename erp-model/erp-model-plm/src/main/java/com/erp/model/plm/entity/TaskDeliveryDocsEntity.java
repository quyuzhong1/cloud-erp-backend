package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
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

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("product_id")
    private String productId;


    @TableField("docs_name_id")
    private String docsNameId;

    @TableField("docs_name")
    private String docsName;

    @TableField(value = "task_id")
    private String taskId;

    @TableField(value = "is_sys")
    private Integer isSys;


    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
