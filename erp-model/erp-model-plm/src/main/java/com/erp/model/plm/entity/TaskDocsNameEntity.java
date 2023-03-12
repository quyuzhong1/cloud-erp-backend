package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname TaskDocesNameEntity
 * @Description TODO
 * @Date 2022-09-22 12:08
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("task_docs_name")
public class TaskDocsNameEntity implements Serializable {


    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("name")
    private String name;



    @TableField("product_id")
    private String productId;


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
