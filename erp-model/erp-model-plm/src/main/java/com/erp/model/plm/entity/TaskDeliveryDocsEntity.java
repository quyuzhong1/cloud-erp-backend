package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
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
public class TaskDeliveryDocsEntity extends BaseEntity implements Serializable {

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

}
